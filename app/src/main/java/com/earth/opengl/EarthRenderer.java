package com.earth.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
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
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        ball = new Ball(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / (float) height;
        // 调用此方法计算产生透视投影矩阵
        //MatrixHelper.setProjectFrustum(-ratio,ratio, -1, 1, 0.1f, 400f);
        MatrixHelper.perspectiveM(MatrixHelper.mProjectionMatrix,
                (float) Ball.overture,
                (float)width/(float)height, 0.1f, 400f);
        // 调用此方法产生摄像机9参数位置矩阵
        MatrixHelper.setCamera(0, 0, 3f, //摄像机位置
                            0f, 0f, -1.0f, //摄像机目标视点
                            0f, 1.0f, 0.0f);//摄像机头顶方向向量
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清除深度缓冲与颜色缓冲
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //updateBallMatrix();
        ball.draw();
    }

    private void updateBallMatrix() {
        Matrix.setIdentityM(MatrixHelper.mModelMatrix, 0);
        Matrix.scaleM(MatrixHelper.mModelMatrix, 0, 1.0f, 1.0f, 1.0f);
        float[] matrixRotationX = MatrixHelper.rotateX(ball.mfingerRotationX);
        Matrix.multiplyMM(MatrixHelper.mModelMatrix,0, MatrixHelper.mModelMatrix,0, matrixRotationX,0);

        float[] matrixRotationY = MatrixHelper.rotateY(ball.mfingerRotationY);
        Matrix.multiplyMM(MatrixHelper.mModelMatrix,0, MatrixHelper.mModelMatrix,0, matrixRotationY,0);

        Matrix.setLookAtM(MatrixHelper.mViewMatrix,0,  0, 3f, 0, 0f, 0f, -1.0f, 0f, 1.0f, 0.0f);
        Matrix.translateM(MatrixHelper.mViewMatrix,0, 0f,0f,ball.scale);
    }

    public void handleTouchUp(float x, float y) {
        if(ball!=null){
            ball.mLastX = 0;
            ball.mLastY = 0;
            Log.w(TAG,"handleTouchUp ball.mLastPostion clear");
        }
    }

    public void handleTouchDown(float x, float y) {
        if(ball!=null){
            ball.mLastX = x;
            ball.mLastY = y;
            Log.w(TAG,"handleTouchUp ball.mLastPostion x:"+x+"     y:"+y);
        }
    }

    public void handleTouchDrag(float x, float y) {
        if(ball != null){
//            float offsetX = ball.mLastX - x;
//            float offsetY = ball.mLastY - y;
//            MatrixHelper.rotate(offsetX, 0, 1, 0);
//            MatrixHelper.rotate(offsetY, 1, 0, 0);
//---------------------------------------------------------------------------------
            float offsetX = ball.mLastX - x;
            float offsetY = ball.mLastY - y;
            offsetY *= 0.005;
            offsetX *= 0.005;
            ball.mfingerRotationX += offsetY * Ball.overture/100;
            if(ball.mfingerRotationX*2 > Math.PI/2 ) ball.mfingerRotationX = (float) (Math.PI/2/2);
            if(ball.mfingerRotationX*2 < -Math.PI/2) ball.mfingerRotationX = (float) (-Math.PI/2/2);
            ball.mfingerRotationY += offsetX * Ball.overture/100;
            Log.w(TAG,"ball.mfingerRotationX : "+ball.mfingerRotationX);
            Log.w(TAG,"ball.mfingerRotationY : "+ball.mfingerRotationY);

            Matrix.setIdentityM(MatrixHelper.mModelMatrix, 0);
            float[] tempx = new float[16];
            float[] tempy = new float[16];
            Matrix.setIdentityM(tempx, 0);
            Matrix.setIdentityM(tempy, 0);
            Matrix.rotateM(tempx, 0, ball.mfingerRotationY*100, 0, 1, 0);

            Matrix.rotateM(tempy, 0, ball.mfingerRotationX*100, 1, 0, 0);
            Matrix.multiplyMM(MatrixHelper.mModelMatrix,0, tempy,0, tempx,0 );
//---------------------------------------------------------------------------------
            ball.mLastX = x;
            ball.mLastY = y;
        }
    }

    public void handleMultiTouch(float distance, boolean isZoom){
        float scale = distance / 10;
        if(LoggerConfig.ON){
            Log.w(TAG, "MultiTouch scale: "+scale);
        }
        if(!isZoom){
            MatrixHelper.scale(1/scale, 1/scale, 1/scale);
        }else{
            MatrixHelper.scale(scale, scale, scale);
        }
    }

    public void handleMultiTouch(float distance){
        float dis = distance / 10;
        float scale;
        if(dis < 0 ){
            //小于0 两点距离比前一刻的两点距离短 在缩小
            scale = -0.1f;
            ball.scale -= 0.1;
        }else{
            scale = 0.1f;
            ball.scale += 0.1;
        }
        if(ball.scale > Ball.SCALE_MAX_VALUE){
            scale = 0.0f;
            ball.scale = Ball.SCALE_MAX_VALUE;
        }
        if(ball.scale < Ball.SCALE_MIN_VALUE){
            scale = 0.0f;
            ball.scale = Ball.SCALE_MIN_VALUE;
        }
        if(LoggerConfig.ON){
            Log.w(TAG, "MultiTouch ball.distance: "+ball.scale);
        }
        Matrix.translateM(MatrixHelper.mViewMatrix,0, 0f,0f,scale);
        ball.currentScale = scale;
    }
}
