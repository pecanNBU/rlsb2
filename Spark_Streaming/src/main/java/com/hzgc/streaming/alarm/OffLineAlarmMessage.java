package com.hzgc.streaming.alarm;

import java.io.Serializable;

/**
 *
 * 离线告警信息类（刘善彬）
 */
public class OffLineAlarmMessage implements Serializable {

    /**
     * 静态信息库id
     */
    private String staticID;

    /**
     * 告警类型
     */
    private String alarmType;

    /**
     * 识别更新时间
     */
    private String updateTime;

    /**
     * 离线天数
     */
    private Double offLineDays;

    /**
     * 对象类型
     */
    private String objectType;

    /**构造函数**/
    public OffLineAlarmMessage(String staticID, String alarmType, String updateTime, Double offLineDays,String objectType) {
        this.staticID = staticID;
        this.alarmType = alarmType;
        this.updateTime = updateTime;
        this.offLineDays = offLineDays;
        this.objectType=objectType;
    }
    public OffLineAlarmMessage() {
    }

    /**Getter and Setter**/
    public String getStaticID() {
        return staticID;
    }

    public void setStaticID(String staticID) {
        this.staticID = staticID;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public Double getOffLineDays() {
        return offLineDays;
    }

    public void setOffLineDays(Double offLineDays) {
        this.offLineDays = offLineDays;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }
}
