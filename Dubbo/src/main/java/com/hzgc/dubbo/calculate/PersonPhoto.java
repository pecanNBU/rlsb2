package com.hzgc.dubbo.calculate;


public class PersonPhoto {

    /**
     * 动态人脸照片ID
     */
    protected String imageId;

    /**
     * 抓取照片的设备ID
     */
    protected String ipc;


    /**
     * 人脸大图数据
     */
    protected byte[] bigImage;

    /**
     * 人脸小图数据
     */
    protected byte[] smallImage;

    /**
     * 图片描述信息
     */
    protected String des;

    /**
     * 图片附加信息
     */
    protected String ex;

    /**
     * 属性1
     */
    protected String property1;
    /**
     * 属性n
     */
    protected String propertyn;


}
