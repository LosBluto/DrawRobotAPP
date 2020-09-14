package com.example.myapplication;

import android.annotation.SuppressLint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.ui.pojo.ImgResult;
import com.example.myapplication.ui.service.BlueService;
import com.example.myapplication.ui.utils.Exif;
import com.example.myapplication.ui.utils.FileUtil;
import com.example.myapplication.ui.utils.HttpUtil;
import com.example.myapplication.ui.utils.otherUtil;
import com.example.myapplication.ui.utils.workUtil;
import com.hjq.toast.ToastUtils;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import pl.droidsonroids.gif.GifImageView;

public class CameraActivity extends AppCompatActivity {
    public static final int ERROR = -1;
    public static final int DRAWOVER = 1;                 //生成简笔画线程
    public static final int UPDATEOVER = 2;                //生成完成

    private String url = "192.168.64.213";
    //CameraView对象
    private CameraView camera;
    //返回按钮
    private ImageButton back;
    //shutter快门按钮
    private ImageButton shutter;
    //重新拍照按钮
    private ImageButton retake;
    //作画按钮
    private ImageButton startDraw;
    //显示时间的文本框
    private TextView textView;
    //存放图片的区域
    private ImageView imageView;
    private ImageView waiting_image;
    private ImageView pen;
    private ImageView line;
    private ImageView robot;
    private ImageView shadow;

    //背景
    private ImageView background;
//    //人脸框
    private ImageView renliankuang;
    //等待时间
    private static final int waitTime = 3;
    //用于记录时间
    private static int timer = waitTime;
    //判断handler是否在执行的标志位
    private boolean ifHandler = false;


    //操作器handler
    @SuppressLint("HandlerLeak")
    public  Handler handler = new Handler(){                            //处理子线程的handler
        public void handleMessage(Message message){
            switch (message.what) {
                case DRAWOVER:
                    ToastUtils.show("绘画完成");
                    textView.setVisibility(View.GONE);      //绘画提示消失
                    imageView.setVisibility(View.VISIBLE);  //简笔画可见
                    camera.setVisibility(View.GONE);
                    back.setVisibility(View.VISIBLE);
//                    camera.stop();                          //关闭摄像头
                    camera.close();



//                    String temp = HttpUtil.jinzhi("http://"+url+":5000/JinZhi");        //绘画完毕开始进纸

                    break;
                case UPDATEOVER:
                    Log.d("test","handler");
                    ToastUtils.show("生成成功");
                    retake.setVisibility(View.VISIBLE);     //重新拍照按钮可见

                    back.setVisibility(View.VISIBLE);

                    waiting_image.setVisibility(View.GONE);
                    pen.setVisibility(View.GONE);
                    line.setVisibility(View.GONE);
                    robot.clearAnimation();             //去除动效
                    shadow.clearAnimation();
                    robot.setVisibility(View.GONE);
                    shadow.setVisibility(View.GONE);

                    imageView.setImageBitmap(bitmap);       //更换为生成图片
                    imageView.setVisibility(View.VISIBLE);

                    textView.setText("");      //生成中提示消失
                    startDraw.setVisibility(View.VISIBLE);  //开始作画按钮出现

                    background.setBackgroundResource(R.drawable.bg_3);

                    break;
                case ERROR:
                    textView.setText("");      //提示消失
            }

            super.handleMessage(message);
        }
    };
    //保存图片的文件夹地址
    String dirpath = Environment.getExternalStorageDirectory()+"/Pictures/test";
    //保存轨迹文件的路径
    private static final String trailPath = Environment.getExternalStorageDirectory()+"/Download/test.txt";

    public static String ImgURL;
    public static Bitmap bitmap;
    //用于保存生成简笔画信息
    private ImgResult imgResult;

    public static BlueService.ConnectedThread thread;

    public Messenger mServiceMessenger;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        thread.handler = handler;                           //初始化handler


        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);


        //实例化对象
        camera = findViewById(R.id.preview_camera);
        camera.setLifecycleOwner(this);
        shutter = findViewById(R.id.takePhoto);
        back = findViewById(R.id.back);

        retake = findViewById(R.id.retake);
        retake.setVisibility(View.GONE);

        textView = findViewById(R.id.textView);

        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);

        waiting_image = findViewById(R.id.waiting_image);           //等待中显示的各个图片
        waiting_image.setVisibility(View.GONE);
        pen = findViewById(R.id.pen);
        pen.setVisibility(View.GONE);
        line = findViewById(R.id.line);
        line.setVisibility(View.GONE);
        robot = findViewById(R.id.robot);
        robot.setVisibility(View.GONE);
        shadow = findViewById(R.id.shadow);
        shadow.setVisibility(View.GONE);

        background = findViewById(R.id.background);

        startDraw = findViewById(R.id.getTrail);
        startDraw.setVisibility(View.GONE);

        renliankuang = findViewById(R.id.camera_image);

        camera.open();
        initCamera();

    }


    //根据activity生命周期 将来启动或者销毁相机
    @Override
    protected void onResume() {
        super.onResume();
        if (!camera.isOpened())
            camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        camera.stop();//退出界面 比如点击home时 停掉相机 以onResume重启
        camera.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁相机释放资源
        camera.destroy();
    }

    //拍照的方法
    public void takephoto(View view) {
        if (!ifHandler){                    //拍照期间点击拍照无用
            ifHandler = true;
            handler.post(runnable);
        }
    }


    /**
     * 删除拍照临时文件
     */
    private void deleteTempPhotoFile() {
        File tempFile = new File(dirpath + "current.jpg");
        if (tempFile.exists() && tempFile.isFile()) {
            tempFile.delete();
        }
    }

    /*
        计时拍照任务
         */
    private Runnable runnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (timer > 0){
//                textView.setText(timer+"");
//                timer--;
//                handler.postDelayed(runnable,1000);         //延迟1s
                animation();
            }else{
                textView.setText("");
                camera.takePicture();

                ifHandler = false;
                timer = waitTime;                                  //重新置数
            }
        }
    };

    @SuppressLint("SetTextI18n")
    private void animation(){
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.1f, 1.3f, 0.1f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        textView.setText(timer+"");
        scaleAnimation.setRepeatCount(timer);
        alphaAnimation.setRepeatCount(timer);
        scaleAnimation.setDuration(1000);
        alphaAnimation.setDuration(1000);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setText("");
                camera.takePicture();

                ifHandler = false;
                timer = waitTime;                                  //重新置数
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                --timer;
                textView.setText(timer+"");
            }
        });

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        textView.startAnimation(animationSet);
    }

    private void animation_shadow() {           //阴影动画
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.5f, 1.3f, 0.5f, 1.3f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setRepeatCount(-1);
        scaleAnimation.setDuration(800);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        shadow.startAnimation(scaleAnimation);
    }

    private void animation_robot() {            //机器人动画
        TranslateAnimation translateAnimation = new TranslateAnimation(0,0,0,30);
        translateAnimation.setRepeatCount(-1);
        translateAnimation.setDuration(800);
        translateAnimation.setRepeatMode(Animation.REVERSE);

        robot.startAnimation(translateAnimation);
    }



    private void initCamera() {
        //初始化相机 设置相机拍照后的处理
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                byte[] jpeg = result.getData();


                //通过jpeg获取图片的旋转角度orientation jpeg中包含了照片角度信息
                int orientation = Exif.getOrientation(jpeg);
                //通过jpeg获取bitmap对象
                bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                //将bitmap按解析出来的角度进行旋转orientation
                //有些机型会将照片旋转90度
                Matrix m = new Matrix();
                m.postScale(-1,1);
                m.postTranslate(bitmap.getWidth(),0);
//                m.postRotate(orientation);
                int cutHeight = ((int) (bitmap.getHeight()*0.166)+1)/2;
                System.out.println(bitmap.getWidth());
                bitmap = Bitmap.createBitmap(bitmap, cutHeight, 0, bitmap.getWidth()-2*cutHeight,
                        bitmap.getHeight(), m, true);
                System.out.println(bitmap.getHeight()+"width"+bitmap.getWidth());
                saveBitmap(bitmap,"current.jpg",50);

                ToastUtils.show("拍照成功");

                //进行拍摄成功后的处理
                shutter.setVisibility(View.GONE);           //隐藏拍照按钮
                camera.setVisibility(View.GONE);            //相机隐藏和关闭
                camera.close();



                UpdateImage();



            }
        });

    }


    /**
     * 保存bitmap照片
     * @param bitmap 照片的bitmap
     */
    public void saveBitmap(Bitmap bitmap,String name,int quality) {
        //拍照成功后进行保存

        File dirFirstFile = new File(dirpath);
        if (!dirFirstFile.exists())
            dirFirstFile.mkdirs();

        Log.d("dirpath",dirpath);
        File file = new File(dirpath,name);
        if (file.exists())
            file.delete();
        try {
            //文件输出流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
            //写入，这里会卡顿，因为图片较大
            fileOutputStream.flush();
            //记得要关闭写入流
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            //失败的提示
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 上传照片到服务器，获取生成的图片
     * @param
     */
    @SuppressLint({"SetTextI18n"})
    public void UpdateImage() {
        back.setVisibility(View.GONE);

        background.setBackgroundResource(R.drawable.bg_2);//切换背景
        waiting_image.setVisibility(View.VISIBLE);
        waiting_image.setImageBitmap(bitmap);
        pen.setVisibility(View.VISIBLE);
        line.setVisibility(View.VISIBLE);
        robot.setVisibility(View.VISIBLE);
        shadow.setVisibility(View.VISIBLE);
        animation_robot();
        animation_shadow();
//        imageView.setImageBitmap(bitmap);


        final File file = new File(dirpath, "current.jpg");

        Thread child = new Thread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {

                imgResult = HttpUtil.genStickByAPI("https://api.aisketcher.com/api/genStick",file);

                if (imgResult == null) {
                    ToastUtils.show("生成失败，请检查服务器是否开启");
                    finish();
                    return;
                }

                ImgURL = imgResult.getImgUrl();
                Log.d("url",ImgURL);

                bitmap = otherUtil.getURLimage(ImgURL);

                saveBitmap(bitmap,otherUtil.getTimaName("jpg"),100);

                Message message = new Message();
                message.what = UPDATEOVER;

                handler.sendMessage(message);

            }
        });
        child.start();

    }

    /*
    获取轨迹文件，并且保存在本地,并且开始摇臂
     */
    public void getTrail(View view){
        camera.setLayoutParams(new RelativeLayout.LayoutParams(1920,1200));
        retake.setVisibility(View.GONE);

        Thread child = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("output",imgResult.getOutput());
                String trail = HttpUtil.genTrail("https://api.aisketcher.com/api/genTrail",imgResult.getOutput());
                Log.d("Trail",trail);
                saveTrail(trailPath,trail);                     //保存trail文件
                String result = null;
//                result = HttpUtil.yaobiUP("http://"+url+":5000/yaobiUp");    //发送摇臂请求
                result = workUtil.yaobiUp(thread,new File(trailPath));


                String drawingResult;


                Message message = new Message();

                if (result != null) {              //判断是否摇臂成功
//                    drawingResult = HttpUtil.startDrawing("http://"+url+":5000/Upload_Trail",trailPath);
//                    drawingResult = workUtil.huihua(thread,new File(trailPath));
                } else{
                    message.what = ERROR;
                    return;
                }

//                if (drawingResult == null) {              //判断绘画是否成功
//                    message.what = ERROR;
//                    return;
//                }
//                Log.d("Trail",drawingResult);
//                if (drawingResult.equals("success")) {
//                    HttpUtil.yaobiUP("http://"+url+":5000/yaobiDown");
//                    workUtil.yaobiDown(thread);
//                    message.what = DRAWOVER;
//                } else
//                    message.what = ERROR;

//                handler.sendMessage(message);
            }
        });
        child.start();
        textView.setText("绘画中请稍等");
        textView.setVisibility(View.VISIBLE);

        shutter.setVisibility(View.GONE);           //照相按钮消失
        back.setVisibility(View.GONE);              //返回按钮消失
        imageView.setVisibility(View.GONE);         //图片不可见
        renliankuang.setVisibility(View.GONE);
        startDraw.setVisibility(View.GONE);
        camera.setFacing(Facing.BACK);              //后置摄像头

        camera.open();
        camera.setVisibility(View.VISIBLE);


    }

    public void back(View view){

//        imageView.getVisibility() == View.VISIBLE && camera.getVisibility() == View.GONE
        if (retake.getVisibility() == View.VISIBLE){
            imageView.setVisibility(View.GONE);             //关闭照片区域
            camera.setVisibility(View.VISIBLE);             //打开相机
            shutter.setVisibility(View.VISIBLE);            //拍照按钮可见
            retake.setVisibility(View.GONE);

            background.setBackgroundResource(R.drawable.bg_1);      //显示拍照的背景

            textView.setText("");

            startDraw.setVisibility(View.GONE);             //开始作画按钮消失

            camera.open();
        }
        else
            this.finish();
    }

    public void finish(View view){
        this.finish();
    }


    /**
     * 保存轨迹图
     * @param trailPath 保存路径
     * @param url_in 网络路径
     * @return 返回保存路径
     */
    public static String saveTrail(String trailPath, String url_in){

        Log.d("url_in",url_in);
        File file = new File(trailPath);
        String result = null;
        if (file.exists()) {
            file.delete();
        }
        try {
            URL url = new URL(url_in);                  //读取url的文件
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();   //转为输入流
            //文件输出流
            FileOutputStream fileOutputStream = FileUtil.Input2File(inputStream,trailPath); //转为输出流
            //写入，
            fileOutputStream.flush();
            //记得要关闭写入流
            fileOutputStream.close();
            //成功的提示，写入成功后，请在对应目录中找保存的图片
            //Toast.makeText(this, "拍照成功", Toast.LENGTH_SHORT).show();
            result = trailPath;
        } catch (Exception e) {
            e.printStackTrace();
            //失败的提示
            ToastUtils.show("保存失败");
        }
        return result;
    }



}
