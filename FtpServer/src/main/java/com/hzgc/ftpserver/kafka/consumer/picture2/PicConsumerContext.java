package com.hzgc.ftpserver.kafka.consumer.picture2;

import com.hzgc.ftpserver.kafka.consumer.ConsumerContext;
import com.hzgc.ftpserver.util.FtpUtil;
import org.apache.hadoop.hbase.client.Connection;

import java.io.FileInputStream;


public class PicConsumerContext extends ConsumerContext {

    public PicConsumerContext(Connection conn) {
        super(conn);
    }

    @Override
    public void run() {
        try {
            resourceFile = FtpUtil.loadResourceFile("consumer-picture.properties");
            System.out.println("****************************************************************************");
            propers.list(System.out);
            System.out.println("****************************************************************************");
            if (resourceFile != null) {
                propers.load(new FileInputStream(resourceFile));
            }
            PicConsumerHandlerGroup consumerGroup = new PicConsumerHandlerGroup(propers, conn);
            consumerGroup.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
