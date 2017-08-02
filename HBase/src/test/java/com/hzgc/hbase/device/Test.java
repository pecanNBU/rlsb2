package com.hzgc.hbase.device;

import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.util.ObjectUtil;
import org.apache.ftpserver.command.impl.DELE;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
//        WarnRuleServiceImpl war = new WarnRuleServiceImpl();
//        List<String> ips = new ArrayList<>();
//        ips.add("ipc1");
//        ips.add("ipc2");
//        ips.add("ipc3");
//        System.out.println(war.configRules(ips, TestData.getDataList()));
//        WarnRuleServiceImpl war = new WarnRuleServiceImpl();
//        List<String> ips = new ArrayList<>();
//        ips.add("ipc4");
//        System.out.println(war.addRules(ips, TestData.getDataList()));

//        System.out.print(war.deleteRules(ips));
//        Table table = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
//        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
//        Get get = new Get(DeviceTable.OFFLINERK);
//        Result result = table.get(get);
//        byte[] bb = result.getValue(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL);
//        Map<String, Map<String, String>> map = (Map<String, Map<String, String>>)ObjectUtil.byteToObject(bb);
//        System.out.println(map);
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
        Get get = new Get(Bytes.toBytes("c1"));
        Result result = table.get(get);
        byte[] bb = result.getValue(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL);
        Map<String, Map<Integer, String>> map = (Map<String, Map<Integer, String>>)ObjectUtil.byteToObject(bb);
        System.out.println(map);
    }
}
