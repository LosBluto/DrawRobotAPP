package com.example.myapplication.ui.Bluetooth;

import android.util.Log;

import com.example.myapplication.ui.utils.FileUtil;
import com.hjq.toast.ToastUtils;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/*
封装的别人的包
 */
public class BluetoothUtil {
    private BluetoothClient client;
    private String mac;
    private UUID serviceUUID;
    private UUID characterUUID;
    private static final String Tag = "BLUE";
    private String receiveMessage = "block";

    public static final String yaobiUp = "yaobiUp";
    public static final String yaobiUpOver = "up_over";
    public static final String yaobiDown = "yaobiDown";
    public static final String yaobiDownOver = "down_over";
    public static final String jinzhi = "jinzhi";
    public static final String jinzhiOver = "jinzhi_over";

    public BluetoothUtil(BluetoothClient client,String mac,UUID serviceUUID,UUID characterUUID) {
        this.client = client;
        this.mac = mac;
        this.serviceUUID = serviceUUID;
        this.characterUUID = characterUUID;
        client.connect(mac, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                if (code == Constants.REQUEST_SUCCESS)
                    Log.d(Tag,"连接成功");
                else {
                    Log.e(Tag, "连接失败");
                    ToastUtils.show("蓝牙连接失败");
                }
            }
        });
    }

    public void sendMessage(String message){
        client.write(mac, serviceUUID, characterUUID,message.getBytes(), new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == Constants.REQUEST_SUCCESS) {
                    Log.d(Tag,"发送成功");
                }else{
                    Log.d(Tag,"发送失败");
                }
            }
        });
    }

    public void sendMessage(File file) throws IOException {
        byte[] bytes = FileUtils.readFileToByteArray(file);
        int length = bytes.length;


//        String message = "length:"+length;              //发送字节总长度
//        client.write(mac, serviceUUID, characterUUID, message.getBytes(), new BleWriteResponse() {
//            @Override
//            public void onResponse(int code) {
//                if (code == Constants.REQUEST_SUCCESS) {
//                    Log.d(Tag, "发送成功");
//                }
//            }
//        });


        for (int i = 20;i<length;i+=20){                //每20个字节发送一次
            byte[] temp = new byte[20];
            System.arraycopy(bytes,i-20,temp,0,20);
            client.write(mac, serviceUUID, characterUUID, temp, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code == Constants.REQUEST_SUCCESS) {
                        Log.d(Tag, "发送成功");
                    }
                }
            });
        }
    }

    public void receiveMessage(){
        client.notify(mac, serviceUUID, characterUUID, new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                if (value == null)
                    Log.d("receive","接收失败");
                else {
                    receiveMessage = new String(value, StandardCharsets.UTF_8);
                    Log.d("receive",receiveMessage);
                    works(receiveMessage);
                }
            }

            @Override
            public void onResponse(int code) {

            }
        });
    }

    public void UnReceiveMessage() {
        client.unnotify(mac, serviceUUID, characterUUID, new BleUnnotifyResponse() {
            @Override
            public void onResponse(int code) {
                if (code == Constants.REQUEST_SUCCESS) {

                }
            }
        });
    }


    private byte[] readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true){
            int length = fis.read(buffer);
            if (-1 == length)
                break;
            baos.write(buffer,0,length);
            if (length != 1024)
                break;
        }
        return baos.toByteArray();
    }

    public String getReceiveMessage() {
        return receiveMessage;
    }

    private void works(String message){
        switch (message){
            case yaobiUp:
                sendMessage("startUp");
                break;
            case yaobiDown:
                sendMessage("startDown");
                break;
            case jinzhi:
                sendMessage("startJinZhi");
                break;
            case yaobiUpOver:                       //摇臂上升结束
                ToastUtils.show("yaobiOver");
                break;
            case yaobiDownOver:
                ToastUtils.show("yaobiOver");
                break;
            case jinzhiOver:
                ToastUtils.show("jinzhiOver");
                break;

        }

    }
}
