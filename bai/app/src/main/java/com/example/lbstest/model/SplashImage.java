package com.example.lbstest.model;

public class SplashImage {
    private final String restUrl = "https://cn.bing.com";
    public ImageUrl[] images;
    class ImageUrl {
        public String url;
    }

    public String getImageUrl() {
        return restUrl + images[0].url;
    }
}
