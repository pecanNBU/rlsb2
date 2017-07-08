package ftpserver.kafka.producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;


public class ProducerCallBack implements Callback {
    private static Logger log = Logger.getLogger(ProducerCallBack.class);
    private final long startTime;   //the time is message was send
    private final String key;  //the message's key

    public ProducerCallBack(long startTime, String key) {
        this.startTime = startTime;
        this.key = key;
    }
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
            log.info("message[" + key + "]:" + " send to partition(" + metadata.partition() + ")," +
                    " offset(" + metadata.offset() + ") in " + elapsedTime + "ms");
        } else {
            log.error("message[" + key + "]:" + " send to partition(" + metadata.partition() + ") is failed");
            exception.printStackTrace();
        }
    }
}
