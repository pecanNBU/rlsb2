package com.hzgc.ftpserver.address;

import com.hzgc.dubbo.address.FtpAddressService;
import com.hzgc.util.FileUtil;
import com.hzgc.util.IOUtil;

import java.io.FileInputStream;
import java.util.Properties;

public class FtpAddressServiceImpl implements FtpAddressService {
    private static Properties proper = new Properties();

    public FtpAddressServiceImpl() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(FileUtil.loadResourceFile("ftpAddress.properties"));
            proper.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(fis);
        }
    }

    @Override
    public Properties getftpAddress() {
        return proper;
    }
}
