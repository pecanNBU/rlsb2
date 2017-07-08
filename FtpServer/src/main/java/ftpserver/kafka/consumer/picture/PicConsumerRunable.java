package ftpserver.kafka.consumer.picture;

import ftpserver.kafka.consumer.ConsumerRunnable;
import org.apache.ftpserver.util.IoUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class PicConsumerRunable extends ConsumerRunnable {
    public PicConsumerRunable(Properties propers) {
        super(propers);
    }

    @Override
    public void run() {
            while (true){
                ConsumerRecords<String, byte[]> records = consumer.poll(100);
                FileOutputStream fis = null;
                for (ConsumerRecord<String, byte[]> record : records) {
                    System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());
                    try {
                        fis = new FileOutputStream(new File("E:\\run.jpg"));
                        fis.write(record.value());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        IoUtils.close(fis);
                    }
                }
            }
    }
}
