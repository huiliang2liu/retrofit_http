package com.xh.http;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class HttpRedirectInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (response.code() < 300)
            return response;
        if (request.url().equals(response.request().url()))
            return response;
        return chain.proceed(request.newBuilder().url(response.request().url()).build());
    }
}
