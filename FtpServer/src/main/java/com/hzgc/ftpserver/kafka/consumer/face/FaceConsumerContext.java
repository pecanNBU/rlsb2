package com.hzgc.ftpserver.kafka.consumer.face;

import com.hzgc.ftpserver.kafka.consumer.ConsumerContext;
import com.hzgc.ftpserver.util.FtpUtil;
import org.apache.hadoop.hbase.client.Connection;

import java.io.FileInputStream;

public class FaceConsumerContext extends ConsumerContext {

    public FaceConsumerContext(Connection conn) {
        super(conn);
    }

    @Override
    public void run() {
        try {
            resourceFile = FtpUtil.loadResourceFile("consumer-face.properties");
            System.out.println("****************************************************************************");
            propers.list(System.out);
            System.out.println("****************************************************************************");
            if (resourceFile != null) {
                propers.load(new FileInputStream(resourceFile));
            }
            FaceConsumerHandlerGroup consumerGroup = new FaceConsumerHandlerGroup(propers, conn);
            consumerGroup.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
