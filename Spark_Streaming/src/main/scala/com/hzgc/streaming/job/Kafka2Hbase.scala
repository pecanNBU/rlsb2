package com.hzgc.streaming.job

import com.hzgc.jni.{FaceFunction, NativeFunction}
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Durations, StreamingContext}

/**
 *  提取动态人脸照片的特征值存入hbase(模拟生成静态信息库)
 */
object Kafka2Hbase {
  def main(args: Array[String]) {
    System.setProperty("hadoop.home.dir","D:\\software\\hadoop-2.7.1");
    //spark streaming 初始化一些信息
    val sc = new SparkConf().setAppName("ToHbase").setMaster("local[2]")
    val scc = new StreamingContext(sc,Durations.seconds(3))
    //配置kafka主题和链接信息
    val topics=Set("testPhoto")
    val kafkaParams=Map(
      "metadata.broker.list" ->
        "172.18.18.107:21005,172.18.18.108:21005,172.18.18.109:21005",
      "group.id" -> "ptoto_id1"
    )
    //初始化算法，因为算法存在加密狗，所以在调用算法之前需要初始化一些加密信息

    NativeFunction.init()
    val result = KafkaUtils.
      createDirectStream[String,Array[Byte],StringDecoder,DefaultDecoder](scc,kafkaParams,topics).
      map(photo => (photo._1,FaceFunction.featureExtract(photo._2))).filter(_._2!=null)
   // result.map(a =>(a._1,a._2.toList)).print()

    result.foreachRDD(rdd =>{
      rdd.foreachPartition(par =>{
        val hconf = HBaseConfiguration.create()
        hconf.set("hbase.zookeeper.property.clientPort","24002")
        hconf.set("hbase.zookeeper.quorum","s107,s108,s109")
        val conn = ConnectionFactory.createConnection(hconf)
        val admin = conn.getAdmin
        //表名
        val stable = TableName.valueOf("TEST_STATIC_STORE")
        //获取表
        val table = conn.getTable(stable)
        par.foreach(photoFeature =>{
          val key = photoFeature._1
          val value = photoFeature._2
          println(value.toList)
          val v2 = FaceFunction.floatArray2string(value)
          val put = new Put(key.getBytes)
          //将数据存储hbase时，主要String和字节数组转换时的编码格式
          put.addColumn("info".getBytes,"photo".getBytes,v2.getBytes("ISO-8859-1"))
          table.put(put)
        })
        conn.close()
        table.close()
      })
    })

    scc.start()
    scc.awaitTermination()








  }



}
