package com.jjstudio.jjtank.util;

public class DataUtils {
    public static String byteToHex(byte b) {
        int i = b & 0xFF;
        return Integer.toHexString(i);
    }

    public static String bytesToHex(byte[] bytes){
        String s="";
        for (byte b : bytes){
            s+=byteToHex(b);
        }
        return s;
    }
}
