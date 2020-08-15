package com.example.myapplication.ui.http.httpUtils;

import android.text.TextUtils;
import android.util.Log;

//import com.example.test.MyApplication;

import com.example.myapplication.MyApplication;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by wangchao on 2016/7/14.
 */
public class OkHttpUtils {

    public static OkHttpClient getOkHttpClient() {

        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        //新建log拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("HttpLog", "OkHttp Message:" + message);
            }
        });
        loggingInterceptor.setLevel(level);


        File cacheDir = MyApplication.getInstance().getCacheDir();
        Cache myCache = new Cache(cacheDir, 10 * 1024 * 1024);

        OkHttpClient client = null;
        Proxy proxy = null;
//        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.70.12.145", 8889));
        if (proxy == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addNetworkInterceptor(new NetworkBaseInterceptor())
                    .cache(myCache)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .build();
        } else {
            client = new OkHttpClient.Builder()
                    .proxy(proxy)
                    .addInterceptor(loggingInterceptor)
                    .addNetworkInterceptor(new NetworkBaseInterceptor())
                    .cache(myCache)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    private static class NetworkBaseInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            String uid = "0";

            Request request = chain.request();
            Response originalResponse = chain.proceed(request);

            String serverCache = originalResponse.header("Cache-Control");
            if (TextUtils.isEmpty(serverCache)) {
                String cacheControl = request.cacheControl().toString();
                return originalResponse.newBuilder()
                        .addHeader("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            } else {
                return originalResponse;
            }
        }
    }

    public static Map<String, RequestBody> getImageMap(String... localPath) {
        Map<String, RequestBody> map = new HashMap<>();
        for (String path : localPath) {
            String type = "image/jpeg";
            File file = new File(path);
            RequestBody requestBody = RequestBody.create(MediaType.parse(type), file);
            map.put("resource\"; filename = \"" + file.getName() + "", requestBody);
        }
        return map;
    }
}
