package com.xh.http;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiTest {
    @GET("json?lang=zh-C")
    Call<Object> create();




}
