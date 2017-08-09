package com.langtao.device;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

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

        CrashReport.initCrashReport(getApplicationContext(), "af614f6c90", false);
    }




    public static GlnkApplication getApp() {
        return glnkApplication;
    }
}
