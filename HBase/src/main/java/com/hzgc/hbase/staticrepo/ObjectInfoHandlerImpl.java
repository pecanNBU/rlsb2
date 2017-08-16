package com.hzgc.hbase.staticrepo;

import com.hzgc.dubbo.staticrepo.*;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.log4j.*;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import sun.rmi.transport.ObjectTable;

public class ObjectInfoHandlerImpl implements ObjectInfoHandler {
    private static Logger LOG = Logger.getLogger(ObjectInfoHandlerImpl.class);

   public ObjectInfoHandlerImpl(){
        NativeFunction.init();
    }

    @Override
    public byte addObjectInfo(String platformId, Map<String, Object> person) {
        Set<String> fieldset = person.keySet();
        List<String> fieldlist = new ArrayList<>();
        fieldlist.addAll(fieldset);
        String idcard = ObjectInfoTable.IDCARD;
        String rowkey =  platformId + idcard;
        LOG.info("rowkey: " + rowkey);
        // 获取table 对象，通过封装HBaseHelper 来获取
        Table objectinfo = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        //构造Put 对象
        Put put = new Put(Bytes.toBytes(rowkey));
        // 添加列族属性
        for (String field : fieldlist) {
            if (ObjectInfoTable.PHOTO.equals(field)) {
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                        (byte[]) person.get(field));
            } else {
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                        Bytes.toBytes((String) person.get(field)));
            }
        }
        // 给表格添加两个时间的字段，一个是创建时间，一个是更新时间
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = format.format(date);
        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.CREATETIME), Bytes.toBytes(dateString));
        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.UPDATETIME), Bytes.toBytes(dateString));
        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.PLATFORMID), Bytes.toBytes(platformId));
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
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
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
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        String id = (String) person.get(ObjectInfoTable.ROWKEY);
        Set<String> fieldset = person.keySet();
        Iterator<String> it = fieldset.iterator();
        List<String> fieldlist = new ArrayList<>();
        while (it.hasNext()) {
            fieldlist.add(it.next());
        }
        Put put = new Put(Bytes.toBytes(id));
        for (String field : fieldlist) {
            if (ObjectInfoTable.PHOTO.equals(field)) {
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                        (byte[]) person.get(field));
            } else {
                put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF), Bytes.toBytes(field),
                        Bytes.toBytes((String) person.get(field)));
            }
        }
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = format.format(date);
        put.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                Bytes.toBytes(ObjectInfoTable.UPDATETIME), Bytes.toBytes(dateString));
        if(fieldlist.contains(ObjectInfoTable.IDCARD)){
            Get get = new Get(Bytes.toBytes(id));
            try {
                table.put(put);
                System.out.println(put);
                LOG.info("table update successed!");
                Result result = table.get(get);
                String platformid = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PLATFORMID)));
                String idcard = (String) person.get(ObjectInfoTable.IDCARD);
                String newRowKey = platformid + idcard;
                Put put1 = new Put(Bytes.toBytes(newRowKey));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.PLATFORMID),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.PLATFORMID)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.TAG),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.TAG)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.PKEY),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.PKEY)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.NAME),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.NAME)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.SEX),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.SEX)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.PHOTO),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.PHOTO)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.FEATURE),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.FEATURE)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.REASON),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.REASON)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.CREATOR),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.CREATOR)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.CPHONE),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.CPHONE)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.CREATETIME),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.CREATETIME)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.UPDATETIME),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.UPDATETIME)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.RELATED),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.RELATED)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),Bytes.toBytes(ObjectInfoTable.ROWKEY),
                        result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                                Bytes.toBytes(ObjectInfoTable.ROWKEY)));
                put1.addColumn(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.IDCARD),Bytes.toBytes(idcard));
                table.put(put1);
                Delete delete = new Delete(Bytes.toBytes(id));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PLATFORMID));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.TAG));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PKEY));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.NAME));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.SEX));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.PHOTO));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.FEATURE));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.REASON));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CREATOR));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CPHONE));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.CREATETIME));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.UPDATETIME));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.RELATED));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.IDCARD));
                delete.addColumns(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                        Bytes.toBytes(ObjectInfoTable.ROWKEY));
                table.delete(delete);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("table update failed!");
            }finally {
                //关闭表连接
                HBaseUtil.closTable(table);
            }
        }
        return 0;
    }

    @Override
    public ObjectSearchResult getObjectInfo(PSearchArgsModel pSearchArgsModel) {
        ObjectSearchResult objectSearchResult;
        switch (pSearchArgsModel.getSearchType()){
            case "searchByPlatFormIdAndIdCard":{
                objectSearchResult = searchByPlatFormIdAndIdCard(pSearchArgsModel.getPaltaformId(),
                        pSearchArgsModel.getIdCard(),
                        pSearchArgsModel.isMoHuSearch(),
                        pSearchArgsModel.getStart(),
                        pSearchArgsModel.getPageSize());
                break;
            }
            case "searchByPhotoAndThreshold":{
                objectSearchResult = searchByPhotoAndThreshold(pSearchArgsModel.getPaltaformId(),
                        pSearchArgsModel.getImage(), pSearchArgsModel.getThredshold(),
                        pSearchArgsModel.getFeature(), pSearchArgsModel.getStart(),
                        pSearchArgsModel.getPageSize());
                break;
            }
            case "searchByRowkey":{
                objectSearchResult = searchByRowkey(pSearchArgsModel.getRowkey());
                break;
            }
            case "searchByCphone":{
                objectSearchResult = searchByCphone(pSearchArgsModel.getCphone(), pSearchArgsModel.getStart(),
                        pSearchArgsModel.getPageSize());
                break;
            }
            case "searchByCreator": {
                objectSearchResult = searchByCreator(pSearchArgsModel.getCreator(),
                        pSearchArgsModel.isMoHuSearch(),
                        pSearchArgsModel.getStart(), pSearchArgsModel.getPageSize());
                break;
            }
            case "searchByName": {
                objectSearchResult = searchByName(pSearchArgsModel.getName(),
                        pSearchArgsModel.isMoHuSearch(),
                        pSearchArgsModel.getStart(), pSearchArgsModel.getPageSize());
                break;
            }
            case "serachByPhotoAndThreshold":{
                objectSearchResult = searchByPhotoAndThreshold(pSearchArgsModel.getPaltaformId(),
                        pSearchArgsModel.getImage(), pSearchArgsModel.getThredshold(),
                        pSearchArgsModel.getFeature(),
                        pSearchArgsModel.getStart(),
                        pSearchArgsModel.getPageSize());
                break;
            }
            default:{
                objectSearchResult = searchByMutiCondition(pSearchArgsModel.getPaltaformId(),
                        pSearchArgsModel.getIdCard(), pSearchArgsModel.getName(),
                        pSearchArgsModel.getSex(), pSearchArgsModel.getImage(), pSearchArgsModel.getFeature(),
                        pSearchArgsModel.getThredshold(), pSearchArgsModel.getPkeys(),
                        pSearchArgsModel.getCreator(), pSearchArgsModel.getCphone(),
                        pSearchArgsModel.getStart(), pSearchArgsModel.getPageSize(),
                        pSearchArgsModel.isMoHuSearch());
                break;
            }
        }
        return objectSearchResult;
    }

    //多条件查询
    private ObjectSearchResult searchByMutiCondition(String platformId, String idCard,String name, int sex,
                                                     byte[] photo, String feature,int threshold,
                                                     List<String> pkeys, String creator, String cphone,
                                                     int start, int pageSize,boolean moHuSearch){
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient()
                .prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setExplain(true).setSize(10000);
        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        // 传入平台ID ，必须是确定的
        if (platformId != null){
            booleanQueryBuilder.must(QueryBuilders.termQuery(ObjectInfoTable.PLATFORMID, platformId));
        }
        // 性别要么是1，要么是0，即要么是男，要么是女
        if (sex != -1){
            booleanQueryBuilder.must(QueryBuilders.termQuery(ObjectInfoTable.SEX, sex));
        }
        // 多条件下，输入手机号，只支持精确的手机号
        if (cphone != null){
            booleanQueryBuilder.must(QueryBuilders.matchPhraseQuery(ObjectInfoTable.CPHONE, cphone)
                    .analyzer("standard"));
        }
        // 人员类型，也是精确的lists
        if (pkeys !=null && pkeys.size() >0){
            booleanQueryBuilder.should(QueryBuilders.termsQuery(ObjectInfoTable.PKEY, pkeys));
        }
        // 身份证号可以是模糊的
        if (idCard != null){
            booleanQueryBuilder.should(QueryBuilders.matchQuery(ObjectInfoTable.IDCARD, idCard));
        }
        // 名字可以是模糊的
        if (name != null){
            booleanQueryBuilder.should(QueryBuilders.matchQuery(ObjectInfoTable.NAME, name));
        }
        // 创建者姓名可以是模糊的
        if (creator != null){
            booleanQueryBuilder.should(QueryBuilders.matchQuery(ObjectInfoTable.CREATOR, creator));
        }

        requestBuilder.setQuery(booleanQueryBuilder);
        // 后续，根据查出来的人员信息，如果有图片，特征值，以及阈值，（则调用算法进行比对，得出相似度比较高的）
        // 由或者多条件查询里面不支持传入图片以及阈值，特征值。
        ObjectSearchResult objectSearchResult = dealWithSearchRequesBuilder(platformId, requestBuilder, null,
                null, null,
                start, pageSize, moHuSearch);
        //处理以图搜图
        if (feature != null && threshold > 0){
            objectSearchResult = searchByPhotoAndThreshold(objectSearchResult.getResults(), platformId, photo,
                    threshold, feature, start, pageSize );
        }
        return objectSearchResult;
    }

    @Override
    public ObjectSearchResult searchByPlatFormIdAndIdCard(String platformId, String idCard,
                                                          boolean moHuSearch, int start, int pageSize) {
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient().prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF).setExplain(true).setSize(10000);
        if (platformId != null){
            if (moHuSearch){
                requestBuilder.setQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery(ObjectInfoTable.PLATFORMID, platformId))
                        .must(QueryBuilders.matchQuery(ObjectInfoTable.IDCARD, idCard)));
            }else {
                requestBuilder.setQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery(ObjectInfoTable.PLATFORMID, platformId))
                        .must(QueryBuilders.matchPhraseQuery(ObjectInfoTable.IDCARD, idCard)
                                .analyzer("standard")));
            }
        }else {
            if (moHuSearch){
                requestBuilder.setQuery(QueryBuilders.matchQuery(ObjectInfoTable.IDCARD, idCard));
            }else {
                requestBuilder.setQuery(QueryBuilders.matchPhraseQuery(ObjectInfoTable.IDCARD, idCard)
                        .analyzer("standard"));
            }
        }
        return dealWithSearchRequesBuilder(platformId, requestBuilder, null,
                null, null,
                start, pageSize, moHuSearch);
    }

    @Override
    public ObjectSearchResult searchByRowkey(String rowkey) {
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result;
        ObjectSearchResult searchResult = new ObjectSearchResult();
        boolean tableExits;
        String searchRowkey = UUID.randomUUID().toString().replace("-", "");
        searchResult.setSearchId(searchRowkey);
        try {
            tableExits = table.exists(get);
            if (tableExits){
                result = table.get(get);
                String[] tmp = result.toString().split(":");
                List<String> cols = new ArrayList<>();
                for (int i = 1; i < tmp.length; i++){
                    cols.add(tmp[i].substring(0, tmp[i].indexOf("/")));
                }
                Map<String, Object> person = new HashMap<>();
                List<Map<String, Object>> hits = new ArrayList<>();
                for (String col:cols){
                    String value = Bytes.toString(result.getValue(Bytes.toBytes(ObjectInfoTable.PERSON_COLF),
                            Bytes.toBytes(col)));
                    person.put(col, value);
                }
                hits.add(person);
                searchResult.setResults(hits);
                searchResult.setSearchStatus(0);
                searchResult.setPhotoId(null);
                searchResult.setSearchNums(1);
            } else {
                searchResult.setResults(null);
                searchResult.setSearchStatus(1);
                searchResult.setSearchNums(0);
                searchResult.setPhotoId(null);
            }
        } catch (IOException e) {
            LOG.info("根据rowkey获取对象信息的时候异常............");
            e.printStackTrace();
        } finally {
            LOG.info("释放table 对象........");
            HBaseUtil.closTable(table);
        }
        putSearchRecordToHBase(null, searchResult, null);
        return searchResult;
    }


    @Override
    public ObjectSearchResult searchByCphone(String cphone, int start, int pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setQuery(QueryBuilders.termQuery(ObjectInfoTable.CPHONE, cphone))
                .setExplain(true).setSize(10000);
        return  dealWithSearchRequesBuilder(null, requestBuilder, null,
                null, null,
                start, pageSize, false);
    }

    // 处理精确查找下，IK 分词器返回多余信息的情况，
    // 比如只需要小王炸，但是返回了小王炸 和小王炸小以及小王炸大的情况
    private void dealWithCreatorAndNameInNoMoHuSearch(ObjectSearchResult searchResult,String searchType,
                                                      String nameOrCreator,
                                                      boolean moHuSearch){
        List<Map<String, Object>> exectResult = new ArrayList<>();
        List<Map<String, Object>> tempList = searchResult.getResults();
        if (!moHuSearch && tempList != null &&(ObjectInfoTable.CREATOR.equals(searchType)
                || ObjectInfoTable.NAME.equals(searchType))){
            for (Map<String, Object> objectMap: tempList){
                String temp = null;
                if (ObjectInfoTable.CREATOR.equals(searchType)){
                    temp = (String) objectMap.get(ObjectInfoTable.CREATOR);
                }else if (ObjectInfoTable.NAME.equals(searchType)){
                    temp = (String) objectMap.get(ObjectInfoTable.NAME);
                }
                if (temp != null && temp.equals(nameOrCreator)){
                    exectResult.add(objectMap);
                }
            }
            searchResult.setResults(exectResult);
            searchResult.setSearchNums(exectResult.size());
        }
    }

    @Override
    public ObjectSearchResult searchByCreator(String creator, boolean moHuSearch,
                                              int start, int pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setExplain(true).setSize(10000);
        if (moHuSearch){
            requestBuilder.setQuery(QueryBuilders.matchQuery(ObjectInfoTable.CREATOR, creator));
        } else {
            requestBuilder.setQuery(QueryBuilders.matchPhraseQuery(ObjectInfoTable.CREATOR, creator));
        }
        return dealWithSearchRequesBuilder(null, requestBuilder, null,
                ObjectInfoTable.CREATOR, creator,
                start, pageSize, moHuSearch);
    }

    @Override
    public ObjectSearchResult searchByName(String name, boolean moHuSearch,
                                           int start, int pageSize) {
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setExplain(true).setSize(10000);
        if(moHuSearch){
            requestBuilder.setQuery(QueryBuilders.matchQuery(ObjectInfoTable.NAME, name));
        }else {
            requestBuilder.setQuery(QueryBuilders.matchPhraseQuery(ObjectInfoTable.NAME,name));
        }
        return dealWithSearchRequesBuilder(null, requestBuilder, null,
                ObjectInfoTable.NAME, name,
                start, pageSize, moHuSearch);
    }

    private ObjectSearchResult getAllObjectINfo(){
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setExplain(true).setSize(10000);
        requestBuilder.setQuery(QueryBuilders.matchAllQuery());
        return dealWithSearchRequesBuilder(true, null, requestBuilder, null,
                null, null, -1, -1 , false );
    }

    private ObjectSearchResult searchByPhotoAndThreshold(List<Map<String, Object>> personInfoList,
                                                        String platformId,
                                                        byte[] photo,
                                                        int threshold,
                                                        String feature,
                                                        long start,
                                                        long pageSize){
        List<Map<String, Object>> resultsTmp;
        if (personInfoList == null || personInfoList.size() <= 0){
            resultsTmp = getAllObjectINfo().getResults();
        } else {
            resultsTmp = personInfoList;
        }

        List<Map<String, Object>> resultsFinal = new ArrayList<>();

        for (Map<String, Object> personInfo: resultsTmp){
            Map<String, Object> personInfoTmp = new HashMap<>();
            personInfoTmp.putAll(personInfo);
            Set<String> attributes = personInfo.keySet();
            for (String attr : attributes) {
                if ("feature".equals(attr)) {
                    String feture_his = (String) personInfo.get(attr);
                    float related = FaceFunction.featureCompare(feature, feture_his);
                    System.out.println(personInfo.get("id") + ", " + related);
                    if (related > threshold) {
                        personInfoTmp.put(ObjectInfoTable.RELATED, related);
                        resultsFinal.add(personInfoTmp);
                    }
                }
            }
        }
        String searchId = UUID.randomUUID().toString().replace("-", "");
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        objectSearchResult.setSearchId(searchId); // searchId
        objectSearchResult.setSearchStatus(0);  // status
        objectSearchResult.setSearchNums(resultsFinal.size());   // results nums
        objectSearchResult.setResults(resultsFinal);  // results
        objectSearchResult.setPhotoId(searchId);   // photoId
        putSearchRecordToHBase(platformId, objectSearchResult, photo);
        HBaseUtil.dealWithPaging(objectSearchResult, (int)start, (int)pageSize);
        return objectSearchResult;

    }

    @Override
    public ObjectSearchResult searchByPhotoAndThreshold(String platformId,
                                                        byte[] photo,
                                                        int threshold,
                                                        String feature,
                                                        long start,
                                                        long pageSize) {
        return searchByPhotoAndThreshold(null, platformId,
                photo, threshold, feature, start, pageSize);
    }

    @Override
    public String getFeature(String tag, byte[] photo) {
        float[] floatFeature = FaceFunction.featureExtract(photo);
        if (floatFeature != null && floatFeature.length == 512) {
            return FaceFunction.floatArray2string(floatFeature);
        }
        return "";
    }

    @Override
    public byte[] getPhotoByKey(String rowkey) {
        Table table = HBaseHelper.getTable(ObjectInfoTable.TABLE_NAME);
        Get get = new Get(Bytes.toBytes(rowkey));
        Result result;
        byte[] photo;
        try {
            result = table.get(get);
            photo = result.getValue(Bytes.toBytes("person"), Bytes.toBytes("photo"));
            LOG.info("get data from table successed!");
        } catch (IOException e) {
            LOG.error("get data from table failed!");
            e.printStackTrace();
            return null;
        } finally {
            HBaseUtil.closTable(table);
        }
        return photo;
    }

    // 保存历史查询记录
    private void putSearchRecordToHBase(String platformId, ObjectSearchResult searchResult, byte[] photo){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = null;
        byte[] results = null;
        if (searchResult !=null){
            try {
                oout = new ObjectOutputStream(bout);
                oout.writeObject(searchResult.getResults());
                results = bout.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (oout != null){
                        oout.close();
                    }
                    bout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (searchResult != null){
            Table table = HBaseHelper.getTable(SrecordTable.TABLE_NAME);
            Put put = new Put(Bytes.toBytes(searchResult.getSearchId()));
            LOG.info("srecord rowkey is:  " + searchResult.getSearchId());
            put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.SEARCH_STATUS),
                    Bytes.toBytes(searchResult.getSearchStatus()))
                    .addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.SEARCH_NUMS),
                            Bytes.toBytes(searchResult.getSearchNums()));
            if (platformId != null){
                put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PLATFORM_ID),
                        Bytes.toBytes(platformId));
            }
            if (searchResult.getPhotoId() != null){
                put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PHOTOID),
                        Bytes.toBytes(searchResult.getPhotoId()));
            }
            if (results != null){
                put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.RESULTS), results);
            }
            if (photo != null){
                put.addColumn(Bytes.toBytes(SrecordTable.RD_CLOF), Bytes.toBytes(SrecordTable.PHOTO), photo);
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
    }

    // 根据ES的SearchRequesBuilder 来查询，并封装返回结果
    private ObjectSearchResult dealWithSearchRequesBuilder(String paltformID, SearchRequestBuilder searchRequestBuilder,
                                                           byte[] photo, String searchType, String creatorOrName,
                                                           int start, int pageSize, boolean moHuSearch){
        return dealWithSearchRequesBuilder(false,
                paltformID,
                searchRequestBuilder,
                photo,
                searchType,
                creatorOrName,
                start,
                pageSize,
                moHuSearch);
    }

    private ObjectSearchResult dealWithSearchRequesBuilder(boolean isSkipRecord,
                                                           String paltformID,
                                                           SearchRequestBuilder searchRequestBuilder,
                                                           byte[] photo,
                                                           String searchType,
                                                           String creatorOrName,
                                                           int start,
                                                           int pageSize,
                                                           boolean moHuSearch){
        SearchResponse response = searchRequestBuilder.get();
        SearchHits hits = response.getHits();
        LOG.info("根据搜索条件得到的记录数是： " + hits.getTotalHits());
        SearchHit[] searchHits = hits.getHits();
        ObjectSearchResult searchResult = new ObjectSearchResult();
        String searchId = UUID.randomUUID().toString().replace("-", "");
        searchResult.setSearchId(searchId);
        if (photo == null){
            searchResult.setPhotoId(null);
        }else {
            searchResult.setPhotoId(searchId);
        }
        searchResult.setSearchNums(hits.getTotalHits());
        List<Map<String, Object>> results = new ArrayList<>();
        if (searchHits.length > 0){
            for (SearchHit hit:searchHits){
                Map<String, Object> source = hit.getSource();
                // ES 的文档名，对应着HBase 的rowkey
                source.put(ObjectInfoTable.ROWKEY, hit.getId());
                results.add(source);
            }
        }
        searchResult.setSearchStatus(0);
        searchResult.setResults(results);
        if (results.size() < 1){
            searchResult.setSearchStatus(1);
        }
        // 处理精确查找下，IK 分词器返回多余信息的情况，
        // 比如只需要小王炸，但是返回了小王炸 和小王炸小以及小王炸大的情况
        dealWithCreatorAndNameInNoMoHuSearch(searchResult, searchType, creatorOrName, moHuSearch);
        if (!isSkipRecord){
            putSearchRecordToHBase(paltformID, searchResult, photo);
        }
        //处理搜索的数据,根据是否需要分页进行返回
        HBaseUtil.dealWithPaging(searchResult, start, pageSize);
        LOG.info("最终返回的记录数是： " + searchResult.getResults().size() + " 条");
        return searchResult;
    }
}
