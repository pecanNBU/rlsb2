package com.hzgc.hbase.device;

import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class DeviceTable {
    private final static String TABLE_NAME = "device";
    private final static byte[] FAMILY = Bytes.toBytes("device");
    private final static byte[] PLAT_ID = Bytes.toBytes("p");
    private final static byte[] NOTES = Bytes.toBytes("n");
    private final static byte[] WARN = Bytes.toBytes("w");
    private final static Integer IDENTIFY = 0;
    private final static Integer ADDED = 1;
    private final static Integer OFFLINE = 2;

    public static byte[] getWarn() {
        return WARN;
    }

    public static Integer getIDENTIFY() {
        return IDENTIFY;
    }

    public static Integer getADDED() {
        return ADDED;
    }

    public static Integer getOFFLINE() {
        return OFFLINE;
    }

    public static String getTableName() {
        return TABLE_NAME;
    }

    public static byte[] getFamily() {
        return FAMILY;
    }

    public static byte[] getPlatId() {
        return PLAT_ID;
    }

    public static byte[] getNotes() {
        return NOTES;
    }

}
