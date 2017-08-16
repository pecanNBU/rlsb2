package com.hzgc.streaming.util;
import java.io.*;
import java.util.Properties;

import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.NativeFunction;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

/**
 * Created by Administrator on 2017/8/1.
 */
public class KafkaUtils implements Serializable {
    private final Producer<String, byte[]> producer;
    public  static String TOPIC = "testface";
    private KafkaUtils(){
        Properties props = new Properties();
        // 此处配置的是kafka的broker地址:端口列表
        props.put("metadata.broker.list", "172.18.18.101:21005");
        //配置key的序列化类
        props.put("key.serializer.class", "kafka.serializer.StringEncoder");
        //配置value的序列化类
        props.put("value.serializer.class", "kafka.serializer.DefaultEncoder");
        props.put("request.required.acks","-1");
        producer = new Producer<String, byte[]>(new ProducerConfig(props));
    }
    void produce() throws Exception{
//        int messageNo=1;
//        String key = String.valueOf(messageNo);
        String filePath="/opt/test.jpg";
        byte[] bt = File2byte(filePath);
        //kafkaid的数格式：000123243453    0001为设备id
        //在往kafka发送之前，将特征值提取。
        NativeFunction.init();
        byte[] feature = FaceFunction.floatArray2ByteArray(FaceFunction.featureExtract(filePath));
//        byte[] feature = "thisisfeature".getBytes("ISO8859-1");
        for(int i=0;i<1;i++){
//            float[] feature = FaceFunction.featureExtract(bt);
//            String feature2string = FaceFunction.floatArray2string(feature);
            producer.send(new KeyedMessage<String, byte[]>(TOPIC, "17130NCY0HZ0002_0000000000000000_170424060313_0000069182_01",
                    feature));
      }

//        NativeFunction.destory();
    }
    public static void main (String[] args) throws Exception{
        new KafkaUtils().produce();
    }
    public static byte[] File2byte(String filePath){
        byte[] buffer = null;
        try{
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n = 0;
            while ((n = fis.read(b)) != -1){
              //  System.out.println(new String(b,0,n));
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return buffer;
    }
}
