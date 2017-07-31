package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.WarnRule;
import com.hzgc.dubbo.device.WarnRuleService;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.ObjectUtil;
import com.hzgc.util.StringUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class WarnRuleServiceImpl implements WarnRuleService {
    private static Logger LOG = Logger.getLogger(WarnRuleServiceImpl.class);
    private static final String OBJTYEPTABLE = "objToDevice";
    private static final byte[] CF = Bytes.toBytes("objType");
    private static final byte[] OFFLINERK = Bytes.toBytes("offlineWarnRowKey");
    private static final byte[] OFFLINECOL = Bytes.toBytes("objTypes");

    @Override
    public Map<String, Boolean> configRules(List<String> ipcIDs, List<WarnRule> rules) {
        Table table = HBaseHelper.getTable(DeviceTable.getTableName());
        Map<String, Boolean> reply = new HashMap<>();
        String id = "";
        try {
            if (ipcIDs != null && rules != null) {
                for (String ipcID : ipcIDs) {
                    id = ipcID;
                    Map<Integer, Map<String, Integer>> temp = parseRuleAndInsert(id, rules, table);
                    Put put = new Put(Bytes.toBytes(id));
                    put.addColumn(DeviceTable.getFamily(), DeviceTable.getWarn(), ObjectUtil.objectToByte(temp));
                    table.put(put);
                    reply.put(id, true);
                }
            }
        } catch (IOException e) {
            reply.put(id, false);
        } finally {
            HBaseUtil.closTable(table);
        }
        return reply;
    }

    @Override
    public Map<String, Boolean> deleteRules(List<String> ipcIDs) {
        Table table = HBaseHelper.getTable(DeviceTable.getTableName());
        Map<String, Boolean> reply = new HashMap<>();
        String id = "";
        if (ipcIDs != null) {
            try {
                for (String ipcID : ipcIDs) {
                    id = ipcID;
                    Delete delete = new Delete(Bytes.toBytes(id));
                    delete.addColumn(DeviceTable.getFamily(), DeviceTable.getWarn());
                    table.delete(delete);
                    reply.put(id, true);
                    delOfflineWarn(ipcID, table);
                }
            } catch (IOException e) {
                reply.put(id, false);
            } finally {
                HBaseUtil.closTable(table);
            }
        }
        return reply;
    }

    @Override
    public List<String> objectTypeHasRule(String objectType) {
        Table table = null;
        List<String> reply = new ArrayList<>();;
        if (StringUtil.strIsRight(objectType)) {
            try {
                table = HBaseHelper.getTable(OBJTYEPTABLE);
                Get get = new Get(Bytes.toBytes(objectType));
                get.addFamily(CF);
                Result result = table.get(get);
                CellScanner scanner =result.cellScanner();
                while (scanner.advance()) {
                    Cell cell = scanner.current();
                    byte[] qualifiers = scanner.current().getQualifierArray();
                    reply.add(new String(qualifiers, cell.getQualifierOffset(), cell.getQualifierLength()));
                }
                return reply;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(table);
            }
        }
        return reply;
    }

    @Override
    public int deleteObjectTypeOfRules(String objectType, List<String> ipcIDs) {
        Table objTable = null;
        Table deviceTable = null;
        if (StringUtil.strIsRight(objectType) && ipcIDs != null) {
            try {
                objTable = HBaseHelper.getTable(OBJTYEPTABLE);
                deviceTable = HBaseHelper.getTable(DeviceTable.getTableName());
                Get objGet = new Get(Bytes.toBytes(objectType));
                Result objResult = objTable.get(objGet);
                for (String str : ipcIDs) {
                    Map<Integer, String> map = deSerializObj(objResult.getValue(CF, Bytes.toBytes(str)));
                    Get deviceGet = new Get(Bytes.toBytes(str));
                    deviceGet.addColumn(DeviceTable.getFamily(), DeviceTable.getWarn());
                    Result deviceResult = deviceTable.get(deviceGet);
                    byte[] deviceByte = deviceResult.getValue(DeviceTable.getFamily(), DeviceTable.getWarn());
                    Map<Integer, Map<String, Integer>> deviceMap = deSerializDevice(deviceByte);
                    Put devicePut = new Put(Bytes.toBytes(str));
                    for (Integer key : map.keySet()) {
                        deviceMap.get(key).remove(objectType);
                    }
                    devicePut.addColumn(DeviceTable.getFamily(), DeviceTable.getWarn(), ObjectUtil.objectToByte(deviceMap));
                    deviceTable.put(devicePut);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(deviceTable);
                HBaseUtil.closTable(objTable);
            }
        }
        return 0;
    }

    @Override
    public Map<String, Boolean> addRules(List<String> ipcIDs, List<WarnRule> rules) {
        return null;
    }

    @Override
    public List<WarnRule> getCompareRules(String ipcID) {
        return null;
    }

    private Map<Integer, Map<String, Integer>> parseRuleAndInsert(String ipcID, List<WarnRule> rules, Table deviceTable) {
        Map<Integer, Map<String, Integer>> result = null;
        Map<String, Map<String, Map<Integer, String>>> objType = new HashMap<>();
        if (null != rules & StringUtil.strIsRight(ipcID)) {
            result = new HashMap<>();
            result.put(0, new HashMap<String, Integer>());
            result.put(1, new HashMap<String, Integer>());
            result.put(2, new HashMap<String, Integer>());
            for (WarnRule rule : rules) {
                Integer code = rule.getCode();
                if (null != code) {
                    if (Objects.equals(code, DeviceTable.getIDENTIFY()) || Objects.equals(code, DeviceTable.getADDED())) {
                        result.get(code).put(rule.getObjectType(), rule.getThreshold());
                        addMembers(objType, rule, ipcID);
                    }
                    if (Objects.equals(code, DeviceTable.getOFFLINE())) {
                        result.get(code).put(rule.getObjectType(), rule.getDayThreshold());
                        addMembers(objType, rule, ipcID);
                        addToOfflineWarn(rule, ipcID, deviceTable);
                    }
                }
            }
            putObjectTypeInfo(objType, ipcID);
        }
        return result;
    }

    private static void addMembers(Map<String, Map<String, Map<Integer, String>>> objType, WarnRule rule, String ipcID) {
        if (null == objType.get(rule.getObjectType())) {
            Map<String, Map<Integer, String>> ipcMap = new HashMap<>();
            Map<Integer, String> warnMap = new HashMap<>();
            warnMap.put(rule.getCode(), "");
            ipcMap.put(ipcID, warnMap);
            objType.put(rule.getObjectType(), ipcMap);
        } else {
            Map<String, Map<Integer, String>> ipcMap = objType.get(rule.getObjectType());
            ipcMap.get(ipcID).put(rule.getCode(), "");
        }
    }

    private void putObjectTypeInfo(Map<String, Map<String, Map<Integer, String>>> objType, String ipcID) {
        Table table = null;
        if (objType != null) {
            try {
                table = HBaseHelper.getTable(OBJTYEPTABLE);
                for (String key : objType.keySet()) {
                    Map<String, Map<Integer, String>> value = objType.get(key);
                    Put put = new Put(Bytes.toBytes(key));
                    put.addColumn(CF, Bytes.toBytes(ipcID), ObjectUtil.objectToByte(value.get(ipcID)));
                    table.put(put);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(table);
            }
        }
    }

    private Map<Integer, String> deSerializObj(byte[] bytes) {
        if (bytes != null) {
            return (Map<Integer, String>)ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    public Map<Integer, Map<String, Integer>> deSerializDevice(byte[] bytes) {
        if (bytes != null) {
            return (Map<Integer, Map<String, Integer>>)ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    private  void addToOfflineWarn(WarnRule rule, String ipcID, Table deviceTable) {
        if (rule != null && deviceTable != null && StringUtil.strIsRight(ipcID)) {
            try {
                Get get = new Get(OFFLINERK);
                Result result = deviceTable.get(get);
                if (result.containsColumn(DeviceTable.getFamily(), OFFLINECOL)) {
                    Map<String, Map<String, String>> map = (Map<String, Map<String, String>>)ObjectUtil.byteToObject(result.getValue(DeviceTable.getFamily(), OFFLINECOL));
                    map.get(rule.getObjectType()).put(ipcID, "");
                    Put put = new Put(OFFLINERK);
                    put.addColumn(DeviceTable.getFamily(), OFFLINECOL, ObjectUtil.objectToByte(map));
                    deviceTable.put(put);
                } else {
                    Put put = new Put(OFFLINERK);
                    Map<String, Map<String, String>> map = new HashMap<>();
                    Map<String, String> ipcMap = new HashMap<>();
                    ipcMap.put(ipcID, "");
                    map.put(rule.getObjectType(), ipcMap);
                    put.addColumn(DeviceTable.getFamily(), OFFLINECOL, ObjectUtil.objectToByte(map));
                    deviceTable.put(put);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void delOfflineWarn(String ipcID, Table deviceTable) {
        if (StringUtil.strIsRight(ipcID) && deviceTable != null) {
            try {
                Get get = new Get(OFFLINERK);
                Result result = deviceTable.get(get);
                if (result.containsColumn(DeviceTable.getFamily(), OFFLINECOL)) {
                    Map<String, Map<String, String>> map = (Map<String, Map<String, String>>)ObjectUtil.byteToObject(result.getValue(DeviceTable.getFamily(),  OFFLINECOL));
                    map.remove(ipcID);
                    Put put = new Put(OFFLINERK);
                    put.addColumn(DeviceTable.getFamily(), OFFLINECOL, ObjectUtil.objectToByte(map));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
