package com.hzgc.streaming.alarm;


import java.io.Serializable;

/**
 * 识别告警推送信息类（刘善彬）
 */
public class RecognizeAlarmMessage implements Serializable {
    /**
     * 告警类型
     */
    private String alarmType;

    /**
     * 动态抓取人脸的设备id
     */
    private String dynamicDeviceID;

    /**
     * 动态抓取人脸id
     */
    private String dynamicID;

    /**
     * 静态信息库的比对结果数组
     */
    private Item[] items;

    /**构造函数**/
    public RecognizeAlarmMessage(String alarmType, String dynamicDeviceID, String dynamicID, Item[] items) {
        this.alarmType = alarmType;
        this.dynamicDeviceID = dynamicDeviceID;
        this.dynamicID = dynamicID;
        this.items = items;
    }
    public RecognizeAlarmMessage() {
    }

    /**Getter and Setter**/
    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getDynamicDeviceID() {
        return dynamicDeviceID;
    }

    public void setDynamicDeviceID(String dynamicDeviceID) {
        this.dynamicDeviceID = dynamicDeviceID;
    }

    public String getDynamicID() {
        return dynamicID;
    }

    public void setDynamicID(String dynamicID) {
        this.dynamicID = dynamicID;
    }

    public Item[] getItems() {
        return items;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }
}
