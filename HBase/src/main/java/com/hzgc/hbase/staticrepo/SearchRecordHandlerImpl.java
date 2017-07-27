package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.dubbo.staticrepo.SearchRecordHandler;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.*;
public class SearchRecordHandlerImpl implements SearchRecordHandler {
    private static Logger LOG = Logger.getLogger(SearchRecordHandlerImpl.class);
    @Override
    public ObjectSearchResult getRocordOfObjectInfo(String rowkey) {
       Table table = HBaseHelper.getTable("srecord");
       Get get = new Get(Bytes.toBytes(rowkey));
       ObjectSearchResult objectSearchResult = new ObjectSearchResult();
       Result result = null;
        try {
            result = table.get(get);
            LOG.info("get data from table successed!");
        } catch (IOException e) {
            LOG.error("get data from table failed!");
            e.printStackTrace();
        }
        objectSearchResult.setSearchStatus(Bytes.toInt(result.getValue(Bytes.toBytes("rd"),Bytes.toBytes("searchstatus"))));
        objectSearchResult.setPhotoId(Bytes.toString(result.getValue(Bytes.toBytes("rd"),Bytes.toBytes("photoid"))));
        objectSearchResult.setSearchId(rowkey);
        objectSearchResult.setSearchNums(Bytes.toInt(result.getValue(Bytes.toBytes("rd"),Bytes.toBytes("searchnums"))));
        byte[] resultBySearch = result.getValue(Bytes.toBytes("rd"),Bytes.toBytes("results"));
        ObjectInputStream ois = null;
        List<Map<String, Object>> results = null;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(resultBySearch));
            results = (List<Map<String, Object>>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        objectSearchResult.setResults(results);
        HBaseUtil.closTable(table);
        return objectSearchResult;
    }

    @Override
    public byte[] getSearchPhoto(String rowkey) {
        Table table = HBaseHelper.getTable("srecord");
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result = null;
        try {
            result = table.get(get);
            LOG.info("get data from table successed!");
        } catch (IOException e) {
            LOG.error("get data from table failed!");
            e.printStackTrace();
        }
        byte[] photo = result.getValue(Bytes.toBytes("rd"), Bytes.toBytes("photo"));
        HBaseUtil.closTable(table);
        return photo;
    }
}
