package com.hzgc.hbase.dynamicrepo;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * 动态库表属性
 */
public class DynamicTable {
    /**
     * person表
     */
    public static final String TABLE_PERSON = "person";
    /**
     * person表列簇
     */
    public static final byte[] PERSON_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 图片
     */
    public static final byte[] PERSON_COLUMN_IMGE = Bytes.toBytes("p");
    /**
     * 描述信息
     */
    public static final byte[] PERSON_COLUMN_DESCRIBE = Bytes.toBytes("d");
    /**
     * 附加信息
     */
    public static final byte[] PERSON_COLUMN_EXTRA = Bytes.toBytes("e");
    /**
     * 设备ID
     */
    public static final byte[] PERSON_COLUMN_IPCID = Bytes.toBytes("f");
    /**
     * 时间戳
     */
    public static final byte[] PERSON_COLUMN_TIMESTAMP = Bytes.toBytes("t");

    /**
     * perFea表
     */
    public static final String TABLE_PERFEA = "perFea";
    /**
     * perFea表列簇
     */
    public static final byte[] PERFEA_COLUMNFAMILY = Bytes.toBytes("f");
    /**
     * 特征值
     */
    public static final byte[] PERFEA_COLUMN_FEA = Bytes.toBytes("fea");

    /**
     * car表
     */
    public static final String TABLE_CAR = "car";
    /**
     * car表列簇
     */
    public static final byte[] CAR_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 图片
     */
    public static final byte[] CAR_COLUMN_IMGE = Bytes.toBytes("p");

    /**
     * 描述信息
     */
    public static final byte[] CAR_COLUMN_DESCRIBE = Bytes.toBytes("d");
    /***
     * 附加信息
     */
    public static final byte[] CAR_COLUMN_EXTRA = Bytes.toBytes("e");
    /**
     * 设备ID
     */
    public static final byte[] CAR_COLUMN_IPCID = Bytes.toBytes("f");
    /**
     * 车牌号
     */
    public static final byte[] CAR_COLUMN_PLATENUM = Bytes.toBytes("n");
    /**
     * 时间戳
     */
    public static final byte[] CAR_COLUMN_TIMESTAMP = Bytes.toBytes("t");

    /**
     * carFea表
     */
    public static final String TABLE_CARFEA = "carFea";
    /**
     * carFea表列簇
     */
    public static final byte[] CARFEA_COLUMNFAMILY = Bytes.toBytes("f");
    /**
     * 特征值
     */
    public static final byte[] CARFEA_COLUMN_FEA = Bytes.toBytes("fea");
    /**
     * 车牌号
     */
    public static final byte[] CARFEA_COLUMN_PLATNUM = Bytes.toBytes("n");

    /**
     * upPerFea表
     */
    public static final String TABLE_UPPERFEA = "upPerFea";
    /**
     * upPerFea表列簇
     */
    public static final byte[] UPPERFEA_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 小图
     */
    public static final byte[] UPPERFEA_COLUMN_SMALLIMAGE = Bytes.toBytes("s");
    /**
     * 特征值
     */
    public static final byte[] UPPERFEA_COLUMN_FEA = Bytes.toBytes("f");

    /**
     * upCarFea表
     */
    public static final String TABLE_UPCARFEA = "upCarFea";
    /**
     * upCarFea表列簇
     */
    public static final byte[] UPCARFEA_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 小图
     */
    public static final byte[] UPCARFEA_COLUMN_SMALLIMAGE = Bytes.toBytes("s");
    /**
     * 特征值
     */
    public static final byte[] UPCARFEA_COLUMN_FEA = Bytes.toBytes("f");
    /**
     * 车牌号
     */
    public static final byte[] UPCARFEA_COLUMN_PLATENUM = Bytes.toBytes("p");

    /**
     * searchRes表
     */
    public static final String TABLE_SEARCHRES = "searchRes";
    /**
     * searchRes表列簇
     */
    public static final byte[] SEARCHRES_COLUMNFAMILY = Bytes.toBytes("i");
    /**
     * 查询图片ID
     */
    public static final byte[] SEARCHRES_COLUMN_SEARCHIMAGEID = Bytes.toBytes("q");
    /**
     * 返回图片ID
     */
    public static final byte[] SEARCHRES_COLUMN_RESIMAGEID = Bytes.toBytes("r");
    /**
     * 相似度
     */
    public static final byte[] SEARCHRES_COLUMN_SIMILARITY = Bytes.toBytes("si");
}
