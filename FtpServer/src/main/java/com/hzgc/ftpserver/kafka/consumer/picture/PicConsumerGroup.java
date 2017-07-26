package com.hzgc.ftpserver.kafka.consumer.picture;

import com.hzgc.ftpserver.kafka.consumer.ConsumerRunnable;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PicConsumerGroup {
    private final Logger LOG = Logger.getLogger(PicConsumerGroup.class);
    private List<ConsumerRunnable> consumers;

    public PicConsumerGroup(Properties propers) {
        consumers = new ArrayList<>();
        int consumerNum = Integer.parseInt(propers.getProperty("consumerNum"));
        for (int i = 0; i < consumerNum; i++) {
            PicConsumerRunable consumerThread = new PicConsumerRunable(propers);
            consumers.add(consumerThread);
        }
    }

    public void execute() {
        for (ConsumerRunnable thread : consumers) {
            new Thread(thread).start();
        }
    }

}
