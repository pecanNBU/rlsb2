package com.hzgc.ftpserver.util;

import com.hzgc.ftpserver.local.LocalOverFtpServer;
import org.apache.ftpserver.util.IoUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.Iterator;

public class Utils {
    private static Logger log = Logger.getLogger(Utils.class);
    public static File jsonLogPath;

    public static boolean checkPort(int checkPort) throws Exception {
        return checkPort > 1024;
    }
    

    public static File loadResourceFile(String resourceName) throws Exception{
        if (false) {
            URL resource = LocalOverFtpServer.class.getResource("/conf/");
            String confPath = resource.getPath();
            confPath = confPath.substring(5, confPath.lastIndexOf("/lib"));
            confPath = confPath + "/conf/";
            System.out.println(confPath);
            File sourceFile = new File(confPath + resourceName);
            PropertyConfigurator.configure(confPath + "/com/hzgc/com.hzgc.ftpserver/log4j.properties");
            PropertyConfigurator.configure(confPath + "/hbase-site.xml");
            if (!sourceFile.exists()) {
                log.error("The local resource file:" + new File(confPath).getAbsolutePath()
                        + "/" + resourceName + " is not found, " +
                        "please check it, System exit.");
                System.exit(1);
            }
            log.info("The resource file:" + new File(confPath).getAbsolutePath() + "was load successfull");
            return sourceFile;
        } else {
            URL resource = LocalOverFtpServer.class.
                    getResource("/conf/" + resourceName);
            if (resource != null) {
                return new File(resource.getFile());
            }
        }
        log.error("Can not find rsource file:/conf" + resourceName);
        return null;
    }


    public static String loadJsonFile(InputStream is) {
//        BufferedInputStream bis;
        BufferedReader reader = null;
        StringBuilder strBuff = new StringBuilder();
        try {
//            bis = new BufferedInputStream(is);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            reader = new BufferedReader(isr);
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                strBuff.append(tempStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                IoUtils.close(reader);
            }
        }
        return strBuff.toString();
    }

    public static void writeJsonLog(String jsonContext) {
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        try {
            fileWriter = new FileWriter(jsonLogPath, true);
            printWriter = new PrintWriter(fileWriter);
            printWriter.println(jsonContext);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtils.close(printWriter);
            IoUtils.close(fileWriter);
        }
    }

    public static void analysisJsonFile(String jsonContext) {
        JSONArray jsonArray = new JSONArray(jsonContext);
        int jsonSize = jsonArray.length();
        System.out.println("Size:" + jsonSize);
        for (int i = 0; i < jsonSize; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Iterator iterable = jsonObject.keys();
            while (iterable.hasNext()) {
                String key = (String) iterable.next();
                String value = jsonObject.get(key).toString();
                if (value.startsWith("[") && value.endsWith("]")) {
                    analysisJsonFile(value);
                }
                System.out.println(key + "=" + value);
            }
        }
    }

    public static ByteArrayOutputStream inputStreamCacher(InputStream is){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = null;
        byte[] buffer = new byte[4096];
        int len;
        try {
            while((len = is.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            IoUtils.close(baos);
            IoUtils.close(is);
        }
        return baos;
    }

    /**
     *
     * @param pictureName
     * determine the picture type based on the file name
     * @return
     * equals 0, it is a picture
     * lager than 0, it is a face picture
     */
    public static int pickPicture(String pictureName) {
        int picType = 0;
        if (null != pictureName) {
            String tmpStr = pictureName.substring(pictureName.lastIndexOf("_") + 1, pictureName.lastIndexOf("."));
            try {
                picType = Integer.parseInt(tmpStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return picType;
    }

    public static String transformNameToKey(String fileName) {
        StringBuilder finalKey = new StringBuilder("");
        StringBuilder key = new StringBuilder("");
        String tempKey;
        if (null != fileName && fileName.length() > 0) {
            tempKey = fileName.substring(0, fileName.lastIndexOf("_"));
            String prefixName = tempKey.substring(tempKey.lastIndexOf("_") + 1, tempKey.length());
            String timeName = tempKey.substring(0, tempKey.lastIndexOf("_")).replace("_", "");
            key = key.append(prefixName).reverse();
            if (prefixName.length() < 10) {
                for (int i = 0; i < 10 - prefixName.length(); i++) {
                    key.insert(0, "0");
                }
                key.append(timeName);
            } else {
                key.append(timeName);
            }
        } else {
            key.append(fileName);
        }
        return key.toString();
    }
}
