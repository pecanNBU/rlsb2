package ftpserver.kafka.consumer.picture2;

import ftpserver.kafka.consumer.ConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;

import java.util.Properties;

public class PicConsumerHandlerThread extends ConsumerHandlerThread {
    public PicConsumerHandlerThread(Properties propers, Connection conn, Class logClass) {
        super(propers, conn, logClass);
        super.columnFamily = propers.getProperty("cf_pic");
        super.column = propers.getProperty("c_pic");
        LOG.info("Create [" + Thread.currentThread().getName() + "] of PicConsumerHandlerThreads success");
    }
}
