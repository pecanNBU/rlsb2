package com.hzgc.jni;

import java.io.Serializable;

public class NativeFunction implements Serializable {
    public static native float[] feature_extract(int[] data, int width, int height);

    public static native void init();

    public static native void destory();

    public static native float compare(float[] currentFeature, float[] historyFeature);

    static {
        System.loadLibrary("FaceLib");
    }
}
