package com.hzgc.streaming.job

import com.hzgc.jni.{FaceFunction, NativeFunction}
import com.hzgc.streaming.util._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Durations, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 人脸识别告警任务
 * 1、本任务为识别告警实现任务。
 * 2、本任务为实时处理任务。
 * 3、本任务数据处理大概流程为：
 *    kafka（集群）--》sparkStreaming--》Hbase
 */
object FaceRecognizeAlarmJobOther {

  //初始化streaming上下文
  val conf = new SparkConf().
    setAppName(PropertiesUtils.getPropertiesValue("job.recognizeAlarm.appName")).
    setMaster(PropertiesUtils.getPropertiesValue("job.recognizeAlarm.master"))
  val sc = new SparkContext(conf)

  val sqlContext = new SQLContext(sc)
  //初始化sqlContext，及隐式转换
  import sqlContext.implicits._
  //初始化StreamingContext
  val ssc = new StreamingContext(sc,Durations.seconds(3))

  def main(args: Array[String]) {
    //获取kafka集群的动态人脸照片
    val photoDStream = DataCompareUtils.getKafkaDynamicPhoto(ssc)
    //获取静态信息库hbase数据
//    val hbaseRDD = ComparisonDataUtils.getHbaseStaticPhoto(sc)
//    //将hbase数据转化为元组（rowkey，photoData）类型的RDD
//    val hbaseTupleRDD = hbaseRDD.
//      map(a =>(Bytes.toString(a._1.get()),StreamingUtils.byteArray2string(a._2.getValue("t1".getBytes,"p".getBytes)),StreamingUtils.byteArray2string(a._2.getValue("t1".getBytes,"objectType".getBytes))))
   //,StreamingUtils.byteArray2string(a._2.getValue("t1".getBytes,"p".getBytes))
   // hbaseTupleRDD.foreach(println)
    //将hbase的数据映射为一张表
    //hbaseTupleRDD.toDF("static_id","objectType","static_feature").registerTempTable("static_table")




    val hbaseRDD = DataCompareUtils.getHbaseStaticPhoto(sc)
    //将hbase数据转化为元组（rowkey，photoData）类型的RDD
    val hbaseTupleRDD = hbaseRDD.
      map(a =>(Bytes.toString(a._1.get()),new String((a._2.getValue("t1".getBytes,"objectType".getBytes))),StreamingUtils.byteArray2string(a._2.getValue("t1".getBytes,"p".getBytes))))
      .toDF("static_id","objectType","static_feature").registerTempTable("static_table")
    //  val hbaseTupleRDDFilter = hbaseTupleRDD.filter(a =>FilterUtils.filterFun(a._2,objectTypeList))
    //  hbaseTupleRDDFilter.map(a =>(a._1,a._2)).foreach(println)
    //提取特征值
    val eatureExtract = photoDStream.map(a =>(a._1,FaceFunction.featureExtract(a._2)))
    //将floatArray类型的特征值转化为string,同时过滤特征值为空的元素
    val eature2string = eatureExtract.map(b =>(b._1,FaceFunction.floatArray2string(b._2))).filter(_._2.length!=0).filter(null!=_._2)

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
    val ss=getCompRule.foreachRDD(rdd =>{
      rdd.foreachPartition(par =>{
        par.foreach(elem =>{
          val rule = HbaseUtils.getRule(elem._2,"recognition")
          val objectType = rule.get("objectTypeList")
          val objectTypeList = objectType.split(" ")
          val result = sqlContext.sql("select * from static_table")
          val df2rdd = result.rdd.map(a =>(a.getAs[String]("static_id"),a.getAs[String]("objectType"),a.getAs[String]("static_feature")))
          val ssss = df2rdd.filter(a =>FilterUtils.filterFun(a._2,objectTypeList))
          ssss.foreach(println)


//          val hbaseRDD = ComparisonDataUtils.getHbaseStaticPhoto(sc)
//          //将hbase数据转化为元组（rowkey，photoData）类型的RDD
//          val hbaseTupleRDD = hbaseRDD.
//            map(a =>(Bytes.toString(a._1.get()),new String((a._2.getValue("t1".getBytes,"objectType".getBytes))),StreamingUtils.byteArray2string(a._2.getValue("t1".getBytes,"p".getBytes))))
//            val hbaseTupleRDDFilter = hbaseTupleRDD.filter(a =>FilterUtils.filterFun(a._2,objectTypeList))
//            hbaseTupleRDDFilter.map(a =>(a._1,a._2)).foreach(println)



          //hbaseTupleRDD.foreach(println)

//          val l =objectTypeList.toList
//          println("============"+objectTypeList.toList)
//
//          val list =List("a","b")
//          println(list)
//          val hbaseTupleRDDFilter = hbaseTupleRDD.filter(_._2.toList.contains(list))
//          hbaseTupleRDDFilter.foreach(println)
//          hbaseTupleRDDFilter.toDF("static_id","objectType","static_feature").registerTempTable("static_table")
//          //sqlContext.sql("select * from static_table")
//          val ss =sqlContext.sql("select * from static_table")
//          ss.show()


        })
      })
    })



    //getCompRule.print()
//    eature2string.map(a =>{
//      val k =a._1
//      val v =a._2
//      val list = List(k,v)
//      val listRDD = sc.parallelize(list)
//    })




   // eature2string.print()


    ssc.start()
    ssc.awaitTermination()




  }

}
