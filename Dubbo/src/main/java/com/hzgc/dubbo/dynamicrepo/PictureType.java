package com.hzgc.dubbo.dynamicrepo;

import java.io.Serializable;

/**
 * 图片类型
 */
public enum PictureType implements Serializable {

    /**
     * 人脸大图小图
     */
    PERSON(0),
    /**
     * 车辆大图小图
     */
    CAR(1),
    /**
     * 人脸小图
     */
    SMALL_PERSON(2),
    /**
     * 车辆小图
     */
    SMALL_CAR(3),
    /**
     * 人脸大图
     */
    BIG_PERSON(4),
    /**
     * 车辆大图
     */
    BIG_CAR(5),
    /**
     * 人脸无图信息
     */
    MESSAGE_PERSON(6),
    /**
     * 车辆无图信息
     */
    MESSAGE_CAR(7);


    private int type;

    PictureType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 根据类型值获取 PictureType,默认为PERSON
     *
     * @param type 类型值
     * @return PictureType
     */
    public static PictureType get(int type) {
        for (PictureType pictureType : PictureType.values()) {
            if (type == pictureType.getType()) {
                return pictureType;
            }
        }
        return PictureType.PERSON;
    }

    @Override
    public String toString() {
        return "PictureType{" +
                "type=" + type +
                '}';
    }
}
