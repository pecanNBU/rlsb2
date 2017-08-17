package com.hzgc.kafka.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

/**
 * Created by Administrator on 2017-7-28.
 */
public class KafkaObjectProducer {
    static final String topic = "kafka_hzgc";// 定义要操作的主题
    static final String ZOOKEEPER_CONNECT = "172.18.18.100:24002";
    static final String METADATA_BROKER_LIST = "172.18.18.100:21005";
    static final String SERIALIZER_CLASS = SearchOptionEncoder.class.getName();
    static Properties props;
    private static Producer<String, SearchOption> producer;

    /* static {
         if (null == props) {
             props = new Properties(); // 定义相应的属性保存
             props.setProperty("zookeeper.connect", ZOOKEEPER_CONNECT); //这里根据实际情况填写你的zk连接地址
             props.setProperty("metadata.broker.list", METADATA_BROKER_LIST); //根据自己的配置填写连接地址
             props.setProperty("serializer.class", SERIALIZER_CLASS); //填写刚刚自定义的Encoder类
             producer = new Producer<String, SearchOption>(new ProducerConfig(props));
         }
     }*/
    public void kafkaObjectProducer(SearchOption option) {
        props = new Properties(); // 定义相应的属性保存
        props.setProperty("zookeeper.connect", ZOOKEEPER_CONNECT); //这里根据实际情况填写你的zk连接地址
        props.setProperty("metadata.broker.list", METADATA_BROKER_LIST); //根据自己的配置填写连接地址
        props.setProperty("serializer.class", SERIALIZER_CLASS); //填写刚刚自定义的Encoder类
        producer = new Producer<String, SearchOption>(new ProducerConfig(props));
        producer.send(new KeyedMessage<String, SearchOption>(topic, option));  //测试发送对象数据
    }
}
