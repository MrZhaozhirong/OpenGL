package com.langtao.device;

import android.util.Log;

import glnk.client.GlnkClient;
import glnk.rt.MyRuntime;

/**
 * Created by shanlin on 2016/3/3.
 */
public class SDKinitUtil {

    private static int count = 0;

    public static GlnkClient gClient = null;

    public static void initGlnkSDK(){
        if(count > 0) {
            Log.w("SDKinitUtil", "SDKinitUtil Already initialized");
            return;
        }
        if(!MyRuntime.supported()){
            return;
        }
        if(gClient==null){
            gClient = GlnkClient.getInstance();
        }
        GlnkApplication app = GlnkApplication.getApp();
        if(app == null){
            Log.w("SDKinitUtil", "GlnkApplication has been destroy");
            return;
        }
        gClient.init(app, "langtao", "20140909", "1234567890", 101, 1);
        gClient.setStatusAutoUpdate(true);
        gClient.setOnDeviceStatusChangedListener(DeviceStatusManager.getInstance());
        gClient.setAppKey("6Dfua7h3VcJDS9Y0/6Kl953+9ZoNBreapNYoZA==");
        gClient.start();
        count++;
        Log.d("SDKinitUtil", "SDKInitUtil initializ done ... ...");
    }

    public static void release(){
        if(gClient == null || count == 0){
            return;
        }
        gClient.setOnDeviceStatusChangedListener(null);
        gClient.release();
        count = 0;
        gClient = null;
        Log.d("SDKinitUtil", "SDKInitUtil release ... ...");
    }


}
