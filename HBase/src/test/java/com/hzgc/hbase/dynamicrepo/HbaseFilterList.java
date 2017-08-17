package com.hzgc.hbase.dynamicrepo;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017-8-3.
 */

public class HbaseFilterList {
    private static List<Filter> filters;

    public static FilterList getFilterList() {
        filters = new ArrayList<Filter>();
        Filter filter1 = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(".*"));
        Filter filter2 = new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator("aaa"));
        filters.add(filter1);
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL, filters);

        Scan scan = new Scan();
        RegexStringComparator comp = new RegexStringComparator("you."); // 以 you 开头的字符串
        SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("family"), Bytes.toBytes("qualifier"), CompareFilter.CompareOp.GREATER_OR_EQUAL, comp);
        scan.setFilter(filter);
        return filterList;
    }
}
