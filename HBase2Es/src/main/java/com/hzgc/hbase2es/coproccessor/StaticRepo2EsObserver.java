package com.hzgc.hbase2es.coproccessor;

import com.hzgc.hbase2es.es.ElasticSearchBulkOperator;
import com.hzgc.hbase2es.util.EsClientUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class StaticRepo2EsObserver extends EsObserver {
    private static Logger LOG = Logger.getLogger(StaticRepo2EsObserver.class);

    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> env,
                        Put put,
                        WALEdit edit,
                        Durability durability) throws IOException {
        String indexId = new String(put.getRow());   // 通过Put 对象获取rowkey 作为id，
        NavigableMap<byte[], List<Cell>> familyMap = put.getFamilyCellMap();
        Map<String, Object> infoJson = new HashMap<>();
        for (Map.Entry<byte[], List<Cell>> entry : familyMap.entrySet()) {
            for (Cell cell : entry.getValue()) {
                String key = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                if (!"photo".equals(key)){
                    infoJson.put(key, value);
                    LOG.info("Put data into es {key:" + key + ", value:" + value + "}");
                }
            }
        }
        ElasticSearchBulkOperator.addUpdateBuilderToBulk(EsClientUtils.client.prepareUpdate(indexName,
                typeName, indexId).setDocAsUpsert(true).setDoc(infoJson));
    }

    @Override
    public void postDelete(ObserverContext<RegionCoprocessorEnvironment> env,
                           Delete delete,
                           WALEdit edit,
                           Durability durability) throws IOException {
        String indexId = new String(delete.getRow());
        LOG.info("Delete data from es {rowkey:" + indexId + "}");
        try {
            ElasticSearchBulkOperator.addDeleteBuilderToBulk(
                    EsClientUtils.client.prepareDelete(indexName, typeName, indexId));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
