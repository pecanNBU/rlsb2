package com.hzgc.ftpserver.kafka.consumer.face;

import com.hzgc.ftpserver.kafka.consumer.ConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;

import java.util.Properties;

public class FaceConsumerHandlerThread extends ConsumerHandlerThread {
    public FaceConsumerHandlerThread(Properties propers, Connection conn, Class logClass) {
        super(propers, conn, logClass);
        super.columnFamily = propers.getProperty("cf_face");
        super.column_pic = propers.getProperty("c_face_pic");
        super.column_ipcID = propers.getProperty("c_face_ipcID");
        super.column_time = propers.getProperty("c_face_time");
        LOG.info("Create [" + Thread.currentThread().getName() + "] of FaceConsumerHandlerThreads success");
    }
}
