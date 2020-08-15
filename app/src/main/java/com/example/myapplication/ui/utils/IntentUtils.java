package com.example.myapplication.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * activity跳转封装工具
 *
 * Created by WangChao on 2019/3/5.
 */
public class IntentUtils {

    public static void startToActivity(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }

    public static void startToActivity(Context context, Class clazz, Bundle bundle) {
        Intent intent = new Intent(context, clazz);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void startToActivity(Context context, Class clazz, int... flags) {
        Intent intent = new Intent(context, clazz);
        for (int flag : flags) {
            intent.addFlags(flag);
        }
        context.startActivity(intent);
    }

    public static void startToActivity(Context context, Class clazz, Bundle bundle, int... flags) {
        Intent intent = new Intent(context, clazz);
        intent.putExtras(bundle);
        for (int flag : flags) {
            intent.addFlags(flag);
        }
        context.startActivity(intent);
    }

    public static void startToActivityForResult(Activity activity, Class clazz, int requestCode) {
        Intent intent = new Intent(activity, clazz);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startToActivityForResult(Activity activity, Class clazz, Bundle bundle, int requestCode) {
        Intent intent = new Intent(activity, clazz);
        intent.putExtras(bundle);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startToActivityForResult(Activity activity, Class clazz, int requestCode, int... flags) {
        Intent intent = new Intent(activity, clazz);
        for (int flag : flags) {
            intent.addFlags(flag);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startToActivityForResult(Activity activity, Class clazz, Bundle bundle, int requestCode, int... flags) {
        Intent intent = new Intent(activity, clazz);
        intent.putExtras(bundle);
        for (int flag : flags) {
            intent.addFlags(flag);
        }
        activity.startActivityForResult(intent, requestCode);
    }
}
