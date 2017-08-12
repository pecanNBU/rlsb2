package com.hzgc.ftpserver.kafka.producer;


import com.hzgc.util.FileUtil;
import org.apache.ftpserver.util.IoUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ProducerOverFtp {
    private static Logger LOG = Logger.getLogger(ProducerOverFtp.class);
    private static KafkaProducer<String, byte[]> kafkaProducer;
    private Properties kafkaPropers = new Properties();
    private FileInputStream fis;
    private static String PICTURE = "picture";
    private static String FACE = "face";
    private static String JSON = "json";
    private static String FEATURE = "feature";

    ProducerOverFtp() {
        try {
            File file = FileUtil.loadResourceFile("producer-over-ftp.properties");
            if (file != null) {
                this.fis = new FileInputStream(file);
            }
            this.kafkaPropers.load(fis);
            PICTURE = kafkaPropers.getProperty("topic-picture");
            FACE = kafkaPropers.getProperty("topic-face");
            JSON = kafkaPropers.getProperty("topic-json");
            FEATURE = kafkaPropers.getProperty("topic-feature");
            if (kafkaPropers != null) {
                kafkaProducer = new KafkaProducer<String, byte[]>(kafkaPropers);
                LOG.info("Create KafkaProducer successfull");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(fis);
        }
    }

    public void sendKafkaMessage(final String topic, final String key, final byte[] value) {
        long startTime = System.currentTimeMillis();
        if (kafkaPropers != null) {
            kafkaProducer.send(new ProducerRecord<String, byte[]>(topic, key, value),
                    new ProducerCallBack(startTime, key));
        }
        LOG.info("Send MQ message[topic:" + topic + ", key:" + key + "]");

    }

    public void closeProducer() {
        if (null != kafkaProducer) {
            kafkaProducer.close();
        }
    }

    public static ProducerOverFtp getInstance() {
        return LazyHandler.instanc;
    }

    private static class LazyHandler {
        private static final ProducerOverFtp instanc = new ProducerOverFtp();
    }

    public static String getPicture() {
        return PICTURE;
    }

    public static String getFace() { return FACE; }

    public static String getJson() {
        return JSON;
    }

    public static String getFEATURE() { return FEATURE; }

}
