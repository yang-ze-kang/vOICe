package com.example.lbstest.controller;

import com.example.lbstest.service.CameraImageService;
import com.example.lbstest.util.http.RetrofitServiceManager;
import io.reactivex.Observable;
import okhttp3.ResponseBody;

public class CameraImageController extends ObjectLoader {
    private CameraImageService cameraImageService;

    public CameraImageController() {
        cameraImageService = RetrofitServiceManager.getInstance().create(CameraImageService.class);
    }

    public Observable<ResponseBody> getCameraImageBitmap() {
        return observe(cameraImageService.getCameraImage());
    }
}
