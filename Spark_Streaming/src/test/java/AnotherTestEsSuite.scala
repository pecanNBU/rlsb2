import java.util

import com.hzgc.hbase.device.DeviceUtilImpl
import com.hzgc.hbase.staticrepo.{ElasticSearchHelper, ObjectInfoInnerHandlerImpl}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object AnotherTestEsSuite {

  val esClient = new ObjectInfoInnerHandlerImpl()
  val helper = ElasticSearchHelper.getEsClient
  val sparkConf: SparkConf = new SparkConf().setAppName("FaceRecognizeAlarmJob").setMaster("local[7]")
  val ssc = new StreamingContext(sparkConf, Seconds(3))
  def main(args: Array[String]): Unit = {

  }
}
