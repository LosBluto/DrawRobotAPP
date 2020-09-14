package com.example.myapplication.ui.utils;
import android.util.Log;

import com.example.myapplication.ui.pojo.ImgResult;

import org.json.JSONObject;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    private static OkHttpClient client_Long = new OkHttpClient().newBuilder()
                                        .connectTimeout(10,TimeUnit.MINUTES)
                                        .readTimeout(10,TimeUnit.MINUTES)
                                        .writeTimeout(10,TimeUnit.MINUTES)
                                        .build();


    //机器人专有的api
    private static String apikey = "c81e728d9d4c2f636f067f89cc14862c";
    private static String apisecret = "b8dd44a56acc48818ca446c930d8ba5b";


    public static ImgResult genStickByAPI(String url,File file){
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"),file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inputFile",file.getName(),fileBody)
                .addFormDataPart("type","exquisite")
                .addFormDataPart("preprocess","1")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("APIKey",apikey)
                .header("APISecret",apisecret)
                .build();

        try {
            Response response = client_Long.newCall(request).execute();
            System.out.println(response.code());
            JSONObject jsonObject = new JSONObject(response.body().string());

            String data = jsonObject.get("data").toString();
            JSONObject jsonObject1 = new JSONObject(data);

            ImgResult imgResult = new ImgResult();
            imgResult.setImgUrl(jsonObject1.getString("outputPath"));
            imgResult.setOutput(jsonObject1.getString("output"));

            return imgResult;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 生成轨迹图
     * @param url
     * @param inputPath
     * @return
     */
    public static String genTrail(String url,String inputPath){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inputPath",inputPath)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("APIKey",apikey)
                .header("APISecret",apisecret)
                .build();

        try {
            Response response = client_Long.newCall(request).execute();

            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /*
    获取可访问的url
     */
    public static String changeURL(String url,String inputPath){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inputPath",inputPath)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = client_Long.newCall(request).execute();

            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



    /**
     * 把轨迹图传到机器人中
     */
    public static String result;
    public static String startDrawing(String url,String trailPath){

        File trail = new File(trailPath);

        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"),trail);


        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("trail",trail.getName(),body)
                                .build();

        final Request request = new Request.Builder()
//        192.168.100.179
                        .url(url)
                        .post(requestBody)
                        .build();

        try{
            Response response = client_Long.newCall(request).execute();
            result = response.body().string();
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    public static String yaobiUP(String url){
//        String url = "http://0.0.0.0:8085/yaobiUp";
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data","ok")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = client_Long.newCall(request).execute();
            Log.d("test",response.code()+"");
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    public static String jinzhi(String url){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data","ok")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = client_Long.newCall(request).execute();
            Log.d("response",response.toString());
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
