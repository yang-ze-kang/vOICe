package com.example.lbstest.util.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class HttpUtils {
    private final static int READ_TIMEOUT = 100;
    private final static int WRITE_TIMEOUT = 60;
    private final static int CONNECT_TIMEOUT = 60;
    private static final String base_url = "https://api.ihanco.com/index.php/api/";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)//设置读取超时时间
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)//设置写的超时时间
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)//设置连接超时时间
            .build();
    public static String get(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }
    /**
     *
     * @param url  接口路由地址
     * @param json json字符串
     * @param builder
     * @return
     * @throws IOException
     */
    private static String _post(String url, String json, Request.Builder builder) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = builder
                .url(base_url + url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }
    public static String post(String url, String token, String json) throws IOException {
        return _post(url, json, new Request.Builder()
                .addHeader("token", token));
    }
    public static String post(String url, String json) throws IOException {
        return _post(url, json, new Request.Builder());
    }
}
