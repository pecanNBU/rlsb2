package com.hzgc.hbase.device;

import org.apache.hadoop.hbase.util.Bytes;

public class DeviceTable {
    final static String TABLE_DEVICE = "device";
    final static String TABLE_OBJTYPE = "objToDevice";
    final static byte[] CF_DEVICE = Bytes.toBytes("device");
    final static byte[] PLAT_ID = Bytes.toBytes("p");
    final static byte[] NOTES = Bytes.toBytes("n");
    final static byte[] WARN = Bytes.toBytes("w");
    final static byte[] CF_OBJTYPE = Bytes.toBytes("objType");
    final static byte[] OBJTYPE_COL = Bytes.toBytes("type");
    final static byte[] OFFLINERK = Bytes.toBytes("offlineWarnRowKey");
    final static byte[] OFFLINECOL = Bytes.toBytes("objTypes");
    final static Integer IDENTIFY = 0;
    final static Integer ADDED = 1;
    final static Integer OFFLINE = 2;

}