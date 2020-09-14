package com.example.myapplication.ui.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.myapplication.CameraActivity;
import com.hjq.toast.ToastUtils;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class BlueService{
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static final String yaobiUp = "yaobiUp";
    public static final String yaobiUpOver = "upOver";
    public static final String yaobiDown = "yaobiDown";
    public static final String yaobiDownOver = "downOver";
    public static final String jinZhi = "jinZhi";
    public static final String jinZhiOver = "jinZhiOver";
    public static final String huiHua = "huiHua";
    public static final String sendFile = "sendFile";
    public static final String huiHuaOver="huiHuaOver";



    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    public static class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private static BluetoothDevice mmdevice;
        private String message;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private File file;
        private String fileMd5 = "MD5:";
        private boolean isConnect = false;
        public Handler handler;


        public  ConnectedThread(BluetoothDevice device){
            init(device);           //初始化
        }

        private void init(BluetoothDevice device){
            mmdevice = device;
            try {
                mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            }catch (IOException e){
                e.printStackTrace();
                ToastUtils.show("蓝牙连接失败");
            }

            connectSocket();

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = mmSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            byte[] temp;

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    Log.e("run","run");
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);

                    temp = new byte[numBytes];
                    System.arraycopy(mmBuffer,0,temp,0,numBytes);

                    if (numBytes != 0)
                        message = new String(temp,StandardCharsets.UTF_8);
                    Log.d(TAG,message);
                    works(message);

                } catch (IOException | InterruptedException | RemoteException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(String m) {
            try {
                mmOutStream.write(m.getBytes());

                Log.e("write","write");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

            }
        }

        public void write(File file) {
            try {
                mmOutStream.write(FileUtils.readFileToByteArray(file));
                mmOutStream.write("over".getBytes());

                System.out.println("write");
                Log.e("write","write");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

            }
        }

        /*
        重连
         */
        public void reConnect(){
            ToastUtils.show("蓝牙连接端开,尝试重连");
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    do {                            //isConnect为true时退出连接
                        init(mmdevice);
                    } while (!isConnect);
                }
            }).start();

        }

        public void disConnect()  {
            try {
                mmSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void setFile(File file) {
            Log.d("setFile",getMD5Three(file));
            fileMd5 = "MD5:";
            this.file = file;
            fileMd5 += getMD5Three(file);
            fileMd5 += "\nover";
        }
        public void setConnect(boolean connect){
            isConnect = connect;
        }

        public String getMessage() {
            return message;
        }

        /*
        判断接收的消息,收到消息0.5秒后再进行操作
         */
        private void works(String message) throws InterruptedException, RemoteException {
            switch (message) {
                case yaobiUp:
                    Thread.sleep(500);
                    write("startUp");
                    break;
                case yaobiDown:
                    Thread.sleep(500);
                    write("startDown");
                    break;
                case jinZhi:
                    Thread.sleep(500);
                    write("startJinZhi");
                    break;
                case huiHua:
                    Log.d(TAG,fileMd5);
                    Thread.sleep(500);
                    write(fileMd5);
                    break;
                case sendFile:
                    Log.d(TAG,"md5,ok");
                    Thread.sleep(500);
                    write(file);
                    break;
                case yaobiUpOver:                       //摇臂上升结束
                    ToastUtils.show("摇臂完成");
                    write("huiHua");
                    break;
                case yaobiDownOver:
                    ToastUtils.show("摇臂完成");
                    Message message1 = new Message();
                    message1.what = CameraActivity.DRAWOVER;
                    handler.sendMessage(message1);
                    break;
                case jinZhiOver:
                    ToastUtils.show("进纸完成");
                    break;
                case huiHuaOver:
                    ToastUtils.show("绘画完成");
                    write("yaobiDown");
                    break;
                case "MD5Wrong":
                    write("huiHua");
                    break;
            }
        }


        public static String getMD5Three(File file) {
            if (!file.isFile()) {
                return null;
            }
            MessageDigest digest = null;
            FileInputStream in = null;
            byte buffer[] = new byte[1024];
            int len;
            try {
                digest = MessageDigest.getInstance("MD5");
                in = new FileInputStream(file);
                while ((len = in.read(buffer, 0, 1024)) != -1) {
                    digest.update(buffer, 0, len);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return bytesToHexString(digest.digest());
        }

        public static String bytesToHexString(byte[] src) {
            StringBuilder stringBuilder = new StringBuilder("");
            if (src == null || src.length <= 0) {
                return null;
            }
            for (int i = 0; i < src.length; i++) {
                int v = src[i] & 0xFF;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }
                stringBuilder.append(hv);
            }
            return stringBuilder.toString();
        }




        /*
        打开socket
         */
        private void connectSocket(){
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                isConnect = true;
            } catch (IOException connectException) {
                System.out.println("BT连接失败");
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("blue", "Could not close the client socket", closeException);
                }
                return;
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
