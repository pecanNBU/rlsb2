package com.hzgc.streaming.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/21.
 */
public class HbaseUtils implements Serializable {
    // 声明静态配置
    private static HBaseAdmin admin = null;
    static Configuration conf = null;

    static {
        conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", PropertiesUtils.getPropertiesValue("hbase.zookeeper.quorum"));
        conf.set("hbase.zookeeper.property.clientPort", PropertiesUtils.getPropertiesValue("hbase.zookeeper.property.clientPort"));
    }

    /**
     * 创建表
     *
     * @param tableName
     * @param family
     * @throws Exception
     */
    public static void createTable(String tableName, String[] family) throws Exception {
        HBaseAdmin admin = new HBaseAdmin(conf);
        HTableDescriptor desc = new HTableDescriptor(tableName);
        for (int i = 0; i < family.length; i++) {
            desc.addFamily(new HColumnDescriptor(family[i]));
        }
        if (admin.tableExists(tableName)) {
            System.out.println("表【" + tableName + "】已经存在！");
            System.exit(0);
        } else {
            admin.createTable(desc);
            System.out.println("表【" + tableName + "】创建成功！");
        }
    }

    public static void addData(String rowKey, String tableName,
                               String[] column1, String[] value1, String[] column2, String[] value2) throws Exception {

        Put put = new Put(rowKey.getBytes());
        HTable table = new HTable(conf, tableName);
        //得到表所有的列族
        HColumnDescriptor[] columnFamilies = table.getTableDescriptor().getColumnFamilies();

        for (int i = 0; i < columnFamilies.length; i++) {
            String familyName = columnFamilies[i].getNameAsString();
            if (familyName.equals("t1")) {
                for (int j = 0; j < column1.length; j++) {
                    put.add(familyName.getBytes(), column1[j].getBytes(), value1[j].getBytes());

                }
            }
            if (familyName.equals("t2")) {
                // author列族put数据
                for (int j = 0; j < column2.length; j++) {
                    put.add(familyName.getBytes(),
                            column2[j].getBytes(), value2[j].getBytes());
                }
            }
        }
        table.put(put);
        System.out.println("向表【" + tableName + "】添加数据成功");

    }

    public static void addData(String rowKey, String tableName,
                               String familyName, String column, String value) throws Exception {

        Put put = new Put(rowKey.getBytes());
        HTable table = new HTable(conf, tableName);
        put.add(familyName.getBytes(), column.getBytes(), value.getBytes());
        table.put(put);
        System.out.println("向表【" + tableName + "】添加数据成功");

    }

    /**
     * 通过设备id、告警类型来获取告警规则
     *
     * @param deviceID  设备id
     * @param alarmType 告警类型
     * @return 告警规则的Map对象
     * @throws Exception
     */
    public static Map<String, String> getRule(String deviceID, String alarmType) throws Exception {
        Get get = new Get(deviceID.getBytes());
        HTable table = new HTable(conf, PropertiesUtils.getPropertiesValue("hbase.table.rule.name").getBytes());
        Result result = table.get(get);
        Map map = new HashMap<String, String>();
        for (KeyValue kv : result.list()) {
            String family = Bytes.toString(kv.getFamily());
            if (family.equals(alarmType)) {
                String qualifier = Bytes.toString(kv.getQualifier());
                String value = Bytes.toString(kv.getValue());
                map.put(qualifier, value);
            }
        }
        return map;
    }


    public static void addData(String rowKey, String tableName, String familyName1, String column11, String value11, String column12, String value12,
                               String column13, String value13, String familyName2, String column21, String value21, String column22, String value22,
                               String column23, String value23, String familyName3, String column31, String value31, String column32, String value32, String column33, String value33
    ) throws Exception {

        Put put = new Put(rowKey.getBytes());
        HTable table = new HTable(conf, tableName);
        //得到表所有的列族
        HColumnDescriptor[] columnFamilies = table.getTableDescriptor().getColumnFamilies();
        put.add(familyName1.getBytes(), column11.getBytes(), value11.getBytes());
        put.add(familyName1.getBytes(), column12.getBytes(), value12.getBytes());
        put.add(familyName1.getBytes(), column13.getBytes(), value13.getBytes());

        put.add(familyName2.getBytes(), column21.getBytes(), value21.getBytes());
        put.add(familyName2.getBytes(), column22.getBytes(), value22.getBytes());
        put.add(familyName2.getBytes(), column23.getBytes(), value23.getBytes());

        put.add(familyName3.getBytes(), column31.getBytes(), value31.getBytes());
        put.add(familyName3.getBytes(), column32.getBytes(), value32.getBytes());
        put.add(familyName3.getBytes(), column33.getBytes(), value33.getBytes());
        table.put(put);
        System.out.println("向表【" + tableName + "】添加数据成功");

    }

    public static void main(String[] args) throws Exception {
//        String []ss={"info"};
//       createTable("TEST_STATIC_STORE",ss);
        //System.out.println(getRule("0001","offLine").size());
        Date d = new Date(System.currentTimeMillis());
        long timestamp = d.getTime();

        addData("15", "TEST_STATIC_STORE", "info", "objectType", "a");
        addData("15", "TEST_STATIC_STORE", "info", "updateTime", Long.toString(timestamp));

//        Map map = getRule("0001","add");
//        System.out.println("size:" + map.size());
//        Iterator iterator =map.keySet().iterator();
//        while (iterator.hasNext()){
//            Object obj =iterator.next();
//            System.out.println(obj);
//            System.out.println(map.get(obj));
//            System.out.println("-----------------------");
//        }

//        String rowKey = "0009";
//        String tableName = "TEST_RULE_STORE";
//        //识别
//        String recognitionFamily = "recognition";
//        String REobjectTypeListColumn = "objectTypeList";
//        String REobjectTypeListValue = "a b c";
//        String REsimilarityColumn = "similarity";
//        String REsimilarityValue = "0.7";
//        String REplatIdColumn = "platId";
//        String REplatIdValue1 = "3";
//        //新增
//        String addFamily = "add";
//        String ADDobjectTypeListcolumn = "objectTypeList";
//        String ADDobjectTypeListValue = "b c";
//        String ADDsimilarityColumn = "similarity";
//        String ADDsimilarityValue = "0.7";
//        String ADDplatIdColumn = "platId";
//        String ADDplatIdValue = "3";
//
//        //离线
//        String offLineFamily = "offLine";
//        String OFFobjectTypeListColumn = "objectTypeList";
//        String OFFobjectTypeListValue = "a b";
//        String OFFdaysColumn = "days";
//        String OFFdaysValue = "3";
//        String OFFplatIdColumn = "platId";
//        String OFFplatIdValue = "3";
//        addData(rowKey,tableName,recognitionFamily,REobjectTypeListColumn,REobjectTypeListValue,REsimilarityColumn,REsimilarityValue,REplatIdColumn,REplatIdValue1,addFamily,
//                ADDobjectTypeListcolumn,ADDobjectTypeListValue,ADDsimilarityColumn,ADDsimilarityValue,ADDplatIdColumn,ADDplatIdValue,offLineFamily,OFFobjectTypeListColumn,OFFobjectTypeListValue,
//                OFFdaysColumn,OFFdaysValue,OFFplatIdColumn,OFFplatIdValue);

    }

}
