package com.earth.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.earth.opengl.shape.Ball;
import com.earth.opengl.utils.LoggerConfig;
import com.earth.opengl.utils.MatrixHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * Created by nicky on 2017/4/17.
 */
public class EarthRenderer implements GLSurfaceView.Renderer {
    public final static String TAG = "OpenGLRenderer";

    private Context context;
    Ball ball;

    public EarthRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //设置屏幕背景色RGBA
        GLES20.glClearColor(0.5f,0.5f,0.5f, 1.0f);
        //打开深度检测
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //打开背面剪裁
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        ball = new Ball(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / height;
        // 调用此方法计算产生透视投影矩阵
        MatrixHelper.setProjectFrustum(-ratio,ratio, -1, 1, 20, 100);
        // 调用此方法产生摄像机9参数位置矩阵
        MatrixHelper.setCamera(0, 0, 30, //摄像机位置
                            0f, 0f, 0f, //摄像机目标视点
                            0f, 1.0f, 0.0f);//摄像机头顶方向向量
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清除深度缓冲与颜色缓冲
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        ball.draw();
    }


    public void handleTouchDown(float x, float y) {
        if(ball!=null){
            ball.mLastX = x;
            ball.mLastY = y;
            if(LoggerConfig.ON){
                Log.w(TAG, "ball.mLastX : "+x);
                Log.w(TAG, "ball.mLastY : "+y);
            }
        }
    }

    public void handleTouchDrag(float x, float y) {
        if(ball!=null){
            float offsetX = ball.mLastX - x;
            float offsetY = ball.mLastY - y;
            MatrixHelper.rotate(offsetX/ball.step, 0, 1, 0);
            MatrixHelper.rotate(offsetY/ball.step, 1, 0, 0);
            if(LoggerConfig.ON){
                Log.w(TAG, "offsetX : "+offsetX);
                Log.w(TAG, "offsetY : "+offsetY);
            }
        }
    }

    public void handleMultiTouch(float distance, boolean isZoom){
        float scale = distance / 10;
        if(LoggerConfig.ON){
            Log.w(TAG, "MultiTouch scale: "+scale);
        }
        if(isZoom){
            MatrixHelper.scale(1/scale, 1/scale, 1/scale);
        }else{
            MatrixHelper.scale(scale, scale, scale);
        }
    }
}
