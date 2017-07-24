package com.hzgc.jni;

public class NativeFunction {
    public static native float[] feature_extract(int[] data, int width, int height);

    public static native void init();

    public static native void destory();

    public static native float compare(float[] currentFeature, float[] historyFeature);

    static {
        System.loadLibrary("FaceLib");
    }
}
