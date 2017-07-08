package ftpserver.kafka.consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class ConsumerRunnable implements Runnable{
    protected final KafkaConsumer<String, byte[]> consumer;

    public ConsumerRunnable(Properties propers) {
        this.consumer = new KafkaConsumer<String, byte[]>(propers);
        String topic = propers.getProperty("topic");
        consumer.subscribe(Arrays.asList(StringUtils.split(topic, ",")));
    }

    public void run() {

    }
}
