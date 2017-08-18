package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import com.hzgc.jni.FaceFunction;
import com.hzgc.util.UuidUtil;
import com.hzgc.util.obejectListSort.ListUtils;
import com.hzgc.util.obejectListSort.SortParam;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.mortbay.log.Log;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Administrator on 2017-8-12.
 */
public class RealTimeCompare implements Serializable {
    private static Logger LOG = Logger.getLogger(RealTimeCompare.class);
    private static byte[] image;// 图片的二进制数据
    private static String imageId;//图片 id ,优先使用图片流数组
    private static String plateNumber;//车牌，当 SearchType 为 CAR 时有用，需要支持模糊搜索
    private static float threshold;//阈值
    private static String sortParams;//排序参数
    private static int offset;//分页查询开始行
    private static int count;//分页查询条数
    private static List<SearchFilter> filters;//人车属性
    private static String searchId;//查询Id 由UUID生成
    private static DynamicPhotoService dynamicPhotoService;
    private static List<String> imageIdList;//用于保存筛选出来的一组一个图片的id
    private static List<Tuple2<String, float[]>> imageIdFeaTupList;//筛选的到的图片的Id和特征元组列表
    private static float similarity;//相似度
    private static List<Tuple2<String, Float>> imageIdSimTupList;//筛选的到的图片的Id和特征元组列表
    private static CapturePictureSearchService capturePictureSearchService;
    private static CapturedPicture capturedPicture;//图片对象
    private static List<CapturedPicture> capturedPictures;//图片对象列表
    private static HashMap<String, Float> imgSimilarityMap;//图片Id和相似度的映射关系
    private static SearchResult searchResult;//查询结果
    private static JavaSparkContext jsc;

    static {
        SparkConf conf = new SparkConf().setAppName("RealTimeCompare").setMaster("local[1]");
        jsc = new JavaSparkContext(conf);
        ElasticSearchHelper.getEsClient();
    }

    public SearchResult pictureSearch(SearchOption option) throws Exception {
        SearchType searchType = option.getSearchType();
        imageId = option.getImageId();
        image = option.getImage();
        plateNumber = option.getPlateNumber();
        threshold = option.getThreshold();
        List<String> deviceIds = option.getDeviceIds();
        String platformId = option.getPlatformId();
        Date startDate = option.getStartDate();
        Date endDate = option.getEndDate();
        List<TimeInterval> intervals = option.getIntervals();
        sortParams = option.getSortParams();
        filters = option.getFilters();
        offset = option.getOffset();
        count = option.getCount();

        dynamicPhotoService = new DynamicPhotoServiceImpl();
        if (null != searchType) {
            //查询的对象库是人
            if (searchType == SearchType.PERSON) {
                PictureType pictureType = PictureType.PERSON;
                //上传的参数中有图
                if (null != image && image.length > 0) {
                    searchResult = compareByImage(pictureType, option);
                } else {
                    //无图，有imageId
                    if (null != imageId) {
                        System.out.println(imageId);
                        searchResult = compareByImageId(pictureType, option);
                    } else {
                        //无图无imageId
                        compareByOthers(pictureType, option);
                    }
                }
            }
            //查询的对象库是车
            if (searchType == SearchType.CAR) {
                PictureType pictureType = PictureType.CAR;
                //上传的参数中有图
                if (null != image && image.length > 0) {
                    searchResult = compareByImage(pictureType, option);
                } else {
                    //无图，有imageId
                    if (null != imageId) {
                        searchResult = compareByImageId(pictureType, option);
                    } else {//无图无imageId
                        searchResult = compareByOthers(pictureType, option);
                    }
                }
            }
        } else {
            LOG.error("SearchType needed");
        }
        return searchResult;
    }

    /**
     * 以图搜图，图片不为空的查询方法
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回所有满足查询条件的图片rowkey
     * @throws Exception
     */
    private SearchResult compareByImage(PictureType pictureType, SearchOption option) throws Exception {
        //提取特征
        float[] searchFea = FaceFunction.featureExtract(image);
        if (null != searchFea && searchFea.length == 512) {
            //生成uuid作为searchId和queryImageId
            searchId = UuidUtil.setUuid();
            String queryImageId = searchId;
            //将图片特征插入到特征库
            boolean insertStatus = dynamicPhotoService.insertPictureFeature(pictureType, queryImageId, searchFea);
            //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
            imageIdList = getImageIdListFromHbase(option);
            if (null != imageIdList && imageIdList.size() > 0) {
                //根据imageId找出对应特征加入组成二元组并加入到列表
                imageIdFeaTupList = getFeaByImageId(imageIdList, pictureType);
                //对上传图片的特征与查询到的满足条件的图片特征进行比对
                imageIdSimTupList = featureCompare(searchFea, imageIdFeaTupList);
                //根据阈值对计算结果进行过滤，并进行排序分页等操作
                searchResult = lastResult(imageIdSimTupList, threshold, pictureType.getType(), sortParams);
            } else {
                Log.info("no image find in HBase satisfy the search option");
            }
        } else {
            LOG.info("The feature of image is null or short than 2048 byte");
        }
        return searchResult;
    }

    /**
     * 根据图片id进行搜图的方法
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回所有满足查询条件的图片rowkey
     * @throws Exception
     */
    private SearchResult compareByImageId(PictureType pictureType, SearchOption option) throws Exception {
        //根据imageId从动态库中获取特征值
        byte[] fea = dynamicPhotoService.getFeature(imageId, pictureType);
        if (null != fea && fea.length == 2048) {
            //将获取到的特征从 byte[] 转化为float[]
            float[] searchFea = FaceFunction.byteArr2floatArr(fea);
            //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
            imageIdList = getImageIdListFromHbase(option);
            //根据imageId找出对应特征加入组成二元组并加入到列表
            imageIdFeaTupList = getFeaByImageId(imageIdList, pictureType);
            if (null != imageIdList && imageIdList.size() > 0) {
                //对特征进行比对
                imageIdSimTupList = featureCompare(searchFea, imageIdFeaTupList);
                System.out.println("比对完成");
                //根据阈值对计算结果进行过滤，并进行排序分页等操作
                searchResult = lastResult(imageIdSimTupList, threshold, pictureType.getType(), sortParams);
            } else {
                Log.info("no image find in HBase satisfy the search option");
            }
        } else {
            Log.info("the feature of" + imageId + "is null or short than 2048");
        }
        return searchResult;
    }

    /**
     * 无图/图片id，仅通过设备、时间等参数进行搜图
     *
     * @param pictureType 图片类型（人、车）
     * @param option      查询参数
     * @return 返回满足所有查询条件的图片rowkey
     * @throws Exception
     */
    private SearchResult compareByOthers(PictureType pictureType, SearchOption option) throws Exception {
        //采用HBase+elasticSearch，根据deviceId、时间参数圈定查询范围,得到一组满足条件的图像id
        imageIdList = getImageIdListFromHbase(option);
        if (null != imageIdList && imageIdList.size() > 0) {
            //根据imageId找出对应特征加入列表
            imageIdFeaTupList = getFeaByImageId(imageIdList, pictureType);
            //查询结果，不需要比对
            searchResult = lastResult(imageIdSimTupList, threshold, pictureType.getType(), sortParams);
        } else {
            Log.info("find no image by deviceIds&time");
        }
        return searchResult;
    }

    /**
     * 通过elasticSearch根据查询参数对HBase数据库进行过滤
     *
     * @param option 查询参数
     * @return 返回满足所有查询条件的结果
     */
    private List<String> getImageIdListFromHbase(SearchOption option) {
        /*Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);//此处需要修改
        List<String> rowKeyList = new ArrayList<>();
        Scan scan = new Scan();
        try {
            ResultScanner scanner = person.getScanner(scan);
            for (Result result : scanner) {
                byte[] bytes = result.getRow();
                String string = Bytes.toString(bytes);
                rowKeyList.add(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("scan table person failed.");
        } finally {
            HBaseUtil.closTable(person);
        }
        return rowKeyList;*/
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        List<String> imageIdList = filterByRowkey.getRowKey(option);
        System.out.println("通过HBase+es读取数据：");
        for (String imageId : imageIdList) {
            System.out.println(imageId);
        }
        return imageIdList;
    }

    /**
     * 通过图片id(rowkey)从对应库中查询其特征值
     *
     * @param imageIdList 图片id列表
     * @param pictureType 图片类型（人、车）
     * @return 图片id及其特征所组成的二元组列表
     * @throws Exception
     */
    private List<Tuple2<String, float[]>> getFeaByImageId(List<String> imageIdList, final PictureType pictureType) throws Exception {
        imageIdFeaTupList = new ArrayList<>();
        JavaRDD<String> imageIdListRdd = jsc.parallelize(imageIdList);
        //根据imageId进行特征查询并转化为float[]
        JavaPairRDD<String, float[]> imageIdFeaRDD = imageIdListRdd.mapToPair((PairFunction<String, String, float[]>) x -> new Tuple2<>(x, FaceFunction.byteArr2floatArr(dynamicPhotoService.getFeature(x, pictureType))));
        List<Tuple2<String, float[]>> tempList = imageIdFeaRDD.collect();
        return imageIdFeaRDD.collect();
    }

    /**
     * 进行特征比对的方法
     *
     * @param searchFea        查询图片的特征
     * @param resultFeaListTup 查询到的图片的id以及其特征二元组列表
     * @return 查询到的图片的id以及其与查询图片的相似度二元组列表
     */
    private List<Tuple2<String, Float>> featureCompare(final float[] searchFea, List<Tuple2<String, float[]>> resultFeaListTup) {
        JavaRDD<Tuple2<String, float[]>> resultFeaListRdd = jsc.parallelize(resultFeaListTup);
        //对两个特征进行比对
        JavaRDD<Tuple2<String, Float>> imageIdSimRDD = resultFeaListRdd.map((Function<Tuple2<String, float[]>, Tuple2<String, Float>>) v1 -> {
            if (null != v1._2 && v1._2.length == 512)
                //图片id及其与查询对象的相似度二元组
                return new Tuple2<>(v1._1, FaceFunction.featureCompare(searchFea, v1._2));
            else {
                return new Tuple2<>(v1._1, 0.00f);
            }
        });
        imageIdSimRDD.foreach((VoidFunction<Tuple2<String, Float>>) stringFloatTuple2 -> System.out.println("图片Id：" + stringFloatTuple2._1 + "相似度：" + stringFloatTuple2._2.toString()));
        return imageIdSimRDD.collect();
    }

    /**
     * 对计算结果根据阈值过滤，根据排序参数排序，分页
     *
     * @param imageIdSimTup 计算得到图片id以及其与查询图片的相似度二元组列表
     * @param threshold     相似度阈值
     * @param type          图片类型
     * @param sortParams    排序参数
     * @return 阈值过滤、排序、分页后最终返回结果
     */
    private SearchResult lastResult(final List<Tuple2<String, Float>> imageIdSimTup, final float threshold, final int type, String sortParams) {
        imgSimilarityMap = new HashMap<>();
        capturePictureSearchService = new CapturePictureSearchServiceImpl();
        capturedPictures = new ArrayList<>();
        JavaPairRDD<String, Float> imageIdSimTupRDD = jsc.parallelizePairs(imageIdSimTup);
        //根据阈值对imageIdSimTupRDD进行过滤，返回大于相似度阈值的结果
        JavaPairRDD<String, Float> filterRDD = imageIdSimTupRDD.filter((Function<Tuple2<String, Float>, Boolean>) v1 -> (v1._2 > threshold));
        filterRDD.foreach((VoidFunction<Tuple2<String, Float>>) filterTuple -> {
                    //根据imageId 读取数据的其他信息
                    capturedPicture = new CapturedPicture();
                    //人车的type+6对应只返回图片的信息，而不返回图片数据
                    int messageType = type + 6;
                    //从动态库中读取图片信息
                    capturedPicture = capturePictureSearchService.getCaptureMessage(filterTuple._1, messageType);
                    //设置图片相似度
                    capturedPicture.setSimilarity(filterTuple._2);
                    //将单张图片加入到capturedPictures列表
                    capturedPictures.add(capturedPicture);
                    //图片Id与相似度以map形式保存到历史searchRes表中
                    imgSimilarityMap.put(filterTuple._1, filterTuple._2);
                }
        );
        for (CapturedPicture capturedPicture1 : capturedPictures) {//内部不锁定，效率最高，但在多线程要考虑并发操作的问题。
            if (null != capturedPicture1) {
                System.out.println(capturedPicture1.toString());
            }
        }
        System.out.println("+++++++++++++++++++++++++++++++");
        System.out.println(capturedPictures.size());

        //对capturePictures进行排序
        System.out.println("排序后");
        System.out.println("+++++++++++++++++++++++++++++++");
        //根据排序参数进行排序
        capturedPictures = sortByParams(capturedPictures, sortParams);
        for (int i = 0; i < capturedPictures.size(); i++) {//内部不锁定，效率最高，但在多线程要考虑并发操作的问题。
            System.out.println("");
        }
        //进行分页操作
        List<CapturedPicture> subCapturedPictures = pageSplit(capturedPictures, offset, count);
        //返回最终结果
        searchResult = new SearchResult();
        //分组返回图片对象
        searchResult.setPictures(subCapturedPictures);
        //searchId 设置为imageId（rowkey）
        searchResult.setSearchId(searchId);
        //设置查询到的总得记录条数
        searchResult.setTotal(capturedPictures.size());
        //保存到Hbase
        //boolean flag = dynamicPhotoService.insertSearchRes(searchResult.getSearchId(), queryImageId, imgSimilarityMap);
        return searchResult;
    }

    /**
     * 根据排序参数对图片对象列表进行排序，支持多字段
     *
     * @param capturedPictures 待排序的图片对象列表
     * @param sortParams       排序参数
     * @return 排序后的图片对象列表
     */
    List<CapturedPicture> sortByParams(List<CapturedPicture> capturedPictures, String sortParams) {
        //对排序参数进行读取和预处理
        SortParam sortParam = ListUtils.getOrderStringBySort(sortParams);
        //内部不锁定，效率最高，但在多线程要考虑并发操作的问题。
        for (CapturedPicture capturedPicture1 : capturedPictures) {
            System.out.println(capturedPicture1.toString());
        }
        if (null != sortParams && sortParams.length() > 0) {
            //根据自定义的排序方法进行排序
            ListUtils.sort(capturedPictures, sortParam.getSortNameArr(), sortParam.getIsAscArr());
        } else {
            Log.info("sortParams is null!");
        }
        return capturedPictures;
    }

    /**
     * 对图片对象列表进行分页返回
     *
     * @param capturedPictures 待分页的图片对象列表
     * @param offset           起始行
     * @param count            条数
     * @return 返回分页查询结果
     */
    List<CapturedPicture> pageSplit(List<CapturedPicture> capturedPictures, int offset, int count) {
        List<CapturedPicture> subCapturePictureList;
        int totalPicture = capturedPictures.size();
        if (offset > -1 && totalPicture > (offset + count - 1)) {
            //结束行小于总数，取起始行开始后续count条数据
            subCapturePictureList = capturedPictures.subList(offset, offset + count);
        } else {
            //结束行大于总数，则返回起始行开始的后续所有数据
            subCapturePictureList = capturedPictures.subList(offset, totalPicture);
        }
        return subCapturePictureList;
    }
}

