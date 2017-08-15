package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * 动态图片定义
 */
public class CapturedPicture implements Serializable {

    /**
     * 图片 id (rowkey)用于获取图片
     */
    private String id;
    /**
     * 捕获照片的设备 id
     */
    private String ipcId;
    /**
     * 图片的描述信息
     */
    private String description;
    /**
     * 图片的相似度
     */
    private float similarity;
    /**
     * 图片的附加信息，扩展预留
     */
    private Map<String, Object> extend;
    /**
     * 图片数据
     */
    private byte[] smallImage;
    /**
     * 大图
     */
    private byte[] bigImage;
    /**
     * 时间戳
     */
    private long timeStamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIpcId() {
        return ipcId;
    }

    public void setIpcId(String ipcId) {
        this.ipcId = ipcId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Float similarity) {
        this.similarity = similarity;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }

    public byte[] getSmallImage() {
        return smallImage;
    }

    public void setSmallImage(byte[] smallImage) {
        this.smallImage = smallImage;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public byte[] getBigImage() {
        return bigImage;
    }

    public void setBigImage(byte[] bigImage) {
        this.bigImage = bigImage;
    }

    @Override
    public String toString() {
        return "CapturedPicture{" +
                "id='" + id + '\'' +
                ", ipcId='" + ipcId + '\'' +
                ", description='" + description + '\'' +
                ", similarity=" + similarity +
                ", extend=" + extend +
                ", smallImage=" + Arrays.toString(smallImage) +
                ", bigImage=" + Arrays.toString(bigImage) +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
