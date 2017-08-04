package com.hzgc.ftpserver.kafka.consumer.json;

import com.hzgc.ftpserver.kafka.consumer.ConsumerContext;
import com.hzgc.util.FileUtil;
import org.apache.hadoop.hbase.client.Connection;

import java.io.FileInputStream;

public class JsonConsumerContext extends ConsumerContext {

    public JsonConsumerContext(Connection conn) {
        super(conn);
    }

    @Override
    public void run() {
        try {
            resourceFile = FileUtil.loadResourceFile("consumer-json.properties");
            System.out.println("****************************************************************************");
            propers.list(System.out);
            System.out.println("****************************************************************************");

            if (resourceFile != null) {
                propers.load(new FileInputStream(resourceFile));
            }
            JsonConsumerHandlerGroup consumerGroup = new JsonConsumerHandlerGroup(propers, conn);
            consumerGroup.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
