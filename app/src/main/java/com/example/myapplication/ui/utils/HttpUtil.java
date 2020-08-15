package com.example.myapplication.ui.utils;
import android.util.Log;

import com.example.myapplication.ui.pojo.ImgResult;
import com.example.myapplication.ui.pojo.RobotUser;
import com.example.myapplication.ui.pojo.works;
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
    private static OkHttpClient client = new OkHttpClient();
    private static OkHttpClient client_Long = new OkHttpClient().newBuilder()
                                        .connectTimeout(10,TimeUnit.MINUTES)
                                        .readTimeout(10,TimeUnit.MINUTES)
                                        .writeTimeout(10,TimeUnit.MINUTES)
                                        .build();


    //机器人专有的api
    private static String apikey = "c20ad4d76fe97759aa27a0c99bff6710";
    private static String apisecret = "b8dd44a56acc48818ca446c930d8ba5b";


    public static ImgResult genStickByAPI(File file){
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"),file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inputFile",file.getName(),fileBody)
                .addFormDataPart("type","exquisite")
                .addFormDataPart("preprocess","1")
                .build();

        Request request = new Request.Builder()
                .url("http://120.55.193.46:8040/api/genStick")
                .post(requestBody)
                .header("APIKey",apikey)
                .header("APISecret",apisecret)
                .build();

        try {
            Response response = client_Long.newCall(request).execute();
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
     * 创建作品
     * @param file 输入图像
     * @return 返回作品号及作品的路径
     */
    public static works createWork(String url, File file){
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"),file);
        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("userId", String.valueOf(RobotUser.userId))
                                .addFormDataPart("inputFile", file.getName(),fileBody)
                                .addFormDataPart("name",otherUtil.getTimaName("jpg"))
                                .build();

        Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();

        try {
            Response response = client_Long.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().string());
            String data = jsonObject.get("data").toString();

            JSONObject jsonObject1 = new JSONObject(data);
            int workId = jsonObject1.getInt("workId");
            String inputPath = jsonObject1.getString("inputPath");

            works work = new works();
            work.setWorkId(workId);
            return work;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成简笔画
     * @param url
     * @param work
     * @return
     */
    public static String genStick(String url,works work){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inputPath",work.getInputPath())
                .addFormDataPart("workId",String.valueOf(work.getWorkId()))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = client_Long.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().string());
            String data = jsonObject.get("data").toString();

            JSONObject jsonObject1 = new JSONObject(data);
            return jsonObject1.getString("outputPath");
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
     * 生成简笔画
     * @param url 访问的url
     * @param file 输入的图片
     * @return 返回错误或者生成图片路径
     */
    public static String uploadImg(String url,File file){

        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpg"),file);
        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("files",file.getName(),fileBody)
                                .addFormDataPart("path","picture/image/output")
//                                .addFormDataPart("data","ok")
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
            return "error";
        }
    }

    /**
     * 获取轨迹图像
     * @param url 访问的url
     * @param stickPath 简笔画的url
     * @return 返回轨迹路径
     */
    public static String getTrail(String url, String stickPath){
        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("stickPath",stickPath)
                                .build();

        Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

        try {
            Response response = client.newCall(request).execute();
            Log.d("trail",String.valueOf(response.code()));
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * 把轨迹图传到机器人中
     */
    public static String result;
    public static String startDrawing(String trailPath){

        File trail = new File(trailPath);

        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"),trail);


        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("trail",trail.getName(),body)
                                .build();

        final Request request = new Request.Builder()
//        192.168.100.179
                        .url("http://192.168.64.157:5000/Upload_Trail")
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

    public static String jinzhi(){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data","ok")
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.64.157:5000/JinZhi")
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
