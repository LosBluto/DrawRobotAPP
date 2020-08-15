package com.example.myapplication.ui.http;

import com.example.myapplication.ui.http.protocol.CommonBaseJson;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface HttpService {

//    @GET("getApp/id/{appId}")
//    Call<CommonBaseJson<App>> getAppById(@Path("appId") int appId);
//
    @POST("user_command")
    Call<CommonBaseJson<String>> userCommand(@Body JsonObject bodyJson);

    @GET("chess_configure")
    Call<CommonBaseJson<String>> chessConfigure();

    @GET("draw_configure")
    Call<CommonBaseJson<String>> drawConfigure();

    @GET("new_draw_image")
    Call<CommonBaseJson<String>> newDrawImage();

    @GET("start_drawing")
    Call<CommonBaseJson<String>> startDraw();

    @FormUrlEncoded
    @POST("view/getTrail")
    Call<CommonBaseJson<String>> trailGet(@Field("img_path") String img_path, @Field("parsing_path") String parsing_path, @Field("trails_path") String trails_path, @Field("train_main_path") String train_main_path, @Field("openid") String openid);


    @Multipart
    @POST("img_upload")
    Call<CommonBaseJson<String>> imgUpload(@Part MultipartBody.Part img);

    @FormUrlEncoded
    @POST("view/getImg")
    Call<CommonBaseJson<String>> imgUploadHashmap(@Part MultipartBody.Part imgData, @Field("path") String imgPath);


    @FormUrlEncoded
    @POST("view/getStick")
    Call<CommonBaseJson<String>> imgToOwnSever(@Field("file") String imgData, @Field("name") String imgName, @Field("path") String imgPath, @Field("openid") String userID, @Field("colortype") int colortype, @Field("content_size") int content_size, @Field("picType") String picType);

    @FormUrlEncoded
    @POST("view/getTrail")
    Call<CommonBaseJson<String>> trailfromOwnSever(@Field("img_path") String img_path, @Field("id") int id, @Field("parsing_path") String parsing_path, @Field("trails_path") String trails_path, @Field("train_main_path") String train_main_path, @Field("openid") String openid);


    @FormUrlEncoded
    @POST("/complete")
    Call<CommonBaseJson<String>> upload(@Part MultipartBody.Part file);

}
