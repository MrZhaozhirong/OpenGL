package com.earth.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.earth.opengl.shape.Onefisheye360;
import com.earth.opengl.utils.CameraViewport;
import com.earth.opengl.utils.LoggerConfig;
import com.earth.opengl.utils.MatrixHelper;
import com.langtao.device.FishEyeDeviceDataSource;
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
    public final static float OVERLOOK_SCALE_MAX_VALUE =3.0f;
    public final static float OVERLOOK_SCALE_MIN_VALUE =0.0f;
    public final static float ENDOSCOPE_SCALE_MAX_VALUE =1.6f;
    public final static float ENDOSCOPE_SCALE_MIN_VALUE =0.0f;
    public final static double overture = 45;

    public static int MODE_OVER_LOOK = 0;
    public static int MODE_ENDOSCOPE = 1;

    private Context context;
    private volatile boolean isNeedAutoScroll = false;
    private volatile boolean operating = false;
    private int frameWidth;
    private int frameHeight;
    private YUVFrame initFrame;
    public Onefisheye360 bowl;
    public FishEyeDeviceDataSource fishEyeDevice;
    public CameraViewport eye;

    public BowlRenderer(Context context,FishEyeDeviceDataSource fishEyeDevice,
                        int frameWidth,int frameHeight,YUVFrame frame) {
        this.context = context;
        eye = new CameraViewport();
        this.fishEyeDevice = fishEyeDevice;
        timer = new Timer();
        timer.schedule(autoScrollTimerTask, 5000, 10000);

        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.initFrame = frame;
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

        bowl = new Onefisheye360(context,frameWidth,frameHeight,initFrame);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / (float) height;
        // 调用此方法计算产生透视投影矩阵
        //MatrixHelper.setProjectFrustum(-ratio,ratio, -1, 1, 0.1f, 400f);
        MatrixHelper.perspectiveM(MatrixHelper.mProjectionMatrix,
                (float) overture, ratio, 0.1f, 100f);
        // 调用此方法产生摄像机9参数位置矩阵
        MatrixHelper.setCamera(0, 0, -3f, //摄像机位置
                                0f, 0f, 0.0f, //摄像机目标视点
                                0f, 1.0f, 0.0f);//摄像机头顶方向向量
        eye.setCameraVector(0, 0, -3f);
        eye.setTargetViewVector(0f, 0f, 0.0f);
        eye.setCameraUpVector(0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清除深度缓冲与颜色缓冲
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        if(fishEyeDevice.isInitedFishDevice() && bowl.isInitialized){
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






    public void resume(){
        fishEyeDevice.startCollectFrame();
    }
    public void pause(){
        fishEyeDevice.stopCollectFrame();
    }
    //---------------------各种操作-------------------------------------------------
    //双击屏幕 切换视角
    public int currentPerspectiveMode = MODE_OVER_LOOK;
    void handleDoubleClick(){
        if(bowl==null) return;
        //把放大缩小还原
        bowl.zoomTimes = 0;
        operating = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean transforming = true;
                while(transforming){
                    try {
                        Thread.sleep(1);
                        if(currentPerspectiveMode == MODE_OVER_LOOK){
                            transforming = transformToEndoscope();
                        }else if(currentPerspectiveMode == MODE_ENDOSCOPE){
                            transforming = transformToOverlook();
                        }
                        isNeedAutoScroll = false;
                    } catch (Exception e) {
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
                        (currentPerspectiveMode == MODE_OVER_LOOK?MODE_ENDOSCOPE:MODE_OVER_LOOK);
                operating = false;
            }
        }).start();
    }

    private boolean transformToOverlook() {
        boolean viewTransforming = true;
        if(eye.cz > -3.0000019f){
            MatrixHelper.setCamera(eye.cx, eye.cy, eye.cz-=0.002f,
                    eye.tx, eye.ty, eye.tz,
                    eye.upx, eye.upy, eye.upz);
        }else {
            viewTransforming = false;
        }

        boolean modelTransforming = true;
        if(bowl.mfingerRotationY > 0){
            bowl.mfingerRotationY -= 0.04f;
        }else{
            modelTransforming = false;
        }

        bowl.mfingerRotationX -= 0.05f;

        if(viewTransforming || modelTransforming){
            return true;
        }else{
            return false;
        }
    }

    private boolean transformToEndoscope() {
        boolean viewTransforming = true;
        if(eye.cz < -1.0000019f){
            MatrixHelper.setCamera(eye.cx, eye.cy, eye.cz+=0.002f,
                    eye.tx, eye.ty, eye.tz,
                    eye.upx, eye.upy, eye.upz);
        }else{
            viewTransforming = false;
        }

        boolean modelTransforming = true;
        if(bowl.mfingerRotationY < 35f){
            bowl.mfingerRotationY += 0.04f;
        } else {
            modelTransforming = false;
        }

        bowl.mfingerRotationX -= 0.05f;

        if(viewTransforming || modelTransforming){
            return true;
        }else{
            return false;
        }
    }

    //点击屏幕
    public void handleTouchDown(float x, float y) {
        if(bowl!=null){
            bowl.mLastX = x;
            bowl.mLastY = y;
            if(LoggerConfig.ON){
                Log.w(TAG,"handleTouchUp bowl.mLastPosition x:"+x+"     y:"+y);
            }
            bowl.gestureInertia_isStop = true;
            operating = true;
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

            if(bowl.mfingerRotationY > 35f){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            endoscopeBoundaryInertia(x,y, xVelocity, yVelocity);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
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

    private void endoscopeBoundaryInertia(float x, float y, float xVelocity, float yVelocity) throws
            InterruptedException {
        if(bowl != null ){
            operating = true;
            bowl.pullupInertia_isStop = false;
            while(!bowl.pullupInertia_isStop){

                bowl.mfingerRotationY -= 0.1f;
                //----------------------------------------------------------------------------
                if(bowl.mfingerRotationY < 35f){
                    bowl.pullupInertia_isStop = true;
                    bowl.mfingerRotationY = 35f;
                }
                Thread.sleep(5);
            }
        }
    }

    private void handleGestureInertia(float x, float y, float xVelocity, float yVelocity)
            throws InterruptedException {
        if(bowl != null ){
            bowl.gestureInertia_isStop = false;
            float mXVelocity = xVelocity/8000f;
            float mYVelocity = yVelocity/8000f;
            Log.w(TAG,"xVelocity : "+xVelocity);
            while(!bowl.gestureInertia_isStop){
                double offsetX = -mXVelocity;

                bowl.mfingerRotationX -= offsetX;

                //----------------------------------------------------------------------------
                if(Math.abs(mXVelocity - 0.995f*mXVelocity) < 0.00000001f){
                    if(bowl.pullupInertia_isStop){
                        bowl.gestureInertia_isStop = true;
                    }
                }
                mYVelocity = 0.995f*mYVelocity;
                mXVelocity = 0.995f*mXVelocity;
                Log.i(TAG,"mXVelocity : "+mXVelocity);
                Thread.sleep(2);
                operating = true;
            }
        }
    }


    //滑动
    void handleTouchMove(float x, float y) {
        if(bowl != null){
            float offsetX = bowl.mLastX - x;
            float offsetY = bowl.mLastY - y;
            bowl.mfingerRotationX -= offsetX/10;
            bowl.mfingerRotationY -= offsetY/10;
            if(currentPerspectiveMode == MODE_ENDOSCOPE){
                if(bowl.mfingerRotationY > 70){
                    bowl.mfingerRotationY = 70;
                }
                if(bowl.mfingerRotationY < 30){
                    bowl.mfingerRotationY = 30;
                }
            }else{  //currentPerspectiveMode == CameraViewport.MODE_OVER_LOOK
                if(bowl.mfingerRotationY > 20f){
                    bowl.mfingerRotationY = 20f;
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
            operating = false;
        }
    };
    void autoRotated(){
        if(bowl==null) return;
        if(operating) return;
        bowl.mfingerRotationX -= 0.2f;
        if(bowl.mfingerRotationX > 360 || bowl.mfingerRotationX < -360){
            bowl.mfingerRotationX = bowl.mfingerRotationX % 360;
        }
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

        if(currentPerspectiveMode == MODE_OVER_LOOK){
            if(bowl.zoomTimes > OVERLOOK_SCALE_MAX_VALUE) {
                scale = 0.0f;
                bowl.zoomTimes = OVERLOOK_SCALE_MAX_VALUE;
            }
        } else {//MODE_ENDOSCOPE
            if(bowl.zoomTimes > ENDOSCOPE_SCALE_MAX_VALUE) {
                scale = 0.0f;
                bowl.zoomTimes = ENDOSCOPE_SCALE_MAX_VALUE;
            }
        }

        if(currentPerspectiveMode == MODE_OVER_LOOK){
            if(bowl.zoomTimes < OVERLOOK_SCALE_MIN_VALUE) {
                scale = 0.0f;
                bowl.zoomTimes = OVERLOOK_SCALE_MIN_VALUE;
            }
        } else {//MODE_ENDOSCOPE
            if(bowl.zoomTimes < ENDOSCOPE_SCALE_MIN_VALUE) {
                scale = 0.0f;
                bowl.zoomTimes = ENDOSCOPE_SCALE_MIN_VALUE;
            }
        }
        //Matrix.setIdentityM(MatrixHelper.mViewMatrix, 0);
        //在原本的基础上添加增值，不需要置零
        Matrix.translateM(MatrixHelper.mViewMatrix,0, 0f,0f,-scale);

        eye.setCameraVector(eye.cx,eye.cy,MatrixHelper.mViewMatrix[14]);
    }

}
