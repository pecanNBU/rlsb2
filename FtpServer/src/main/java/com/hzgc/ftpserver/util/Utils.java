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

    public static String faceNum(String facekey){
        String column = facekey.substring(facekey.lastIndexOf("_") + 1, facekey.length());
        char[] chars = column.toCharArray();
        int a = 0;
        for (int i = 0; i < column.length(); i++) {
            if (chars[i] != '0') {
                a = i;
                break;
            }
        }
        column = column.substring(a,column.length());
        //System.out.println("faceColumn = " + column);
        return column;
    }

    public static String faceRowKey(String faceKey){
        String rowKeyStr = faceKey.substring(0,faceKey.lastIndexOf("_"));
        StringBuilder rowkey = new StringBuilder();
        rowkey.append(rowKeyStr);
        int rowKeyLen = rowkey.length();

        if (rowKeyLen % 8 != 0){
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < 7 - rowKeyLen % 8; i++){
                stringBuffer.insert(0,"0");
            }
            rowkey.append("_").append(stringBuffer);
        }

        //System.out.println("faceRowKey = " + rowkey.toString() + "  faceRowKey.length() = " + rowkey.length());
        return rowkey.toString();
    }

    public static String faceKey(int faceNum,String key){
        StringBuilder faceKey = new StringBuilder();
        String faceNumStr = faceNum + "";

        if (key != null && key.length() > 0){
            String[] strs = key.split("_");
            int piece = strs.length;
            if (piece == 4){
                if (faceNumStr.length() <= strs[3].length()){
                    String faceKeyStr = key.substring(0, key.length() - faceNumStr.length());
                    faceKey.append(faceKeyStr).append(faceNum);
                    //System.out.println("faceKey = " + faceKey.toString() + "  faceKey.length() = " + faceKey.length());
                }else {
                    String faceKeyStr = key.substring(0, key.lastIndexOf("_"));
                    faceKey.append(faceKeyStr);
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 0; i < 7 - (faceKey.length() + faceNumStr.length()) % 8; i++){
                        stringBuffer.insert(0,"0");
                    }
                    faceKey.append("_").append(stringBuffer).append(faceNum);
                    //System.out.println("faceKey = " + faceKey + "  faceKey.length() = " + faceKey.length());
                }
            }else if (piece == 3){
                faceKey.append(key).append("_");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 7 - faceNumStr.length(); i++){
                    stringBuilder.insert(0,"0");
                }
                faceKey.append(stringBuilder).append(faceNum);
                //System.out.println("faceKey = " + faceKey.toString() + "  faceKey.length() = " + faceKey.length());
            }
        }else {
            faceKey.append(faceKey).append(faceNum);
        }

        return faceKey.toString();
    }

    public static String transformNameToKey(String fileName) {
        StringBuilder key = new StringBuilder();

        if(fileName != null && fileName.length() >0){
            String ipcID = fileName.substring(1,fileName.indexOf("/",2));
            String tempKey = fileName.substring(fileName.lastIndexOf("/"), fileName.lastIndexOf("_")).replace("/","");
            String prefixName = tempKey.substring(tempKey.lastIndexOf("_") + 1, tempKey.length());
            String timeName = tempKey.substring(0, tempKey.lastIndexOf("_")).replace("_", "");

            key = key.append(ipcID).append("_").append(timeName).append("_");
            StringBuffer prefixNameKey = new StringBuffer();
            prefixNameKey = prefixNameKey.append(prefixName).reverse();

            if (prefixName.length() < 10){
                for (int i = 0; i < 10 - prefixName.length(); i++){
                    prefixNameKey.insert(0,"0");
                }
                key.append(prefixNameKey);
            }else {
                key.append(prefixName);
            }

            if (key.length() % 8 != 0){
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < 7 - key.length() % 8; i++){
                    stringBuffer.insert(0,"0");
                }
                key.append("_").append(stringBuffer);
            }
        }else {
            key.append(fileName);
        }

        //System.out.println("key = " + key + " key.length() = " + key.length());
        return key.toString();
    }
}
