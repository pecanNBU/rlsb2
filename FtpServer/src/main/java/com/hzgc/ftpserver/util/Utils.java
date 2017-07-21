package com.hzgc.ftpserver.util;

import com.hzgc.ftpserver.local.LocalOverFtpServer;
import org.apache.ftpserver.util.IoUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    public static String faceNum(String facekey){
        String faceNum = facekey.substring(facekey.lastIndexOf("_") + 1,facekey.length());
        return faceNum;
    }

    public static String faceRowKey(String faceKey){
        String faceRowKeyStr = faceKey.substring(0,faceKey.lastIndexOf("_"));
        return faceRowKeyStr;
    }

    public static String faceKey(int faceNum,String key){
        StringBuilder faceKey = new StringBuilder();
        faceKey.append(key).append("_").append(faceNum);
        return faceKey.toString();
    }

    public static String transformNameToKey(String fileName) {
        StringBuilder key = new StringBuilder();

        if(fileName != null && fileName.length() >0){
            String ipcID = fileName.substring(1,fileName.indexOf("/",2));
            String tempKey = fileName.substring(fileName.lastIndexOf("/"), fileName.lastIndexOf("_")).replace("/","");
            String prefixName = tempKey.substring(tempKey.lastIndexOf("_") + 1, tempKey.length());
            String timeName = tempKey.substring(2, tempKey.lastIndexOf("_")).replace("_", "");

            StringBuffer prefixNameKey = new StringBuffer();
            prefixNameKey = prefixNameKey.append(prefixName).reverse();
            if (prefixName.length() < 10){
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 10 - prefixName.length(); i++){
                    stringBuilder.insert(0,"0");
                }
                prefixNameKey.insert(0,stringBuilder);
            }

            if (ipcID.length() == 32){
                key.append(ipcID).append("_").append(timeName).append("_").append(prefixNameKey);
            }else if (ipcID.length() == 31){
                key.append(ipcID).append("__").append(timeName).append("_").append(prefixNameKey);
            }else if (ipcID.length() <= 30){
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < 31 - ipcID.length(); i++){
                    stringBuffer.insert(0,"0");
                }
                key.append(ipcID).append("_").append(stringBuffer).append("_").append(timeName).append("_").append(prefixNameKey);
            }
        }else {
            key.append(fileName);
        }
        return key.toString();
    }

    public static Map getRowKeyMessage(String rowKey){
        String ipcID = rowKey.substring(0,rowKey.indexOf("_"));
        String timeStr = rowKey.substring(33,rowKey.lastIndexOf("_"));

        String year = timeStr.substring(0,2);
        String month = timeStr.substring(2,4);
        String day = timeStr.substring(4,6);
        String hour = timeStr.substring(6,8);
        String minute = timeStr.substring(8,10);
        String second = timeStr.substring(10,12);

        StringBuilder time = new StringBuilder();
        time = time.append(20).append(year).append("-").append(month).append("-").append(day).append(" ").append(hour).append(":").append(minute).append(":").append(second);
        SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        Map<String,String> map = new HashMap<>();
        try {
            Date date = sdf.parse(time.toString());
            long timeStamp = date.getTime();
            map.put("ipcID",ipcID);
            map.put("time",String.valueOf(timeStamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
