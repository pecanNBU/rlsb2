package com.hzgc.streaming.util;

import org.omg.CORBA.StringHolder;

import java.io.Serializable;
import java.util.Date;

/**
 * 离线告警时间更新
 */
public class TimeUpdateUtils implements Serializable {

    public static void timeUpdate(String staticId) throws Exception{
        Date d = new Date(System.currentTimeMillis());
        long timestamp = d.getTime();
        System.out.println(d.getTime());
        String rowKey = staticId;
        String tableName = PropertiesUtils.getPropertiesValue("TEST_STATIC_STORE");
        String familyName = "info";
        String column = "updateTime";
        HbaseUtils.addData(rowKey,tableName,familyName,column,Long.toString(timestamp));
    }

    public static void main(String []args)throws Exception{
        timeUpdate("13");
    }
}
