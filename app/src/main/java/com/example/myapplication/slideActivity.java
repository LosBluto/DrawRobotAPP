package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.ui.service.HttpService;
import com.example.myapplication.ui.views.SlideView;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastWhiteStyle;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class slideActivity extends AppCompatActivity {

    //装载GIF的区域
    public GifImageView gif1;

//    SlideView slideView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);

        ToastUtils.init(getApplication(), new ToastWhiteStyle(getApplicationContext()));
        isHasPermission();

        gif1 = findViewById(R.id.gif1);

//        slideView = findViewById(R.id.slidView);
//        slideView.setVisibility(View.VISIBLE);
//        slideView.setmLockListener(new SlideView.OnLockListener() {
//            @Override
//            public void onOpenLockSuccess() {
//                gif1.setVisibility(View.GONE);
//                slideView.setVisibility(View.GONE);
//                changeActivity();
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        gif1.setVisibility(View.VISIBLE);
//        slideView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void changeActivity(View view){
        Intent intent = new Intent(slideActivity.this, MainActivity.class);
        startActivity(intent);
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
                            XXPermissions.gotoPermissionSettings(slideActivity.this);
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
        if (XXPermissions.isHasPermission(slideActivity.this, Permission.Group.STORAGE)) {

        }else {
            requestPermission();
        }
    }

    public void change(View view){
        Intent intent = new Intent(slideActivity.this, MainActivity.class);
        startActivity(intent);
    }
}