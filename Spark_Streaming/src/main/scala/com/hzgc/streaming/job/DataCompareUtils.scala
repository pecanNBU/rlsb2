package com.hzgc.streaming.job

import java.util.{Date, Properties}

import com.hzgc.hbase.util.HBaseHelper
import com.hzgc.streaming.util.PropertiesUtils
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils

object DataCompareUtils {

  /**
   * SparkStreaming从kafka集群读取photo数据
   * @param ssc streaming上下文环境
   * @return 返回InputDStream
   */
  def getKafkaDynamicPhoto(ssc :StreamingContext): InputDStream[Tuple2[String, Array[Byte]]] ={
    val topics = Set(PropertiesUtils.getPropertiesValue("kafka.topic.name"))
    val brokers = PropertiesUtils.getPropertiesValue("kafka.metadata.broker.list")
    val groupId = PropertiesUtils.getPropertiesValue("kafka.group.id")
    val kafkaParams = Map(
    "metadata.broker.list" -> brokers
    ,"group.id" ->"1"
    )
    val kafkainput = KafkaUtils.createDirectStream[String, Array[Byte],StringDecoder, DefaultDecoder](ssc, kafkaParams,topics)
    kafkainput
  }

  /**
   * 读取静态信息库的数据
   * @param sc spark上下文环境
   * @return RDD[(ImmutableBytesWritable,Result)]
   */
  def getHbaseStaticPhoto(sc:SparkContext): RDD[(ImmutableBytesWritable, Result)] ={
    val hbaseConf = HBaseConfiguration.create()
    HBaseHelper.getHBaseConfiguration
    val tableName = PropertiesUtils.getPropertiesValue("hbase.table.static.name")
    hbaseConf.set("hbase.zookeeper.quorum",PropertiesUtils.getPropertiesValue("hbase.zookeeper.quorum"))
    hbaseConf.set("hbase.zookeeper.property.clientPort",PropertiesUtils.getPropertiesValue("hbase.zookeeper.property.clientPort"))
    hbaseConf.set(TableInputFormat.INPUT_TABLE,tableName)
    println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
    val rdd = sc.newAPIHadoopRDD(hbaseConf,classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])
   rdd
  }

  def write2Kafka(str:String,platId:String): Unit ={
    val props = new Properties()
    props.put("metadata.broker.list",PropertiesUtils.getPropertiesValue("kafka.metadata.broker.list"))  // broker 如果有多个,中间使用逗号分隔
    props.put("serializer.class","kafka.serializer.StringEncoder")
    props.put("request.required.acks","1")
    val config = new ProducerConfig(props)
    val producer = new Producer[String,String](config)
    val runtime = new Date().toString
    val topic=platId
    val data = new KeyedMessage[String,String](topic,str)
    producer.send(data)
    producer.close()
    println("警推送至MQ")

  }
}
