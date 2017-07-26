package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.ObjectInfoHandler;
import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.hbase.util.HBaseHelper;
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
    public static ExecutorService pool = Executors.newCachedThreadPool();;

    public ObjectInfoHandlerImpl(){}

    public ObjectInfoHandlerImpl(String paltformID,  ObjectSearchResult objectSearchResult) {
        this.paltformID = paltformID;
        this.objectSearchResult = objectSearchResult;
    }

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
            try {
                objectinfo.close();
                LOG.info("table closed successed!");
            } catch (IOException e) {
                LOG.error("table closed failed!");
                e.printStackTrace();
            }
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
            try {
                table.close();
                LOG.info("table closed successed!");
            } catch (IOException e) {
                LOG.error("table closed failed!");
                e.printStackTrace();
            }
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
            try {
                table.close();
                LOG.info("table close successed!");
            } catch (IOException e) {
                LOG.error("table close failed!");
                e.printStackTrace();
            }
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
        ObjectSearchResult objectSearchResult = null;
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
                objectSearchResult = searchByMutiCondition(platformId, idCard, name, sex, rowkey, image, feature
                        ,threshold, pkeys, creator, cphone, start, pageSize
                        ,serachId, serachType, moHuSearch);
                break;
            }
        }
        return objectSearchResult;
    }

    // 还少一个根据图片搜索的功能
    public ObjectSearchResult searchByMutiCondition(String platformId, String idCard,String name, Integer sex,
                                                    String rowkey, byte[] image, String feature,int threshold,
                                                    List<String> pkeys, String creator, String cphone,
                                                    long start, long pageSize,
                                                    String serachId,
                                                    String serachType, boolean moHuSearch){
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
        // 把数据存到HBase，
        // 另外起一个进程，把查询记录存到HBase 的searchrecord 里面
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        //putSearchRecordToHBase(platformId, searchResult);

        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByPlatFormIdAndIdCard(String platformId, String IdCard,
                                                          boolean moHuSearch, long start, long pageSize) {
        // 构造搜索对象
        SearchResponse response = null;
        // 设置搜索条件(根据idcard 分页和 模糊查询情况)
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient().prepareSearch("objectinfo")
                .setTypes("person").setExplain(true);

        if (platformId != null){
            requestBuilder.setQuery(QueryBuilders.termQuery("platformid", platformId));
        }

        // 判断是否是模糊查询
        if (moHuSearch){
            // 如果是模糊查询，需要构造正则表达式
            IdCard = ".*" + IdCard + ".*";
            QueryBuilder qb = regexpQuery(
                    "idcard",
                    IdCard
            );

            requestBuilder.setQuery(qb);
            // 判断是否要分页
            if (start != -1 && pageSize != -1){
                // 分页的情况下,设置分页，并且把数据存到HBase
                requestBuilder.setFrom((int)start).setSize((int)pageSize);
            }
        } else {
            // 如果不是模糊查询，直接通过匹配Id 来查
            requestBuilder.setQuery(QueryBuilders.termQuery("idcard", IdCard));
        }

        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(requestBuilder);

        // 把数据存到HBase，
        // 另外起一个进程，把查询记录存到HBase 的searchrecord 里面
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        //putSearchRecordToHBase(platformId, searchResult);

        return searchResult;
    }

    /**
     * 根据rowkey 取出相应的人员信息，实现可以直接从HBase 中读取数据。
     * @param rowkey  标记一条对象信息的唯一标志。
     * @return 返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    @Override
    public ObjectSearchResult searchByRowkey(String rowkey) {
        // 获取表格对象
        Table table = new HBaseHelper().getTable("objectinfo");
        // 构造Get 对象，确定需要取的是哪一行
        Get get = new Get(Bytes.toBytes(rowkey));
        // 通过table 的get 方法返回一行数据，
        Result result = null;
        //构造要封装的返回对象
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        // bool 值, 判断表是否存在
        boolean tableExits;
        // 生成查询rowkey
        String searchRowkey = UUID.randomUUID().toString().replace("-", "");
        objectSearchResult.setSearchId(searchRowkey); // 设置查询ID

        try {
            // 如果存在
            tableExits = table.exists(get);
            if (tableExits){
                result = table.get(get);
                Cell[] cells =  result.rawCells();
                List<Map<String, Object>> hits = new ArrayList<Map<String, Object>>();
                Map<String, Object> person = new HashMap<String, Object>();
                for (Cell cell:cells){
                    String column = Bytes.toString(cell.getQualifierArray());
                    column = column.substring(column.indexOf("person") + 6, column.indexOf("]h") - 1).trim();
                    if ("photo".equals(column)){
                        continue;
                    } else {
                        String value = Bytes.toString(cell.getValueArray());
                        value = value.substring(value.indexOf('>') + 1).trim();
                        System.out.println(value);
                        person.put(column, value);
                    }
                }
                hits.add(person);
                objectSearchResult.setResults(hits);  // 封装查询到的人员列表。
                objectSearchResult.setSearchStatus(0);
                objectSearchResult.setPhotoId(null);
                objectSearchResult.setSearchNums(1);
            } else {
                objectSearchResult.setResults(null); // 搜索到的结果数为空
                objectSearchResult.setSearchStatus(1); // 搜索到的状态为1，
                objectSearchResult.setSearchNums(0); // 搜索到的数量
                objectSearchResult.setPhotoId(null);  // 传过来的照片为空。
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // setObjectSearchResult(objectSearchResult);
        pool.execute(new PutRecoredToHBaseThread(null, objectSearchResult));

        return objectSearchResult;
    }


    @Override
    public ObjectSearchResult searchByCphone(String cphone) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchResponse response = null;
        SearchRequestBuilder requestBuilder = client.prepareSearch("objectinfo")
                .setTypes("person")
                .setQuery(QueryBuilders.termQuery("cphone", cphone))
                .setExplain(true);

        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(requestBuilder);

        // 另外起一个进程，把查询记录存到HBase 的searchrecord 里面
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        //putSearchRecordToHBase(platformId, searchResult);

        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByCreator(String creator, boolean moHuSearch,
                                              long start, long pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchResponse response = null;
        SearchRequestBuilder requestBuilder = client.prepareSearch("objectinfo")
                .setTypes("person")
                .setExplain(true);

        // 判断是否是模糊查询
        if (moHuSearch){
            // 如果是模糊查询，需要构造正则表达式
            creator = ".*" + creator + ".*";
            QueryBuilder qb = regexpQuery(
                    "creator",
                    creator
            );

            requestBuilder.setQuery(qb);
            // 判断是否要分页
            if (start != -1 && pageSize != -1){
                // 分页的情况下,设置分页，并且把数据存到HBase
                requestBuilder.setFrom((int)start).setSize((int)pageSize);
            }
        } else {
            // 如果不是模糊查询，直接通过匹配Id 来查
            requestBuilder.setQuery(QueryBuilders.termQuery("creator", creator));
        }

        ObjectSearchResult searchResult = dealWithSearchRequesBuilder(requestBuilder);

        // 把数据存到HBase，
        // 另外起一个进程，把查询记录存到HBase 的searchrecord 里面
        pool.execute(new PutRecoredToHBaseThread(paltformID, searchResult));
        //putSearchRecordToHBase(platformId, searchResult);
        return searchResult;
    }

    @Override
    public ObjectSearchResult searchByName(String name, boolean moHuSearch,
                                           long start, long pageSize) {
        return null;
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
        return null;
    }

    // 存储历史记录
    // 把list对象转化成字节数组的时候，里面的数据必须是可以序列化的，
    // 所以ObjectSearchResult 必须实现Serializable 接口
    public void putSearchRecordToHBase(String platformId, ObjectSearchResult searchResult){
        // 把查找出的list 数据转化成byte[] 数组，然后存到HBase
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(bout);
            oout.writeObject(searchResult.getResults());
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] results = bout.toByteArray();
        try {
            oout.close();
            bout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Table table = new HBaseHelper().getTable("srecord");
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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ObjectSearchResult dealWithSearchRequesBuilder(SearchRequestBuilder searchRequestBuilder){
        SearchResponse response = searchRequestBuilder.get();
        // 封装返回的结果
        SearchHits hits = response.getHits();
        System.out.println("总记录数是： " + hits.getTotalHits()); // 查询到的记录的封装
        SearchHit[] searchHits = hits.getHits();  // 实际返回的记录
        ObjectSearchResult searchResult = new ObjectSearchResult();
        String searchId = UUID.randomUUID().toString().replace("-", "");
        searchResult.setSearchId(searchId);  // 搜索ID
        searchResult.setPhotoId(null);  // 照片ID
        searchResult.setSearchNums(hits.getTotalHits());  // 搜索出的记录数
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();  // 搜索出来的人员信息列表
        System.out.println("需要返回的记录数是： " + searchHits.length);
        if (searchHits.length > 0){
            for (SearchHit hit:searchHits){
                Map<String, Object> source = hit.getSource();
                source.put("id", hit.getId());
                results.add(source);
            }
        }
        searchResult.setSearchStatus(0);  // 搜索状态
        searchResult.setResults(results); // 设置搜索出来的ID
        if (results.size() < 1){
            searchResult.setSearchStatus(1);
        }
        return searchResult;
    }

}

// 执行插入历史查询记录的线程
class PutRecoredToHBaseThread implements Runnable{
    ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();
    public PutRecoredToHBaseThread(){}
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
