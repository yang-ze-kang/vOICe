package com.example.lbstest.service;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
//ip1:192.168.43.214
//ip2:192.168.43.42
public interface CameraImageService {
    @GET("http://192.168.43.214/capture")
    Observable<ResponseBody> getCameraImage();
}
