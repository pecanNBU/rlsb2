package com.hzgc.hbase.staticreposuite;

import com.hzgc.hbase.staticrepo.ElasticSearchHelper;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class EsDataSuite {
    @Test
    public void testPutFloatArrayToEs() throws IOException {
        Client client = ElasticSearchHelper.getEsClient();
        IndexResponse response = client.prepareIndex("twitter", "tweet", "1000")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("user" ,"enhaye")
                        .field("postDate", new float[]{1.23411321341f, 1.09f, 2.3454f, 1.0939309f})
                        .endObject()).get();

        String _index = response.getIndex();
        String _type = response.getType();
        String _id = response.getId();
        long _version = response.getVersion();
        RestStatus status = response.status();
        System.out.println("_index: " + _index  + ", _type: " + _type + ", _id： "
                + _id + "_version: " + _version  + "status: " + status );
    }
}
