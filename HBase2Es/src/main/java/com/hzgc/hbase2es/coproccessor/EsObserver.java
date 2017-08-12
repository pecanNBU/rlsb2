package com.hzgc.hbase2es.coproccessor;

import com.hzgc.hbase2es.es.ElasticSearchBulkOperator;
import com.hzgc.hbase2es.util.EsClientUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;

public class EsObserver extends BaseRegionObserver implements Serializable {
    private static Logger LOG = Logger.getLogger(EsObserver.class);
    String indexName;
    String typeName;

    private void readConfiguration(CoprocessorEnvironment env) {
        Configuration conf = env.getConfiguration();  // 集群信息
        EsClientUtils.clusterName = conf.get("es_cluster");  // es集群名字
        EsClientUtils.nodeHosts = conf.get("es_hosts");    // es 列表,传入字符串，用逗号分隔
        EsClientUtils.nodePort = conf.getInt("es_port", 9300);  // es的端口
        indexName = conf.get("es_index");  // 索引
        typeName = conf.get("es_type");       // 类型
        LOG.info("the es cluster info :===================== cluser_name:" + EsClientUtils.clusterName
                + ", cluster_hosts:" + EsClientUtils.nodeHosts + ", cluster_port:" + EsClientUtils.nodePort
                + ",index:" + indexName + ",type:" + typeName + " ===========================");
    }

    @Override
    public void start(CoprocessorEnvironment env) throws IOException {
        readConfiguration(env);       // 读取Es 配置信息
        if (EsClientUtils.client == null) {
            EsClientUtils.initEsClient();  // 初始化Es 客户端
        }
    }

    @Override
    public void stop(CoprocessorEnvironment e) throws IOException {
        EsClientUtils.clostEsClient();
        ElasticSearchBulkOperator.shutdownScheduEx();
    }
}
