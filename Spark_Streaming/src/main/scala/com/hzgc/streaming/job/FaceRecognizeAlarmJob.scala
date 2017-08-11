package com.hzgc.streaming.job

import com.google.gson.Gson
import com.hzgc.streaming.alarm.{Item, RecognizeAlarmMessage}
import com.hzgc.streaming.util._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Durations, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
/**
 *
 * 人脸识别告警实时任务（刘善彬）
 * 1、本任务为识别告警实现任务。
 * 2、本任务为实时处理任务。
 * 3、本任务数据处理大概流程为：
 *    kafka（集群）--》sparkStreaming--》Hbase
 */
object FaceRecognizeAlarmJob {

  //初始化streaming上下文
  val conf = new SparkConf().setAppName("FaceRecognizeAlarmJob").setMaster("yarn-client")
    //.setMaster("local[*]")
  val sc = new SparkContext(conf)
  val sqlContext = new SQLContext(sc)

  def main(args: Array[String]) {
    //初始化sqlContext，及隐式转换
    import sqlContext.implicits._
    //初始化StreamingContext
    val ssc = new StreamingContext(sc,Durations.seconds(3))

    //获取kafka集群的动态人脸特征值
    val photoDStream = DataCompareUtils.getKafkaDynamicPhoto(ssc)
    //将字节数组类型的特征值转化为string,同时过滤特征值为空的元素
    val eature2string = photoDStream.map(b =>(b._1,StreamingUtils.byteArray2string(b._2))).filter(_._2.length!=0).filter(null!=_._2)

    //截取出设备id字段
    val subDeviceID = eature2string.map(a =>{
      val id = a._1
      val feature = a._2
      val deviceID =id.substring(0,4)
      (id,deviceID,feature)
    })
    //通过设备id获取比对规则，过滤识别告警类型
    val getCompRule = subDeviceID.map(a =>{
      val id = a._1
      val deviceID = a._2
      val feature = a._3
      //注意：抓取人脸的设备id如果在规则库里面不存在的话，本程序会报错停止运行。
      //获取识别告警的规则
      val rule = HbaseUtils.getRule(deviceID,"recognition")
      //标识0为不存在识别告警，1存在识别告警
      if(rule.size()==0){
        //(id,设备id，null,特征值)
        (id,deviceID,"0",feature)
      }
      else{
        (id,deviceID,"1",feature)
      }
    }).filter(_._3.equals("1"))
    //getCompRule.print()
    val ss=getCompRule.foreachRDD(rdd =>{
     rdd.foreachPartition(par =>{
       par.foreach(elem =>{
        //整个作用域都是对单条数据进行操作的。
          //通过设备id获取相应的比对规则(id,deviceID,"1",feature)
          val recognitionRule = HbaseUtils.getRule(elem._2,"recognition")
          val offLineRule = HbaseUtils.getRule(elem._2,"offLine")
          val objectTypeOffLine=offLineRule.get("objectTypeList")
          val objectType = recognitionRule.get("objectTypeList")
          val platId = recognitionRule.get("platId")
          val similarity = recognitionRule.get("similarity")
          val objectTypeList = objectType.split(" ")
          //通过对象类型的集合获取过滤的静态信息库集合
         println("This is ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
         val hbaseRDD = DataCompareUtils.getHbaseStaticPhoto(sc)
          //将hbase数据转化为元组（rowkey，photoData）类型的RDD
          val hbaseTupleRDD = hbaseRDD.
            map(a =>(Bytes.toString(a._1.get()),new String((a._2.getValue("info".getBytes,"objectType".getBytes))),StreamingUtils.byteArray2string(a._2.getValue("info".getBytes,"photo".getBytes))))
          val hbaseTupleRDDFilter = hbaseTupleRDD.filter(a =>FilterUtils.filterFun(a._2,objectTypeList))
          //hbaseTupleRDDFilter.map(a =>(a._1,a._2)).foreach(println)
          hbaseTupleRDDFilter.toDF("staticID","staticObjectType","staticFeature").registerTempTable("staticTable")
          //将foreach里面的一条动态数据转化为一张sql表。注意：list的类型为元组
          val face = List((elem._1,platId,elem._2,elem._4))
          val faceRdd = sc.parallelize(face)
          val faceTable = faceRdd.toDF("dynamicID","dynamicPlatId","dynamicDeviceID","dynamicFeature").registerTempTable("dynamicTable")
          val joinTable = sqlContext.sql("select * from staticTable cross join dynamicTable")
          joinTable.registerTempTable("joinTable")
         // joinTable.show()
          //自定义sql函数transition
          sqlContext.udf.register("comp",(a:String,b:String) =>FaceFunctionTransition.functionTransition(a,b))
          val result =sqlContext.
            sql ("select dynamicID,dynamicPlatId,dynamicDeviceID,staticID,staticObjectType,comp(dynamicFeature,staticFeature) as similarity from joinTable")
          //根据相似度阈值对比对结果进行过滤。本任务为离线告警：处理结果数据必须大于特定阈值
          val filterResult = result.filter(result("similarity")>similarity)
          //对过滤结果针对similarity列进行降序排列
          val descResult = filterResult.orderBy(filterResult("similarity").desc)
        // descResult.show()
         //将识别告警结果由DataFrame转化为RDD
          val resultRDD = descResult.rdd.map(row =>{
           (row.getAs[String]("dynamicID"),row.getAs[String]("dynamicPlatId"),row.getAs[String]("dynamicDeviceID"),row.getAs[String]("staticID"),row.getAs[String]("staticObjectType"),row.getAs[String]("similarity"))
          })
         //对识别符合识别标准的静态信息库的数据进行识别时间的更新操作。
         //首先通过设备id判断是否存在离线告警，如果存在离线告警，进行更新时间
          if(!offLineRule.equals("null")){
            //通过设备id来获取对比的范围列表
            val objectTypeOffLineL = objectTypeOffLine.split(" ")
            //println(objectTypeOffLineL.toList)
            resultRDD.foreach(relrd =>{
              objectTypeOffLineL.foreach(toll =>{
                if (relrd._5.equals(toll)){
                  //对这条数据进行离线告警的识别时间进行刷新
                  val relrdRowKey = relrd._4
                  TimeUpdateUtils.timeUpdate(relrdRowKey)
                }
              })
            })
          }
          //将识别告警推送至RocketMQ（模拟推送至kafka）
          val collectResult = resultRDD.collect()
         val ram = new RecognizeAlarmMessage()
         ram.setAlarmType("识别告警")
          if(collectResult.length!=0){
            val collectResultIterator = collectResult.iterator
            val gson = new Gson()
            var dynamicDeviceID = ""
            var DynamicID = ""
            //长度可变数组
            val items = ArrayBuffer[Item]()
            while (collectResultIterator.hasNext){
              val el =collectResultIterator.next()
              dynamicDeviceID = el._3
              DynamicID = el._1
              val staticIDv = el._4
              val staticObjectTypev = el._5
              val similarityv =String.valueOf(el._6)
              //val it = s{"staticID":"$staticIDv","staticObjectType":"$staticObjectTypev","similarity":"$similarityv"}"""
              val it = new Item()
              it.setStaticID(staticIDv)
              it.setSimilarity(similarityv)
              it.setStaticObjectType(staticObjectTypev)
              items +=it
            }
            ram.setDynamicDeviceID(dynamicDeviceID)
            ram.setDynamicID(DynamicID)
            ram.setItems(items.toArray)
            val str = gson.toJson(ram)
            println(str)
            //ComparisonDataUtils.write2Kafka(str,platId)
            DataCompareUtils.write2Kafka(str,"alarmInfo")
          }
        })
     })
   })
    getCompRule.print()
    ssc.start()
    ssc.awaitTermination()
  }
}
