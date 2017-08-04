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
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.langtao.device.DeviceStatusManager;
import com.langtao.device.SDKinitUtil;

import static com.langtao.device.DeviceStatusManager.DEV_ID;

/**
 * Created by zzr on 2017/7/26.
 */

public class BowlActivity extends Activity implements View.OnTouchListener {

    private static final String TAG = "BowlActivity";
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
    private BowlRenderer bowlRenderer ;


    private DeviceStatusListener deviceStatusListener = new DeviceStatusListener();
    public class DeviceStatusListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(DeviceStatusManager.DSM_ON_PUSH_SVRINFO_CALL) ||
                    action.equalsIgnoreCase(DeviceStatusManager.DSM_ON_CHANGED_CALL)){
                if(bowlRenderer!=null && bowlRenderer.fishEyeDevice!=null){
                    Bundle extras = intent.getExtras();
                    String devId = (String) extras.get(DEV_ID);
                    if("804000ad".equals(devId)){
                        bowlRenderer.fishEyeDevice.connect("804000ad","admin","123456",0,1,2);
                    } else if("v3027ee621".equals(devId)){
                        bowlRenderer.fishEyeDevice.connect("v3027ee621","admin","admin",0,1,2);
                    }
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

        //opengl
        glSurfaceView = new GLSurfaceView(this);
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
            bowlRenderer = new BowlRenderer(this);
            glSurfaceView.setRenderer(bowlRenderer);
            rendererSet = true;
        } else {
            Toast.makeText(this, "this device does not support OpenGL ES 2.0",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        glSurfaceView.setOnTouchListener(this);
        setContentView(glSurfaceView);

        //  设备相关部分代码
        //SDKinitUtil.gClient.addGID("804000ad");
        SDKinitUtil.gClient.addGID("v3027ee621");

        IntentFilter filter = new IntentFilter();
        filter.addAction(DeviceStatusManager.DSM_ON_CHANGED_CALL);
        filter.addAction(DeviceStatusManager.DSM_ON_PUSH_SVRINFO_CALL);
        registerReceiver(deviceStatusListener,filter);
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
            bowlRenderer.resume();
            glSurfaceView.onResume();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rendererSet){
            bowlRenderer.pause();
            glSurfaceView.onPause();
        }
        if(null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(deviceStatusListener);
    }

    private VelocityTracker mVelocityTracker = null;
    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;
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
                Log.d(TAG,"ACTION_POINTER_DOWN oldDist : "+oldDist);
                mode = 2;
                break;
        }
        // ------------------------------------------------------------
        if(event.getAction() == MotionEvent.ACTION_UP){
            //---双击屏幕--------
            if(mPreviousUpEvent!=null){
                mCurrentDownEvent = MotionEvent.obtain(event);
            }else{
                mPreviousUpEvent = MotionEvent.obtain(event);
            }
            if(mPreviousUpEvent!=null && mCurrentDownEvent!=null){
                if(checkDoubleClick(mPreviousUpEvent, mCurrentDownEvent)){
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            bowlRenderer.handleDoubleClick();
                        }
                    });
                }
                mPreviousUpEvent = null;
                mCurrentDownEvent = null;
            }
            //---滑动屏幕手指离开惯性--------
            final float x = event.getX();
            final float y = event.getY();
            final float xVelocity = mVelocityTracker.getXVelocity();
            final float yVelocity = mVelocityTracker.getYVelocity();
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    bowlRenderer.handleTouchUp(x, y, xVelocity, yVelocity);
                }
            });
        }
        else if(event.getAction() == MotionEvent.ACTION_DOWN){
            if (mode == 1) {
                final float x = event.getX();
                final float y = event.getY();
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        bowlRenderer.handleTouchDown(x, y);
                    }
                });

                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
            }
        }
        else if(event.getAction() ==MotionEvent.ACTION_MOVE){
            if (mode == 2) {
                //双指操作
                float newDist = spacing(event);
                if ( (newDist > oldDist + 10) || (newDist < oldDist - 15) ) {
                    final float distance = newDist - oldDist;
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            bowlRenderer.handleMultiTouch(distance);
                        }
                    });
                    oldDist = newDist;
                }
            }
            if(mode == 1){
                final float x = event.getX();
                final float y = event.getY();
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        bowlRenderer.handleTouchMove(x,y);
                    }
                });

                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
            }
        }
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private final int DOUBLE_TAP_TIMEOUT = 500;
    private boolean checkDoubleClick(MotionEvent firstUp, MotionEvent secondUp) {
        long l = secondUp.getEventTime() - firstUp.getEventTime();
        Log.d(TAG,"secondUp.getEventTime() - firstUp.getEventTime() = "+l);
        if (secondUp.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }
        int deltaX = (int) secondUp.getX() - (int) firstUp.getX();
        int deltaY = (int) secondUp.getY() - (int) firstUp.getY();
        int i = deltaX * deltaX + deltaY * deltaY;
        Log.d(TAG,"deltaX * deltaX + deltaY * deltaY = "+i);
        return deltaX * deltaX + deltaY * deltaY < 5000;
    }




}
