package com.hzgc.ftpserver.util;

import org.apache.ftpserver.util.EncryptUtils;

public class PasswrodToMD5 {
    public static void main(String args[]) {
        System.out.println(EncryptUtils.encryptMD5("123456"));
        System.out.println(Integer.MAX_VALUE);
            if (args.length == 1) {
                try {
                    String passed = EncryptUtils.encryptMD5(args[0]);
                    System.out.println(passed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.exit(1);
            }
    }
}
