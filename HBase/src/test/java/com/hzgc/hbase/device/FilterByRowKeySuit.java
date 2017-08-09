package com.hzgc.hbase.device;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.hbase.dynamicrepo.FilterByRowkey;
import org.junit.Test;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017-8-9.
 */
public class FilterByRowKeySuit {
    @Test
    public void testGetRowKey(){
        SearchOption option = new SearchOption();
        List<String> deviecId = new ArrayList<>();
        deviecId.add("17130NCY0HZ0002");
        deviecId.add("17130NCY0HZ0003");
        option.setDeviceIds(deviecId);
        option.setSearchType(SearchType.PERSON);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
           date = simpleDateFormat.parse("2017-04-23 00:02:49");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        option.setStartDate(date);
        Date date1 = new Date();
        try {
            date1 = simpleDateFormat.parse("2017-04-25 00:19:58");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        option.setEndDate(date1);
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        List<String> rowKey = filterByRowkey.getRowKey(option);
        System.out.println(rowKey);
    }
}
