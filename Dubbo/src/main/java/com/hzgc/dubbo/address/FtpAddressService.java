package com.hzgc.dubbo.address;

import java.util.Properties;

public interface FtpAddressService {

    /**
     * 获取Ftp地址和端口号
     *
     * @return key = ip，返回地址；key = port，返回端口号
     */
	public Properties getftpAddress();

}
