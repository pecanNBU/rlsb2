package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import org.apache.hadoop.hbase.client.Scan;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-8-4.
 */
public class filterByTimeTest {
    @Test
    public void filterByTimeTest() {
        List<String> rowKeyList = new ArrayList<>();
        FilterByRowkey filterByRowkey = new FilterByRowkey();
        SearchOption searchOption = new SearchOption();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            searchOption.setStartDate(df.parse("2017-08-01"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        searchOption.setSearchType(SearchType.PERSON);
        Scan scan = new Scan();
        //rowKeyList = filterByRowkey.filterByTime(searchOption,scan);
        System.out.println(rowKeyList);
    }

}
