package com.earth.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.earth.opengl.shape.BallRollBoundaryDirection;
import com.earth.opengl.shape.PANORAMA_Ball;
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
    //Ball ball;
    //YUV_ELEMENT_Ball ball;
    PANORAMA_Ball ball;
    public final static float SCALE_MAX_VALUE=1.0f;
    public final static float SCALE_MIN_VALUE=-1.0f;
    public final static double overture = 45;

    public EarthRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //设置屏幕背景色RGBA
        GLES20.glClearColor(0.0f,0.0f,0.0f, 1.0f);
        //打开深度检测
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glEnable(GLES20.GL_BLEND);
        //打开背面剪裁
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        ball = new PANORAMA_Ball(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / (float) height;
        // 调用此方法计算产生透视投影矩阵
        //MatrixHelper.setProjectFrustum(-ratio,ratio, -1, 1, 0.1f, 400f);
        MatrixHelper.perspectiveM(MatrixHelper.mProjectionMatrix,
                (float) overture,
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
        updateBallMatrix();
        ball.draw();
    }

    private void updateBallMatrix() {

        Matrix.setIdentityM(MatrixHelper.mModelMatrix, 0);
        Matrix.setIdentityM(ball.mMatrixFingerRotationX, 0);
        Matrix.setIdentityM(ball.mMatrixFingerRotationY, 0);
        //Matrix.rotateM(ball.mMatrixFingerRotationY, 0, ball.mfingerRotationY*100, 0, 1, 0);
        //Matrix.rotateM(ball.mMatrixFingerRotationX, 0, ball.mfingerRotationX*100, 1, 0, 0);
        if(ball.mfingerRotationY > 360 || ball.mfingerRotationY < -360){
            ball.mfingerRotationY = ball.mfingerRotationY % 360;
        }
        Matrix.rotateM(ball.mMatrixFingerRotationY, 0, ball.mfingerRotationY, 0, 1, 0);
        Matrix.rotateM(ball.mMatrixFingerRotationX, 0, ball.mfingerRotationX, 1, 0, 0);
        Matrix.multiplyMM(MatrixHelper.mModelMatrix,0, ball.mMatrixFingerRotationX,0, ball.mMatrixFingerRotationY,0 );
    }

    public void handleTouchUp(final float x, final float y, final float xVelocity, final float yVelocity) {
        if(ball!=null){
            ball.gestureInertia_isStop = false;
            ball.mLastX = 0;
            ball.mLastY = 0;
            if(LoggerConfig.ON){
                Log.w(TAG,"ball.boundaryDirection current state : "+ball.boundaryDirection);
                Log.w(TAG,"handleTouchUp ball.mLastPosition clear");
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleGestureInertia(x,y, xVelocity, yVelocity);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void handleTouchDown(float x, float y) {
        if(ball!=null){
            ball.gestureInertia_isStop = true;
            ball.mLastX = x;
            ball.mLastY = y;
            if(LoggerConfig.ON){
                Log.w(TAG,"handleTouchUp ball.mLastPosition x:"+x+"     y:"+y);
            }
        }
    }

    public void handleMultiTouch(float distance){
        float dis = distance / 10;
        float scale;
        if(dis < 0 ){
            //小于0 两点距离比前一刻的两点距离短 在缩小
            scale = -0.1f;
            ball.zoomTimes -= 0.1;
        }else{
            scale = 0.1f;
            ball.zoomTimes += 0.1;
        }
        if(ball.zoomTimes > SCALE_MAX_VALUE){
            scale = 0.0f;
            ball.zoomTimes = SCALE_MAX_VALUE;
        }
        if(ball.zoomTimes < SCALE_MIN_VALUE){
            scale = 0.0f;
            ball.zoomTimes = SCALE_MIN_VALUE;
        }
        if(LoggerConfig.ON){
            Log.w(TAG, "MultiTouch ball has zoom times: "+ball.zoomTimes*10);
        }
        //Matrix.setIdentityM(MatrixHelper.mViewMatrix, 0);
        //在原本的基础上添加增值，不需要置零
        Matrix.translateM(MatrixHelper.mViewMatrix,0, 0f,0f,scale);
    }


    public void handleTouchDrag(float x, float y) {
        if(ball != null){
            ball.gestureInertia_isStop = true;
//            float offsetX = ball.mLastX - x;
//            float offsetY = ball.mLastY - y;
//            MatrixHelper.rotate(offsetX, 0, 1, 0);
//            MatrixHelper.rotate(offsetY, 1, 0, 0);
//---------------------------------------------------------------------------------
            float offsetX = ball.mLastX - x;
            float offsetY = ball.mLastY - y;
            //offsetY *= 0.005*2;
            //offsetX *= 0.005*2;
            //ball.mfingerRotationY += offsetX * Ball.overture/100;
            //ball.mfingerRotationX += offsetY * Ball.overture/100;
            // 增加 角度限制的惯性特效
            if(ball.mfingerRotationX%360 > 90 ){
                float temp = seriesDampDrag(ball.mfingerRotationX, offsetY/2);
                Log.w(TAG,"ball.mfingerRotationX offset : "+temp);
                ball.mfingerRotationX += temp;
            }else
            if(ball.mfingerRotationX%360 < -90 ) {
                float temp = seriesDampDrag(ball.mfingerRotationX, offsetY/2);
                Log.w(TAG,"ball.mfingerRotationX offset : "+temp);
                ball.mfingerRotationX += temp;
            }else
            {
                ball.mfingerRotationX += offsetY/5 ;
                Log.w(TAG,"ball.mfingerRotationX offset : "+offsetY/5);
            }

            ball.mfingerRotationY += offsetX/8 ;
            Log.w(TAG,"ball.mfingerRotationY offset : "+ offsetX/8);

            updateBallBoundary();
            if(true){
                Log.w(TAG,"ball.mfingerRotationY : "+ball.mfingerRotationY);
                Log.w(TAG,"ball.mfingerRotationX : "+ball.mfingerRotationX);
            }
            //！！！模型矩阵的操作，全都归并到 updateBallMatrix
//            Matrix.setIdentityM(MatrixHelper.mModelMatrix, 0);
//            Matrix.setIdentityM(ball.mMatrixfingerRotationX, 0);
//            Matrix.setIdentityM(ball.mMatrixfingerRotationY, 0);
//            Matrix.rotateM(ball.mMatrixfingerRotationX, 0, ball.mfingerRotationY*100, 0, 1, 0);
//            Matrix.rotateM(ball.mMatrixfingerRotationY, 0, ball.mfingerRotationX*100, 1, 0, 0);
//            Matrix.multiplyMM(MatrixHelper.mModelMatrix,0, mMatrixfingerRotationY,0, mMatrixfingerRotationX,0 );
//---------------------------------------------------------------------------------
            ball.mLastX = x;
            ball.mLastY = y;
        }
    }

    public void handleGestureInertia(float upX, float upY, float xVelocity, float yVelocity) throws InterruptedException {
        //因为是action_up的时候调用的，此时ball.mLast=0
        if(ball!=null){
            boolean isInertiaX = true;
            boolean isInertiaY = true;
            //通过速度判断是否有惯性，
            //if(Math.abs(xVelocity) > 10f) isInertiaX = true;
            //if(Math.abs(yVelocity) > 10f) isInertiaY = true;
            //if(!isInertiaX && !isInertiaY) ball.gestureInertia_isStop = true;
            //ball.gestureInertia_isStop = !isInertiaX && !isInertiaY;
            ball.gestureInertia_isStop = false;
            float mXVelocity = xVelocity;
            float mYVelocity = yVelocity;
            while(!ball.gestureInertia_isStop){
//---------------------------------------------------------------------------------
                float offsetY = -mYVelocity / 2000;
                if(ball.boundaryDirection == BallRollBoundaryDirection.NORMAL ) {
                    ball.mfingerRotationX = ball.mfingerRotationX + offsetY;
                    if(ball.mfingerRotationX%360 > 90 ){
                        ball.mfingerRotationX = 90;
                    }else
                    if(ball.mfingerRotationX%360 < -90 ) {
                        ball.mfingerRotationX = -90;
                    }
                }else
                if(ball.boundaryDirection == BallRollBoundaryDirection.BOTTOM ){
                    double temp = seriesMoveReturn(ball.mfingerRotationX);
                    ball.mfingerRotationX -= temp;
                }else
                if(ball.boundaryDirection == BallRollBoundaryDirection.TOP ){
                    double temp = seriesMoveReturn(ball.mfingerRotationX);
                    ball.mfingerRotationX += temp;
                }else{

                }

                if(isInertiaX){
                    float offsetX = -mXVelocity / 2000;
                    ball.mfingerRotationY = ball.mfingerRotationY + offsetX;
                }

                updateBallBoundary();
                if(false) {
                    Log.i(TAG,"Inertia ball.mfingerRotationX : "+ball.mfingerRotationX);
                    Log.i(TAG,"Inertia ball.mfingerRotationY : "+ball.mfingerRotationY);
                    Log.i(TAG,"//------------------------------------------");
                }
//---------------------------------------------------------------------------------
                if(Math.abs(mYVelocity - 0.97f*mYVelocity) < 0.00001f
                        || Math.abs(mXVelocity - 0.97f*mXVelocity) < 0.00001f){
                    if(ball.boundaryDirection == BallRollBoundaryDirection.NORMAL){
                        ball.gestureInertia_isStop = true;
                    }
                }
                mYVelocity = 0.975f*mYVelocity;
                mXVelocity = 0.975f*mXVelocity;
                Thread.sleep(5);
            }
        }
    }




    //*****************************************************************************
    private double seriesMoveReturn(float mFingerRotation){
        //mFingerRotation > 0 = BOTTOM
        //mFingerRotation < 0 = TOP
        float absRotation = Math.abs(mFingerRotation);
        ball.moving_count_auto_return = absRotation - 90;
        ball.moving_count_auto_return = (float) (Math.sqrt(Math.pow(ball.moving_count_auto_return, 2.0)) / 15.0);
        if(ball.moving_count_auto_return <= 0.000015){
            ball.moving_count_auto_return = 0.000015;
        }
        Log.d(TAG, "moving_count_auto_return : "+ball.moving_count_auto_return);
        return ball.moving_count_auto_return;
    }

    private void updateBallBoundary() {
        if(ball.mfingerRotationX > 90) {
            ball.boundaryDirection = BallRollBoundaryDirection.BOTTOM;
        }else if(ball.mfingerRotationX < -90){
            ball.boundaryDirection = BallRollBoundaryDirection.TOP;
        }else{
            ball.boundaryDirection = BallRollBoundaryDirection.NORMAL;
        }
        if(LoggerConfig.ON){
            Log.w(TAG,"ball.boundaryDirection : "+ ball.boundaryDirection.name());
        }
    }

    private float seriesDampDrag(float mfingerRotationX, float offset) {
        float absRotation = Math.abs(mfingerRotationX);
        float level = absRotation - 90;
        if(mfingerRotationX * offset< 0){
            //反方向往回滚
            if(level < 10){
                //   90°~100°
                return offset * 0.8f;
            }else if(level < 20){
                //   100°~110°
                return offset * 0.6f;
            }else if(level < 30){
                //   110°~120°
                return offset * 0.4f;
            }else{
                //   >120°
                return offset * 0.2f;
            }
        }
        else //同方向一直拖着
        {
            if(level < 10){
                //   90°~100°
                return offset * 0.5f;
            }else if(level < 20){
                //   100°~110°
                return offset * 0.3f;
            }else if(level < 30){
                //   110°~120°
                return offset * 0.1f;
            }else{
                //   >120°
                return offset * 0.1f;
            }
        }
    }

}
