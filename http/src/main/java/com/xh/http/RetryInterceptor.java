package com.xh.http;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class RetryInterceptor implements Interceptor {
    private int times = 0;
    private int maxTimes;

    public RetryInterceptor(int retryTimes) {
        maxTimes = retryTimes;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        IOException exception = null;
        Response response = null;
        while (times <= maxTimes) {
            exception = null;
            response = null;
            try {
                response = chain.proceed(chain.request());
                if (response.isSuccessful())
                    break;
            } catch (IOException e) {
                exception = e;
            }
            times++;
        }
        if (exception != null)
            throw exception;
        return response;
    }
}
