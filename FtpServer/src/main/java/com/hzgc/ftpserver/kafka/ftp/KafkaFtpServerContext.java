package com.hzgc.ftpserver.kafka.ftp;

import com.hzgc.ftpserver.kafka.producer.ProducerOverFtp;
import com.hzgc.rocketmq.util.RocketMQProducer;
import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.apache.log4j.Logger;

public class KafkaFtpServerContext extends DefaultFtpServerContext {
    private static Logger log = Logger.getLogger(KafkaFtpServerContext.class);
    private ProducerOverFtp producerOverFtp = ProducerOverFtp.getInstance();
    private RocketMQProducer producerRocketMQ = RocketMQProducer.getInstance();
    ProducerOverFtp getProducerOverFtp() {
        return producerOverFtp;
    }
    RocketMQProducer getProducerRocketMQ() {return  producerRocketMQ; }
}
