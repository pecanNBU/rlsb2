package com.hzgc.hbase.dynamicrepo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-8-12.
 */
public class ListTest {
    public static void main(String args[]) {
        List<String> list = new ArrayList<>();
        list.add(null);
        list.add("1");
        list.add("2");
        System.out.println(list.size());
    }
}
