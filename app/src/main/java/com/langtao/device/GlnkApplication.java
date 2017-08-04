package com.langtao.device;

import android.app.Application;

/**
 * Created by zzr on 2017/8/3.
 */

public class GlnkApplication extends Application {

    private static GlnkApplication glnkApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        glnkApplication = this;

        SDKinitUtil.initGlnkSDK();

    }




    public static GlnkApplication getApp() {
        return glnkApplication;
    }
}
