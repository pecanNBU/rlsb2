package com.hzgc.hbase.sort;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserInfo implements Serializable{

    //private static final long serialVersionUID = -3522051445403971732L;

    private Integer userId;
    private String username;
    private Date birthDate;
    private Integer age;
    private float fRate;
    private char ch;

    public Date getBirthDate() {
        return birthDate;
    }

    public String getBirthDatestr() {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
        return formater.format(getBirthDate());
    }

    public UserInfo(Integer userId, String username, Date birthDate, Integer age, float fRate, char ch) {
        super();
        this.userId = userId;
        this.username = username;
        this.birthDate = birthDate;
        this.age = age;
        this.fRate = fRate;
        this.ch = ch;
    }

    @Override
    public String toString() {
        return "UserInfo [userId=" + userId + ", \tusername=" + username + ", \tbirthDate=" + getBirthDatestr()
                + ", \tage=" + age + ", fRate=" + fRate + ", ch=" + ch + "]";
    }
}
