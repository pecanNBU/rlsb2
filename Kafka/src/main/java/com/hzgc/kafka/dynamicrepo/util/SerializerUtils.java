package com.hzgc.kafka.dynamicrepo.util;

import com.hzgc.dubbo.dynamicrepo.SearchOption;

import java.io.*;

/**
 * Created by Administrator on 2017-7-26.
 */

public class SerializerUtils {

    private SerializerUtils() {
    }

    /**
     * 对象转字节数组
     *
     * @param option
     * @return byte[]
     */
    public static byte[] ObjectToBytes(SearchOption option) {
        byte[] bytes = null;
        ByteArrayOutputStream bo = null;
        ObjectOutputStream oo = null;
        try {
            bo = new ByteArrayOutputStream();
            oo = new ObjectOutputStream(bo);
            oo.writeObject(option);
            bytes = bo.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bo != null) {
                    bo.close();
                }
                if (oo != null) {
                    oo.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * 字节数组转对象
     *
     * @param bytes
     * @return
     */
    public static SearchOption BytesToObject(byte[] bytes) {
        SearchOption option = null;
        ByteArrayInputStream bi = null;
        ObjectInputStream oi = null;
        try {
            bi = new ByteArrayInputStream(bytes);
            oi = new ObjectInputStream(bi);
            option = (SearchOption) oi.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bi != null) {
                    bi.close();
                }
                if (oi != null) {
                    oi.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return option;
    }
}

