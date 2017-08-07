package com.hzgc.hbase2es.table;

import com.hzgc.hbase2es.util.HBaseHelper;
import com.hzgc.util.FileUtil;
import com.hzgc.util.IOUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class CreateStaticRepoTable {
    private static Logger LOG = Logger.getLogger(CreateStaticRepoTable.class);

    // 获取ES集群的信息
    private static Map<String, String> getCoproccessorArgs(String path) {
        Properties prop = new Properties();
        Map<String, String> mapOfOberserverArgs = new HashMap<>();
        InputStream is = null;
        try {
            if (path == null || path.length() < 1) {
                File file = FileUtil.loadResourceFile("es-static-index.properties");
                if (file != null) {
                    is = new FileInputStream(file);
                }
            } else {
                is = new FileInputStream(new File(path));
            }
            prop.load(is);
            for (Object kv : prop.keySet()) {
                mapOfOberserverArgs.put((String)kv, (String)prop.get(kv));
            }
        } catch (IOException e) {
            LOG.error("输入的es集群信息文件路径不存在！！！");
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(is);
        }
        return mapOfOberserverArgs;
    }

    private static void showUsage() {
        LOG.info("用法：1，可以使用jar包内部默认Es 集群信息和协处理器信息。");
        LOG.info("2,传入两个参数，第一个参数包含ES 集群信息，第二个文件包含表格和协处理器信息。");
    }

    public static void main(String[] args) {
        Properties tableProper = new Properties();
        if (args != null && args.length != 0 && args.length != 2) {
            showUsage();
            return;
        }
        Map<String, String> mapOfOberserverArgs = null;
        InputStream coproccessor = null;
        if (args != null) {
            if (args.length > 0) {
                LOG.info("================es properties " + args[0] + " =====================");
                LOG.info("=============== coproccessor properties " + args[1] + "===================");
                mapOfOberserverArgs = getCoproccessorArgs(args[0]);
                try {
                    coproccessor = new FileInputStream(args[1]);
                } catch (FileNotFoundException e) {
                    LOG.error("输入的Coproccessor 配置文件不存在，请检查数据的路径。");
                    e.printStackTrace();
                }
            } else {
                mapOfOberserverArgs = getCoproccessorArgs(null);
                File file = FileUtil.loadResourceFile("static-table.properties");
                try {
                    if (file != null) {
                        coproccessor = new FileInputStream(file);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            tableProper.load(coproccessor);
            String tableName = tableProper.getProperty("table.name");
            String colfamsString = tableProper.getProperty("table.colfams");
            String coproccessorName = tableProper.getProperty("table.coprocessor.name");
            String coproccessorPath = tableProper.getProperty("table.coprocessor.path");
            String maxVersion = tableProper.getProperty("table.maxversion");
            String timetToLive = tableProper.getProperty("table.timetolive");
            String[] colfams = colfamsString.split("-");
            if (HBaseHelper.getHBaseConnection().getAdmin().tableExists(TableName.valueOf(tableName))) {
                LOG.error("表格:" + tableName + "已经存在，请进行确认是否删除表格，需要手动到HBase 客户端删除表格。");
                return;
            }
            if (timetToLive != null) {
                HBaseHelper.createTableWithCoprocessor(tableName, coproccessorName, coproccessorPath, mapOfOberserverArgs,
                        Integer.parseInt(maxVersion), Integer.parseInt(timetToLive), colfams);
            } else {
                HBaseHelper.createTableWithCoprocessor(tableName, coproccessorName, coproccessorPath, mapOfOberserverArgs,
                        Integer.parseInt(maxVersion), colfams);
            }
            LOG.info("====================== create table " + tableName + "success.. ==================");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(coproccessor);
        }
    }
}
