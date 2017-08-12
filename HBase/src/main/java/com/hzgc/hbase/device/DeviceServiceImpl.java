package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.DeviceService;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import com.hzgc.util.StringUtil;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
<<<<<<< HEAD
=======

>>>>>>> ae471914925a1a169191942125bac31921eb3696

public class DeviceServiceImpl implements DeviceService {
    private static Logger LOG = Logger.getLogger(DeviceServiceImpl.class);

    @Override
    public boolean bindDevice(String platformId, String ipcID, String notes) {
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(ipcID) && StringUtil.strIsRight(platformId)) {
            try {
                Put put = new Put(Bytes.toBytes(ipcID));
                put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.PLAT_ID, Bytes.toBytes(platformId));
                put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.NOTES, Bytes.toBytes(notes));
                table.put(put);
                LOG.info("Put data[" + ipcID + ", " + platformId + "] successful");
                return true;
            } catch (Exception e) {
                LOG.error("Current bind is failed!");
                return false;
            } finally {
                HBaseUtil.closTable(table);
            }
        } else {
            LOG.error("Please check the arguments!");
            return false;
        }
    }

    @Override
    public boolean unbindDevice(String platformId, String ipcID) {
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(platformId) && StringUtil.strIsRight(ipcID)) {
            try {
                Delete delete = new Delete(Bytes.toBytes(ipcID));
                delete.addColumn(DeviceTable.CF_DEVICE, DeviceTable.PLAT_ID);
                table.delete(delete);
                LOG.info("Unbind device:" + ipcID + " and " + platformId + " successful");
                return true;
            } catch (Exception e) {
                LOG.error("Current unbind is failed!");
                return false;
            } finally {
                HBaseUtil.closTable(table);
            }
        } else {
            LOG.error("Please check the arguments!");
            return false;
        }
    }

    @Override
    public boolean renameNotes(String notes, String ipcID) {
        Table table = HBaseHelper.getTable(DeviceTable.TABLE_DEVICE);
        if (StringUtil.strIsRight(ipcID)) {
            try {
                Put put = new Put(Bytes.toBytes(ipcID));
                put.addColumn(DeviceTable.CF_DEVICE, DeviceTable.NOTES, Bytes.toBytes(notes));
                table.put(put);
                LOG.info("Rename " + ipcID + "'s notes successful!");
                return true;
            } catch (Exception e) {
                LOG.error("Current renameNotes is failed!");
                return false;
            }
        } else {
            LOG.error("Please check the arguments!");
            return false;
        }
    }
}
