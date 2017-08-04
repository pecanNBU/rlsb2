package com.hzgc.ftpserver.kafka.consumer.picture2;

import com.hzgc.ftpserver.kafka.consumer.ConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;

import java.util.Properties;

public class PicConsumerHandlerThread extends ConsumerHandlerThread {
    public PicConsumerHandlerThread(Properties propers, Connection conn, Class logClass) {
        super(propers, conn, logClass);
        super.columnFamily = propers.getProperty("cf_pic");
        super.column_pic = propers.getProperty("c_pic_pic");
        super.column_ipcID = propers.getProperty("c_pic_ipcID");
        super.column_time = propers.getProperty("c_pic_time");
        LOG.info("Create [" + Thread.currentThread().getName() + "] of PicConsumerHandlerThreads success");
    }
}
