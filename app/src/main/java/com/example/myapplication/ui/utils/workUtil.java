package com.example.myapplication.ui.utils;

import com.example.myapplication.ui.service.BlueService;
import com.hjq.toast.ToastUtils;

import java.io.File;
import java.util.Date;


public class workUtil {
    public static String message;

    public static String yaobiUp(BlueService.ConnectedThread thread,File file){
        thread.setFile(file);
        thread.write("yaobiUp");
        try {
            timeout(thread, BlueService.yaobiUpOver, 10000);
        }catch (InterruptedException e){
            e.printStackTrace();
            return null;
        }
         return message;
    }

    public static String yaobiDown(BlueService.ConnectedThread thread){
        thread.write("yaobiDown");
        try {
            timeout(thread, BlueService.yaobiDownOver, 10000);
        }catch (InterruptedException e){
            e.printStackTrace();
            return null;
        }
        return message;
    }

    public static String jinzhi(BlueService.ConnectedThread thread){
        thread.write("jinZhi");
        try {
            timeout(thread, BlueService.jinZhiOver, 10000);
        }catch (InterruptedException e){
            e.printStackTrace();
            return null;
        }
        return message;
    }

    public static String huihua(BlueService.ConnectedThread thread, File file) {
//        thread.setFile(file);
        thread.write("huiHua");
        try {
            timeout(thread, BlueService.huiHuaOver, 10000);
        }catch (InterruptedException e){
            e.printStackTrace();
            return null;
        }
        return message;
    }

    /*
    超时循环
     */
    private static void timeout(final BlueService.ConnectedThread thread, final String target, final long time) throws InterruptedException {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Date curDate = new Date(System.currentTimeMillis());
                while (true){
                    try {
                        Thread.sleep(500);                              //每个循环睡500ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Date endDate = new Date(System.currentTimeMillis());
                    if (endDate.getTime() - curDate.getTime() >= time) {             //超时
                        message = null;
                        ToastUtils.show("请求连接超时");

                        break;
                    }
                    if (thread.getMessage()!= null && thread.getMessage().equals(target)) {             //获取到需要的内容
                        message = "success";
                        break;
                    }
                }
            }
        });
        thread1.join();
        thread1.start();
        

    }
}
