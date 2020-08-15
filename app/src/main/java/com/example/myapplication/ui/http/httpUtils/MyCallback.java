package com.example.myapplication.ui.http.httpUtils;

import android.text.TextUtils;
import android.util.Log;

//import com.example.test.http.protocol.CommonBaseJson;

import com.example.myapplication.ui.http.protocol.CommonBaseJson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Bruce on 2017/7/7.
 */
public abstract class MyCallback<T extends CommonBaseJson> implements Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        onResponse(response);
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (t != null && !TextUtils.isEmpty(t.getMessage()))
            Log.d("HttpLog", t.getMessage());
        onFailure(t);
    }

    public abstract void onResponse(Response<T> response);

    public abstract void onFailure(Throwable t);

}

