package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.DeviceService;

public class DeviceServiceImpl implements DeviceService {
    @Override
    public boolean bindDevice(String platformId, String ipcID, String notes) {
        return false;
    }

    @Override
    public boolean unbindDevice(String platformId, String ipcID) {
        return false;
    }

    @Override
    public boolean renameNotes(String notes, String ipcID) {
        return false;
    }
}
