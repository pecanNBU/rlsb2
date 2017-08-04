package com.hzgc.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;

public class FileUtil {
    private static Logger LOG = Logger.getLogger(FileUtil.class);

    public static File loadResourceFile(String resourceName) {
        if (StringUtil.strIsRight(resourceName)) {
            URL url = ClassLoader.getSystemResource(resourceName);
            if (url != null) {
                File file = new File(url.getPath());
                LOG.info("Load resource file:" + url.getPath() + " successful!");
                return file;
            } else {
                LOG.error("Resource file:" + resourceName + " is not exist!");
                System.exit(1);
            }
        } else {
            LOG.error("The file name is not vaild!");
        }
        return null;
    }
}
