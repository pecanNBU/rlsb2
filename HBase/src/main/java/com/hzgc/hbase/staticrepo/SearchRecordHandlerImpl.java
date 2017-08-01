package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.dubbo.staticrepo.SearchRecordHandler;
import com.hzgc.dubbo.staticrepo.SrecordTable;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import org.apache.log4j.*;

public class SearchRecordHandlerImpl implements SearchRecordHandler {
    private static Logger LOG = Logger.getLogger(SearchRecordHandlerImpl.class);

    @Override
    public ObjectSearchResult getRocordOfObjectInfo(String rowkey,int from,int size) {
        Table table = HBaseHelper.getTable(SrecordTable.TABLE_NAME);
        Get get = new Get(Bytes.toBytes(rowkey));
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        List<Map<String,Object>> filterList = null;
        Result result = null;
        try {
            result = table.get(get);
        } catch (IOException e) {
            LOG.error("get data by rowkey from srecord table failed! used method getRocordOfObjectInfo.");
            e.printStackTrace();
        }
        if (result != null) {
            String useString = result + ".";
            if (useString.contains(SrecordTable.SEARCH_STATUS)) {
                objectSearchResult.setSearchStatus(Bytes.toInt(result.getValue(Bytes.toBytes(SrecordTable.RD_CLOF),
                        Bytes.toBytes(SrecordTable.SEARCH_STATUS))));
            }
            if(useString.contains(SrecordTable.PHOTOID)) {
                objectSearchResult.setPhotoId(Bytes.toString(result.getValue(Bytes.toBytes(SrecordTable.RD_CLOF),
                        Bytes.toBytes(SrecordTable.PHOTOID))));
            }
            objectSearchResult.setSearchId(rowkey);
            if (useString.contains(SrecordTable.SEARCH_NUMS)) {
                objectSearchResult.setSearchNums(Bytes.toInt(result.getValue(Bytes.toBytes(SrecordTable.RD_CLOF),
                        Bytes.toBytes(SrecordTable.SEARCH_NUMS))));
            }
            if(useString.contains(SrecordTable.RESULTS)) {
                byte[] resultBySearch = result.getValue(Bytes.toBytes(SrecordTable.RD_CLOF),
                        Bytes.toBytes(SrecordTable.RESULTS));
                ObjectInputStream ois;
                List<Map<String, Object>> results;
                try {
                    ois = new ObjectInputStream(new ByteArrayInputStream(resultBySearch));
                    results = (List<Map<String, Object>>) ois.readObject();
                    if(from + size - 1 < results.size()) {
                        filterList = results.subList(from - 1, from + size - 1);
                    }else{
                        filterList = results.subList(from - 1, results.size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    HBaseUtil.closTable(table);
                }
                objectSearchResult.setResults(filterList);
            }
        }
        return objectSearchResult;
    }

    @Override
    public byte[] getSearchPhoto(String rowkey) {
        Table table = HBaseHelper.getTable("srecord");
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result = null;
        try {
            result = table.get(get);
        } catch (IOException e) {
            LOG.error("get data by rowkey from srecord table failed! used method getSearchPhoto.");
            e.printStackTrace();
        }finally {
            HBaseUtil.closTable(table);
        }
        return result.getValue(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PHOTO));
    }
}
