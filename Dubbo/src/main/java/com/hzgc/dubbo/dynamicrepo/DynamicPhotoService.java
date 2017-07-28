package com.hzgc.dubbo.dynamicrepo;


public interface DynamicPhotoService {
    /**
     * 获取动态图片信息（外）to 陆春峰（刘善彬）
     * @param DynamicPhotoID 动态图片ID
     * @return 动态图片的具体信息（大图、小图等等）
     */
    public DynamicObject getDynamicPhotoInfo(String DynamicPhotoID);

    /**
     * 将rowKey、特征值插入人脸/车辆特征库 （内）（刘思阳）
     *
     * @param type    图片类型（人/车）
     * @param rowKey  图片id（rowkey）
     * @param feature 特征值
     * @return boolean 是否插入成功
     */
    public boolean insertePictureFeature(PictureType type, String rowKey, float[] feature);
}
