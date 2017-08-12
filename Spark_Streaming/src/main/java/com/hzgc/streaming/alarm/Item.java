package com.hzgc.streaming.alarm;

import java.io.Serializable;

/**
 * 识别告警静态库比对结果类。（刘善彬）
 */
public class Item implements Serializable {
    /**
     * 静态库id
     */
    private String staticID;

    /**
     * 对象类型
     */
    private String staticObjectType;

    /**
     * 相似度
     */
    private String similarity;

    /**构造函数**/
    public Item(String staticID, String staticObjectType, String similarity) {
        this.staticID = staticID;
        this.staticObjectType = staticObjectType;
        this.similarity = similarity;
    }
    public Item() {
    }

    /**Getter and Setter**/
    public String getStaticID() {
        return staticID;
    }

    public void setStaticID(String staticID) {
        this.staticID = staticID;
    }

    public String getStaticObjectType() {
        return staticObjectType;
    }

    public void setStaticObjectType(String staticObjectType) {
        this.staticObjectType = staticObjectType;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }
}
