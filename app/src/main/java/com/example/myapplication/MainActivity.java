package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import com.example.myapplication.ui.Bluetooth.BluetoothUtil;
import com.example.myapplication.ui.pojo.Task;
import com.example.myapplication.ui.service.BlueService;
import com.example.myapplication.ui.utils.HttpUtil;
import com.example.myapplication.ui.utils.workUtil;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastWhiteStyle;
import com.inuker.bluetooth.library.BluetoothClient;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;


import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {

    private static final int ERROR = -1;
    private static final int SLEEP = 1;
    private static final int REQUEST_ENABLE_BT = 200;

    private static final String trailPath = Environment.getExternalStorageDirectory()+"/Download/test.txt";

    //转移activity按钮
    public ImageButton change;

//    public GifImageView gif;

    public static long waitingTime = 30000;

    private String url = "192.168.64.213";
//    private String url = "0.0.0.0";

//    public Task task;

    public TimerTask timerTask;
    public BluetoothAdapter bluetoothAdapter;
    public UUID TEST_UUID=  UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public UUID MY_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public UUID CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public BluetoothUtil bluetoothUtil;
    public BluetoothDevice mdevice;
    public static BlueService.ConnectedThread thread;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case SLEEP:
//                    gif.setVisibility(View.VISIBLE);
            }
        }
    };


//    D7:C3:7C:8D:AB:1F
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerBoradcastReceiver();

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
                if (deviceName.equals("HC-02")){
                    mdevice = device;
                    break;
                }
            }
        }

        System.out.println(Arrays.toString(mdevice.getUuids()));
        System.out.println(mdevice.getAddress());

        thread = new BlueService.ConnectedThread(mdevice);          //初始化进程
        CameraActivity.thread = thread;

        thread.start();                                             //开启接收消息监听
//        thread.SocketListener();                                //打开连接监听




//        gif = findViewById(R.id.gif);
//        gif.setVisibility(View.VISIBLE);

//        timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                if (gif.getVisibility() == View.GONE) {
//                    Message message = new Message();
//                    message.what = SLEEP;
//                    handler.sendMessage(message);
//                }
//            }
//        };
//        task = new Task(waitingTime,timerTask);
//        task.start();

        //初始化吐司工具类
        ToastUtils.init(getApplication(), new ToastWhiteStyle(getApplicationContext()));
        //判断是否有权限，没有权限则获取
        isHasPermission();

        change = findViewById(R.id.change);
        change.setVisibility(View.VISIBLE);
        animation_pic();

        String result = workUtil.jinzhi(thread);


//        workUtil.huihua(thread,new File(trailPath));


    }

    /*
    注册广播,监听蓝牙
     */
    private void registerBoradcastReceiver() {
        //注册监听
        IntentFilter stateChangeFilter = new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter connectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter disConnectedFilter = new IntentFilter(
                BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(stateChangeReceiver, stateChangeFilter);
        registerReceiver(stateChangeReceiver, connectedFilter);
        registerReceiver(stateChangeReceiver, disConnectedFilter);
    }

    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                Log.d("blue","蓝牙连接成功");
                ToastUtils.show("蓝牙连接成功");
                //连接上了
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                //蓝牙连接被切断
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                Log.d("blue",name+"蓝牙连接断开");
                ToastUtils.show("蓝牙连接断开");
                thread.setConnect(false);                       //连接状态为断开
                thread.reConnect();                             //开始自动重连
            }
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
    }

    /*
        重新唤醒黑屏
         */
    @Override
    protected void onResume() {
        super.onResume();
        registerBoradcastReceiver();
//        Thread child = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String temp = HttpUtil.jinzhi("http://"+url+":5000/JinZhi");
//            }
//        });
//        child.start();


        animation_pic();
//        change.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stateChangeReceiver);
//        task.stop();
    }

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

    public void wakeUp(View view){
//        gif.setVisibility(View.GONE);
//        Thread child = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String temp = HttpUtil.jinzhi("http://"+url+":5000/JinZhi");
//            }
//        });
//        child.start();
    }


    /**
     * 转换到照相activity
     * @param view
     */
    public void changeActivity(View view){
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);

        startActivity(intent);
    }

    private void animation_pic(){
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setRepeatCount(-1);



        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);

        change.setAnimation(animationSet);
    }




}