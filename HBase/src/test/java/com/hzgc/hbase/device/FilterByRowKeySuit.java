package com.hzgc.hbase.device;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.dubbo.dynamicrepo.TimeInterval;
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
        List<TimeInterval> list = new ArrayList<>();
        TimeInterval timeInterval = new TimeInterval();
        timeInterval.setStart(1739);
        timeInterval.setEnd(1740);
        list.add(timeInterval);
        TimeInterval timeInterval1 = new TimeInterval();

        timeInterval1.setStart(1742);
        timeInterval1.setEnd(1743);
        list.add(timeInterval1);
        option.setIntervals(list);
        option.setDeviceIds(deviecId);
        option.setSearchType(SearchType.PERSON);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
           date = simpleDateFormat.parse("2017-08-13 17:41:24");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        option.setStartDate(date);
        Date date1 = new Date();
        try {
            date1 = simpleDateFormat.parse("2017-08-15 17:45:17");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        option.setEndDate(date1);
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        List<String> rowKey = filterByRowkey.getRowKey(option);
        System.out.println(rowKey.size() + " " + rowKey);
    }
}
