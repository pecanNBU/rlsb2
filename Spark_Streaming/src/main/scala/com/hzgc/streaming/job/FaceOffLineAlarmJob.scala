package com.hzgc.streaming.job

import com.google.gson.Gson
import com.hzgc.streaming.alarm.OffLineAlarmMessage
import com.hzgc.streaming.util.{PropertiesUtils, StreamingUtils}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 *  人脸离线告警任务（刘善彬）
 */
object FaceOffLineAlarmJob {
  val olam = new OffLineAlarmMessage()
  val gson = new Gson()
  def main(args: Array[String]) {
    val conf = new SparkConf().
      setAppName(PropertiesUtils.getPropertiesValue("job.offLine.appName")).
      setMaster(PropertiesUtils.getPropertiesValue("job.offLine.master"))
    val sc = new SparkContext(conf)
    val hbaseRDD = DataCompareUtils.getHbaseStaticPhoto(sc).map(pair =>{
      val key = Bytes.toString(pair._1.get())
      val updateTime = new String (pair._2.getValue("t1".getBytes(),"updateTime".getBytes()))
      val objectType = new String(pair._2.getValue("t1".getBytes(),"objectType".getBytes()))
      ("离线告警",key,updateTime,objectType)
    }).filter(a =>a._3!="")

    val transition = hbaseRDD.map(b =>{
      (b._1,b._2,b._3,StreamingUtils.timeTransition(b._3),b._4)
    })
    val offLineDays = PropertiesUtils.getPropertiesValue("job.offLine.offLineDays")
    val result = transition.filter(f =>f._4 > offLineDays.toDouble)
    result.foreach(res =>{
      olam.setAlarmType(res._1)
      olam.setStaticID(res._2)
      olam.setUpdateTime(res._3)
      olam.setOffLineDays(res._4)
      olam.setObjectType(res._5)
      val str = gson.toJson(olam)
      DataCompareUtils.write2Kafka(str,"offLine")
    })
  }

}
