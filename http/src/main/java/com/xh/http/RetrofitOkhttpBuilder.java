package com.xh.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class RetrofitOkhttpBuilder {
    private static final Map<Class, Object> retrofitMap = new HashMap<>();


//    public static final String URL_TEMPLE = "https*://";

    public String base;
    public boolean openLog = true;
    public boolean openCrossDomainRedirect = true;
    private List<Interceptor> interceptors = new ArrayList<>();
    private List<Interceptor> networkInterceptors = new ArrayList<>();
    private Converter.Factory factory;
    private long connectTimeout = 8;
    private long readTimeout = 8;
    private long writeTimeout = 8;
    private List<String> filters = new ArrayList<>();
    private Dns dns = Dns.SYSTEM;

    public RetrofitOkhttpBuilder(String base) {
        assert base != null && !base.isEmpty() : "base is empty";
        this.base = base;
    }

    public RetrofitOkhttpBuilder closeLog() {
        openLog = false;
        return this;
    }

    public RetrofitOkhttpBuilder closeCrossDomainRedirect() {
        openCrossDomainRedirect = false;
        return this;
    }

    public RetrofitOkhttpBuilder addInterceptor(Interceptor interceptor) {
        assert interceptor != null : "interceptor is null";
        interceptors.add(interceptor);
        return this;
    }

    public RetrofitOkhttpBuilder addFilter(String filter) {
        if (filter == null || filter.isEmpty())
            return this;
        filters.add(filter);
        return this;
    }

    public RetrofitOkhttpBuilder addNetworkInterceptor(Interceptor interceptor) {
        assert interceptor != null : "interceptor is null";
        networkInterceptors.add(interceptor);
        return this;
    }

    public RetrofitOkhttpBuilder setFactory(Converter.Factory factory) {
        assert factory != null : "factory is null";
        this.factory = factory;
        return this;
    }

    public RetrofitOkhttpBuilder setConnectTimeout(long connectTimeout) {
        assert connectTimeout > 0 : "connectTimeout must >0";
        this.connectTimeout = connectTimeout;
        return this;
    }

    public RetrofitOkhttpBuilder setReadTimeout(long readTimeout) {
        assert readTimeout > 0 : "readTimeout must >0";
        this.readTimeout = readTimeout;
        return this;
    }

    public RetrofitOkhttpBuilder setWriteTimeout(long writeTimeout) {
        assert writeTimeout > 0 : "writeTimeout must >0";
        this.writeTimeout = writeTimeout;
        return this;
    }

    public RetrofitOkhttpBuilder setDns(Dns dns) {
        if (dns == null)
            return this;
        this.dns = dns;
        return this;
    }

    public <T> T build(Class<T> clazz) {
        if (retrofitMap.containsKey(clazz))
            return (T) retrofitMap.get(clazz);
        synchronized (clazz) {
            if (retrofitMap.containsKey(clazz))
                return (T) retrofitMap.get(clazz);
            Retrofit.Builder builder = new Retrofit.Builder();
            builder.baseUrl(base);
            if (factory != null)
                builder.addConverterFactory(factory);
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .writeTimeout(writeTimeout, TimeUnit.SECONDS);
            clientBuilder.dns(dns);
            if (openCrossDomainRedirect)
                clientBuilder.addInterceptor(new HttpRedirectInterceptor());
            if (openLog)
                clientBuilder.addInterceptor(new HttpLoggingInterceptor().setFilters(filters));
            for (Interceptor interceptor : interceptors) {
                clientBuilder.addInterceptor(interceptor);
            }
            for (Interceptor interceptor : networkInterceptors) {
                clientBuilder.addNetworkInterceptor(interceptor);
            }
            builder.client(clientBuilder.build());
            T t = builder.build().create(clazz);
            retrofitMap.put(clazz, t);
            return t;
        }
    }
}
