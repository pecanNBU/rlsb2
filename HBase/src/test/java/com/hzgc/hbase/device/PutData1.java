package com.hzgc.hbase.device;

import com.hzgc.dubbo.device.WarnRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-8-15.
 */
public class PutData1 {
    public static List<WarnRule> setwarnRuleData(){
        List<WarnRule> rules = new ArrayList<WarnRule>();
        WarnRule warnRule9 = new WarnRule();
        warnRule9.setCode(1);
        warnRule9.setDayThreshold(4);
        warnRule9.setThreshold(89);
        warnRule9.setObjectType("student");
        rules.add(warnRule9);
        WarnRule warnRule14 = new WarnRule();
        warnRule14.setCode(2);
        warnRule14.setDayThreshold(9);
        warnRule14.setThreshold(100);
        warnRule14.setObjectType("businessman");
        rules.add(warnRule14);
        WarnRule warnRule5 = new WarnRule();
        warnRule5.setCode(0);
        warnRule5.setDayThreshold(2);
        warnRule5.setThreshold(56);
        warnRule5.setObjectType("thief");
        rules.add(warnRule5);
        return rules;
    }
    public static List<String> setIpcId() {
        List<String> list = new ArrayList<>();
        list.add("17130NCY0HZ0004-g");
        return list;
    }
}
