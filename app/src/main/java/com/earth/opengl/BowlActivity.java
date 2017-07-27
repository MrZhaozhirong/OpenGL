package com.earth.opengl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by zzr on 2017/7/26.
 */

public class BowlActivity extends Activity implements View.OnTouchListener {

    private static final String TAG = "BowlActivity";
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
    final BowlRenderer bowlRenderer = new BowlRenderer(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
            glSurfaceView.setRenderer(bowlRenderer);
            rendererSet = true;
        } else {
            Toast.makeText(this, "this device does not support OpenGL ES 2.0",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        glSurfaceView.setOnTouchListener(this);
        setContentView(glSurfaceView);
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
            glSurfaceView.onResume();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(rendererSet){
            glSurfaceView.onPause();
        }
    }



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
            if(mode == 1){//单指操作
                // 在获取速度之前总要进行以上两步
                final float x = event.getX();
                final float y = event.getY();
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        bowlRenderer.handleTouchMove(x,y);
                    }
                });
            }
        }
        return true;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private final int DOUBLE_TAP_TIMEOUT = 200;
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
