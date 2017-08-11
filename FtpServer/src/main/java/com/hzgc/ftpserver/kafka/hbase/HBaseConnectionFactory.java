package com.hzgc.ftpserver.kafka.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;

public class HBaseConnectionFactory implements Serializable {
    private static final Logger LOG = Logger.getLogger(HBaseConnectionFactory.class);
    private static Connection hbaseConnection;
    private static Configuration conf = HBaseConfiguration.create();

    public static Connection createConnection() {
        try {
            hbaseConnection = ConnectionFactory.createConnection(conf);
            LOG.info("Create hbase connection success");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hbaseConnection;
    }

    public static void setConf(Configuration conf) {
        HBaseConnectionFactory.conf = conf;
    }
}
