package com.hzgc.ftpserver.kafka.ftp;

import com.hzgc.ftpserver.kafka.producer.ProducerOverFtp;
import org.apache.ftpserver.impl.DefaultFtpServerContext;
import org.apache.log4j.Logger;

public class KafkaFtpServerContext extends DefaultFtpServerContext {
    private static Logger log = Logger.getLogger(KafkaFtpServerContext.class);
    private ProducerOverFtp producerOverFtp = ProducerOverFtp.getInstance();

    public ProducerOverFtp getProducerOverFtp() {
        return producerOverFtp;
    }

    public void setProducerOverFtp(ProducerOverFtp producerOverFtp) {
        this.producerOverFtp = producerOverFtp;
    }
}
