package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class FilterByRowkey {
    private static Logger LOG = Logger.getLogger(FilterByRowkey.class);

    /**
     * 根据设备id过滤rowKey范围
     *
     * @param option 搜索选项
     * @param scan   scan对象
     * @return List<String> 符合条件的rowKey集合
     */
    public List<String> filterByDeviceId(SearchOption option, Scan scan) {
        SearchType searchType = option.getSearchType();
        List<String> deviceIdList = option.getDeviceIds();
        List<String> rowKeyList = null;

        if (searchType.equals(SearchType.PERSON)) {
            Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERSON);
            for (String device : deviceIdList) {
                Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(device + ".*"));
                scan.setFilter(filter);
                try {
                    ResultScanner scanner = person.getScanner(scan);
                    for (Result result : scanner) {
                        byte[] bytes = result.getRow();
                        String string = Bytes.toString(bytes);
                        rowKeyList.add(string);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("filter rowkey by deciceId from table_person failed! used method FilterByRowkey.filterByDeviceId.");
                } finally {
                    HBaseUtil.closTable(person);
                }
            }
        } else if (searchType.equals(SearchType.CAR)) {
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            for (String device : deviceIdList) {
                Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(device + ".*"));
                scan.setFilter(filter);
                try {
                    ResultScanner scanner = car.getScanner(scan);
                    for (Result result : scanner) {
                        byte[] bytes = result.getRow();
                        String string = Bytes.toString(bytes);
                        rowKeyList.add(string);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("filter rowkey by deciceId from table_car failed! used method FilterByRowkey.filterByDeviceId.");
                } finally {
                    HBaseUtil.closTable(car);
                }
            }
        }
        return rowKeyList;
    }

    /**
     * 根据车牌号过滤rowKey范围
     *
     * @param option 搜索选项
     * @param scan   scan对象
     * @return List<String> 符合条件的rowKey集合
     */
    public List<String> filterByPlateNumber(SearchOption option, Scan scan) {
        List<String> rowKeyList = new ArrayList<>();

        if (option.getPlateNumber() == null) {
            String plateNumber = option.getPlateNumber();
            Table car = HBaseHelper.getTable(DynamicTable.TABLE_CAR);
            try {
                ResultScanner scanner = car.getScanner(scan);
                Map<String, String> map = new HashMap<>();
                for (Result result : scanner) {
                    byte[] rowKey = result.getRow();
                    String rowKeyStr = Bytes.toString(rowKey);
                    byte[] plateNum = result.getValue(DynamicTable.CAR_COLUMNFAMILY, DynamicTable.CAR_COLUMN_PLATENUM);
                    String plateNumStr = Bytes.toString(plateNum);
                    map.put(rowKeyStr, plateNumStr);
                }
                if (!map.isEmpty()) {
                    Iterator<String> iter = map.keySet().iterator();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        String value = map.get(key);
                        if (value.contains(plateNumber)) {
                            rowKeyList.add(key);
                        }
                    }
                } else {
                    LOG.info("map is empty,used method FilterByRowkey.filterByPlateNumber.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                HBaseUtil.closTable(car);
            }
        } else {
            LOG.error("param is empty,used method FilterByRowkey.filterByPlateNumber.");
        }
        return rowKeyList;
    }

    /**
     * 根据小图rowKey获取小图特征值 （内）（刘思阳）
     *
     * @param imageId 小图rowKey
     * @param type    人/车
     * @return byte[] 小图特征值
     */
    public byte[] getFeature(String imageId, SearchType type) {
        byte[] feature = null;
        if (null != imageId) {
            Table personTable = HBaseHelper.getTable(DynamicTable.TABLE_PERFEA);
            Table carTable = HBaseHelper.getTable(DynamicTable.TABLE_CARFEA);
            Get get = new Get(Bytes.toBytes(imageId));
            if (type == SearchType.PERSON) {
                try {
                    Result result = personTable.get(get);
                    feature = result.getValue(DynamicTable.PERFEA_COLUMNFAMILY, DynamicTable.PERFEA_COLUMN_FEA);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get feature by imageId from table_person failed! used method FilterByRowkey.getSmallImage");
                } finally {
                    HBaseUtil.closTable(personTable);
                }
            } else if (type == SearchType.CAR) {
                try {
                    Result result = carTable.get(get);
                    feature = result.getValue(DynamicTable.CARFEA_COLUMNFAMILY, DynamicTable.CARFEA_COLUMN_FEA);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.error("get feature by imageId from table_car failed! used method FilterByRowkey.getSmallImage");
                } finally {
                    HBaseUtil.closTable(personTable);
                }
            }
        } else {
            LOG.error("method FilterByRowkey.getFeature param is empty");
        }
        return feature;
    }
}
