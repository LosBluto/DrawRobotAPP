package com.example.myapplication.ui.httpserver;

import android.os.Environment;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fi.iki.elonen.NanoHTTPD;

public class FileServer extends NanoHTTPD {
    private static final int DEFAULT_SERVER_PORT = 8080;


    //文件服务器开启，端口号默认8080,只能存放一个文件
    public FileServer(){
        super(DEFAULT_SERVER_PORT);

    }


    //返回图片
    @Override
    public Response serve(IHTTPSession session){
        Log.d("test", String.valueOf(session.getMethod()));
        if (session.getMethod().equals(Method.POST)){                //如果传入请求是post请求
            //提取session中的信息
            try {
                session.parseBody(new HashMap<String, String>());
                Map<String,String> data = session.getParms();                //获取post信息
                if (data == null)
                    return response404(session,null);
                Log.d("data",data.get("data"));

                if (!Objects.equals(data.get("data"), "ok"))
                    return response404(session,null);
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
                return response404(session,null);
            }

            return responseSuccess(session);
        }else
            return response404(session,null);
//        return response404(session,null);
    }

    //页面不存在，或者文件不存在时
    public Response response404(IHTTPSession session,String url) {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("404 NOT NOT FOUND" + url + " !");
        builder.append("</body></html>\n");
        return newFixedLengthResponse(builder.toString());
    }

    public Response responseSuccess(IHTTPSession session) {
        String data = "success";
        return newFixedLengthResponse(Response.Status.OK,"application/x-www-form-urlencoded",data);
    }



}
