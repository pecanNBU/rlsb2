package com.hzgc.ftpserver.kafka.consumer;

import org.apache.hadoop.hbase.client.Connection;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;

public abstract class ConsumerContext implements Runnable, Serializable {
    protected File resourceFile;
    protected Properties propers = new Properties();
    protected Connection conn;

    public ConsumerContext(Connection conn) {
        this.conn = conn;
    }
}
