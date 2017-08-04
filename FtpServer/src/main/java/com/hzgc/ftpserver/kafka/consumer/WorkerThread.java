package com.hzgc.ftpserver.kafka.consumer;

import com.hzgc.ftpserver.util.FtpUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerThread implements Runnable {
    private Logger LOG = Logger.getLogger(WorkerThread.class);
    protected ConsumerRecord<String, byte[]> consumerRecord;
    protected BlockingQueue<ConsumerRecord<String, byte[]>> buffer;
    protected ConcurrentHashMap<String, Boolean> isCommit;
    protected Connection hbaseConn;
    protected Table picTable;
    protected String tableName;
    protected String columnFamily;
    protected String column_pic;
    protected String column_ipcID;
    protected String column_time;

    public WorkerThread(Connection conn,
                        BlockingQueue<ConsumerRecord<String, byte[]>> buffer,
                        String tableName,
                        String columnFamily,
                        String column_pic,
                        String column_ipcID,
                        String column_time,
                        ConcurrentHashMap<String, Boolean> commit) {
        this.buffer = buffer;
        this.hbaseConn = conn;
        this.isCommit = commit;
        this.tableName = tableName;
        this.columnFamily = columnFamily;
        this.column_pic = column_pic;
        this.column_ipcID = column_ipcID;
        this.column_time = column_time;
        LOG.info("Create [" + Thread.currentThread().getName() + "] of PicWorkerThreads success");
    }

    public void run() {
        send();
    }

    public void send() {
        try {
            if (null != tableName) {
                picTable = hbaseConn.getTable(TableName.valueOf(tableName));
            }
            while (true) {
                consumerRecord = buffer.take();
                picTable = hbaseConn.getTable(TableName.valueOf(tableName));
                if (null != columnFamily && null != column_pic && null != consumerRecord) {
                    Put put = new Put(Bytes.toBytes(consumerRecord.key()));
                    put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_pic), consumerRecord.value());
                    Map<String, String> map = FtpUtil.getRowKeyMessage(consumerRecord.key());
                    String ipcID = map.get("ipcID");
                    long timestamp = Long.valueOf(map.get("time"));
                    put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_ipcID), Bytes.toBytes(ipcID));
                    put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column_time), Bytes.toBytes(timestamp));
                    picTable.put(put);
                    System.out.printf(Thread.currentThread().getName() + "topic = %s, offset = %d, key = %s, value = %s, patition = %s\n",
                            consumerRecord.topic(), consumerRecord.offset(), consumerRecord.key(), consumerRecord.value(), consumerRecord.partition());
                }
            }
        } catch (Exception e) {
            isCommit.replace("isCommit", false);
            e.printStackTrace();
        } finally {
            try {
                picTable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
