package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
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
import org.apache.spark.sql.SQLContext;
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
    private static SearchType searchType;//搜索类型，人PERSON（0）,车CAR（1）
    private static byte[] image;// 图片的二进制数据
    private static String imageId;//图片 id ,优先使用图片流数组
    private static String plateNumber;//车牌，当 SearchType 为 CAR 时有用，需要支持模糊搜索
    private static float threshold;//阈值
    private static List<String> deviceIds;//搜索的设备范围
    private static String platformId;//平台 Id 优先使用 deviceIds 圈定范围
    private static Date startDate;//开始日期
    private static Date endDate;//截止日期
    private static List<TimeInterval> intervals;//搜索的时间区间，为空或者没有传入这个参数时候搜索整天
    private static String sortParams;//排序参数
    private static int offset;//分页查询开始行
    private static int count;//分页查询条数
    private static List<SearchFilter> filters;//参数筛选选项
    private static String queryImageId;//查询图片Id由UUID生成
    private static String searchId;//查询Id 由UUID生成
    private static DynamicPhotoService dynamicPhotoService;
    private static List<String> imageIdList;//用于保存筛选出来的一组一个图片的id
    private static List<Tuple2<String, float[]>> imageIdFeaTupList;//筛选的到的图片的Id和特征元组列表
    private static float similarity;//相似度
    private static List<Tuple2<String, Float>> imageIdSimTupList;//筛选的到的图片的Id和特征元组列表
    private static CapturePictureSearchService capturePictureSearchService;
    private static CapturedPicture capturedPicture;//图片对象
    private static List<CapturedPicture> capturedPictures;//图片对象列表
    private static List<CapturedPicture> finalCapturedPictures;
    private static HashMap<String, Float> imgSimilarityMap;//图片Id和相似度的映射关系
    private static SearchResult searchResult;//查询结果
    private static SparkConf conf;
    private static JavaSparkContext jsc;
    private static SQLContext sqlContext;

    static {
        conf = new SparkConf().setAppName("RealTimeCompare").setMaster("local[1]");
        jsc = new JavaSparkContext(conf);
    }

    public SearchResult pictureSearch(SearchOption option) throws Exception {
        searchType = option.getSearchType();
        imageId = option.getImageId();
        image = option.getImage();
        plateNumber = option.getPlateNumber();
        threshold = option.getThreshold();
        deviceIds = option.getDeviceIds();
        platformId = option.getPlatformId();
        startDate = option.getStartDate();
        endDate = option.getEndDate();
        intervals = option.getIntervals();
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
                        System.out.println("++++++++++++++");
                        System.out.println(imageId);
                        searchResult = compareByImageId(pictureType, option);
                    } else {//无图无imageId
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

    private SearchResult compareByImage(PictureType pictureType, SearchOption option) throws Exception {
        //提取特征
        float[] searchFea = FaceFunction.featureExtract(image);
        if (null != searchFea && searchFea.length == 512) {
            //生成uuid作为rowkey
            queryImageId = UuidUtil.setUuid();
            //插入到特征表
            boolean insertStatus = dynamicPhotoService.insertPictureFeature(pictureType, queryImageId, searchFea);
            //根据deviceId、时间参数圈定查询范围,得到一组rowkey,Hbase+es
            imageIdList = getImageIdListFromHbase(option);
            if (null != imageIdList && imageIdList.size() > 0) {
                //根据imageId找出对应特征加入列表
                imageIdFeaTupList = getFeaByImageId(imageIdList, pictureType);
                //对特征进行比对
                imageIdSimTupList = featureCompare(searchFea, imageIdFeaTupList);
                //返回大于阈值的结果
                searchResult = lastResult(imageIdSimTupList, threshold, pictureType.getType(), sortParams);
            } else {
                Log.info("find no image by deviceIds&time");
            }
        } else {
            LOG.info("The feature of image is null or length less than 2048 byte!");
        }
        return searchResult;
    }

    private SearchResult compareByImageId(PictureType pictureType, SearchOption option) throws Exception {
        //根据imageId获取特征值
        byte[] fea = dynamicPhotoService.getFeature(imageId, pictureType);
        if (null != fea && fea.length == 2048) {
            //byte[] 转化为float[]
            float[] searchFea = FaceFunction.byteArr2floatArr(fea);
            //根据deviceId、时间参数圈定查询范围,得到一组rowkey,Hbase+es
            imageIdList = getImageIdListFromHbase(option);
            //根据imageId找出对应特征加入列表
            imageIdFeaTupList = getFeaByImageId(imageIdList, pictureType);
            if (null != imageIdList && imageIdList.size() > 0) {
                //对特征进行比对
                imageIdSimTupList = featureCompare(searchFea, imageIdFeaTupList);
                System.out.println("比对完成");
                //返回大于阈值的结果
                searchResult = lastResult(imageIdSimTupList, threshold, pictureType.getType(), sortParams);
            } else {
                Log.info("The feature of iamgeId:" + imageId + "is null or length less short 2048 byte!");
            }
        } else {
            Log.info("the feature of" + imageId + "is null or short than 2048");
        }
        return searchResult;
    }

    private SearchResult compareByOthers(PictureType pictureType, SearchOption option) throws Exception {
        //根据deviceId、时间参数圈定查询范围,得到一组rowkey
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
        for (String imageId : imageIdList) {
            System.out.println(imageId);
        }
        return imageIdList;
    }

    private List<Tuple2<String, float[]>> getFeaByImageId(List<String> imageIdList, final PictureType pictureType) throws Exception {
        imageIdFeaTupList = new ArrayList<>();
        JavaRDD<String> imageIdListRdd = jsc.parallelize(imageIdList);
        JavaPairRDD<String, float[]> imageIdFeaRDD = imageIdListRdd.mapToPair((PairFunction<String, String, float[]>) x -> new Tuple2<>(x, FaceFunction.byteArr2floatArr(dynamicPhotoService.getFeature(x, pictureType))));
        List<Tuple2<String, float[]>> tempList = imageIdFeaRDD.collect();
        for (Tuple2<String, float[]> temp : tempList) {
            System.out.println(temp.toString());
        }
        return imageIdFeaRDD.collect();
    }

    private List<Tuple2<String, Float>> featureCompare(final float[] searchFea, List<Tuple2<String, float[]>> resultFeaListTup) {
        JavaRDD<Tuple2<String, float[]>> resultFeaListRdd = jsc.parallelize(resultFeaListTup);
        JavaRDD<Tuple2<String, Float>> imageIdSimRDD = resultFeaListRdd.map((Function<Tuple2<String, float[]>, Tuple2<String, Float>>) v1 -> {
            if (null != v1._2 && v1._2.length == 512)
                return new Tuple2<>(v1._1, FaceFunction.featureCompare(searchFea, v1._2));
            else {
                return new Tuple2<>(v1._1, 0.00f);
            }
        });
        imageIdSimRDD.foreach((VoidFunction<Tuple2<String, Float>>) stringFloatTuple2 -> System.out.println("图片Id：" + stringFloatTuple2._1 + "相似度：" + stringFloatTuple2._2.toString()));
        return imageIdSimRDD.collect();
    }

    private SearchResult lastResult(final List<Tuple2<String, Float>> imageIdSimTup, final float threshold, final int type, String sortParams) {
        imgSimilarityMap = new HashMap<>();
        capturePictureSearchService = new CapturePictureSearchServiceImpl();
        capturedPictures = new ArrayList<>();
        JavaPairRDD<String, Float> imageIdSimTupRDD = jsc.parallelizePairs(imageIdSimTup);
        JavaPairRDD<String, Float> filterRDD = imageIdSimTupRDD.filter((Function<Tuple2<String, Float>, Boolean>) v1 -> (v1._2 > threshold));
        filterRDD.foreach((VoidFunction<Tuple2<String, Float>>) filterTuple -> {  //根据imageId 读取数据的其他信息
                    capturedPicture = new CapturedPicture();
                    //从动态库中读取图片信息
                    capturedPicture = capturePictureSearchService.getCaptureMessage(filterTuple._1, type + 6);
                    //设置similarity
                    capturedPicture.setSimilarity(filterTuple._2);
                    //加入到capturedPictures
                    capturedPictures.add(capturedPicture);
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
        capturedPictures = sortByParams(capturedPictures, sortParams);
        for (int i = 0; i < capturedPictures.size(); i++) {//内部不锁定，效率最高，但在多线程要考虑并发操作的问题。
            System.out.println("");
        }
        //分组返回
        List<CapturedPicture> subCapturedPictures = pageSplit(capturedPictures, offset, count);
        //返回最终结果
        searchResult = new SearchResult();
        //图片信息分组返回
        searchResult.setPictures(subCapturedPictures);
        //searchId 设置为imageId（rowkey）
        searchResult.setSearchId(queryImageId);
        //设置查询到的总得记录条数
        searchResult.setTotal(capturedPictures.size());
        //保存到Hbase
        //boolean flag = dynamicPhotoService.insertSearchRes(searchResult.getSearchId(), queryImageId, imgSimilarityMap);
        return searchResult;
    }

    /**
     * 通过sparkSQL进行排序
     *
     * @param capturedPictures 未排序前的查询结果列表
     * @param sortParams       排序参数
     * @return 排序后的查询结果列表
     */
    List<CapturedPicture> sortByParams(List<CapturedPicture> capturedPictures, String sortParams) {
        finalCapturedPictures = new ArrayList<>();
        SortParam sortParam = ListUtils.getOrderStringBySort(sortParams);
        for (CapturedPicture capturedPicture1 : capturedPictures) {//内部不锁定，效率最高，但在多线程要考虑并发操作的问题。
            System.out.println(capturedPicture1.toString());
        }
        if (null != sortParams && sortParams.length() > 0) {
            ListUtils.sort(capturedPictures, sortParam.getSortNameArr(), sortParam.getIsAscArr());
        } else {
            Log.info("sortParams is null!");
        }
        for (CapturedPicture finalCapturedPicture : finalCapturedPictures) {
            System.out.println(finalCapturedPicture.toString());
        }
        return capturedPictures;
    }

    List<CapturedPicture> pageSplit(List<CapturedPicture> capturedPictures, int offset, int count) {
        List<CapturedPicture> subCapturePictureList;
        int totalPicture = capturedPictures.size();
        if (offset > -1 && totalPicture > (offset + count - 1)) {
            //结束行小于总数
            subCapturePictureList = capturedPictures.subList(offset, offset + count);
        } else {
            //结束行大于总数
            subCapturePictureList = capturedPictures.subList(offset, totalPicture);
        }
        return subCapturePictureList;
    }
}

