package com.hzgc.hbase.staticrepo;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ObjectInfoInnerHandlerImpl implements ObjectInfoInnerHandler, Serializable{
    static {
        ElasticSearchHelper.getEsClient();
    }

    @Override
    public List<String> searchByPkeys(List<String> pkeys) {
        //遍历人员类型
        Iterator it = pkeys.iterator();
        //构造搜索对象
        SearchResponse searchResponse;
        //定义一个List用来存在查询得到的结果
        List<String> findResult = new ArrayList<>();
        //设置搜索条件
        SearchRequestBuilder requestBuilder = ElasticSearchHelper.getEsClient().prepareSearch("objectinfo")
                .setTypes("person").setExplain(true).setSize(10000);
        while (it.hasNext()){
            //取出遍历的值
            String a = (String) it.next();
            //根据遍历得到的人员类型进行精确查询
            requestBuilder.setQuery(QueryBuilders.termQuery("pkey",a));
            //通过requestBuilder的get方法执行查询任务
            searchResponse = requestBuilder.get();
            //将结果进行封装
            SearchHits hits = searchResponse.getHits();
            //输出某个人员类型对应的记录条数
            SearchHit[] searchHits = hits.getHits();
            System.out.println("pkey为：" + a + "时，查询得到的记录数为：" + hits.getTotalHits());
            if(searchHits.length > 0){
                for(SearchHit hit : searchHits){
                    //得到每个人员类型对应的rowkey
                    String id = hit.getId();
                    //得到每个人员类型对应的特征值
                    Map<String,Object> sourceList = hit.getSource();
                    String feature = (String)sourceList.get("feature");
                    //当有特征值时，才将结果返回
                    if(null != feature){
                        //将人员类型、rowkey和特征值进行拼接
                        String result = id + "ZHONGXIAN" + a + "ZHONGXIAN" + feature;
                        //将结果添加到集合中
                        findResult.add(result);
                    }
                }
            }
        }
        return findResult;
    }
}
