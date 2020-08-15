package com.example.myapplication.ui.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ThreadHelper {

    private static Handler backgroundHandler;

    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    static {
        init();
    }

    public static void init() {
        destroy();
        HandlerThread singleExecutor = new HandlerThread(ThreadHelper.class.getName());
        singleExecutor.start();
        backgroundHandler = new Handler(singleExecutor.getLooper());
    }

    public static void destroy() {
        if (backgroundHandler != null) {
            backgroundHandler.getLooper().quit();
        }
    }

    public static void clear() {
        if (backgroundHandler != null) {
            backgroundHandler.removeCallbacksAndMessages(null);
            mainHandler.removeCallbacksAndMessages(null);
        }
    }

    public static void runSequentialTask(Runnable runnable) {
        backgroundHandler.post(runnable);
    }

    public static void runSequentialTask(Runnable runnable, long delay) {
        backgroundHandler.postDelayed(runnable, delay);
    }

    public static void runMainDelay(Runnable runnable, long delay) {
        mainHandler.postDelayed(runnable, delay);
    }

    public static void runMain(Runnable runnable) {
        mainHandler.post(runnable);
    }
}
