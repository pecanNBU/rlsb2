package com.hzgc.streaming.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 *  转换工具类（刘善彬）（内 to 刘善彬）
 */
public class StreamingUtils implements Serializable {
    /**
     * 字节数组转化为字符串
     * @param b
     * @return
     * @throws Exception
     */
    public static String byteArray2string(byte [] b)throws Exception {
        String str = new String(b,"ISO-8859-1");
        return str;
    }

    public static Double timeTransition(String updateTime){
        Date d = new Date(System.currentTimeMillis());
        long currentTime = d.getTime();
        long updateT = Long.valueOf(updateTime);
        //一个小时的毫秒数
        long t =1000*60*60;
        long interval = currentTime - updateT;
        return interval  * 1.0/t;
    }

    public static Integer getSimilarity(Map<String, Integer> map) {
        for (String key : map.keySet()) {
            if (map.get(key) != null) {
                return map.get(key);
            }
        }
        return null;
    }

    public static List<String> getTypeList(Map<String, Integer> map) {
        List<String> list = new ArrayList<>();
        list.addAll(map.keySet());
        return list;
    }
}
