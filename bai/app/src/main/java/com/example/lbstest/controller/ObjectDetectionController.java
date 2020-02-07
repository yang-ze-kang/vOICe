package com.example.lbstest.controller;

import android.graphics.Bitmap;
import com.example.lbstest.model.DirectedResult;
import com.example.lbstest.service.YOLOService;
import com.example.lbstest.util.BitmapRequestBody;
import com.example.lbstest.util.FileUtil;
import com.example.lbstest.util.http.RetrofitServiceManager;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
public class ObjectDetectionController extends ObjectLoader {
    private YOLOService yoloService;
    public ObjectDetectionController() {
        yoloService = RetrofitServiceManager.getInstance().create(YOLOService.class);
    }
    public Observable<DirectedResult> uploadImageBitmap(Bitmap bitmap) {
        RequestBody requestBody = new BitmapRequestBody(bitmap, 90);
        MultipartBody.Part body = MultipartBody.Part
                .createFormData("file",
                        FileUtil.getTimeStampFileName(".jpg"), requestBody);
        return observe(yoloService.upLoadImage(body).map(res -> res.data));
    }
}
