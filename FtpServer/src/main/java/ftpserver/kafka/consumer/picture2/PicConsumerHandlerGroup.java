package ftpserver.kafka.consumer.picture2;

import ftpserver.kafka.consumer.ConsumerGroup;
import ftpserver.kafka.consumer.ConsumerHandlerThread;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PicConsumerHandlerGroup implements ConsumerGroup {
    private final Logger LOG = Logger.getLogger(PicConsumerHandlerGroup.class);
    private List<ConsumerHandlerThread> consumerHandler;
    private Connection hbaseConn;

    public PicConsumerHandlerGroup(Properties propers, Connection conn) {
        this.hbaseConn = conn;
        consumerHandler = new ArrayList<>();
        int consumerNum = Integer.parseInt(propers.getProperty("consumerNum"));
        LOG.info("The number of consumer thread is " + consumerNum);
        for (int i = 0; i < consumerNum; i++ ) {
            LOG.info("Start create the thread PicConsumerHandlerThread");
            ConsumerHandlerThread consumerThread = new PicConsumerHandlerThread(propers, hbaseConn, PicConsumerHandlerThread.class);
            consumerHandler.add(consumerThread);
        }
    }

    public void execute() {
        for (ConsumerHandlerThread thread : consumerHandler) {
            LOG.info("Start-up the thread is PicConsumerHandlerThread");
            new Thread(thread).start();
        }
    }
}
