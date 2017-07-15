package com.hzgc.dubbo.device;

public interface DeviceService {
    /**
     * 绑定设备到平台
     *
     * @param platformId 平台 id
     * @param ipcID   设备 ipcID
     * @param notes      备注
     * @return 是否绑定成功
     */
    boolean bindDevice(String platformId, String ipcID, String notes);

    /**
     * 解除设备与平台的绑定关系
     *
     * @param platformId 平台 id
     * @param ipcID   设备 ipcID
     * @return 是否解除绑定成功
     */
    boolean unbindDevice(String platformId, String ipcID);

    /**
     * 修改备注
     *
     * @param notes 备注信息
     * @param ipcID    设备在平台上的 ipcID
     * @return 是否重命名成功
     */
    boolean renameNotes(String notes, String ipcID);
}
