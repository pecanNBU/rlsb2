package com.hzgc.hbase.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.dubbo.dynamicrepo.SearchType;
import com.hzgc.hbase.util.HBaseHelper;
import com.hzgc.hbase.util.HBaseUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
     * 根据时间段过滤rowKey范围
     *
     * @param option 搜索选项
     * @param scan   scan对象
     * @return List<String> 符合条件的rowKey集合
     */
    public List<String> filterByTime(SearchOption option, Scan scan) {
        List<String> rowKeyList = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd");
        Table person = HBaseHelper.getTable(DynamicTable.TABLE_PERFEA);
        Table car = HBaseHelper.getTable(DynamicTable.TABLE_CARFEA);
        if (option.getSearchType() == SearchType.PERSON) {
            if (option.getStartDate() != null && option.getEndDate() != null) {
                String startDate = df.format(option.getStartDate()).replace("-", "");
                String endDate = df.format(option.getEndDate()).replace("-", "");
                filterByDate(rowKeyList, startDate, endDate, scan, person);

            } else if (option.getStartDate() != null && option.getEndDate() == null) {
                String startDate = df.format(option.getStartDate()).replace("-", "");
                String endDate = df.format(new Date()).replace("-", "");
                filterByDate(rowKeyList, startDate, endDate, scan, person);

            } else if (option.getStartDate() == null && option.getEndDate() != null) {
                String startDate = "1970-01-01".replace("-", "");
                String endDate = df.format(option.getEndDate()).replace("-", "");
                filterByDate(rowKeyList, startDate, endDate, scan, person);

            } else if (option.getStartDate() == null && option.getEndDate() == null) {
                LOG.error("Date is null,used method FilterByRowkey.filterByTime");
            }
        } else if (option.getSearchType() == SearchType.CAR) {
            if (option.getStartDate() != null && option.getEndDate() != null) {
                String startDate = df.format(option.getStartDate()).replace("-", "");
                String endDate = df.format(option.getEndDate()).replace("-", "");
                filterByDate(rowKeyList, startDate, endDate, scan, car);

            } else if (option.getStartDate() != null && option.getEndDate() == null) {
                String startDate = df.format(option.getStartDate()).replace("-", "");
                String endDate = df.format(new Date()).replace("-", "");
                filterByDate(rowKeyList, startDate, endDate, scan, car);

            } else if (option.getStartDate() == null && option.getEndDate() != null) {
                String startDate = "1970-01-01".replace("-", "");
                String endDate = df.format(option.getEndDate()).replace("-", "");
                filterByDate(rowKeyList, startDate, endDate, scan, car);

            } else if (option.getStartDate() == null && option.getEndDate() == null) {
                LOG.error("Date is null,used method FilterByRowkey.filterByTime");
            }
        } else {
            LOG.error("param SearchType is empty,used method FilterByRowkey.filterByTime");
        }

        return rowKeyList;
    }

    public List<String> filterByDate(List<String> rowKeyList, String startDate, String endDate, Scan scan, Table table) {
        int start = Integer.parseInt(startDate);
        int end = Integer.parseInt(endDate);

        List<Filter> filterList = new ArrayList<>();
        Filter startFilter = new RowFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL, new RegexStringComparator(".*" + start + ".*"));
        filterList.add(startFilter);
        Filter endFilter = new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL, new RegexStringComparator(".*" + end + "_" + ".*"));
        filterList.add(endFilter);
        FilterList filter = new FilterList(FilterList.Operator.MUST_PASS_ALL, filterList);

        scan.setFilter(filter);
        try {
            ResultScanner scanner = table.getScanner(scan);
            for (Result result : scanner) {
                byte[] bytes = result.getRow();
                String string = Bytes.toString(bytes);
                rowKeyList.add(string);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("filter rowkey by Date failed! used method FilterByRowkey.filterByDate.");
        } finally {
            HBaseUtil.closTable(table);
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
