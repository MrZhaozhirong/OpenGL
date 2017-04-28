package com.earth.opengl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.earth.opengl.utils.LoggerConfig;
import com.pixel.opengl.util.Geometry;

/**
 * Created by nicky on 2017/4/17.
 */

public class EarthActivity extends Activity implements View.OnTouchListener {
    public final static String TAG = "OpenGLActivity";
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    final EarthRenderer earthRenderer = new EarthRenderer(this);

    // 速度相关 http://leonard-peng.github.io/2016/02/21/android-basic-gesture-detec/
    // 速度相关 http://blog.csdn.net/bingxianwu/article/details/7446799
    private VelocityTracker mVelocityTracker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        glSurfaceView = new GLSurfaceView(this);
        int glVersion = getGLVersion();
        if(glVersion > 0x20000){
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setRenderer(earthRenderer);
            rendererSet = true;
        } else {
            Toast.makeText(this, "this device does not support OpenGL ES 2.0",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        glSurfaceView.setOnTouchListener(this);
        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(rendererSet){
            glSurfaceView.onPause();
        }
        if(null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet){
            glSurfaceView.onResume();
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private int getGLVersion(){
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo deviceConfigurationInfo =
                activityManager.getDeviceConfigurationInfo();
        int reqGlEsVersion = deviceConfigurationInfo.reqGlEsVersion;
        return reqGlEsVersion;
    }

    private int mode = 0;
    float oldDist;
    @Override
    public boolean onTouch(View v, MotionEvent event) {

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
        if(LoggerConfig.ON){
            Log.w(TAG, "mode : "+mode);
        }
        // ------------------------------------------------------------
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if (mode == 1) {
                final float x = event.getX();
                final float y = event.getY();
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        earthRenderer.handleTouchDown(x, y);
                    }
                });
                //20170425 增加速度
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
            }
        }
        else if(event.getAction() == MotionEvent.ACTION_UP){
            final float x = event.getX();
            final float y = event.getY();
            final float xVelocity = mVelocityTracker.getXVelocity();
            final float yVelocity = mVelocityTracker.getYVelocity();
            glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        earthRenderer.handleTouchUp(x, y, xVelocity, yVelocity);
                    }
                });

        }
        else if(event.getAction() ==MotionEvent.ACTION_MOVE) {
            if (mode == 2) {
                //双指操作
                float newDist = spacing(event);
                if ( (newDist > oldDist + 10) || (newDist < oldDist - 10) ) {
                    final float distance = newDist - oldDist;
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            earthRenderer.handleMultiTouch(distance);
                        }
                    });
                    oldDist = newDist;
                }
            }
            if(mode == 1){//单指操作
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                // 在获取速度之前总要进行以上两步
                final float x = event.getX();
                final float y = event.getY();
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        earthRenderer.handleTouchDrag(x,y);
                    }
                });
            }
        }
        return true;//返回 true 表示该动作已被处理
    }





    private float spacing(Geometry.Point oldPoint,Geometry.Point newPoint){
        float x = oldPoint.x - newPoint.x;
        float y = oldPoint.y - newPoint.y;
        return (float) Math.sqrt(x*x + y*y);
    }
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

}
