package com.hzgc.hbase2es;

import org.apache.hadoop.hbase.TableName;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class CreateStaticRepoTable {
    private static Logger LOG = Logger.getLogger(CreateStaticRepoTable.class);
    // 获取ES集群的信息
    private static Map<String, String> getCoproccessorArgs(String path){
        Properties prop = new Properties();
        Map<String, String> mapOfOberserverArgs = new HashMap<>();
        InputStream in = null;
        if (path == null){
            in = CreateStaticRepoTable.class
                    .getResourceAsStream("/es_cluster_config.properties");
        } else {
            try {
                in = new FileInputStream(new File(path));
            } catch (FileNotFoundException e) {
                LOG.info("输入的es集群信息文件路径不存在！！！");
                e.printStackTrace();
            }
        }
        try {
            prop.load(in);
            String es_cluster = prop.getProperty("es.cluster.name");
            String es_index = prop.getProperty("es.cluster.index");
            String es_type = prop.getProperty("es.cluster.type");
            String es_hosts = prop.getProperty("es.cluster.hosts");// 用下划线分开
            String es_port = prop.getProperty("es.cluster.port");
            mapOfOberserverArgs.put("es_cluster", es_cluster);
            mapOfOberserverArgs.put("es_hosts", es_hosts);
            mapOfOberserverArgs.put("es_port", es_port);
            mapOfOberserverArgs.put("es_index", es_index);
            mapOfOberserverArgs.put("es_type", es_type);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mapOfOberserverArgs;
    }

    private static void showUsage(){
        LOG.info("用法：1，可以使用jar包内部默认Es 集群信息和协处理器信息。");
        LOG.info("2,传入两个参数，第一个参数包含ES 集群信息，第二个文件包含表格和协处理器信息。");
    }

    public static void main(String[] args) {
        // 如果传入的参数不为空
        if (args != null && args.length != 0 && args.length != 2){
            showUsage();
            return;
        }
        Map<String, String> mapOfOberserverArgs = new HashMap<>();
        InputStream coproccessor = null;
        if (args.length > 0){
            LOG.info("================es properties " + args[0] + " =====================");
            LOG.info("=============== coproccessor properties " + args[1] + "===================");
            mapOfOberserverArgs = getCoproccessorArgs(args[0]);
            try {
                coproccessor = new FileInputStream(args[1]);
            } catch (FileNotFoundException e) {
                LOG.info("输入的Coproccessor 配置文件不存在，请检查数据的路径。");
                e.printStackTrace();
            }
        } else {
            mapOfOberserverArgs = getCoproccessorArgs(null);
            coproccessor = CreateStaticRepoTable.class.getResourceAsStream("/coprocessor.properties");
        }
        Properties properties = new Properties();
        try {
            properties.load(coproccessor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String tableName =  properties.getProperty("table.name");
        String colfamsString =  properties.getProperty("table.colfams");
        String coproccessorName = properties.getProperty("table.coprocessor.name");
        String coproccessorPath = properties.getProperty("table.coprocessor.path");
        String maxVersion = properties.getProperty("table.maxversion");
        String timetToLive = properties.getProperty("table.timetolive");
        String[] colfams = colfamsString.split("-");
        // 如果表已经存在，直接返回
        try {
            if (HBaseHelper.getHBaseConnection().getAdmin().tableExists(TableName.valueOf(tableName))){
                LOG.info("表格已经存在，请进行确认是否删除表格，需要手动到HBase 客户端删除表格。");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (timetToLive != null){
            HBaseHelper.createTableWithCoprocessor(tableName, coproccessorName, coproccessorPath, mapOfOberserverArgs,
                    Integer.parseInt(maxVersion), Integer.parseInt(timetToLive), colfams);
        }else {
            HBaseHelper.createTableWithCoprocessor(tableName, coproccessorName, coproccessorPath, mapOfOberserverArgs,
                    Integer.parseInt(maxVersion), colfams);
        }
        LOG.info("====================== create table " + tableName + "success.. ==================");
    }
}
