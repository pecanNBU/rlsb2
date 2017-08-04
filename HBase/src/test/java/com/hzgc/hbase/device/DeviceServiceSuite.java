package com.hzgc.hbase.device;

import org.junit.Test;

/**
 * Created by Administrator on 2017-8-4.
 */
public class DeviceServiceSuite {
    @Test
    public void testBindDevice(){
        String platformId = "hhhhh100";
        String ipcID = "hhh100";
        String notes = "police";
        DeviceServiceImpl deviceService = new DeviceServiceImpl();
        Boolean a = deviceService.bindDevice(platformId,ipcID,notes);
        System.out.println(a);
    }
    @Test
    public void testUnbindDevice(){
        String platformId = "hhhhh100";
        String ipcID = "hhh100";
        DeviceServiceImpl deviceService = new DeviceServiceImpl();
        Boolean a = deviceService.unbindDevice(platformId,ipcID);
        System.out.println(a);
    }
    @Test
    public void testRenameNotes(){
        String notes = "policeff";
        String ipcID = "hhh100";
        DeviceServiceImpl deviceService = new DeviceServiceImpl();
        Boolean a  = deviceService.renameNotes(notes,ipcID);
        System.out.println(a);
    }
}
