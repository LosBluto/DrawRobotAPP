package com.example.myapplication.ui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class otherUtil {

    public static String changeURL(int a, int b, String str,String init){
        str = str.trim();
        String[] out = str.split("/");
        StringBuilder str_out = new StringBuilder(init);
        for (int i = a;i<out.length-b;i++){
            str_out.append("/").append(out[i]);
        }

        return str_out.toString();
    }

    public static String getTimaName(String back){
        long timeStamp = System.currentTimeMillis();
        return timeStamp+"."+back;
    }

    public static Bitmap getURLimage(String url){

        Bitmap bitmap = null;
        try {
            URL myURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
            connection.setConnectTimeout(6000);
            connection.setDoInput(true);
            connection.setUseCaches(false);//不缓存
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
