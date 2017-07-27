package com.hzgc.hbase.staticreposuite;

import com.hzgc.dubbo.staticrepo.ObjectSearchResult;
import com.hzgc.hbase.staticrepo.ObjectInfoHandlerImpl;
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandler;
import com.hzgc.hbase.staticrepo.ObjectInfoInnerHandlerImpl;
import com.hzgc.hbase.staticrepo.SearchRecordHandlerImpl;
import com.hzgc.hbase.util.HBaseHelper;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class StaticRepoUtilSuite {

    @Test
    public void testAddObjectInfo(){
        String platformId = "1234";
        Map<String, Object> person = new HashMap<String, Object>();
        person.put("id","1111111111jkh11111111");
        person.put("name", "小王炸");
        person.put("idcard", "1111111111jkh11111111");
        person.put("sex", "1");
       try {
            person.put("photo", Image2Byte2Image.image2byte("E:\\1.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        person.put("reason", "赌博");
        person.put("pkey", "123456");
        person.put("creator", "羊驼");
        person.put("cphone", "18069222222");
        person.put("tag", "person");
        person.put("feature", "123455555555");

        int flag = new ObjectInfoHandlerImpl().addObjectInfo(platformId, person);
        System.out.println(flag);
    }
    @Test
    public void testUpdateObjectInfo(){
        String platformId = "123456";
        Map<String, Object> person = new HashMap<String, Object>();
        person.put("id", "1111111111jkh11111111");
        person.put("name", "小王炸炸");
        person.put("idcard", "222111111111111111");
        person.put("sex", "0");
        try {
            person.put("photo", Image2Byte2Image.image2byte("E:\\1.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        person.put("pkey", "123456");
        person.put("reason", "赌博+暴力倾向");
        person.put("creator", "羊驼神兽");
        person.put("feature", "123455555555");

        int flag = new ObjectInfoHandlerImpl().updateObjectInfo(person);
        System.out.println(flag);
    }

    @Test
    public void testDeleteObjectInfo() throws IOException {
        Table tableName = HBaseHelper.getTable("objectinfo");
        Put put = new Put(Bytes.toBytes("111111111111111111123456"));
        put.addColumn(Bytes.toBytes("person"),Bytes.toBytes("name"),Bytes.toBytes("Liu siyang"));
        tableName.put(put);
        List<String> rowkeys = new ArrayList<String>();
        rowkeys.add("111111111111111111123456");
        int flag = new ObjectInfoHandlerImpl().deleteObjectInfo(rowkeys);
        System.out.println(flag);
    }
    @Test
    public void testconn(){
        Connection conn = HBaseHelper.getHBaseConnection();
        System.out.println(conn);
    }

    @Test
    public void testByGetByPlatFormIdAndIdCard(){
        ObjectInfoHandlerImpl impl = new ObjectInfoHandlerImpl();
        ObjectSearchResult searchResult = impl.searchByPlatFormIdAndIdCard("1234", "111",
                true, 1, 3);
        System.out.println(searchResult);
    }

    @Test
    public void testSearchByRowkey(){
        ObjectSearchResult objectSearchResult = new ObjectInfoHandlerImpl().searchByRowkey("3598d31ddcb546d1acd3240c8ddb9f0a");
        System.out.println(objectSearchResult);
    }

    @Test
    public void testSearchByPhone(){
        ObjectSearchResult objectSearchResult = new ObjectInfoHandlerImpl().searchByCphone("18069222222");
        System.out.println(objectSearchResult);
    }

    @Test
    public void testSearchByCreator(){
        ObjectSearchResult objectSearchResult = new ObjectInfoHandlerImpl().searchByCreator("羊驼驼羊",
                false, 1, 5);
        System.out.println(objectSearchResult);
    }

    @Test
    public void testGetPhotoByKey() throws IOException {
        byte[] photo = new ObjectInfoHandlerImpl().getPhotoByKey("e590e89a002e44b3b302257a85684d1a");
        System.out.println(photo);
        //Image2Byte2Image.byte2image(photo, "C:\\Users\\lenovo\\Desktop\\nima.png");
    }

    @Test
    public void testimpl(){
        ObjectInfoInnerHandlerImpl objectInfoInnerHandler = new ObjectInfoInnerHandlerImpl();
        List<String> a = new ArrayList<>();
        a.add("123456");
        List<String> b = objectInfoInnerHandler.searchByPkeys(a);
        System.out.println(b);
    }
    @Test
    public void testGetPhotoByRowkey() throws IOException {
       ObjectInfoHandlerImpl objectInfoHandler = new ObjectInfoHandlerImpl();
        String rk = "e590e89a002e44b3b302257a85684d1a";
        byte[] photo = objectInfoHandler.getPhotoByKey(rk);
        System.out.println(photo);
        Image2Byte2Image.byte2image(photo, "C:\\nika.png");
    }
    @Test
    public void testGetRocordOfObjectInfo(){
        SearchRecordHandlerImpl searchRecordHandler = new SearchRecordHandlerImpl();
        String rk = "b63914dc5f9448f5a0c3b0d6056c4bef";
        ObjectSearchResult o = searchRecordHandler.getRocordOfObjectInfo(rk);
        System.out.println(o);
    }
}
