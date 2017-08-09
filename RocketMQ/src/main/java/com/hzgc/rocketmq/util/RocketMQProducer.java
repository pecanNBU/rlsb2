package com.hzgc.rocketmq.util;

import com.hzgc.util.FileUtil;
import com.hzgc.util.IOUtil;
import com.hzgc.util.StringUtil;
import org.apache.log4j.Logger;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class RocketMQProducer implements Serializable {
    private static Logger LOG = Logger.getLogger(RocketMQProducer.class);
    private static String topic;
    private static Properties properties = new Properties();
    private static RocketMQProducer instance = null;
    private DefaultMQProducer producer;

    private RocketMQProducer() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(FileUtil.loadResourceFile("rocketmq.properties"));
            properties.load(fis);
            String namesrvAddr = properties.getProperty("address");
            topic = properties.getProperty("topic");
            String producerGroup = properties.getProperty("group", UUID.randomUUID().toString());
            if (StringUtil.strIsRight(namesrvAddr) && StringUtil.strIsRight(topic) && StringUtil.strIsRight(producerGroup)) {
                producer = new DefaultMQProducer(producerGroup);
                producer.setNamesrvAddr(namesrvAddr);
                producer.start();
                LOG.info("producer started...");
            } else {
                LOG.error("parameter init error");
                throw new Exception("parameter init error");
            }
        } catch (Exception e) {
            LOG.error("producer init error...");
            throw new RuntimeException(e);
        } finally {
            IOUtil.closeStream(fis);
        }
    }

    public static RocketMQProducer getInstance() {
        if (instance == null) {
            synchronized (RocketMQProducer.class) {
                if (instance == null) {
                    instance = new RocketMQProducer();
                }
            }
        }
        return instance;
    }

    public void send(byte[] data) {
        send(topic, null, null, data, null);
    }

    public void send(String tag, byte[] data) {
        send(topic, tag, null, data, null);
        LOG.info("Send MQ message[topic:" + topic + ", tag:" + tag + "]");
    }

    public void send(String tag, String key, byte[] data) {
        send(topic, tag, key, data, null);
    }

    public void send(String topic, String tag, String key, byte[] data, final MessageQueueSelector selector) {
        try {
            Message msg;
            if (tag == null || tag.length() == 0) {
                msg = new Message(topic, data);
            } else if (key == null || key.length() == 0) {
                msg = new Message(topic, data);
            } else {
                msg = new Message(topic, tag, key, data);
            }

            //long startTime = System.currentTimeMillis();
            SendResult sendResult;
            if (selector != null) {
                sendResult = producer.send(msg, new MessageQueueSelector() {
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        return selector.select(mqs, msg, arg);
                    }
                }, key);
            } else {
                sendResult = producer.send(msg);
            }
            //log.info(startTime);
            LOG.info(sendResult);
            System.out.println(sendResult);

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("send message error...");
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        if (producer != null) {
            producer.shutdown();
        }
    }
}
