package com.hzgc.hbase.device;

import com.hzgc.util.StringUtil;

import java.util.List;

public class DeviceUtilImpl implements DeviceUtil {
    @Override
    public String getplatfromID(String ipcID) {
        if (StringUtil.strIsRight(ipcID)) {

        }
        return null;
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
