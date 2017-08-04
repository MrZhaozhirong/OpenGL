package com.earth.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.earth.opengl.shape.Onefisheye360;
import com.earth.opengl.utils.BowlViewport;
import com.earth.opengl.utils.LoggerConfig;
import com.earth.opengl.utils.MatrixHelper;
import com.langtao.device.FisheyeDeviceDataSource;
import com.langtao.device.YUVFrame;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



/**
 * Created by nicky on 2017/4/17.
 */
public class BowlRenderer implements GLSurfaceView.Renderer {
    public final static String TAG = "BowlRenderer";
    public final static float SCALE_MAX_VALUE=3.0f;
    public final static float SCALE_MIN_VALUE=0.0f;
    public final static double overture = 45;

    private Context context;
    private boolean isNeedAutoScroll = false;
    public Onefisheye360 bowl;
    public FisheyeDeviceDataSource fishEyeDevice;
    public BowlViewport eye;

    public BowlRenderer(Context context) {
        this.context = context;
        eye = new BowlViewport();
        fishEyeDevice = new FisheyeDeviceDataSource(context);
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

        bowl = new Onefisheye360(context);

        timer = new Timer();
        timer.schedule(autoScrollTimerTask, 5000, 10000); // 5s后执行task,经过10s再次执行
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / (float) height;
        // 调用此方法计算产生透视投影矩阵
        //MatrixHelper.setProjectFrustum(-ratio,ratio, -1, 1, 0.1f, 400f);
        MatrixHelper.perspectiveM(MatrixHelper.mProjectionMatrix,
                (float) overture,
                (float)width/(float)height, 0.1f, 100f);
        // 调用此方法产生摄像机9参数位置矩阵
        MatrixHelper.setCamera(0, 0, -4f, //摄像机位置
                                0f, 0f, 0.0f, //摄像机目标视点
                                0f, 1.0f, 0.0f);//摄像机头顶方向向量
        eye.setCameraVector(0, 0, -4f);
        eye.setTargetViewVector(0f, 0f, 0.0f);
        eye.setCameraUpVector(0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清除深度缓冲与颜色缓冲
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        if(fishEyeDevice.isInitedFishDevice()){
            updateTexture();
            updateBallMatrix();
            if(isNeedAutoScroll){
                autoRotated();
            }
            bowl.draw();
        }
    }

    private void updateTexture() {
        YUVFrame yuvFrame = fishEyeDevice.getYUVFrame();
        if(yuvFrame==null) return;
        bowl.updateTexture(yuvFrame);
        yuvFrame.release();
    }

    private void updateBallMatrix() {

        Matrix.setIdentityM(MatrixHelper.mModelMatrix, 0);
        Matrix.scaleM(MatrixHelper.mModelMatrix,0,1.0f,1.0f,1.0f);

        Matrix.setIdentityM(bowl.mMatrixFingerRotationX, 0);
        Matrix.setIdentityM(bowl.mMatrixFingerRotationZ, 0);
        Matrix.rotateM(bowl.mMatrixFingerRotationZ, 0, bowl.mfingerRotationX, 0, 0, 1);
        Matrix.rotateM(bowl.mMatrixFingerRotationX, 0, bowl.mfingerRotationY, 1, 0, 0);
        Matrix.multiplyMM(MatrixHelper.mModelMatrix,0, bowl.mMatrixFingerRotationX,0, bowl.mMatrixFingerRotationZ,0 );
    }







    //---------------------各种操作-------------------------------------------------
    //双击屏幕 切换视角
    public int currentPerspectiveMode = BowlViewport.MODE_OVER_LOOK;
    void handleDoubleClick(){
        //把放大缩小还原
        bowl.zoomTimes = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean transforming = true;
                while(transforming){
                    try {
                        Thread.sleep(45);
                        if(currentPerspectiveMode == BowlViewport.MODE_OVER_LOOK){
                            transforming = transformToEndoscope();
                        }else if(currentPerspectiveMode == BowlViewport.MODE_ENDOSCOPE){
                            transforming = transformToOverlook();
                        }
                        isNeedAutoScroll = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.w(TAG,"current mViewMatrix: "+"\n"+
                        eye.cx+" "+eye.cy+" "+eye.cz+"\n"+
                        eye.tx+" "+eye.ty+" "+eye.tz+"\n"+
                        eye.upx+" "+eye.upy+" "+eye.upz+"\n");
                Log.w(TAG,"mfingerRotationY : "+bowl.mfingerRotationY);
                Log.w(TAG,"mfingerRotationX : "+bowl.mfingerRotationX);

                currentPerspectiveMode =
                        (currentPerspectiveMode == BowlViewport.MODE_OVER_LOOK?
                                BowlViewport.MODE_ENDOSCOPE:BowlViewport.MODE_OVER_LOOK);
                isNeedAutoScroll = true;
            }
        }).start();
    }

    private boolean transformToOverlook() {
        boolean viewTransforming = true;
        if(eye.cz >= -3.7f){
            MatrixHelper.setCamera(eye.cx, eye.cy, eye.cz-=0.3f,
                    eye.tx, eye.ty, eye.tz,
                    eye.upx, eye.upy, eye.upz);
        }else {
            viewTransforming = false;
        }

        boolean modelTransforming = true;
        if(bowl.mfingerRotationY > 4){
            bowl.mfingerRotationY -= 4.0f;
        }else{
            modelTransforming = false;
        }

        bowl.mfingerRotationX += 3.0f;

        if(viewTransforming || modelTransforming){
            return true;
        }else{
            return false;
        }
    }

    private boolean transformToEndoscope() {
        boolean viewTransforming = true;
        if(eye.cz < -1.3f){
            MatrixHelper.setCamera(eye.cx, eye.cy, eye.cz+=0.3f,
                    eye.tx, eye.ty, eye.tz,
                    eye.upx, eye.upy, eye.upz);
        }else{
            viewTransforming = false;
        }

        boolean modelTransforming = true;
        if(bowl.mfingerRotationY < 40f){
            bowl.mfingerRotationY += 4.0f;
        }else{
            modelTransforming = false;
        }

        bowl.mfingerRotationX += 3.0f;

        if(viewTransforming || modelTransforming){
            return true;
        }else{
            return false;
        }
    }

    //点击屏幕
    void handleTouchDown(float x, float y) {
        if(bowl!=null){
            bowl.mLastX = x;
            bowl.mLastY = y;
            if(LoggerConfig.ON){
                Log.w(TAG,"handleTouchUp bowl.mLastPosition x:"+x+"     y:"+y);
            }
            isNeedAutoScroll = false;
            if(timer!=null){
                timer.purge();
            }
        }
    }

    //滑动惯性
    public void handleTouchUp(final float x, final float y, final float xVelocity, final float yVelocity) {
        if(bowl!=null){
            bowl.mLastX = 0;
            bowl.mLastY = 0;
            bowl.gestureInertia_isStop = false;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleGestureInertia(x,y, xVelocity, yVelocity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void handleGestureInertia(float x, float y, float xVelocity, float yVelocity)
            throws InterruptedException {
        if(bowl != null ){
            bowl.gestureInertia_isStop = false;
            float mXVelocity = xVelocity;
            float mYVelocity = yVelocity;
            while(!bowl.gestureInertia_isStop){
                float offsetX = -mXVelocity / 800;

                bowl.mfingerRotationX += offsetX;

                //----------------------------------------------------------------------------
                if(Math.abs(mXVelocity - 0.97f*mXVelocity) < 0.00001f){
                    bowl.gestureInertia_isStop = true;
                }
                mYVelocity = 0.975f*mYVelocity;
                mXVelocity = 0.975f*mXVelocity;
                Thread.sleep(5);
            }
        }
    }


    //滑动
    void handleTouchMove(float x, float y) {
        if(bowl != null){
            float offsetX = bowl.mLastX - x;
            float offsetY = bowl.mLastY - y;
            bowl.mfingerRotationX += offsetX/10;
            bowl.mfingerRotationY += offsetY/30;
            if(currentPerspectiveMode == BowlViewport.MODE_ENDOSCOPE){
                if(bowl.mfingerRotationY > 50){
                    bowl.mfingerRotationY = 50;
                }
                if(bowl.mfingerRotationY < 40){
                    bowl.mfingerRotationY = 40;
                }
            }else{  //currentPerspectiveMode == BowlViewport.MODE_OVER_LOOK
                if(bowl.mfingerRotationY > 40f){
                    bowl.mfingerRotationY = 40f;
                }
                if(bowl.mfingerRotationY < -20f){
                    bowl.mfingerRotationY = -20f;
                }
            }
            //Log.w(TAG, "mfingerRotationX : "+bowl.mfingerRotationX);
            Log.w(TAG, "mfingerRotationY : "+bowl.mfingerRotationY);
            bowl.mLastX = x;
            bowl.mLastY = y;
        }
    }

    //自动旋转相关
    private Timer timer;
    TimerTask autoScrollTimerTask = new TimerTask(){
        @Override
        public void run() {
            isNeedAutoScroll = true;
        }
    };
    void autoRotated(){
        bowl.mfingerRotationX += 0.1f;
        if(bowl.mfingerRotationX > 360 || bowl.mfingerRotationX < -360){
            bowl.mfingerRotationX = bowl.mfingerRotationX % 360;
        }
    }


    public void resume(){
        fishEyeDevice.startCollectFrame();
    }
    public void pause(){
        fishEyeDevice.stopCollectFrame();
    }








    //双指操作
    void handleMultiTouch(float distance){
        float dis = distance / 10;
        float scale;
        if(dis < 0 ){
            //小于0 两点距离比前一刻的两点距离短 在缩小
            scale = -0.1f;
            bowl.zoomTimes -= 0.1;
        }else{
            scale = 0.1f;
            bowl.zoomTimes += 0.1;
        }
        if(bowl.zoomTimes > SCALE_MAX_VALUE){
            scale = 0.0f;
            bowl.zoomTimes = SCALE_MAX_VALUE;
        }
        if(bowl.zoomTimes < SCALE_MIN_VALUE){
            scale = 0.0f;
            bowl.zoomTimes = SCALE_MIN_VALUE;
        }
        //Matrix.setIdentityM(MatrixHelper.mViewMatrix, 0);
        //在原本的基础上添加增值，不需要置零
        Matrix.translateM(MatrixHelper.mViewMatrix,0, 0f,0f,-scale);
    }
}
