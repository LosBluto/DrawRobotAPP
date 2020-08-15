package com.example.myapplication.ui.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


import com.example.myapplication.ui.httpserver.FileServer;
import com.google.gson.JsonObject;

import java.io.IOException;

public class HttpService extends Service {
    public static final String TAG = "HttpService";
    public static FileServer myServer = new FileServer();
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            try {
                myServer.start();
                Log.d(TAG,"sever start");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return super.onStartCommand(intent, flags, startId);
        }


        @Override
        public void onDestroy() {
            myServer.stop();
            super.onDestroy();
            Log.d(TAG, "server stop");
        }



    }

