package ftpserver.kafka.consumer.face;

import ftpserver.kafka.consumer.ConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;

import java.util.Properties;

public class FaceConsumerHandlerThread extends ConsumerHandlerThread {
    public FaceConsumerHandlerThread(Properties propers, Connection conn, Class logClass) {
        super(propers, conn, logClass);
        super.columnFamily = propers.getProperty("cf_face");
        super.column = propers.getProperty("c_face");
        LOG.info("Create [" + Thread.currentThread().getName() + "] of FaceConsumerHandlerThreads success");
    }
}
