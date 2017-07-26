package com.hzgc.hbase.staticrepo;

import com.hzgc.hbase.util.HBaseUtil;
import org.apache.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class ElasticSearchHelper {
    private static Settings settings = null;
    private static TransportClient client = null;
    private static String es_cluster = null;
    private static String es_hosts = null;
    private static Integer es_port = null;
    private static Logger logger = Logger.getLogger(ElasticSearchHelper.class);

    /**
     * 初始化Es 集群信息
     */
    public static void initElasticSearchClient() {
        // 从外部读取Es集群配置信息
        Properties properties_es_config = new Properties();
        try {
            File file = HBaseUtil.loadResourceFile("es_cluster_config_staticrepo.properties");
            properties_es_config.load(new FileInputStream(file));
        } catch (Exception e) {
            logger.info("File does not find!");
            e.printStackTrace();
        }
        es_cluster = properties_es_config.getProperty("es.cluster.name");
        es_hosts = properties_es_config.getProperty("es.hosts");
        es_port = new Integer(properties_es_config.getProperty("es.cluster.port"));

        // 初始化配置文件
        settings = Settings.builder().put("cluster.name", es_cluster).build();

        //初始化client
        client = new PreBuiltTransportClient(settings);
        for (String host:es_hosts.split(",")){
            try {
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host),es_port));
                logger.info("Address addition successed!");
            } catch (UnknownHostException e) {
                logger.info("Host can not be identify!");
                e.printStackTrace();
            }
        }
    }

    /**
     * 返回client 对象信息
     */
    public static TransportClient getEsClient(){
        if (client == null){
            initElasticSearchClient();
        }
        return ElasticSearchHelper.client;
    }
}
