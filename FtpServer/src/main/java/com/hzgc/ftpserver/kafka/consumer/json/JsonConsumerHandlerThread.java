package com.hzgc.ftpserver.kafka.consumer.json;

import com.hzgc.ftpserver.kafka.consumer.ConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;

import java.util.Properties;

public class JsonConsumerHandlerThread extends ConsumerHandlerThread {
    public JsonConsumerHandlerThread(Properties propers, Connection conn, Class logClass) {
        super(propers, conn, logClass);
        super.columnFamily = propers.getProperty("cf_json");
        super.column_pic = propers.getProperty("c_json_desc");
        super.column_ipcID = propers.getProperty("c_json_ipcID");
        super.column_time = propers.getProperty("c_json_time");
        LOG.info("Create [" + Thread.currentThread().getName() + "] of JsonConsumerHandlerThreads success");
    }
}
