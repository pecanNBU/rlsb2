package ftpserver.kafka.producer;


import ftpserver.util.Utils;
import org.apache.ftpserver.util.IoUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class ProducerOverFtp {
    private static Logger log = Logger.getLogger(ProducerOverFtp.class);
    private static KafkaProducer kafkaProducer;
    private Properties kafkaPropers = new Properties();
    private FileInputStream fis;
    private static  String PICTURE = "picture";
    private static  String FACE = "face";
    private static  String JSON = "json";
    public ProducerOverFtp() {
        try {
            File file = Utils.loadResourceFile("producer-over-ftp.properties");
            this.fis = new FileInputStream(file);
            this.kafkaPropers.load(fis);
            PICTURE = kafkaPropers.getProperty("topic-picture");
            FACE = kafkaPropers.getProperty("topic-face");
            JSON = kafkaPropers.getProperty("topic-json");
            if (kafkaPropers != null) {
                kafkaProducer = new KafkaProducer(kafkaPropers);
                log.info("Create KafkaProducer successfull");
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
    }

    public void closeProducer() {
        if (null != kafkaProducer) {
            kafkaProducer.close();
        }
    }

    public static final ProducerOverFtp getInstance() {
        return LazyHandler.instanc;
    }

    public static class LazyHandler {
        private static final ProducerOverFtp instanc = new ProducerOverFtp();
    }

    public static String getPicture() {
        return PICTURE;
    }

    public static String getFace() {
        return FACE;
    }

    public static String getJson() {
        return JSON;
    }
}
