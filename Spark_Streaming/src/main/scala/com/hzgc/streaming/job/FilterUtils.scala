package com.hzgc.streaming.job

/**
 * Created by Administrator on 2017/7/24.
 */
object FilterUtils {
  def filterFun(elem:String,list:Array[String]): Boolean ={
    var flag=false
    list.foreach(a =>{
      if(elem.equals(a)){
        flag = true
      }
    })
    flag
  }
}
