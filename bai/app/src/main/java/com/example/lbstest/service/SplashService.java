package com.example.lbstest.service;

import com.example.lbstest.model.SplashImage;
import com.example.lbstest.model.SplashText;
import io.reactivex.Observable;
import retrofit2.http.GET;
public interface SplashService {
    @GET("http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1")
    Observable<SplashImage> getSplashImage();
    @GET("http://open.iciba.com/dsapi")
    Observable<SplashText> getSplashText();
}
