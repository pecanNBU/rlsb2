package ftpserver.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Properties;


public class Consumer {
    public static void main(String args[]) throws Exception {
            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("group.id", "test");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("session.timeout.ms", "30000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
            KafkaConsumer consumer = new KafkaConsumer(props);
            consumer.subscribe(Arrays.asList("picture"));
            try {
                while (true){
                    ConsumerRecords<String, byte[]> records = consumer.poll(100);
                    FileOutputStream fis;
                    for (ConsumerRecord<String, byte[]> record : records) {
                        System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());
                        fis = new FileOutputStream(new File("E:\\test.jpg"));
                        fis.write(record.value());
                        fis.close();
                    }
                }
            } finally {
                consumer.close();
            }
        }
    }
