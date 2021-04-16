package com.live.streaming;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class Util {
    public static String LOG_TAG = "LiveLib";
    public static final int REQUEST_MEDIA_PROJECTION = 1002;
    public static int vWidth = 1920;
    public static int vHeight = 1080;
    public static int DPI = 360;

    /**
     * 获取屏幕的密度
     * @return dpi
     */
    public static int getDensityDpi(Context context){
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }
}
