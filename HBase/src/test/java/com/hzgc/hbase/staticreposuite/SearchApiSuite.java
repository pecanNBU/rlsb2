package com.hzgc.hbase.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectInfoTable;
import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SearchApiSuite {
    @Test
    public void testSearchByTeams(){
        List<String> pkeys = new ArrayList<>();
        pkeys.add("223456");
        pkeys.add("223458");
        Client client = ElasticSearchHelper.getEsClient();
        SearchRequestBuilder requestBuilder = client.prepareSearch(ObjectInfoTable.TABLE_NAME)
                .setTypes(ObjectInfoTable.PERSON_COLF)
                .setExplain(true).setSize(10000);
        requestBuilder.setQuery(QueryBuilders.termsQuery("pkey", pkeys));
        System.out.println(requestBuilder.get().getHits().getTotalHits());
    }
}
