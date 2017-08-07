package com.hzgc.hbase2es.util;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EsClientUtils{
    private static Logger LOG = Logger.getLogger(EsClientUtils.class);
    public static String clusterName;
    public static String nodeHosts;
    public static int nodePort;
    public static Client client;

    public static void initEsClient() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", EsClientUtils.clusterName).build();
        LOG.debug("====================== "  + EsClientUtils.nodeHosts + "=======================");
        for (String host:EsClientUtils.nodeHosts.split("_")){
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host),
                            EsClientUtils.nodePort));
        }
    }

    public static void clostEsClient(){
        EsClientUtils.client.close();
    }
}
