package com.hzgc.hbase.device;

import java.util.List;

public interface DeviceUtil {
    /**
     * 获取设备绑定的平台ID（内）（赵喆）
     *
     * @param ipcID 设备的ipcID
     * @return 平台ID，长度为0则未绑定平台ID
     */
    public String getplatfromID(String ipcID);
    /**
     * 查看是否布控某种告警类型（内）（赵喆）
     *
     * @param ipcID 设备的ipcID
     * @param warnType 告警类型
     * 0：代表识别告警
     * 1：代表新增告警
     * 2：代表离线告警
     * @return 0为未绑定，1为绑定
     */
    public int isWarnTypeBinding(String ipcID, String warnType);

    /**
     * 获取某种告警类型包含的对象类型列表（内）（赵喆）
     *
     * @param ipcID 设备的ipcID
     * @param warnType
     * 0：代表识别告警
     * 1：代表新增告警
     * 2：代表离线告警
     * @return objectType列表
     */
    public List<String> getObjectTypeList(String ipcID, String warnType);
    /**
     *支持获取设备的阈值（内）（赵喆）
     *
     * @param ipcID 设备的ipcID
     * @param warnType
     * 0：代表识别告警
     * 1：代表新增告警
     * 2：代表离线告警
     * @return 若warnType为0或1，返回相似度阈值，如果warnType为2，则返回离线天数阈值
     */
    public Integer getThreshold(String ipcID, String warnType);
}
