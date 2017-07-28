package com.hzgc.hbase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
//        com.alibaba.dubbo.container.Main.main(args);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");
        context.start();
            while (true) {
                try {
                    Main.class.wait();
                } catch (Throwable e) {
                }
            }
    }
}
