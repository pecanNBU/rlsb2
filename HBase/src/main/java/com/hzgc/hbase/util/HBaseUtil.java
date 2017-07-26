package com.hzgc.hbase.util;

import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class HBaseUtil {
    private static Logger LOG = Logger.getLogger(HBaseUtil.class);

    public static boolean strIsRight(String str) {
        if (null != str && str.length() > 0) {
            return true;
        }
        return false;
    }

    public static void closTable(Table table) {
        if (null != table) {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File loadResourceFile(String resourceName) {
        if (false) {
            URL resource = HBaseUtil.class.getResource("/");
            String confPath = resource.getPath();
            confPath = confPath.substring(5, confPath.lastIndexOf("/lib"));
            confPath = confPath + "/conf/";
            System.out.println(confPath);
            File sourceFile = new File(confPath + resourceName);
            PropertyConfigurator.configure(confPath + "/com/hzgc/com.hzgc.ftpserver/log4j.properties");
            PropertyConfigurator.configure(confPath + "/hbase-site.xml");
            if (!sourceFile.exists()) {
                LOG.error("The local resource file:" + new File(confPath).getAbsolutePath()
                        + "/" + resourceName + " is not found, " +
                        "please check it, System exit.");
                System.exit(1);
            }
            LOG.info("The resource file:" + new File(confPath).getAbsolutePath() + "was load successfull");
            return sourceFile;
        } else {
            URL resource = HBaseUtil.class.
                    getResource("/" + resourceName);
            if (resource != null) {
                return new File(resource.getFile());
            }
        }
        LOG.error("Can not find rsource file:" + HBaseUtil.class.getResource("/") + resourceName);
        return null;
    }
}
