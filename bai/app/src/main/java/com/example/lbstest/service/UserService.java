package com.example.lbstest.service;




import com.example.lbstest.model.LoginData;
import com.example.lbstest.model.RegisterData;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UserService {
    @FormUrlEncoded
    @POST("/login.php")
    Observable<LoginData> userLogin(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("/getUserByUid.php")
    Observable<LoginData> userLogin(@Field("uid") String uid);

    @FormUrlEncoded
    @POST("/register.php")
    Observable<RegisterData> userRegister(@Field("username") String username, @Field("password") String password, @Field("gender") String gender);
}
