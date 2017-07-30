package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.*;
import com.hzgc.ftpserver.util.FtpUtil;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 以图搜图接口实现类，内含五个方法（外）（彭聪）
 */
public class CapturePictureSearchServiceImpl implements CapturePictureSearchService {
    private static Logger LOG = Logger.getLogger(CapturePictureSearchServiceImpl.class);

    /**
     * 接收应用层传递的参数进行搜图，如果大数据处理的时间过长，
     * 则先返回searchId,finished=false,然后再开始计算；如果能够在秒级时间内计算完则计算完后再返回结果
     *
     * @param option 搜索选项
     * @return 搜索结果SearchResult对象
     */
    @Override
    public SearchResult search(SearchOption option) {
        SearchResult searchResult = new SearchResult();
        Scan scan = new Scan();

        if (null != option) {
            if (!option.getDeviceIds().isEmpty() && null != option.getSearchType()) {
                List<String> rowKeyList = new FilterByRowkey().filterByDeviceId(option, scan);

            }
        }

        return null;
    }

    /**
     * @param searchId 搜索的 id（rowkey）
     * @param offset   从第几条开始
     * @param count    条数
     * @return SearchResult对象
     */
    @Override
    public SearchResult getSearchResult(String searchId, int offset, int count) {
        Table searchResTable = HBaseHelper.getTable(DynamicTable.TABLE_SEARCHRES);
        Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);

        Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(searchId + ".*"));
        Get get = null;
        get.setFilter(filter);

        SearchResult searchResult = null;
        List<CapturedPicture> capturedPictureList = null;
        List<CapturedPicture> capturedPictureCutList = null;

        boolean ifHaveResult = false;
        try {
            ifHaveResult = searchResTable.exists(get);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ifHaveResult) {
            Scan scan = new Scan();
            scan.setFilter(filter);
            try {
                ResultScanner resultScanner = searchResTable.getScanner(scan);
                for (Result result : resultScanner) {
                    searchResult.setFinished(true);

                    String imageId = Bytes.toString(result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SEARCHIMAGEID));
                    searchResult.setImageId(imageId);

                    CapturedPicture capturedPicture = null;
                    String returnId = Bytes.toString(result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_RESIMAGEID));
                    capturedPicture.setId(returnId);

                    Get get1 = new Get(Bytes.toBytes(returnId));
                    Result personResult = personTable.get(get1);

                    String description = Bytes.toString(personResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE));
                    capturedPicture.setDescription(description);

                    String ipcID = Bytes.toString(personResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IPCID));
                    capturedPicture.setIpcId(ipcID);

                    int similarity = Bytes.toInt(result.getValue(DynamicTable.SEARCHRES_COLUMNFAMILY, DynamicTable.SEARCHRES_COLUMN_SIMILARITY));
                    capturedPicture.setSimilarity(similarity);

                    String extend = Bytes.toString(personResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA));
                    capturedPicture.setExtend(extend);

                    byte[] smallImage = personResult.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    capturedPicture.setSmallImage(smallImage);

                    capturedPictureList.add(capturedPicture);
                    capturedPictureCutList = capturedPictureList.subList(offset - 1, offset + count - 1);
                }
                if (null != capturedPictureCutList) {

                    searchResult.setPictures(capturedPictureCutList);
                }
                searchResult.setSearchId(searchId);
                searchResult.setTotal(capturedPictureList.size());
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("get SearchResult Object by searchId from table_searchres failed! used method CapturePictureSearchServiceImpl.getSearchResult.");
            }
        }
        return searchResult;
    }

    /**
     * 查看人、车图片有哪些属性
     *
     * @param type 图片类型（人、车）
     * @return 过滤参数键值对
     */
    @Override
    public Map<String, String> getSearchFilterParams(int type) {
        return null;
    }

    /**
     * 根据id（rowkey）获取动态信息库内容（DynamicObject对象）（刘思阳）
     *
     * @param imageId id（rowkey）
     * @param type    图片类型，人/车
     * @return DynamicObject    动态库对象
     */
    @Override
    public CapturedPicture getCaptureMessage(String imageId, int type) {
        CapturedPicture capturedPicture = new CapturedPicture();
        capturedPicture.setId(imageId);

        Map<String, String> map = FtpUtil.getRowKeyMessage(imageId);
        if (!map.isEmpty()) {
            String ipcID = map.get("ipcID");
            String timeStampStr = map.get("time");
            capturedPicture.setIpcId(ipcID);
            capturedPicture.setTimeStamp(Long.valueOf(timeStampStr));
        }

        String rowKey = imageId.substring(0, imageId.lastIndexOf("_"));
        StringBuilder bigImageRowKey = new StringBuilder();
        bigImageRowKey.append(rowKey).append("_").append("00");

        if (null != imageId && PictureType.PERSON.equals(type) || PictureType.CAR.equals(type)) {
            if (PictureType.PERSON.equals(type)) {
                Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERFEA);
                try {
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = person.get(get);

                    byte[] smallImage = result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    capturedPicture.setSmallImage(smallImage);

                    String des = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_DESCRIBE));
                    capturedPicture.setDescription(des);

                    String ex = Bytes.toString(result.getValue(DynamicTable.PERSON_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_EXTRA));
                    capturedPicture.setExtend(ex);

                    Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                    Result bigImageResult = person.get(bigImageGet);

                    byte[] bigImage = bigImageResult.getValue(DynamicTable.PERFEA_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    capturedPicture.setBigImage(bigImage);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get DynamicObject by rowkey from table_person failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.");
                } finally {
                    HBaseUtil.closTable(person);
                }
            } else if (PictureType.CAR.equals(type)) {
                Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
                try {
                    Get get = new Get(Bytes.toBytes(imageId));
                    Result result = car.get(get);

                    byte[] smallImage = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_IMGE);
                    capturedPicture.setSmallImage(smallImage);

                    String des = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_DESCRIBE));
                    capturedPicture.setDescription(des);

                    String ex = Bytes.toString(result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_EXTRA));
                    capturedPicture.setExtend(ex);

                    Get bigImageGet = new Get(Bytes.toBytes(bigImageRowKey.toString()));
                    Result bigImageResult = car.get(bigImageGet);

                    byte[] bigImage = bigImageResult.getValue(DynamicTable.PERFEA_COLUMNFAMILY, DynamicTable.PERSON_COLUMN_IMGE);
                    capturedPicture.setBigImage(bigImage);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get DynamicObject by rowkey from table_car failed! used method CapturePictureSearchServiceImpl.getCaptureMessage.");
                } finally {
                    HBaseUtil.closTable(car);
                }
            }
        } else {
            LOG.error("method CapturePictureSearchServiceImpl.getCaptureMessage param is empty.");
        }
        return capturedPicture;
    }
}
