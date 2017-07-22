package com.hzgc.dubbo.provider;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;

public class ServiceThread implements Runnable {
	private ServiceConfig<?> selfservice;
	private String interfaceClazz;
	public <T> ServiceThread(ServiceConfig<T> service, Properties properties, Class<?> interfaceClass, T t){
				//init
				selfservice = service;
				interfaceClazz = interfaceClass.getName();
				//application
				ApplicationConfig application = new ApplicationConfig();
				application.setName(properties.getProperty("applicationName", "dubbo-provider"));
				//registry
				RegistryConfig registry = new RegistryConfig();
				registry.setAddress(properties.getProperty("registryAddress"));
				//username & password
				//registry.setUsername(properties.getProperty("userName"));
				//registry.setPassword(properties.getProperty("password"));
				//Protocol
				ProtocolConfig protocol = new ProtocolConfig();
				protocol.setName(properties.getProperty("protocolName"));
				protocol.setPort(Integer.parseInt(properties.getProperty("protocolPort")));
				//threads
				//protocol.setThreads(Integer.parseInt(properties.getProperty("protocolThreads"),100));
				//service
				service.setApplication(application);
				service.setRegistry(registry);
				service.setProtocol(protocol);
				service.setInterface(interfaceClass);
				service.setRef(t);
				//version
				//service.setVersion("1.0.0");
				
	}
	
	public String getInfo(){
		//return serviceMap.get(Thread.currentThread()).toString() + serviceMap.values() + interfaceMap.values();
		return selfservice.toString() + interfaceClazz;
	}
	
	public void run() {
			if(selfservice!=null){
				Long startTime = System.currentTimeMillis();
				selfservice.export();
				System.out.println("service " + interfaceClazz + " started...cost " + (System.currentTimeMillis()-startTime) + "ms");
			}
			while(true){
			}
		}
}
