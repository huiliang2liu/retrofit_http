package com.xh.http;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ApiTest apiTest = new RetrofitOkhttpBuilder("https://pay.imobybox.com")
                .setCache(getApplicationContext())
                .setFactory(GsonConverterFactory.create()).build(ApiTest.class);
        try {
            apiTest.create(new ApiTest.Entity()).enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    Log.e("=====","response");
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.e("======","",t);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}