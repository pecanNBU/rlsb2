package com.hzgc.jni;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Main {
//    static {
//        NativeFunction.init();
//    }
    public static void main(String args[]) throws UnsupportedEncodingException {
        byte[] bytes = new byte[] { 50, 0, -1, 28, -24 };
        String isoString = new String(bytes, "ISO-8859-1");
        System.out.println(isoString.length());
        System.out.println(isoString);
        byte[] isoret = isoString.getBytes("ISO-8859-1");
        System.out.println(isoret.length);
        System.out.println(new String(isoret, "ISO-8859-1"));
    }

    public static byte[] getBytes(char data)
    {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    public static char getChar(byte[] bytes)
    {
        return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }
}
