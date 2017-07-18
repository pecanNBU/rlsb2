package com.hzgc.dubbo.photo;

/**
 * 因为算法不定，故目前属性不定
 */
public class PersonPhoto {
    /**
     * 动态人脸照片ID
     */
    String imageId;

    /**
     * 抓取照片的设备ID
     */
    String ipc;


    /**
     * 人脸大图数据
     */
    byte[] bigImage;

    /**
     * 人脸小图数据
     */
    byte[] smallImage;

    /**
     * 图片描述信息
     */
    String des;

    /**
     * 图片附加信息
     */
    String ex;

    /**
     * 属性1
     */
    String property1;
    /**
     * 属性n
     */
    String propertyn;


}
