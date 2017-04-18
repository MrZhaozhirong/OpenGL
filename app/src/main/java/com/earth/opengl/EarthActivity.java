package com.earth.opengl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.earth.opengl.utils.LoggerConfig;

/**
 * Created by nicky on 2017/4/17.
 */

public class EarthActivity extends Activity implements View.OnTouchListener {
    public final static String TAG = "OpenGLActivity";
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    final EarthRenderer openGLRenderer = new EarthRenderer(this);

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
            glSurfaceView.setRenderer(openGLRenderer);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet){
            glSurfaceView.onResume();
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
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mode = 1;
                break;
            case MotionEvent.ACTION_UP:
                mode = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode -= 1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                mode += 1;
                break;
        }
        if(LoggerConfig.ON){
            Log.w(TAG, "mode : "+mode);
        }
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if (mode == 1) {
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        openGLRenderer.handleTouchDown(x, y);
                    }
                });
            }
        }
        else if(event.getAction() ==MotionEvent.ACTION_MOVE) {
            if (mode >= 2) {
                float newDist = spacing(event);
                if (newDist > oldDist + 10) {
                    final float distance = newDist - oldDist;
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            openGLRenderer.handleMultiTouch(distance,true);
                        }
                    });
                    oldDist = newDist;
                }
                if (newDist < oldDist - 10) {
                    final float distance = oldDist - newDist;
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            openGLRenderer.handleMultiTouch(distance,false);
                        }
                    });
                    oldDist = newDist;
                }
            }else{
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        openGLRenderer.handleTouchDrag(x,y);
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
}
