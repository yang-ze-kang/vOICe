package com.example.lbstest.util.http;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitFileServiceManager {
    private Retrofit mRetrofit;
    private RetrofitFileServiceManager() {
        // 创建Retrofit
        mRetrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    private static class SingletonHolder {
        private static final RetrofitFileServiceManager INSTANCE = new RetrofitFileServiceManager();
    }
    public static RetrofitFileServiceManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }
}
