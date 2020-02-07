package com.example.lbstest.service;

import com.example.lbstest.model.DetectedResultData;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
public interface YOLOService {
    @Multipart
    @POST("http://47.96.188.216:5000/upload")
    Observable<DetectedResultData> upLoadImage(@Part MultipartBody.Part file);
}
