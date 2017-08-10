package com.earth.opengl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.langtao.device.DeviceStatusManager;
import com.langtao.device.FishEyeDeviceDataSource;
import com.langtao.device.SDKinitUtil;
import com.langtao.device.YUVFrame;
import com.pixel.opengl.R;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import static com.langtao.device.DeviceStatusManager.DEV_ID;

/**
 * Created by zzr on 2017/8/9.
 */

public class CurvedPlateActivity extends Activity implements View.OnTouchListener {

    private static final String TAG = "CurvedPlateActivity";
    private RelativeLayout root;
    private FishEyeDeviceDataSource curvedPlateDevice;
    private SafeHandler safeHandler = new SafeHandler(this);

    private DeviceStatusReceiver deviceStatusReceiver = new DeviceStatusReceiver();

    public class DeviceStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(DeviceStatusManager.DSM_ON_PUSH_SVRINFO_CALL) ||
                    action.equalsIgnoreCase(DeviceStatusManager.DSM_ON_CHANGED_CALL)){

                Bundle extras = intent.getExtras();
                String devId = (String) extras.get(DEV_ID);
                if("804000ad".equals(devId)){
                    curvedPlateDevice.connect("804000ad","admin","123456",0,1,2);
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_crooked);
        root = (RelativeLayout) this.findViewById(R.id.root);
        //"804000ad","admin","123456"
        SDKinitUtil.gClient.addGID("804000ad");

        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceStatusManager.DSM_ON_CHANGED_CALL);
        filter.addAction(DeviceStatusManager.DSM_ON_PUSH_SVRINFO_CALL);
        registerReceiver(deviceStatusReceiver,filter);

        curvedPlateDevice = new FishEyeDeviceDataSource(this);
        curvedPlateDevice.setYuvCallback(new FishEyeDeviceDataSource.YuvCallback() {
            @Override
            public void yuv_callback(int width, int height, YUVFrame frame) {
                if(curvedPlateDevice.isInitedFishDevice()){
                    Message obtain = new Message();
                    obtain.what = 100;
                    obtain.arg1 = width;
                    obtain.arg2 = height;
                    obtain.obj = frame;
                    safeHandler.sendMessage(obtain);//initOpenGL();
                }
            }
        });
    }


    private boolean rendererSet = false;
    private GLSurfaceView glSurfaceView;
    private CurvedPlateRenderer curvedPlateRenderer;
    private void initOpenGL(int frameWidth,int frameHeight,YUVFrame frame) {
        if(rendererSet) return;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        glSurfaceView = new GLSurfaceView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width,width);
        layoutParams.width = width;
        layoutParams.height = width;
        glSurfaceView.setLayoutParams(layoutParams);
        //opengl
        int glVersion = getGLVersion();
        final boolean supportsEs2 =
                glVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));
        if(supportsEs2){
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setPreserveEGLContextOnPause(true);
            curvedPlateRenderer = new CurvedPlateRenderer(this, curvedPlateDevice, frameWidth,frameHeight,frame);
            glSurfaceView.setRenderer(curvedPlateRenderer);
            rendererSet = true;
            curvedPlateRenderer.resume();
        } else {
            Toast.makeText(this, "this device does not support OpenGL ES 2.0",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        glSurfaceView.setOnTouchListener(this);
        glSurfaceView.setVisibility(View.VISIBLE);
        root.addView(glSurfaceView);
    }

    private int getGLVersion(){
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo deviceConfigurationInfo =
                activityManager.getDeviceConfigurationInfo();
        int reqGlEsVersion = deviceConfigurationInfo.reqGlEsVersion;
        return reqGlEsVersion;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet){
            curvedPlateRenderer.resume();
            glSurfaceView.onResume();
        }
        timer = new Timer();
        timer.schedule(clearDoubleClick, 500, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rendererSet){
            curvedPlateRenderer.pause();
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(deviceStatusReceiver);
    }








    //自动旋转相关
    private Timer timer;
    private int click_count = 0;
    private TimerTask clearDoubleClick = new TimerTask() {
        @Override
        public void run() {
            click_count = 0;
        }
    };

    private int mode = 0;
    float oldDist;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // -------------判断多少个触碰点---------------------------------
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mode = 1;
                break;
            case MotionEvent.ACTION_UP:
                mode = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                mode = 2;
                break;
        }
        // -------------对应操作-----------------------------------------
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            click_count++;
            if(click_count == 2){
                Log.w(TAG,"double click");
                click_count = 0;
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        curvedPlateRenderer.handleDoubleClick();
                    }
                });
            }
            if (mode == 1) {
                final float x = event.getX();
                final float y = event.getY();
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        curvedPlateRenderer.handleTouchDown(x, y);
                    }
                });
            }
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE){
            if (mode == 2) {
                //双指操作
                float newDist = spacing(event);
                if ( (newDist > oldDist + 10) || (newDist < oldDist - 15) ) {
                    final float distance = newDist - oldDist;
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            curvedPlateRenderer.handleMultiTouch(distance);
                        }
                    });
                    oldDist = newDist;
                }
            }
            final float x = event.getX();
            final float y = event.getY();
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    curvedPlateRenderer.handleTouchMove(x,y);
                }
            });
        }
        else if(event.getAction() == MotionEvent.ACTION_UP){
            //---滑动屏幕手指离开惯性--------
            final float x = event.getX();
            final float y = event.getY();
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    curvedPlateRenderer.handleTouchUp(x, y);
                }
            });
        }
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }













    static class SafeHandler extends Handler {
        WeakReference<CurvedPlateActivity> mActivity;

        public SafeHandler(CurvedPlateActivity activity){
            mActivity = new WeakReference<CurvedPlateActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    CurvedPlateActivity activity = mActivity.get();
                    if(activity!=null){
                        activity.initOpenGL(msg.arg1,  msg.arg2, (YUVFrame) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
