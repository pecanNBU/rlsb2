package com.hzgc.hbase2es;

import org.apache.hadoop.hbase.TableName;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CreateSrecordTable {
    private static Logger LOG = Logger.getLogger(CreateSrecordTable.class);
    private static void showUsage(){
        LOG.info("用法：1，可以使用jar包内部默认Es 集群信息和协处理器信息。");
        LOG.info("2,传入两个参数，第一个参数包含历史查询记录表格的表名，列族名等信息。");
    }

    public static void main(String[] args) {
        // 如果传入的参数不为空
        if (args != null && args.length != 0 && args.length != 1){
            showUsage();
            return;
        }
        InputStream srecordProp = null;
        if (args.length > 0){
            LOG.info("================== the table properties file is " + args[0] + "============");
            try {
                srecordProp = new FileInputStream(args[0]);
            } catch (FileNotFoundException e) {
                LOG.info("srecord 表格的配置文件不存在，请检查数据的路径。");
                e.printStackTrace();
            }
        } else {
            LOG.info("============== innner cofig file =============");
            srecordProp = CreateSrecordTable.class.getResourceAsStream("/srecord.properties");
        }
        Properties prop = new Properties();
        try {
            prop.load(srecordProp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String tableName =  prop.getProperty("table.srecord.name");
        String colfamsString =  prop.getProperty("table.srecord.colfams");
        String maxVersion = prop.getProperty("table.srecord.maxversion");
        String timetToLive = prop.getProperty("table.sercord.timetolive");
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
            HBaseHelper.createTable(tableName, Integer.parseInt(maxVersion), Integer.parseInt(timetToLive), colfams);
        }else {
            HBaseHelper.createTable(tableName, Integer.parseInt(maxVersion), colfams);
        }
        LOG.info("create table " + tableName + "success..");
    }
}
