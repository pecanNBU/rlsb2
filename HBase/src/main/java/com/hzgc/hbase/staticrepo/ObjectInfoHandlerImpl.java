package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoHandler;
import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.hbase.util.HBaseHelper;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.log4j.*;

public class ObjectInfoHandlerImpl implements ObjectInfoHandler {
    private static Logger LOG = Logger.getLogger(ObjectInfoHandlerImpl.class);

    @Override
    public byte addObjectInfo(String platformId, Map<String, Object> person) {
        Set<String> fieldset = person.keySet();
        List<String> fieldlist = new ArrayList<>();
        fieldlist.addAll(fieldset);
        String rowkey = UUID.randomUUID().toString().replace("-", "");
        // 获取table 对象，通过封装HBaseHelper 来获取
        Table objectinfo = HBaseHelper.getTable("objectinfo");
        //构造Put 对象
        Put put = new Put(Bytes.toBytes(rowkey));
        // 添加列族属性
        for (String field : fieldlist) {
            if ("photo".equals(field)) {
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes(field),
                        (byte[]) person.get(field));
            } else {
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes(field),
                        Bytes.toBytes((String) person.get(field)));
            }
        }
        // 给表格添加两个时间的字段，一个是创建时间，一个是更新时间
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = format.format(date);
        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("createtime"), Bytes.toBytes(dateString));
        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("updatetime"), Bytes.toBytes(dateString));
        put.addColumn(Bytes.toBytes("person"), Bytes.toBytes("platformId"), Bytes.toBytes(platformId));
        // 执行Put 操作，往表格里面添加一行数据
        try {
            objectinfo.put(put);
            LOG.info("Form addition successed!");
            return 0;
        } catch (IOException e) {
            LOG.error("Form addition failed!");
            e.printStackTrace();
            return 1;
        } finally {
            // 关闭表格和连接对象。
            HBaseHelper.closetableconn(objectinfo);
        }
    }

    @Override
    public int deleteObjectInfo(List<String> rowkeys) {
        // 获取table 对象，通过封装HBaseHelper 来获取
        Table table = HBaseHelper.getTable("objectinfo");
        List<Delete> deletes = new ArrayList<>();
        Delete delete;
        for (String rowkey : rowkeys) {
            delete = new Delete(Bytes.toBytes(rowkey));
            deletes.add(delete);
        }
        // 执行删除操作
        try {
            table.delete(deletes);
            LOG.info("table delete successed!");
            return 0;
        } catch (IOException e) {
            LOG.error("table delete failed!");
            e.printStackTrace();
            return 1;
        } finally {
            //关闭表连接
            HBaseHelper.closetableconn(table);
        }
    }

    @Override
    public int updateObjectInfo(Map<String, Object> person) {
        // 获取table 对象，通过封装HBaseHelper 来获取
        Table table = HBaseHelper.getTable("objectinfo");
        String id = (String) person.get("id");
        Set<String> fieldset = person.keySet();
        Iterator<String> it = fieldset.iterator();
        List<String> fieldlist = new ArrayList<>();
        while (it.hasNext()) {
            fieldlist.add(it.next());
        }
        Put put = new Put(Bytes.toBytes(id));
        for (String field : fieldlist) {
            if ("photo".equals(field)) {
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes(field),
                        (byte[]) person.get(field));
            } else {
                put.addColumn(Bytes.toBytes("person"), Bytes.toBytes(field),
                        Bytes.toBytes((String) person.get(field)));
            }
        }
        try {
            table.put(put);
            LOG.info("table update successed!");
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("table update failed!");
        } finally {
            //关闭表连接
            HBaseHelper.closetableconn(table);
        }
        return 0;
    }

    @Override
    public ObjectSearchResult getObjectInfo(String platformId, String name, String idCard,
                                            String rowkey, byte[] image,
                                            int threshold, List<String> pkeys,
                                            String creator, String cphone,
                                            long start, long pageSize,
                                            int serachId, String serachType,
                                            boolean moHuSearch) {
        return null;
    }

    @Override
    public ObjectSearchResult searchByPlatFormIdAndIdCard(String platformId, String IdCard,
                                                          boolean moHuSearch, long start, long pageSize) {
        return null;
    }

    @Override
    public ObjectSearchResult searchByRowkey(String rowkey) {
        return null;
    }

    @Override
    public ObjectSearchResult serachByCphone(String cphone) {
        return null;
    }

    @Override
    public ObjectSearchResult searchByCreator(String creator, boolean moHuSearch,
                                              long start, long pageSize) {
        return null;
    }

    @Override
    public ObjectSearchResult searchByName(String name, boolean moHuSearch,
                                           long start, long pageSize) {
        return null;
    }

    @Override
    public ObjectSearchResult serachByPhotoAndThreshold(String platformId, byte[] photo,
                                                        int threshold, String feature,
                                                        long start, long pageSize) {
        return null;
    }

    @Override
    public String getFeature(String tag, byte[] photo) {
        return null;
    }

    @Override
    public byte[] getPhotoByKey(String rowkey) {
        Table table = HBaseHelper.getTable("objectinfo");
        Scan scan = new Scan(Bytes.toBytes(rowkey));
        ResultScanner rs = null;
        try {
            rs = table.getScanner(scan);
            LOG.info("Get the scan result by rowkey successed!");
        } catch (IOException e) {
            LOG.error("Get the scan result by rowkey failed!");
            e.printStackTrace();
        }
        Iterator<Result> it;
        if (null != rs) {
            it = rs.iterator();
            byte[] photo = null;
            while (it.hasNext()) {
                Result r = it.next();
                photo = r.getValue(Bytes.toBytes("person"), Bytes.toBytes("photo"));
            }
            return photo;
        }
        HBaseHelper.closetableconn(table);
        return null;
    }
}
