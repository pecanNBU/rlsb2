package com.hzgc.jni;

public class Test {
    public static void main(String[] args) {
        System.out.println(System.getProperty("java.library.path"));
        System.out.println("Start init");
        NativeFunction.init();
        float[] a = new float[2];
        float[] b = new float[2];
        System.out.println("start compare");
        NativeFunction.compare(a, b);
    }
}
