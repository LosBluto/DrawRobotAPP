package com.example.myapplication;

import android.app.Application;

//import com.inuker.bluetooth.library.BluetoothClient;

public class MyApplication extends Application {

//    public BluetoothClient mClient;

    private static MyApplication singleton;

    public static MyApplication getInstance(){
        return singleton;
    }

//    @Override
//    public final void onCreate() {
//        super.onCreate();
//        singleton = this;
//        mClient = new BluetoothClient(this);
//        mClient.openBluetooth();
//    }

//    public BluetoothClient getClient() {
//        return mClient;
//    }
}