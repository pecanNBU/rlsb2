package com.hzgc.streaming.job

import com.hzgc.ftpserver.util.FtpUtil
import com.hzgc.hbase.device.{DeviceTable, DeviceUtilImpl}
import com.hzgc.hbase.staticrepo.{ElasticSearchHelper, ObjectInfoInnerHandlerImpl}
import com.hzgc.jni.FaceFunction
import com.hzgc.streaming.util.StreamingUtils
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.JavaConverters


object Test {
  val esClient = new ObjectInfoInnerHandlerImpl()
  val warnClient = new DeviceUtilImpl()
  val sparkConf: SparkConf = new SparkConf().setAppName("FaceRecognizeAlarmJob").setMaster("local[7]")
  val ssc = new StreamingContext(sparkConf, Seconds(3))
  val separator = "ZHONGXIAN"
  val interrupt = "SHUXIAN"
  val broadcastSeparator: Broadcast[String] = ssc.sparkContext.broadcast(separator)
  val broadcastInterrupt: Broadcast[String] = ssc.sparkContext.broadcast(interrupt)

  case class feature( dynamicRK: String,
                      ipcID: String,
                      dynamicFeature: String,
                      threshold: String,
                      staticRK: String,
                      objType: String,
                      staticFeature: String)

  def main(args: Array[String]): Unit = {
    val args = Array("172.18.18.101:21005", "testface")
    // Usage
    if (args.length < 2) {
      System.err.println(
        s"""
           |Usage: DirectKafkaWordCount <brokers> <topics>
           |  <brokers> is a list of one or more Kafka brokers
           |  <topics> is a list of one or more kafka topics to consume from
           |
        """.stripMargin)
      System.exit(1)
    }
    // 映射 brokers和topic
    val Array(brokers, topics) = args
    // 生成topic列表
    val topicSet = topics.split(",").toSet
    //配置Kafka参数
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers)
    //获取Kafka DStream
    val kafkaDstream = KafkaUtils.
      createDirectStream[String, String, StringDecoder, FeatureDecoder](ssc, kafkaParams, topicSet).repartition(1)
    //对Dstream进行转换，将读取到的消息与静态库进行匹配查询，然后关联
    val kafkaFeature = kafkaDstream.map(message => {
      //通过message的key获取ipcID
      val ipcID = FtpUtil.getRowKeyMessage(message._1).get("ipcID")
//      println("ipcID:" + ipcID)
      //使用StringBuilder来组合一条Kafka消息和底库中符合条件的数据
      val newMessage = new StringBuilder
      //通过设备ID获取平台ID
      val platID = warnClient.getplatfromID(ipcID)
//      println("platID:" + platID)
      //通过设备ID获取此设备绑定的布控预案
      val javaObjTyeList = warnClient.isWarnTypeBinding(ipcID)
//      println("javaObjTypeList" + javaObjTyeList)
      if (platID.length > 0 && javaObjTyeList != null) {
        val identifyMap = javaObjTyeList.get(DeviceTable.IDENTIFY)
//        println("identifyMap" + identifyMap)
        if (identifyMap != null && identifyMap.size() > 0) {
          val similarity = StreamingUtils.getSimilarity(identifyMap)
//          println("similarity:" + similarity)
          val javaTypeList = StreamingUtils.getTypeList(identifyMap)
//          println("javaTypeList:" + javaTypeList.getClass)
//          println("javaTypeList" + javaTypeList)
          if (similarity != null && javaTypeList != null) {
            val esResult = esClient.searchByPkeys(javaTypeList)
//            println("esResult:" + esResult)
            val scalaEsResult = JavaConverters.asScalaBufferConverter(esResult).asScala
//            println("scalaEsResult:" + scalaEsResult)
            scalaEsResult.foreach(result => {
              val tempMessage = broadcastSeparator.value + message._1 +
                broadcastSeparator.value + ipcID +
                broadcastSeparator.value + message._2 +
                broadcastSeparator.value + similarity +
                broadcastSeparator.value + result +
                broadcastInterrupt.value
              newMessage.append(tempMessage)
            })
          }
        }
      }
      newMessage.toString()
    }
    )

    kafkaFeature.foreachRDD(rddMessage => {
      rddMessage.foreachPartition(partitionMessage => {
        val sqlContext = new org.apache.spark.sql.SQLContext(ssc.sparkContext)
        import sqlContext.implicits._
        partitionMessage.foreach(message => {
          val tempMessage = message.split(broadcastInterrupt.value).
            map(_.split(broadcastSeparator.value)).
            map(f => feature(f(1), f(2), f(3), f(4), f(5), f(6), f(7)))
          tempMessage.foreach(println)
//          val rddMessage = ssc.sparkContext.parallelize(tempMessage).toDF().registerTempTable("data")
//          sqlContext.udf.register("compare", (str1: String, str2: String) => FaceFunction.featureCompare(str1, str2))
//          sqlContext.sql("select * from data compare(dynamicFeature, staticFeature) > threshold").registerTempTable("result")
//          sqlContext.sql("select * from result sort by  ")
        })
        println("+++++++++++++++++++++++++++++++++++")
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}
