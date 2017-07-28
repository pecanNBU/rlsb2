package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.WarnRule;
import com.hzgc.dubbo.device.WarnRuleService;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.ObjectUtil;
import com.hzgc.util.StringUtil;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WarnRuleServiceImpl implements WarnRuleService {
    private static Logger LOG = Logger.getLogger(WarnRuleServiceImpl.class);
    private static final Table OBJTYPETABLE = HBaseHelper.getTable("objToDevice");
    private static final byte[] CF = Bytes.toBytes("objType");

    @Override
    public Map<String, Boolean> configRules(List<String> ipcIDs, List<WarnRule> rules) {
        Table table = HBaseHelper.getTable(DeviceTable.getTableName());
        Map<String, Boolean> reply = new HashMap<>();
        String id = "";
        try {
            if (ipcIDs != null && rules != null) {
                for (String ipcID : ipcIDs) {
                    id = ipcID;
                    Map<Integer, Map<String, Integer>> temp = parseRuleAndInsert(id, rules);
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
        return null;
    }

    @Override
    public int deleteObjectTypeOfRules(String objectType, List<String> ipcIDs) {
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

    private Map<Integer, Map<String, Integer>> parseRuleAndInsert(String ipcID, List<WarnRule> rules) {
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
        if (objType != null) {
            try {
                for (String key : objType.keySet()) {
                    Map<String, Map<Integer, String>> value = objType.get(key);
                    Put put = new Put(Bytes.toBytes(key));
                    put.addColumn(CF, Bytes.toBytes(ipcID), ObjectUtil.objectToByte(value.get(ipcID)));
                    OBJTYPETABLE.put(put);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
