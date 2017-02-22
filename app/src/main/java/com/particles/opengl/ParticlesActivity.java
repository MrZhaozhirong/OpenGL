package com.particles.opengl;

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
 * Created by ZZR on 2017/2/17.
 */

public class ParticlesActivity extends Activity implements View.OnTouchListener {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    final ParticlesRenderer particlesRenderer = new ParticlesRenderer(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);

        int glVersion = getGLVersion();
        if(glVersion > 0x20000){
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setRenderer(particlesRenderer);
            glSurfaceView.setOnTouchListener(this);
            rendererSet = true;
        } else {
            Toast.makeText(this, "this device does not support OpenGL ES 2.0",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        setContentView(glSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(rendererSet){
            glSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet){
            glSurfaceView.onPause();
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

    private float previousX,previousY;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event!=null){
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                previousX = event.getX();
                previousY = event.getY();
            }else if(event.getAction() == MotionEvent.ACTION_MOVE){
                final float deltaX = event.getX() - previousX;
                final float deltaY = event.getY() - previousY;

                previousX = event.getX();
                previousY = event.getY();

                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        particlesRenderer.handleTouchDrag(deltaX, deltaY);
                    }
                });
            }
            return true;
        }else{
            return false;
        }
    }
}
