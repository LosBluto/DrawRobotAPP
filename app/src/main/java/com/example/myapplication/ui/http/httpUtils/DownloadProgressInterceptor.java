package com.example.myapplication.ui.http.httpUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 下载图片进度拦截器
 * Created by wangchao on 2016/7/13.
 */
public class DownloadProgressInterceptor implements Interceptor {
    private DownloadProgressListener progressListener;

    public DownloadProgressInterceptor(DownloadProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder()
                .body(new DownloadProgressResponseBody(originalResponse.body(), progressListener))
                .build();
    }


    public interface DownloadProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }
}
