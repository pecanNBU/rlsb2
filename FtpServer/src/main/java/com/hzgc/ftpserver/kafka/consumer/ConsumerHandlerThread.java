package com.hzgc.ftpserver.kafka.consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.*;

public class ConsumerHandlerThread implements Runnable {
    protected final Logger LOG;
    private final KafkaConsumer<String, byte[]> consumer;
    private Connection hbaseConn;
    private ExecutorService executors;
    private Properties propers;
    private final ConcurrentHashMap<String, Boolean> isCommit;
    protected String tableName;
    protected String columnFamily;
    protected String column_pic;
    protected String column_ipcID;
    protected String column_time;


    public ConsumerHandlerThread(Properties propers, Connection conn, Class logClass) {
        this.propers = propers;
        this.hbaseConn = conn;
        this.consumer = new KafkaConsumer<>(propers);
        this.LOG = Logger.getLogger(logClass);
        String topic = propers.getProperty("topic");
        consumer.subscribe(Arrays.asList(StringUtils.split(topic, ",")));
        this.isCommit = new ConcurrentHashMap<>();
        this.isCommit.put("isCommit", true);
        this.tableName = propers.getProperty("table_name");
    }

    public void run() {
        int workerNum = Integer.parseInt(propers.getProperty("workerNum"));
        long getTimeOut = Long.parseLong(propers.getProperty("getTimeOut"));
//        executors = new ThreadPoolExecutor(workerNum, workerNum, 0L, TimeUnit.MILLISECONDS,
//                new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
        BlockingQueue<ConsumerRecord<String, byte[]>> buffer = new LinkedBlockingQueue<>();
        executors = Executors.newFixedThreadPool(workerNum);
        final int minBatchSize = Integer.parseInt(propers.getProperty("minBatchSize"));
        final int commitFailure = Integer.parseInt(propers.getProperty("commitFailure"));
        int consumerTimes = 1;
        int failWorker = 0;
        for (int i = 0; i < workerNum; i++) {
            executors.submit(new WorkerThread(hbaseConn, buffer, tableName, columnFamily, column_pic, column_ipcID, column_time, isCommit));
        }
        try {
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(getTimeOut);
                if (!records.isEmpty()) {
                    for (final ConsumerRecord<String, byte[]> record : records) {
                        buffer.put(record);
                        consumerTimes++;
                    }
                }
                if (!isCommit.get("isCommit")) {
                    failWorker++;
                }
                if (consumerTimes >= minBatchSize && failWorker <= commitFailure) {
                    consumer.commitSync();
                    LOG.info(Thread.currentThread().getName() + ": Commit offset success");
                    consumerTimes = 0;
                    failWorker = 0;
                }
                if (failWorker > commitFailure) {
                    throw new Exception("The number of consumer failures exceeded the threshold, " +
                            "pulling up consumer threads for consumption");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ConsumerHandlerThread tempHandlerThread = new ConsumerHandlerThread(propers, hbaseConn, LOG.getClass());
            Thread thread = new Thread(tempHandlerThread);
            thread.start();
        }
    }
//    public void shutDown() {
//        if (consumer != null) {
//            consumer.close();
//        }
//        if (executors != null) {
//            executors.shutdown();
//        }
//        try {
//            if (!executors.awaitTermination(10, TimeUnit.MILLISECONDS)) {
//                System.out.println("TimeOut..... Ignore for this case");
//            }
//        } catch (InterruptedException ignored) {
//            System.out.println("Other thread interrupted this shutdown, ignore for this case.");
//            Thread.currentThread().interrupt();
//        }
//    }
}
