package com.hzgc.ftpserver.kafka.consumer;

import com.hzgc.ftpserver.kafka.consumer.face.FaceConsumerContext;
import com.hzgc.ftpserver.kafka.consumer.json.JsonConsumerContext;
import com.hzgc.ftpserver.kafka.consumer.picture2.PicConsumerContext;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerGroupsMain implements Serializable {
    private static Logger LOG = Logger.getLogger(ConsumerGroupsMain.class);
    private static ExecutorService executors;

    public static void main(String argsp[]) {
        LOG.info("Start create the hbase connection ");
        try {
            Configuration hbaseConf = HBaseConfiguration.create();
            Connection hbaseConn = ConnectionFactory.createConnection(hbaseConf);
            executors = Executors.newFixedThreadPool(3);
            executors.submit(new PicConsumerContext(hbaseConn));
            executors.submit(new FaceConsumerContext(hbaseConn));
            executors.submit(new JsonConsumerContext(hbaseConn));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
