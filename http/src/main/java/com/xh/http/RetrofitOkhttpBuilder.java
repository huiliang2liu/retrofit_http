package com.xh.http;

import android.content.Context;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
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
    private int retryTime = 0;
    private List<String> filters = new ArrayList<>();
    private Dns dns = Dns.SYSTEM;
    private File cache;
    private Context context;
    private long cacheSize = 10 * 1024 * 1024;
    private CallAdapter.Factory callAdapterFactory;
    private boolean needProxy = false;

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

    public RetrofitOkhttpBuilder setRetryTime(int retryTime) {
        this.retryTime = retryTime;
        return this;
    }

    public RetrofitOkhttpBuilder setCache(File cache, Context context) {
        this.cache = cache;
        this.context = context;
        return this;
    }

    public RetrofitOkhttpBuilder setCache(Context context) {
        this.context = context;
        return this;
    }

    public RetrofitOkhttpBuilder setNeedProxy(boolean needProxy) {
        this.needProxy = needProxy;
        return this;
    }

    public RetrofitOkhttpBuilder setCacheSize(long cacheSize) {
        this.cacheSize = cacheSize;
        return this;
    }

    public RetrofitOkhttpBuilder setCallAdapterFactory(CallAdapter.Factory callAdapterFactory) {
        this.callAdapterFactory = callAdapterFactory;
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
//                    .proxy(Proxy.NO_PROXY);
            if (!needProxy)
                clientBuilder.proxy(Proxy.NO_PROXY);
            clientBuilder.dns(dns);
            if (openCrossDomainRedirect)
                clientBuilder.addInterceptor(new HttpRedirectInterceptor());
            for (Interceptor interceptor : interceptors) {
                clientBuilder.addInterceptor(interceptor);
            }
            if (openLog)
                clientBuilder.addInterceptor(new HttpLoggingInterceptor().setFilters(filters));
            if (retryTime > 0) {
                clientBuilder.addInterceptor(new RetryInterceptor(retryTime));
            }
            if (context != null) {
                if (cache == null)
                    cache = new File(context.getCacheDir(), "okhttp");
                clientBuilder.cache(new Cache(cache, cacheSize));
                clientBuilder.addInterceptor(new CacheInterceptor(context));
            }
            for (Interceptor interceptor : networkInterceptors) {
                clientBuilder.addNetworkInterceptor(interceptor);
            }
            builder.client(clientBuilder.build());
            if (callAdapterFactory != null)
                builder.addCallAdapterFactory(callAdapterFactory);
            T t = builder.build().create(clazz);
            retrofitMap.put(clazz, t);
            return t;
        }
    }
}
