package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.DeviceService;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

public class DeviceServiceImpl implements DeviceService {
    private static Logger LOG = Logger.getLogger(DeviceServiceImpl.class);
    private final static String TABLE_NAME = "device";
    private final static byte[] FAMILY = Bytes.toBytes("device");
    private final static byte[] PLAT_ID = Bytes.toBytes("p");
    private final static byte[] NOTES = Bytes.toBytes("n");
    private final static Table TABLE = HBaseHelper.getTable(TABLE_NAME);

    @Override
    public boolean bindDevice(String platformId, String ipcID, String notes) {
        if (HBaseUtil.strIsRight(ipcID) && HBaseUtil.strIsRight(platformId)) {
            try {
                Put put = new Put(Bytes.toBytes(ipcID));
                put.addColumn(FAMILY, PLAT_ID, Bytes.toBytes(platformId));
                put.addColumn(FAMILY, NOTES, Bytes.toBytes(notes));
                TABLE.put(put);
                LOG.info("Put data[" + ipcID + ", " + platformId + "] successful");
                return true;
            } catch (Exception e) {
                LOG.error("Current bind is failed!");
                return false;
            }
        } else {
            LOG.error("Please check the arguments!");
            return false;
        }
    }

    @Override
    public boolean unbindDevice(String platformId, String ipcID) {
        if (HBaseUtil.strIsRight(platformId) && HBaseUtil.strIsRight(ipcID)) {
            try {
                Delete delete = new Delete(Bytes.toBytes(ipcID));
                delete.addColumn(FAMILY, PLAT_ID);
                TABLE.delete(delete);
                LOG.info("Unbind device:" + ipcID + " and " + platformId + " successful");
                return true;
            } catch (Exception e) {
                LOG.error("Current unbind is failed!");
                return false;
            }
        } else {
            LOG.error("Please check the arguments!");
            return false;
        }
    }

    @Override
    public boolean renameNotes(String notes, String ipcID) {
        if (HBaseUtil.strIsRight(ipcID)) {
            try {
                Put put = new Put(Bytes.toBytes(ipcID));
                put.addColumn(FAMILY, NOTES, Bytes.toBytes(notes));
                TABLE.put(put);
                LOG.info("Rename notes");
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

    @Override
    public void closeTable() {
        HBaseUtil.closTable(TABLE);
    }
}
