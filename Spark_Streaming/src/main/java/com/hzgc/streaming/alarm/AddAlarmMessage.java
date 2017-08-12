package com.hzgc.streaming.alarm;

import java.io.Serializable;

/**
 *
 * 新增告警推送信息类（刘善彬）
 */
public class AddAlarmMessage implements Serializable {
    /**
     * 告警类型
     */

    private String alarmType;

    /**
     * 动态抓取人脸id
     */
    private String dynamicID;

    /**
     * 抓取人脸的设备id
     */
    private String dynamicDeviceID;

    /**
     * 类构造函数
     * @param alarmType
     * @param dynamicID
     * @param dynamicDeviceID
     */
    public AddAlarmMessage(String alarmType, String dynamicID, String dynamicDeviceID) {
        this.alarmType = alarmType;
        this.dynamicID = dynamicID;
        this.dynamicDeviceID = dynamicDeviceID;
    }
    public AddAlarmMessage() {
    }

    /**Getter and Setter**/
    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getDynamicID() {
        return dynamicID;
    }

    public void setDynamicID(String dynamicID) {
        this.dynamicID = dynamicID;
    }

    public String getDynamicDeviceID() {
        return dynamicDeviceID;
    }

    public void setDynamicDeviceID(String dynamicDeviceID) {
        this.dynamicDeviceID = dynamicDeviceID;
    }
}
