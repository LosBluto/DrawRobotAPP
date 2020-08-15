package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.myapplication.ui.pojo.ImgResult;
import com.example.myapplication.ui.utils.Exif;
import com.example.myapplication.ui.utils.FileUtil;
import com.example.myapplication.ui.utils.HttpUtil;
import com.example.myapplication.ui.utils.otherUtil;
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
    private static final int ERROR = -1;
    private static final int DRAWOVER = 1;                 //生成简笔画线程
    private static final int UPDATEOVER = 2;                //生成完成

    private static boolean isUp = false;                    //标志摇臂是否在上面

    private String url = "192.168.64.157";

    //CameraView对象
    private CameraView camera;
    //返回按钮
    private ImageButton back;
    //shutter快门按钮
    private ImageButton shutter;
    //上传按钮
    private ImageButton uploadPic;
    //作画按钮
    private ImageButton startDraw;
    //显示时间的文本框
    private TextView textView;
    //存放图片的区域
    private ImageView imageView;
    //等待中
    private GifImageView waiting;
//    //人脸框
    private ImageView renliankuang;
    //等待时间
    private static final int waitTime = 3;
    //用于记录时间
    private static int timer = waitTime;
    //判断handler是否在执行的标志位
    private boolean ifHandler = false;

    MotionEvent motionEvent;

    //操作器handler
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){                            //处理子线程的handler
        public void handleMessage(Message message){
            switch (message.what) {
                case DRAWOVER:
                    ToastUtils.show("绘画完成");
                    textView.setVisibility(View.GONE);      //绘画提示消失
//                    camera.stop();                          //关闭摄像头
                    camera.close();
                    camera.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);  //简笔画可见

                    String temp = HttpUtil.jinzhi();        //绘画完毕开始进纸

                    break;
                case UPDATEOVER:
                    Log.d("test","handler");
                    ToastUtils.show("生成成功");
                    imageView.setImageBitmap(bitmap);       //更换为生成图片
                    uploadPic.setVisibility(View.GONE);     //上传按钮消失
                    textView.setText("");      //生成中提示消失
                    startDraw.setVisibility(View.VISIBLE);  //开始作画按钮出现
                    waiting.setVisibility(View.GONE);       //等待图片消失

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        //实例化对象
        camera = findViewById(R.id.preview_camera);
        camera.setLifecycleOwner(this);
        shutter = findViewById(R.id.takePhoto);
        back = findViewById(R.id.back);

        uploadPic = findViewById(R.id.uploadPic);
        uploadPic.setVisibility(View.GONE);

        textView = findViewById(R.id.textView);

        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);

        startDraw = findViewById(R.id.getTrail);
        startDraw.setVisibility(View.GONE);

        waiting = findViewById(R.id.waiting);
        waiting.setVisibility(View.GONE);
        waiting.bringToFront();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    Uri resultUri = UCrop.getOutput(data);
                    System.out.println("裁剪成功"+resultUri.toString());
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                        saveBitmap(bitmap,"current.jpg",50);        //保存图片

                        shutter.setVisibility(View.GONE);           //隐藏拍照按钮
                        camera.setVisibility(View.GONE);            //相机隐藏和关闭
                        camera.close();

                        imageView.setImageBitmap(bitmap);           //图片区域可见
                        imageView.setVisibility(View.VISIBLE);

                        uploadPic.setVisibility(View.VISIBLE);          //上传按钮
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            //错误裁剪的结果
            case UCrop.RESULT_ERROR:
                if (resultCode == RESULT_OK) {
                    final Throwable cropError = UCrop.getError(data);
                    handleCropError(cropError);
                }
                break;
        }
    }

    //处理剪切失败的返回值
    private void handleCropError(Throwable cropError) {
        deleteTempPhotoFile();
        if (cropError != null) {
            Toast.makeText(CameraActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(CameraActivity.this, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
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

    //图片裁剪的方法
    private void startCrop(){
        UCrop.Options options = new UCrop.Options();
        //裁剪后图片保存在文件夹中
        Uri uri = Uri.fromFile(new File(dirpath, "current.jpg"));
        Uri destinationUri = Uri.fromFile(new File(dirpath, "tete.jpg"));
        UCrop uCrop = UCrop.of(uri, destinationUri);//第一个参数是裁剪前的uri,第二个参数是裁剪后的uri
        uCrop.withAspectRatio(1,1);//设置裁剪框的宽高比例
        //下面参数分别是缩放,旋转,裁剪框的比例
        options.setAllowedGestures(UCropActivity.NONE,UCropActivity.NONE,UCropActivity.ALL);
        options.setToolbarTitle("裁剪");//设置标题栏文字
        options.setCropGridStrokeWidth(2);//设置裁剪网格线的宽度(我这网格设置不显示，所以没效果)
        options.setCropFrameStrokeWidth(10);//设置裁剪框的宽度
//        options.setMaxScaleMultiplier(3);//设置最大缩放比例
        options.setHideBottomControls(true);//隐藏下边控制栏
        options.setShowCropGrid(false);  //设置是否显示裁剪网格
        options.setShowCropFrame(false); //设置是否显示裁剪边框(true为方形边框)
        options.setToolbarWidgetColor(Color.parseColor("#ffffff"));//标题字的颜色以及按钮颜色
        options.setDimmedLayerColor(Color.parseColor("#FFD700"));//设置裁剪外颜色
        options.setToolbarColor(Color.parseColor("#FFD700")); // 设置标题栏颜色
        options.setStatusBarColor(Color.parseColor("#FFD700"));//设置状态栏颜色
        options.setCropGridColor(Color.parseColor("#ffffff"));//设置裁剪网格的颜色
        options.setCropFrameColor(Color.parseColor("#ffffff"));//设置裁剪框的颜色
        uCrop.withOptions(options);
        uCrop.start(this);
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
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                //将bitmap按解析出来的角度进行旋转orientation
                //有些机型会将照片旋转90度
                Matrix m = new Matrix();
                m.postScale(-1,1);
                m.postTranslate(bitmap.getWidth(),0);
//                m.postRotate(orientation);
                int cutHeight = ((int) (bitmap.getHeight()*0.166)+1)/2;
                System.out.println(bitmap.getWidth());
                bitmap = Bitmap.createBitmap(bitmap, 0, cutHeight, bitmap.getWidth(),
                        bitmap.getHeight()-cutHeight*2, m, true);
                System.out.println(bitmap.getHeight()+"width"+bitmap.getWidth());
                saveBitmap(bitmap,"current.jpg",100);

                ToastUtils.show("拍照成功");

                //进行拍摄成功后的处理
                shutter.setVisibility(View.GONE);           //隐藏拍照按钮
                camera.setVisibility(View.GONE);            //相机隐藏和关闭
                camera.close();

                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);

                uploadPic.setVisibility(View.VISIBLE);



//                startCrop();
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
     * @param view
     */
    @SuppressLint("SetTextI18n")
    public void UpdateImage(View view) {
        final File file = new File(dirpath, "current.jpg");
//        imageView.setVisibility(View.VISIBLE);

//        textView.setVisibility(View.VISIBLE);
////        textView.setText("Generating");
        waiting.setVisibility(View.VISIBLE);

        Thread child = new Thread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {

                imgResult = HttpUtil.genStickByAPI(file);

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
        Thread child = new Thread(new Runnable() {
            @Override
            public void run() {
//                String trail = HttpUtil.getTrail("http://47.113.110.11:5901/genTrail",imgResult.getOutput());
                Log.d("output",imgResult.getOutput());
                String trail = HttpUtil.genTrail("http://120.55.193.46:8012/genTrail",imgResult.getOutput());
                Log.d("Trail",trail);
                saveTrail(trailPath,trail);                     //保存trail文件
                String result = null;
                result = HttpUtil.yaobiUP("http://"+url+":5000/yaobiUp");    //发送摇臂请求


                Message message = new Message();
                String drawingResult;
                if (result.equals("success")) {              //判断是否摇臂成功
                    drawingResult = HttpUtil.startDrawing(trailPath);
                    isUp = true;                        //摇臂成功修改标志位
                }
                else{
                    message.what = ERROR;
                    return;
                }

                if (drawingResult == null) {              //判断绘画是否成功
                    message.what = ERROR;
                    return;
                }
                Log.d("Trail",drawingResult);
                if (drawingResult.equals("success")) {
                    HttpUtil.yaobiUP("http://"+url+":5000/yaobiDown");
                    message.what = DRAWOVER;
                } else if (drawingResult.equals("error"))
                    message.what = ERROR;

                handler.sendMessage(message);
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
        if (imageView.getVisibility() == View.VISIBLE && camera.getVisibility() == View.GONE){
            imageView.setVisibility(View.GONE);             //关闭照片区域
            camera.setVisibility(View.VISIBLE);             //打开相机
            shutter.setVisibility(View.VISIBLE);            //拍照按钮可见
            uploadPic.setVisibility(View.GONE);             //上传按钮消失

            waiting.setVisibility(View.GONE);
            textView.setText("");

            startDraw.setVisibility(View.GONE);             //开始作画按钮消失

            camera.open();
        }
        else
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
