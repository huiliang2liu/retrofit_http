package com.xh.http;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };
    ApiTest test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        System.getProperties().put("http.proxyHost", "192.168.1.5");
//        System.getProperties().put("http.proxyPort", 8888);
//        System.getProperties().put("https.proxyHost", "192.168.1.5");
//        System.getProperties().put("https.proxyPort", 8888);
        setContentView(R.layout.activity_main);
        RetrofitOkhttpBuilder builder = new RetrofitOkhttpBuilder("http://ip-api.com");
        test = builder.setFactory(GsonConverterFactory.create()).build(ApiTest.class);
        findViewById(R.id.test).setOnClickListener((v) -> {
            test.create().enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {

                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {

                }
            });
        });


    }
}