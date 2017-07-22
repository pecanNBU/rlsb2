package com.hzgc.dubbo.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.hzgc.dubbo.ftpAddress.ftpAddressService;
import com.hzgc.dubbo.ftpAddress.ftpAddressServiceImpl;

public class DubboProvider {
	private static Properties properties = new Properties();
	private static long startTime;
	private static String interfaceClazz;
	private static FileInputStream fis;
	private static String path;
	
	static {
		try {
			String classRoute = new Object(){
				public String getClassPath(){
					String clazzPath = this.getClass().getResource("/").getPath();
					return clazzPath;
				}
			}.getClassPath();
			int index = classRoute.indexOf("Dubbo");
			String dir = classRoute.substring(0, index);
			path = dir + "Distribution/conf/dubbo-provider.properties";
			fis = new FileInputStream(new File(path));
			properties.load(fis);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if(fis!=null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
		}
	}
	
	private static <T> void startService(ServiceConfig<T> service, Properties properties,
										Class<?> interfaceClass, T t){
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
		
		startTime = System.currentTimeMillis();
		service.export();
		interfaceClazz = interfaceClass.getName();
		System.out.println("service " + interfaceClazz + " started...cost " + (System.currentTimeMillis()-startTime) + "ms");
		
	}
	
	
	public static void main(String[] args) {

		ftpAddressService ftpService = new ftpAddressServiceImpl();

		ServiceConfig<ftpAddressService> service = new ServiceConfig<ftpAddressService>();
		//Examples of starting service by method
		//startService(service, properties, ftpAddressService.class, ftpService);
		//System.in.read();
		//Examples of starting service by thread
		ServiceThread serviceThread = new ServiceThread(service, properties, ftpAddressService.class, ftpService);
		Thread thread1 = new Thread(serviceThread);
		thread1.start();


	}
}
