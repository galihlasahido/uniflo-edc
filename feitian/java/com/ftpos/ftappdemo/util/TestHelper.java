package com.ftpos.ftappdemo.util;


import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

public class TestHelper {
    public static  void setFullscreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
