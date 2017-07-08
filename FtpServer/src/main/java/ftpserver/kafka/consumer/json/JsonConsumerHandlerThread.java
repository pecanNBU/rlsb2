package ftpserver.kafka.consumer.json;

import ftpserver.kafka.consumer.ConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;

import java.util.Properties;

public class JsonConsumerHandlerThread extends ConsumerHandlerThread {
    public JsonConsumerHandlerThread(Properties propers, Connection conn, Class logClass) {
        super(propers, conn, logClass);
        super.columnFamily = propers.getProperty("cf_json");
        super.column = propers.getProperty("c_json");
        LOG.info("Create [" + Thread.currentThread().getName() + "] of JsonConsumerHandlerThreads success");
    }
}
