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

    @Override
    public Map<String, Boolean> configRules(List<String> ipcIDs, List<WarnRule> rules) {
        deleteRules(ipcIDs);
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        Map<String, Boolean> reply = new HashMap<>();
        String id = "";
        try {
            if (ipcIDs != null && rules != null) {
                for (String ipcID : ipcIDs) {
                    id = ipcID;
                    Map<Integer, Map<String, Integer>> temp = parseRuleAndInsert(id, rules, table);
                    Put put = new Put(Bytes.toBytes(id));
                    put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, ObjectUtil.objectToByte(temp));
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

    /**
     * 删除设备的布控规则（外）（赵喆）
     */
    @Override
    public Map<String, Boolean> deleteRules(List<String> ipcIDs) {
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        Table objTable = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
        Map<String, Boolean> reply = new HashMap<>();
        String id = "";
        if (ipcIDs != null) {
            try {
                for (String ipcID : ipcIDs) {
                    id = ipcID;
                    Delete delete = new Delete(Bytes.toBytes(id));
                    delete.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN);
                    table.delete(delete);
                    reply.put(id, true);
                    delOfflineWarn(ipcID, table);
                }
                delIpcInObjToDevice(ipcIDs, objTable);
            } catch (IOException e) {
                reply.put(id, false);
            } finally {
                HBaseUtil.closTable(table);
            }
        }
        return reply;
    }

    /**
     * 查看有多少设备绑定了此人员类型objectType
     */
    @Override
    public List<String> objectTypeHasRule(String objectType) {
        Table table = null;
        List<String> reply = new ArrayList<>();;
        if (StringUtil.strIsRight(objectType)) {
            try {
                table = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
                Get get = new Get(Bytes.toBytes(objectType));
                get.addFamily(DeviceTable.CF_OBJTYPE);
                Result result = table.get(get);
                CellScanner scanner =result.cellScanner();
                while (scanner.advance()) {
                    Cell cell = scanner.current();
                    byte[] qualifiers = scanner.current().getQualifierArray();
                    reply.add(new String(qualifiers, cell.getQualifierOffset(), cell.getQualifierLength()));
                }
                return reply;
            } catch (IOException e) {
                LOG.error(e.getMessage());
            } finally {
                HBaseUtil.closTable(table);
            }
        }
        return reply;
    }


    /**
     * 在所有设备列表绑定的规则中，删除此objectType
     */
    @Override
    public int deleteObjectTypeOfRules(String objectType, List<String> ipcIDs) {
        Table objTable = null;
        Table deviceTable = null;
        int count = 0;
        if (StringUtil.strIsRight(objectType) && ipcIDs != null) {
            try {
                objTable = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
                deviceTable = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
                for (String ipcID : ipcIDs) {
                    Get objGet = new Get(Bytes.toBytes(objectType));
                    Result objResult = objTable.get(objGet);
                    if (objResult.containsColumn(DeviceTable.CF_OBJTYPE, Bytes.toBytes(ipcID))) {
                        Map<String, Map<Integer, String>> objMap =
                                deSerializObjToDevice(objResult.getValue(DeviceTable.CF_OBJTYPE, Bytes.toBytes(ipcID)));
                        objMap.remove(ipcID);
                        for (String ipc : objMap.keySet()) {
                            for (Integer code : objMap.get(ipc).keySet()) {
                                if (Objects.equals(code, DeviceTable.OFFLINE)) {
                                    deleteOfflineObjeType(deviceTable, objectType, ipcID);
                                }
                            }
                            objMap.remove(ipc);
                            Get deviceGet = new Get(Bytes.toBytes(ipcID));
                            Result deviceResult = deviceTable.get(deviceGet);
                            if (deviceResult.containsColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN)) {
                                byte[] deviceByte = deviceResult.getValue(DeviceTable.CF_DEVICE, DeviceTable.WARN);
                                Map<Integer, Map<String, Integer>> deviceMap = deSerializDevice(deviceByte);
                                deviceMap.get(ipc).remove(objectType);
                                Put devicePut = new Put(Bytes.toBytes(ipcID));
                                devicePut.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, ObjectUtil.objectToByte(deviceMap));
                                deviceTable.put(devicePut);
                            }
                        }
                    }
                    count++;
                }
                return count;
            } catch (IOException e) {
                LOG.error(e.getMessage());
            } finally {
                HBaseUtil.closTable(deviceTable);
                HBaseUtil.closTable(objTable);
            }
        }
        return 0;
    }

    @Override
    public Map<String, Boolean> addRules(List<String> ipcIDs, List<WarnRule> rules) {
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        Map<String, Boolean> reply = new HashMap<>();
        String id = "";
        try {
            if (ipcIDs != null && rules != null) {
                for (String ipcID : ipcIDs) {
                    id = ipcID;
                    Map<Integer, Map<String, Integer>> temp = parseRuleAndInsert(id, rules, table);
                    Get get = new Get(Bytes.toBytes(ipcID));
                    Result result = table.get(get);
                    if (result.containsColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN)) {
                        byte[] tempByte = result.getValue(DeviceTable.CF_DEVICE, DeviceTable.WARN);
                        Map<Integer, Map<String, Integer>> tempMap = deSerializDevice(tempByte);
                        Set<Integer> tempSet = temp.keySet();
                        for (Integer code : tempSet) {
                            if (tempMap.get(code) != null) {
                                Set<String> set = temp.get(code).keySet();
                                for (String type : set) {
                                    tempMap.get(code).put(type, temp.get(code).get(type));
                                }
                            } else {
                                temp.put(code, temp.get(code));
                            }
                        }
                        Put put = new Put(Bytes.toBytes(ipcID));
                        put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, ObjectUtil.objectToByte(tempMap));
                        table.put(put);
                    } else {
                        Put put = new Put(Bytes.toBytes(id));
                        put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN, ObjectUtil.objectToByte(temp));
                        table.put(put);
                    }
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

    /**
     * 获取设备的对比规则
     *
     * @param ipcID 设备 ipcID
     * @return
     */
    @Override
    public List<WarnRule> getCompareRules(String ipcID) {
        List<WarnRule> reply = new ArrayList<>();
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(ipcID)) {
            try {
                Get get = new Get(Bytes.toBytes(ipcID));
                Result result = table.get(get);
                if (result.containsColumn(DeviceTable.CF_DEVICE, DeviceTable.WARN)) {
                    byte[] deviceByte = result.getValue(DeviceTable.CF_DEVICE, DeviceTable.WARN);
                    Map<Integer, Map<String, Integer>> deviceMap = deSerializDevice(deviceByte);
                    for (Integer code : deviceMap.keySet()) {
                        Map<String, Integer>  tempMap = deviceMap.get(code);
                        for (String type : deviceMap.get(code).keySet()) {
                            WarnRule warnRule = new WarnRule();
                            warnRule.setCode(code);
                            warnRule.setObjectType(type);
                            if (code == DeviceTable.OFFLINE) {
                                warnRule.setDayThreshold(tempMap.get(type));
                            } else {
                                warnRule.setThreshold(tempMap.get(type));
                            }
                            reply.add(warnRule);
                        }
                    }
                    return reply;
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            } finally {
                HBaseUtil.closTable(table);
            }
        }
        return reply;
    }

    /**
     * 删除离线告警中某一objectType包含的某一ipcID
     */
    private void deleteOfflineObjeType(Table deviceTable, String objType, String ipcID) {
        Get get = new Get(DeviceTable.OFFLINERK);
        Put put = new Put(DeviceTable.OFFLINERK);
        try {
            Result result = deviceTable.get(get);
            byte[] offLine = result.getValue(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL);
            Map<String, Map<String, String>> offLineMap = deSerializOffLine(offLine);
            offLineMap.get(objType).remove(ipcID);
            put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(offLineMap));
            deviceTable.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析configRules()传入的布控规则，并在解析的同时同步其他相关数据
     */
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
                    if (Objects.equals(code, DeviceTable.IDENTIFY) || Objects.equals(code, DeviceTable.ADDED)) {
                        result.get(code).put(rule.getObjectType(), rule.getThreshold());
                        addMembers(objType, rule, ipcID);
                    }
                    if (Objects.equals(code, DeviceTable.OFFLINE)) {
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

    /**
     * 向参数objectType对应的数据类型中添加成员
     */
    private static void addMembers(Map<String, Map<String, Map<Integer, String>>> objType, WarnRule rule, String ipcID) {
        if (objType.get(rule.getObjectType()) == null) {
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

    /**
     * 将添加完成收的objectType插入“objToDevice”表
     */
    private void putObjectTypeInfo(Map<String, Map<String, Map<Integer, String>>> objType, String ipcID) {
        Table table = null;
        if (objType != null) {
            try {
                table = HBaseHelper.getTable(DeviceTable.TABLE_OBJTYPE);
                for (String key : objType.keySet()) {
                    Get get = new Get(Bytes.toBytes(key));
                    Result result = table.get(get);
                    if (result.containsColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL)) {
                        byte[] objBytes = result.getValue(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL);
                        Map<String, Map<Integer, String>> typeMap = deSerializObjToDevice(objBytes);
                        if (typeMap.get(ipcID) == null) {
                            typeMap.put(ipcID, objType.get(key).get(ipcID));
                        } else {
                            for (Integer code : objType.get(key).get(ipcID).keySet()) {
                                typeMap.get(ipcID).put(code, objType.get(key).get(ipcID).get(code));
                            }
                        }
                        Put put = new Put(Bytes.toBytes(key));
                        put.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(typeMap));
                        table.put(put);
                    } else {
                        Map<String, Map<Integer, String>> value = objType.get(key);
                        Put put = new Put(Bytes.toBytes(key));
                        put.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(value));
                        table.put(put);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(table);
            }
        }
    }

    /**
     * objectType数据类型：Map<Integer, String>
     * 反序列化此数据类型
     */
    private Map<String, Map<Integer, String>> deSerializObjToDevice(byte[] bytes) {
        if (bytes != null) {
            return (Map<String, Map<Integer, String>>)ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    /**
     * 设备布控预案数据类型：Map<Integer, Map<String, Integer>>
     * 反序列化此数据类型
     */
    private Map<Integer, Map<String, Integer>> deSerializDevice(byte[] bytes) {
        if (bytes != null) {
            return (Map<Integer, Map<String, Integer>>)ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    /**
     * 离线告警数据类型：Map<String, Map<String, String>>
     * 反序列化此数据类型
     */
    private Map<String, Map<String, String>> deSerializOffLine(byte[] bytes) {
        if (bytes != null) {
            return (Map<String, Map<String, String>>)ObjectUtil.byteToObject(bytes);
        }
        return null;
    }

    /**
     * 同步离线告警布控预案
     */
    private void addToOfflineWarn(WarnRule rule, String ipcID, Table deviceTable) {
        if (rule != null && deviceTable != null && StringUtil.strIsRight(ipcID)) {
            try {
                Get get = new Get(DeviceTable.OFFLINERK);
                Result result = deviceTable.get(get);
                if (result.containsColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL)) {
                    byte[] offLine = result.getValue(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL);
                    Map<String, Map<String, String>> map = deSerializOffLine(offLine);
                    Map<String, String> objType = map.get(rule.getObjectType());
                    if (objType != null) {
                        objType.put(ipcID, "");
                    } else {
                        Map<String, String> tempMap = new HashMap<>();
                        tempMap.put(ipcID, "");
                        map.put(rule.getObjectType(), tempMap);
                    }
                    Put put = new Put(DeviceTable.OFFLINERK);
                    put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(map));
                    deviceTable.put(put);
                } else {
                    Put put = new Put(DeviceTable.OFFLINERK);
                    Map<String, Map<String, String>> map = new HashMap<>();
                    Map<String, String> ipcMap = new HashMap<>();
                    ipcMap.put(ipcID, "");
                    map.put(rule.getObjectType(), ipcMap);
                    put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(map));
                    deviceTable.put(put);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除某一设备的离线告警
     */
    private void delOfflineWarn(String ipcID, Table deviceTable) {
        if (StringUtil.strIsRight(ipcID) && deviceTable != null) {
            try {
                Get get = new Get(DeviceTable.OFFLINERK);
                Result result = deviceTable.get(get);
                if (result.containsColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL)) {
                    byte[] offLine = result.getValue(DeviceTable.CF_DEVICE,  DeviceTable.OFFLINECOL);
                    Map<String, Map<String, String>> offLinemap = deSerializOffLine(offLine);
                    Set<String> temSet = offLinemap.keySet();
                    for (String type : temSet) {
                        offLinemap.get(type).remove(ipcID);
                    }
                    Put put = new Put(DeviceTable.OFFLINERK);
                    put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.OFFLINECOL, ObjectUtil.objectToByte(offLinemap));
                    deviceTable.put(put);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void delIpcInObjToDevice(List<String> ipcIDs, Table objTable) {
        if (ipcIDs != null && objTable != null) {
            List<Delete> ipcList = new ArrayList<>();
            try {
                Scan scan = new Scan();
                ResultScanner results =objTable.getScanner(scan);
                for (Result result : results) {
                    Get get = new Get(result.getRow());
                    Result tempResult = objTable.get(get);
                    if (tempResult.containsColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL)) {
                        byte[] tempByte = tempResult.getValue(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL);
                        Map<String, Map<Integer, String>> tempMap = deSerializObjToDevice(tempByte);
                        for (String id : ipcIDs) {
                            tempMap.remove(id);
                        }
                        if (tempMap.isEmpty()) {
                            Delete delete = new Delete(result.getRow());
                            objTable.delete(delete);
                        } else {
                            Put put = new Put(result.getRow());
                            put.addColumn(DeviceTable.CF_OBJTYPE, DeviceTable.OBJTYPE_COL, ObjectUtil.objectToByte(tempMap));
                            objTable.put(put);
                        }
                    }
                }
                objTable.delete(ipcList);
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }
}
