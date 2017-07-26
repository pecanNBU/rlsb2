package com.hzgc.hbase.util;

import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class HBaseUtil {
    private static Logger LOG = Logger.getLogger(HBaseUtil.class);

    public static void closTable(Table table) {
        if (null != table) {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
