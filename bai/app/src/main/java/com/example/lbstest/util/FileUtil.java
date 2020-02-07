package com.example.lbstest.util;

public class FileUtil {
    public static String getTimeStampFileName(String suffix) {
        return System.currentTimeMillis() + suffix;
    }
}
