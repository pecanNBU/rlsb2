package com.hzgc.hbase.device;

import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.util.StringUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.List;

public class DeviceUtilImpl implements DeviceUtil {
    @Override
    public String getplatfromID(String ipcID) {
        if (StringUtil.strIsRight(ipcID)) {
            try {
                Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
                Get get = new Get(Bytes.toBytes(ipcID));
                Result result = table.get(get);
                if (result.containsColumn(DeviceTable.CF_DEVICE, DeviceTable.PLAT_ID)) {
                    return new String(result.getValue(DeviceTable.CF_DEVICE, DeviceTable.PLAT_ID));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    @Override
    public int isWarnTypeBinding(String ipcID, String warnType) {
        return 0;
    }

    @Override
    public List<String> getObjectTypeList(String ipcID, String warnType) {
        return null;
    }

    @Override
    public Integer getThreshold(String ipcID, String warnType) {
        return null;
    }
}
