package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.WarnRule;

import java.util.ArrayList;
import java.util.List;

public class TestData {
    protected static List<WarnRule> getDataList() {
        List<WarnRule> rule = new ArrayList<>();
        WarnRule w1 = new WarnRule();
        w1.setThreshold(12);
        w1.setDayThreshold(3);
        w1.setCode(0);
        w1.setObjectType("a1");
        rule.add(w1);
        WarnRule w2 = new WarnRule();
        w2.setThreshold(12);
        w2.setDayThreshold(3);
        w2.setCode(0);
        w2.setObjectType("b1");
        rule.add(w2);
        WarnRule w3 = new WarnRule();
        w3.setThreshold(12);
        w3.setDayThreshold(3);
        w3.setCode(0);
        w3.setObjectType("c1");
        rule.add(w3);
        WarnRule w4 = new WarnRule();
        w4.setThreshold(12);
        w4.setDayThreshold(3);
        w4.setCode(1);
        w4.setObjectType("b1");
        rule.add(w4);
        WarnRule w5 = new WarnRule();
        w5.setThreshold(12);
        w5.setDayThreshold(3);
        w5.setCode(1);
        w5.setObjectType("c1");
        rule.add(w5);
        WarnRule w6 = new WarnRule();
        w6.setThreshold(12);
        w6.setDayThreshold(3);
        w6.setCode(1);
        w6.setObjectType("d1");
        rule.add(w6);
        WarnRule w7 = new WarnRule();
        w7.setThreshold(12);
        w7.setDayThreshold(3);
        w7.setCode(2);
        w7.setObjectType("c1");
        rule.add(w7);
        WarnRule w8 = new WarnRule();
        w8.setThreshold(12);
        w8.setDayThreshold(3);
        w8.setCode(2);
        w8.setObjectType("d1");
        rule.add(w8);
        WarnRule w9 = new WarnRule();
        w9.setThreshold(12);
        w9.setDayThreshold(3);
        w9.setCode(2);
        w9.setObjectType("e1");
        rule.add(w9);
        return rule;
    }
}
