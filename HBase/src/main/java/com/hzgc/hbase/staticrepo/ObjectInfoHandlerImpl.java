package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoHandler;
import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.*;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;

public class ObjectInfoHandlerImpl implements ObjectInfoHandler {
    private static Logger LOG = Logger.getLogger(ObjectInfoHandlerImpl.class);
    private String paltformID;
    private ObjectSearchResult objectSearchResult;
    private static ExecutorService pool = Executors.newCachedThreadPool();

    public ObjectInfoHandlerImpl(){}


    public String getPaltformID() {
        return paltformID;
    }

    public void setPaltformID(String paltformID) {
        this.paltformID = paltformID;
    }

    public ObjectSearchResult getObjectSearchResult() {
        return objectSearchResult;
    }

    public void setObjectSearchResult(ObjectSearchResult objectSearchResult) {
        this.objectSearchResult = objectSearchResult;
    }

    @Override
    public byte addObjectInfo(String platformId, Map<String, Object> person) {
        Set<String> fieldset = person.keySet();
        List<String> fieldlist = new ArrayList<>();
        fieldlist.addAll(fieldset);
        String rowkey = UUID.randomUUID().toString().replace("-", "");
        LOG.info("rowkey: " + rowkey);
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
            HBaseUtil.closTable(objectinfo);
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
            HBaseUtil.closTable(table);
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
            HBaseUtil.closTable(table);;
        }
        return 0;
    }

    @Override
    public ObjectSearchResult getObjectInfo(String platformId, String name, String idCard, int sex,
                                            String rowkey, byte[] image, String feature,
                                            int threshold, List<String> pkeys,
                                            String creator, String cphone,
                                            long start, long pageSize,
                                            String serachId, String serachType,
                                            boolean moHuSearch) {
        ObjectSearchResult objectSearchResult;
        switch (serachType){
            case "searchByPlatFormIdAndIdCard":{
                objectSearchResult = searchByCreator(creator, moHuSearch, start, pageSize);
                break;
            }
            case "searchByRowkey":{
                objectSearchResult = searchByRowkey(rowkey);
                break;
            }
            case "searchByCphone":{
                objectSearchResult = searchByCphone(cphone);
                break;
            }
            case "searchByCreator": {
                objectSearchResult = searchByCreator(creator, moHuSearch, start, pageSize);
                break;
            }
            case "searchByName": {
                objectSearchResult = searchByName(name, moHuSearch, start, pageSize);
                break;
            }
            case "serachByPhotoAndThreshold":{
                objectSearchResult = searchByPhotoAndThreshold(paltformID, image, threshold, feature, start, pageSize);
            }
            default:{
                objectSearchResult = searchByMutiCondition(platformId, idCard, name, sex, rowkey, feature
                        ,threshold, pkeys, creator, cphone, start, pageSize,moHuSearch);
                break;
            }
        }
        return objectSearchResult;
    }

    //功能跟有待完善
    public ObjectSearchResult searchByMutiCondition(String platformId, String idCard,String name, Integer sex,
                                                    String rowkey,String feature,int threshold,
                                                    List<String> pkeys, String creator, String cphone,
                                                    long start, long pageSize,boolean moHuSearch){
        SearchResponse response = null;
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch("objectinfo")
                .setTypes("person")
                .setExplain(true);
        if (platformId != null){
            requestBuilder.setQuery(QueryBuilders.termQuery("platformid", platformId));
        }
        if (sex != null){
            requestBuilder.setQuery(QueryBuilders.termQuery("sex", sex));
        }
        if (cphone != null){
            requestBuilder.setQuery(QueryBuilders.termQuery("cphone", cphone));
        }
        if (rowkey != null){
            requestBuilder.setQuery(QueryBuilders.termQuery("rowkey", rowkey));
        }
        if (idCard != null){
            idCard = ".*" + idCard + ".*";
            QueryBuilder qb = regexpQuery(
                    "idcard",
                    idCard
            );
            requestBuilder.setQuery(qb);
        }
        if (name != null){
            name = ".*" + name + ".*";
            QueryBuilder qb = regexpQuery(
                    "name",
                    name
            );
            requestBuilder.setQuery(qb);
        }
        if (creator != null){
            creator = ".*" + creator + ".*";
            QueryBuilder qb = regexpQuery(
                    "creator",
                    creator
            );
            requestBuilder.setQuery(qb);
        }
        if (start != -1 && pageSize != -1){
            requestBuilder.setFrom((int)start).setSize((int) pageSize);
        }
        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(requestBuilder);
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByPlatFormIdAndIdCard(String platformId, String IdCard,
                                                          boolean moHuSearch, long start, long pageSize) {
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient().prepareSearch("objectinfo")
                .setTypes("person").setExplain(true);
        if (platformId != null){
            requestBuilder.setQuery(QueryBuilders.termQuery("platformid", platformId));
        }
        if (moHuSearch){
            IdCard = ".*" + IdCard + ".*";
            QueryBuilder qb = regexpQuery(
                    "idcard",
                    IdCard
            );
            requestBuilder.setQuery(qb);
            if (start != -1 && pageSize != -1){
                requestBuilder.setFrom((int)start).setSize((int)pageSize);
            }
        } else {
            requestBuilder.setQuery(QueryBuilders.termQuery("idcard", IdCard));
        }
        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(requestBuilder);
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByRowkey(String rowkey) {
        Table table = HBaseHelper.getTable("objectinfo");
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result;
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        boolean tableExits;
        String searchRowkey = UUID.randomUUID().toString().replace("-", "");
        objectSearchResult.setSearchId(searchRowkey);
        try {
            tableExits = table.exists(get);
            if (tableExits){
                result = table.get(get);
                Cell[] cells =  result.rawCells();
                List<Map<String, Object>> hits = new ArrayList<>();
                Map<String, Object> person = new HashMap<>();
                for (Cell cell:cells){
                    String column = Bytes.toString(cell.getQualifierArray());
                    column = column.substring(column.indexOf("person") + 6, column.indexOf("]h") - 1).trim();
                    if (!"photo".equals(column)){
                        String value = Bytes.toString(cell.getValueArray());
                        value = value.substring(value.indexOf('>') + 1).trim();
                        person.put(column, value);
                    }
                }
                hits.add(person);
                objectSearchResult.setResults(hits);
                objectSearchResult.setSearchStatus(0);
                objectSearchResult.setPhotoId(null);
                objectSearchResult.setSearchNums(1);
            } else {
                objectSearchResult.setResults(null);
                objectSearchResult.setSearchStatus(1);
                objectSearchResult.setSearchNums(0);
                objectSearchResult.setPhotoId(null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.execute(new PutRecoredToHBaseThread(null, objectSearchResult));

        return objectSearchResult;
    }


    @Override
    public ObjectSearchResult searchByCphone(String cphone) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch("objectinfo")
                .setTypes("person")
                .setQuery(QueryBuilders.termQuery("cphone", cphone))
                .setExplain(true);
        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(requestBuilder);
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByCreator(String creator, boolean moHuSearch,
                                              long start, long pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch("objectinfo")
                .setTypes("person")
                .setExplain(true);
        if (moHuSearch){
            creator = ".*" + creator + ".*";
            QueryBuilder qb = regexpQuery(
                    "creator",
                    creator
            );
            requestBuilder.setQuery(qb);
            if (start != -1 && pageSize != -1){
                requestBuilder.setFrom((int)start).setSize((int)pageSize);
            }
        } else {
            requestBuilder.setQuery(QueryBuilders.termQuery("creator", creator));
        }
        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(requestBuilder);
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByName(String name, boolean moHuSearch,
                                           long start, long pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch("objectinfo")
                .setTypes("person")
                .setExplain(true);
        if(name != null && moHuSearch){
            name = ".*" + name + ".*";
            QueryBuilder qb = regexpQuery("name", name);
            requestBuilder.setQuery(qb);
            if (start != -1 && pageSize != -1){
                requestBuilder.setFrom((int)start).setSize((int)pageSize);
            }
        }else if(name != null && !moHuSearch){
            requestBuilder.setQuery(QueryBuilders.termQuery("name",name));
            if (start != -1 && pageSize != -1){
                requestBuilder.setFrom((int)start).setSize((int)pageSize);
            }
        }
        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(requestBuilder);
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByPhotoAndThreshold(String platformId, byte[] photo,
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
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result = null;
        try {
            result = table.get(get);
            LOG.info("get data from table successed!");
        } catch (IOException e) {
            LOG.error("get data from table failed!");
            e.printStackTrace();
        }
        byte[] photo = result.getValue(Bytes.toBytes("person"), Bytes.toBytes("photo"));
        HBaseUtil.closTable(table);
        return photo;
    }

    public void putSearchRecordToHBase(String platformId, ObjectSearchResult searchResult){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = null;
        byte[] results = null;
        try {
            oout = new ObjectOutputStream(bout);
            oout.writeObject(searchResult.getResults());
            results = bout.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oout.close();
                bout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Table table = HBaseHelper.getTable("srecord");
        Put put = new Put(Bytes.toBytes(searchResult.getSearchId()));
        put.addColumn(Bytes.toBytes("rd"), Bytes.toBytes("searchstatus"),
                Bytes.toBytes(searchResult.getSearchStatus()))
                .addColumn(Bytes.toBytes("rd"), Bytes.toBytes("searchnums"),
                        Bytes.toBytes(searchResult.getSearchNums()));
        if (paltformID != null){
            put.addColumn(Bytes.toBytes("rd"), Bytes.toBytes("paltformid"),
                    Bytes.toBytes(platformId));
        }
        if (searchResult.getPhotoId() != null){
            put.addColumn(Bytes.toBytes("rd"), Bytes.toBytes("photoid"),
                    Bytes.toBytes(searchResult.getPhotoId()));
        }
        if (results != null){
            put.addColumn(Bytes.toBytes("rd"), Bytes.toBytes("results"), results);
        }
        try {
            table.put(put);
            LOG.info("excute putSearchRecordToHBase done.");
        } catch (IOException e) {
            LOG.info("excute putSearchRecordToHBase failed.");
            e.printStackTrace();
        } finally {
            LOG.info("释放table 对象......");
            HBaseUtil.closTable(table);
        }
    }

    private ObjectSearchResult dealWithSearchRequesBuilder(SearchRequestBuilder searchRequestBuilder){
        SearchResponse response = searchRequestBuilder.get();
        SearchHits hits = response.getHits();
        LOG.info("总记录数是： " + hits.getTotalHits());
        SearchHit[] searchHits = hits.getHits();
        ObjectSearchResult searchResult = new ObjectSearchResult();
        String searchId = UUID.randomUUID().toString().replace("-", "");
        searchResult.setSearchId(searchId);
        searchResult.setPhotoId(null);
        searchResult.setSearchNums(hits.getTotalHits());
        List<Map<String, Object>> results = new ArrayList<>();
        LOG.info("需要返回的记录数是： " + searchHits.length);
        if (searchHits.length > 0){
            for (SearchHit hit:searchHits){
                Map<String, Object> source = hit.getSource();
                source.put("id", hit.getId());
                results.add(source);
            }
        }
        searchResult.setSearchStatus(0);
        searchResult.setResults(results);
        if (results.size() < 1){
            searchResult.setSearchStatus(1);
        }
        return searchResult;
    }

}

class PutRecoredToHBaseThread implements Runnable{
    private ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();
    public PutRecoredToHBaseThread(String platformId, ObjectSearchResult objectSearchResult){
        if (platformId != null){
            objectInfoHandler.setPaltformID(platformId);
        }
        if (objectSearchResult != null){
            objectInfoHandler.setObjectSearchResult(objectSearchResult);
        }
    }
    @Override
    public void run() {
        new ObjectInfoHandlerImpl().putSearchRecordToHBase(objectInfoHandler.getPaltformID(),
                objectInfoHandler.getObjectSearchResult());
    }
}
