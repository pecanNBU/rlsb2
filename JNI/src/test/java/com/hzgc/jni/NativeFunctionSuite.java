package com.hzgc.jni;

import org.junit.Before;
import org.junit.Test;

public class NativeFunctionSuite {
    public static void main(String[] args) {
        NativeFunction.init();
//        -Djava.library.path=/opt/GsFaceLib/face_libs
        System.out.println(System.getProperty("java.library.path"));
    }
}
