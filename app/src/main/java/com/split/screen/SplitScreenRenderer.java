package com.split.screen;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.langtao.device.FishEyeDeviceDataSource;
import com.split.screen.shape.FourEye360;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by zzr on 2017/8/10.
 */

public class SplitScreenRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "SplitScreenRenderer";
    private Context context;
    public FourEye360 fourEye;
    public FishEyeDeviceDataSource fishEyeDevice;



    public SplitScreenRenderer(Context context, FishEyeDeviceDataSource fishEyeDevice) {
        this.context = context;
        this.fishEyeDevice = fishEyeDevice;

        fourEye = new FourEye360(context,fishEyeDevice);
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //所有关于GLES20的初始化要放在三个回调接口上，不能放在构造函数
        GLES20.glClearColor(0.0f,0.0f,0.0f, 1.0f);

        fourEye.onSurfaceCreate();

    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);

        fourEye.onSurfaceChange(width, height);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        long startTime = System.nanoTime();

        fourEye.onDrawFrame();

        long estimatedTime = System.nanoTime() - startTime;
        Log.w(TAG,"estimatedTime : "+estimatedTime);
    }



    //*****************************************************************************
    public void handleTouchDown(float x, float y) {
        fourEye.handleTouchDown(x,y);
    }

    public void handleTouchUp(float x, float y, float xVelocity, float yVelocity) {
        fourEye.handleTouchUp(x, y, xVelocity, yVelocity);
    }

    public void handleTouchMove(float x, float y) {
        fourEye.handleTouchMove(x, y);
    }
}
