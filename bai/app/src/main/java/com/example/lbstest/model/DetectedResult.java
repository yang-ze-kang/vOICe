package com.example.lbstest.model;

public class DetectedResult {
    public String typeName;
    public int number;
    @android.support.annotation.NonNull
    @Override
    public String toString() {
        return typeName + ":" + number;
    }
}
