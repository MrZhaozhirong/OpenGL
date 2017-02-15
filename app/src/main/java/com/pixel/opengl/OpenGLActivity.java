package com.pixel.opengl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by ZZR on 2017/2/6.
 */

public class OpenGLActivity extends Activity implements View.OnTouchListener {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    final OpenGLRenderer openGLRenderer = new OpenGLRenderer(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public boolean onTouch(View v, MotionEvent event) {
        if(event != null){
            // Convert touch coordinates into normalized device coordinates.
            // keeping in mind that Android's Y coordinates are inverted.
            // 将触摸坐标转换成标准化设备坐标。
            // 记住Android的Y坐标是倒置的。而且还有归一化
            final float normalizedX =
                    (event.getX() /(float) v.getWidth()) * 2 - 1 ;
            final float normalizedY =
                    -((event.getY() /(float) v.getHeight()) * 2 - 1 );
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        openGLRenderer.handleTouchPress(normalizedX,normalizedY);
                    }
                });
            }else if(event.getAction() == MotionEvent.ACTION_MOVE){
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        openGLRenderer.handleTouchDrag(normalizedX,normalizedY);
                    }
                });
            }
            return true;
        }else{
            return false;
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

}
