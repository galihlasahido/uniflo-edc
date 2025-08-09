package com.ftpos.pay.demo;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;


/**
 * @author GuoJirui.
 * @date 2021/4/25.
 * @desc
 */
public class FtApplication extends Application {
    private static final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        //Initialize and bind the service through the service helper class
        SvrHelper.instance().init(this);
        SvrHelper.instance().bindService();
    }

    public static Handler getUiHandler() {
        return handler;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //Unbind the service through the service helper class
        SvrHelper.instance().unbindService();
    }

}
