package com.hzgc.kafka.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.kafka.dynamicrepo.util.SerializerUtils;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.hzgc.kafka.dynamicrepo.KafkaObjectProducer.topic;

/**
 * Created by Administrator on 2017-7-28.
 */
public class KafkaObjectConsumer {
    private static Properties props;// 定义相应的属性保存
    //private static String topic = "kafka_hzgc";// 定义要操作的主题
    private static ConsumerConnector consumer;
    private static String ZOOKEEPER_CONNECT = "172.18.18.100:24002";
    private static String KAFKA_CONNECT = "172.18.18.100:21005";

    static {
        if (null == props) {
            props = new Properties();
            props.setProperty("zookeeper.connect", ZOOKEEPER_CONNECT); //这里根据实际情况填写你的zk连接地址
            props.setProperty("zookeeper.connection.timeout.ms", "10000");
            props.setProperty("metadata.broker.list", KAFKA_CONNECT); //根据自己的配置填写连接地址
            props.setProperty("group.id", "group1");
            consumer = getConsumer();
        }
    }

    //创建consumer
    public static ConsumerConnector getConsumer() {
        if (consumer == null) {
            consumer = consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        }
        return consumer;
    }

    public static void main(String[] args) {
        // 需要定义一个主题的映射的存储集合
        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        topicMap.put(topic, 1); // 设置要读取数据的主题
        Map<String, List<KafkaStream<byte[], byte[]>>> messageStreams = consumer.createMessageStreams(topicMap);   // 现在只有一个主题，所以此处只接收第一个主题的数据即可
        KafkaStream<byte[], byte[]> stream = messageStreams.get(topic).get(0); // 第一个主题
        ConsumerIterator<byte[], byte[]> iter = stream.iterator();
        while (iter.hasNext()) {
            // String msg = new String(iter.next().message()) ;
            SearchOption option = (SearchOption) SerializerUtils.BytesToObject(iter.next().message());   //接收消息，并将字节数组转换为对象
            System.out.println("接收到消息：" + option);
        }
    }
}
