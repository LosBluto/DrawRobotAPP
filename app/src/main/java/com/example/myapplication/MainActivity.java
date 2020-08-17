package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;

import com.example.myapplication.ui.pojo.Task;
import com.example.myapplication.ui.service.BlueServerService;
import com.example.myapplication.ui.service.BlueService;
import com.example.myapplication.ui.utils.HttpUtil;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastWhiteStyle;

import androidx.appcompat.app.AppCompatActivity;


import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private static final int ERROR = -1;
    private static final int SLEEP = 1;
    private static final int REQUEST_ENABLE_BT = 200;

    //转移activity按钮
    public ImageButton change;

    public GifImageView gif;

    public static long waitingTime = 30000;

    private String url = "192.168.64.157";

    public Task task;

    public TimerTask timerTask;

    public BluetoothAdapter bluetoothAdapter;
    public String NAME = "`blue";
    public UUID MY_UUID = UUID.nameUUIDFromBytes("0cc54b8a-8616-4dae-afbd-4a64290b4da2".getBytes());
    public BluetoothDevice device;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case SLEEP:
                    gif.setVisibility(View.VISIBLE);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        ToastUtils.init(getApplication(), new ToastWhiteStyle(getApplicationContext()));
        isHasPermission();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            ToastUtils.show("设备无蓝牙");
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                if (deviceName.equals("VLT-WX0")) {
                    ParcelUuid[] uuids = device.getUuids();
                    for (int i = 0;i<6;i++){
                        System.out.println(i+":"+uuids[i].toString());
                    }
                    this.device = device;
                    break;
                }
            }
        }
        ConnectThread connectThread = new ConnectThread(device);
        connectThread.start();


        setContentView(R.layout.activity_main);


        gif = findViewById(R.id.gif);
        gif.setVisibility(View.VISIBLE);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (gif.getVisibility() == View.GONE) {
                    Message message = new Message();
                    message.what = SLEEP;
                    handler.sendMessage(message);
                }
            }
        };
        task = new Task(waitingTime,timerTask);
        task.start();

        //初始化吐司工具类
        ToastUtils.init(getApplication(), new ToastWhiteStyle(getApplicationContext()));
        //判断是否有权限，没有权限则获取
        isHasPermission();

        change = findViewById(R.id.change);
        change.setVisibility(View.VISIBLE);
    }

    /*
    重新唤醒黑屏
     */
    @Override
    protected void onResume() {
        Thread child = new Thread(new Runnable() {
            @Override
            public void run() {
                String temp = HttpUtil.jinzhi("http://"+url+":5000/JinZhi");
            }
        });
        child.start();
        super.onResume();
//        change.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.stop();
    }

    /*
    获取权限
     */
    public void requestPermission() {
        XXPermissions.with(this)
                // 可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                //.constantRequest()
                // 支持请求6.0悬浮窗权限8.0请求安装权限
                //.permission(Permission.REQUEST_INSTALL_PACKAGES)
                // 不指定权限则自动获取清单中的危险权限
                .permission(Permission.Group.STORAGE, Permission.Group.CALENDAR)
                .request(new OnPermission() {

                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        if (all) {
                            ToastUtils.show("成功");
                        }else {
                            ToastUtils.show("获取权限成功，部分权限未正常授予");
                        }
                    }
                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            ToastUtils.show("被永久拒绝授权，请手动授予权限");
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.gotoPermissionSettings(MainActivity.this);
                        }else {
                            ToastUtils.show("获取权限失败");
                            System.out.println("测试失败了");
                        }
                    }
                });
    }

    /*
    检测是否获取权限
     */
    public void isHasPermission() {
        if (XXPermissions.isHasPermission(MainActivity.this, Permission.Group.STORAGE)) {

        }else {
            requestPermission();
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(mmDevice.getUuids()[3].getUuid());
            } catch (IOException e) {
                Log.e("client", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {

                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("client", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
//            manageMyConnectedSocket(mmSocket);
            BlueService.ConnectedThread thread = new BlueService.ConnectedThread(mmSocket);
            thread.write("hello".getBytes());
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("client", "Could not close the client socket", e);
            }
        }
    }

    public void wakeUp(View view){
        gif.setVisibility(View.GONE);
        Thread child = new Thread(new Runnable() {
            @Override
            public void run() {
                String temp = HttpUtil.jinzhi("http://"+url+":5000/JinZhi");
            }
        });
        child.start();
    }


    /**
     * 转换到照相activity
     * @param view
     */
    public void changeActivity(View view){
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }




}