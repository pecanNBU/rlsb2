package com.hzgc.hbase2es.util;

import com.hzgc.util.FileUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;


import java.io.File;
import java.io.IOException;
import java.util.Map;

public class HBaseHelper {
    private static Logger LOG = Logger.getLogger(HBaseHelper.class);
    private static Configuration innerHBaseConf;
    private static Connection innerHBaseConnection;

    private HBaseHelper(){}

    private static void initHbaseConf(){
        LOG.info("to init hbase configuration.");
        innerHBaseConf = HBaseConfiguration.create();
        File file = FileUtil.loadResourceFile("hbase-site.xml");
        if (file.exists()){
            innerHBaseConf.addResource(file.getPath());
        }
    }

    private static void initHBaseConn(){
        if (innerHBaseConf == null){
            initHbaseConf();
        }
        try {
            innerHBaseConnection = ConnectionFactory.createConnection(innerHBaseConf);
            LOG.info("init hbase connection success!");
        } catch (IOException e) {
            LOG.info("init hbase connection faile!");
            e.printStackTrace();
        }

    }

    public static Connection getHBaseConnection(){
        if (innerHBaseConnection == null){
            initHBaseConn();
        }
        return innerHBaseConnection;
    }

    public static Table getTable(String tableName){
        return  getTable(TableName.valueOf(tableName));
    }

    private static Table getTable(TableName tableName){
        try {
            return  HBaseHelper.getHBaseConnection().getTable(tableName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void dropTable(String name) throws IOException {
        dropTable(TableName.valueOf(name));
    }


    private static void dropTable(TableName name) {
        Admin admin = null;
        try {
            admin = HBaseHelper.getHBaseConnection().getAdmin();
            boolean flag = admin.tableExists(name);
            if (flag) {
                if (admin.isTableEnabled(name)) {
                    admin.disableTable(name);
                }
                admin.deleteTable(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (admin != null) {
                    admin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Table createTableWithCoprocessor(String tableName,
                                                  String observerName,
                                                  String path,
                                                  Map<String, String> mapOfOberserverArgs,
                                                  int maxVersion, String... colfams) {
        return createTableWithCoprocessor(tableName, observerName, path,
                mapOfOberserverArgs, maxVersion,105120000, colfams );
    }

    public static Table createTable(String tableName,  int maxVersion, String... colfams){
        return createTable(tableName, maxVersion,105120000, colfams);
    }

    public static Table createTable(String tableName,  int maxVersion, int timeToLive, String... colfams){
        HTableDescriptor tableDescriptor = null;
        Admin admin = null;
        Table table = null;
        // 创建表格
        try {
            admin = HBaseHelper.getHBaseConnection().getAdmin();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                LOG.info("Table: " + tableName + " have already exit, quit with status 0.");
                return getTable(tableName);
            }
            tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
            for (String columnFamily : colfams) {
                HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnFamily);
                columnDescriptor.setMaxVersions(maxVersion);
                columnDescriptor.setTimeToLive(timeToLive);
                tableDescriptor.addFamily(columnDescriptor);
            }
            admin.createTable(tableDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 关闭admin 对象。
        try {
            if (admin != null) {
                admin.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }
    public static Table createTableWithCoprocessor(String tableName,
                                           String observerName,
                                           String path,
                                           Map<String, String> mapOfOberserverArgs,
                                           int maxVersion, int timeToLive, String... colfams) {
        HTableDescriptor tableDescriptor = null;
        Admin admin = null;
        Table table = null;
        // 创建表格
        try {
            admin = HBaseHelper.getHBaseConnection().getAdmin();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                LOG.info("Table: " + tableName + " have already exit, quit with status 0.");
                return getTable(tableName);
            }
            tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
            for (String columnFamily : colfams) {
                HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnFamily);
                columnDescriptor.setMaxVersions(maxVersion);
                columnDescriptor.setTimeToLive(timeToLive);
                tableDescriptor.addFamily(columnDescriptor);
            }
            admin.createTable(tableDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 添加Coprocessor。
        try {
            table = getTable(tableName);
            if (admin != null) {
                admin.disableTable(TableName.valueOf(tableName));
                if (tableDescriptor != null) {
                    tableDescriptor.addCoprocessor(observerName, new Path(path), Coprocessor.PRIORITY_USER, mapOfOberserverArgs);
                }
                admin.modifyTable(TableName.valueOf(tableName), tableDescriptor);
                admin.enableTable(TableName.valueOf(tableName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 关闭admin 对象。
        try {
            if (admin != null) {
                admin.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }
}
