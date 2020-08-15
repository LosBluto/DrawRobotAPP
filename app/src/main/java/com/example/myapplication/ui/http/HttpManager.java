
package com.example.myapplication.ui.http;

import com.example.myapplication.ui.http.httpUtils.MyCallback;
import com.example.myapplication.ui.http.httpUtils.OkHttpUtils;
import com.example.myapplication.ui.http.protocol.CommonBaseJson;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HttpManager {

    //public static final String RELEASE_HOST = "http://101.201.68.145:8085/";
    public static final String RELEASE_HOST = "http://47.113.110.11:5901/genStick";

    public static final String RELEASE_HOST_1 = "http://127.0.0.1:8085/";

    public static final String IMAGE_PATH = RELEASE_HOST + "draw_image";

    public static final int RESULT_CODE_SUCCESS = 0;

    public static final String RESULT_MSG_SUCCESS = "success";
    public static final String RESULT_MSG_FAILED = "failed";
    public static final String RESULT_MSG_NO_DATA = "no data";

    private static HttpManager mHttpManager;

    public static HttpManager getInstance() {
        if (mHttpManager == null)
            return new HttpManager();
        else
            return mHttpManager;
    }

    private HttpManager() {
        super();
        mHttpManager = this;
    }

    private HttpService httpService;

    //ip为非本地
    private HttpService getHttpService() {
        if (httpService == null) {
            Retrofit retrofit;
            OkHttpClient client = OkHttpUtils.getOkHttpClient();
            if (client != null) {
                retrofit = new Retrofit.Builder().baseUrl(HttpManager.RELEASE_HOST).addConverterFactory(GsonConverterFactory.create()).client(client).build();
            } else {
                retrofit = new Retrofit.Builder().baseUrl(HttpManager.RELEASE_HOST).addConverterFactory(GsonConverterFactory.create()).build();
            }
            httpService = retrofit.create(HttpService.class);
        }
        return httpService;
    }

    //ip地址为127.0.0.1本地
    private HttpService getHttpService1() {
        if (httpService == null) {
            Retrofit retrofit;
            OkHttpClient client = OkHttpUtils.getOkHttpClient();
            if (client != null) {
                retrofit = new Retrofit.Builder().baseUrl(HttpManager.RELEASE_HOST_1).addConverterFactory(GsonConverterFactory.create()).client(client).build();
            } else {
                retrofit = new Retrofit.Builder().baseUrl(HttpManager.RELEASE_HOST_1).addConverterFactory(GsonConverterFactory.create()).build();
            }
            httpService = retrofit.create(HttpService.class);
        }
        return httpService;
    }


    public void userCommand(String cmd, MyCallback<CommonBaseJson<String>> callback) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_command", cmd);
        Call<CommonBaseJson<String>> call = getHttpService().userCommand(jsonObject);
        call.enqueue(callback);
    }

    public void chessConfigure(MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().chessConfigure();
        call.enqueue(callback);
    }

    public void drawConfigure(MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().drawConfigure();
        call.enqueue(callback);
    }

    public void takePhoto(MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().newDrawImage();
        call.enqueue(callback);
    }

    public void startDraw(MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().startDraw();
        call.enqueue(callback);
    }


    public void imgUpload(MultipartBody.Part img, MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().imgUpload(img);
        call.enqueue(callback);
    }

    public void trailGet(String img_path,String parsing_path,String trails_path,String train_main_path,String openid, MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().trailGet(img_path,parsing_path,trails_path,train_main_path,openid);
        call.enqueue(callback);
    }
    public void imgUploadHashmap(MultipartBody.Part imgData,String imgPath, MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().imgUploadHashmap(imgData,imgPath);
        call.enqueue(callback);
    }

    public void upload(MultipartBody.Part file,MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService1().upload(file);
        call.enqueue(callback);
    }

    public void imgToOwnSever(String imgData,String imgName,String imgPath,String userID, int colortype, int content_size,String picType, MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().imgToOwnSever(imgData,imgName,imgPath,userID,colortype,content_size,picType);
        call.enqueue(callback);
    }

    public void trailfromOwnSever(String img_path,int id,String parsing_path,String trails_path,String train_main_path,String openid, MyCallback<CommonBaseJson<String>> callback) {
        Call<CommonBaseJson<String>> call = getHttpService().trailfromOwnSever(img_path,id,parsing_path,trails_path,train_main_path,openid);
        call.enqueue(callback);
    }
}
