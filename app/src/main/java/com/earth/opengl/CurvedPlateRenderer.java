package com.earth.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.earth.opengl.shape.Onefisheye180;
import com.earth.opengl.utils.CameraViewport;
import com.earth.opengl.utils.MatrixHelper;
import com.langtao.device.FishEyeDeviceDataSource;
import com.langtao.device.YUVFrame;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zzr on 2017/8/9.
 */

public class CurvedPlateRenderer implements GLSurfaceView.Renderer {
    private final static String TAG = "CurvedPlateRenderer";
    public final static float PLATE_SCALE_MAX_VALUE =2.2f;
    public final static float PLATE_SCALE_MIN_VALUE =0.0f;
    private final static double overture = 45;
    private static int MODE_OVER_LOOK = 0;
    private static int MODE_ENDOSCOPE = 1;

    private Context context;
    private int frameWidth;
    private int frameHeight;
    private YUVFrame initFrame;
    private FishEyeDeviceDataSource curvedPlateDevice;
    private Onefisheye180 CurvedPlate;
    private CameraViewport eye;

    public CurvedPlateRenderer(Context context, FishEyeDeviceDataSource curvedPlateDevice,
                               int frameWidth, int frameHeight, YUVFrame frame){
        this.context = context;
        this.curvedPlateDevice = curvedPlateDevice;
        eye = new CameraViewport();
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.initFrame = frame;

        timer = new Timer();
        timer.schedule(autoCruiseTimerTask, 5000, 10000);
    }



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f,0.0f,0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        CurvedPlate = new Onefisheye180(context,frameWidth,frameHeight,initFrame);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / (float) height;
        MatrixHelper.perspectiveM(MatrixHelper.mProjectionMatrix,
                (float) overture, ratio, 0.1f, 100f);
        MatrixHelper.setCamera(0, 0, -2.5f, //摄像机位置
                            0f, 0f, 0.0f, //摄像机目标视点
                            0f, 1.0f, 0.0f);//摄像机头顶方向向量

        eye.setCameraVector(0, 0, -2.5f);
        eye.setTargetViewVector(0f, 0f, 0.0f);
        eye.setCameraUpVector(0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        if(curvedPlateDevice.isInitedFishDevice() && CurvedPlate.isInitialized){
            updateTexture();
            updateBallMatrix();
            if(CurvedPlate.isAutoCruise && currentPerspectiveMode==MODE_ENDOSCOPE){
                autoCruise();
            }
            CurvedPlate.draw();
        }
    }

    private void updateTexture() {
        YUVFrame yuvFrame = curvedPlateDevice.getYUVFrame();
        if(yuvFrame==null) return;
        CurvedPlate.updateTexture(yuvFrame);
        yuvFrame.release();
    }

    private void updateBallMatrix() {

        Matrix.setIdentityM(MatrixHelper.mModelMatrix, 0);
        Matrix.scaleM(MatrixHelper.mModelMatrix,0,1.0f,1.0f,1.0f);

        Matrix.setIdentityM(CurvedPlate.mMatrixFingerRotationX, 0);
        Matrix.setIdentityM(CurvedPlate.mMatrixFingerRotationY, 0);
        Matrix.rotateM(CurvedPlate.mMatrixFingerRotationY, 0, CurvedPlate.mfingerRotationX, 0, 1, 0);
        Matrix.rotateM(CurvedPlate.mMatrixFingerRotationX, 0, CurvedPlate.mfingerRotationY, 1, 0, 0);
        Matrix.multiplyMM(MatrixHelper.mModelMatrix,0, CurvedPlate.mMatrixFingerRotationX,0, CurvedPlate.mMatrixFingerRotationY,0 );
    }



    //---------------------各种操作-------------------------------------------------
    public void resume(){
        curvedPlateDevice.startCollectFrame();
    }
    public void pause(){
        curvedPlateDevice.stopCollectFrame();
    }

    //自动巡航相关
    private Timer timer;
    TimerTask autoCruiseTimerTask = new TimerTask(){
        @Override
        public void run() {
            CurvedPlate.isAutoCruise = true;
        }
    };
    private int cruise_flag = 0;
    private void autoCruise() {
        if(cruise_flag == 0){
            CurvedPlate.mfingerRotationX += 0.2f;
        }else{
            CurvedPlate.mfingerRotationX -= 0.2f;
        }
        if(CurvedPlate.mfingerRotationX > 55f){
            cruise_flag = 1;
        }else if(CurvedPlate.mfingerRotationX < -55f){
            cruise_flag = 0;
        }
    }

    //处理
    public void handleTouchUp(float x, float y) {
        if(CurvedPlate!=null){
            CurvedPlate.mLastX = 0;
            CurvedPlate.mLastY = 0;
            //if(currentPerspectiveMode == MODE_ENDOSCOPE)
            {
                if(CurvedPlate.mfingerRotationX > 45f){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(CurvedPlate.mfingerRotationX > 45f){
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                CurvedPlate.mfingerRotationX -=0.2f;
                            }
                        }
                    }).start();
                }
                if(CurvedPlate.mfingerRotationX < -45f){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(CurvedPlate.mfingerRotationX < -45f){
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                CurvedPlate.mfingerRotationX +=0.2f;
                            }
                        }
                    }).start();
                }
            }
        }
    }


    //点击
    public void handleTouchDown(float x, float y) {
        if(CurvedPlate!=null){
            CurvedPlate.mLastX = x;
            CurvedPlate.mLastY = y;
            CurvedPlate.isAutoCruise = false;
        }
    }

    //滑动
    public void handleTouchMove(float x, float y) {
        if(CurvedPlate!=null){
            float offsetX = CurvedPlate.mLastX - x;
            float offsetY = CurvedPlate.mLastY - y;
            CurvedPlate.mfingerRotationX += offsetX/10;
            CurvedPlate.mfingerRotationY -= offsetY/10;
            if(CurvedPlate.mfingerRotationY > 10f){
                CurvedPlate.mfingerRotationY = 10f;
            }
            if(CurvedPlate.mfingerRotationY < -10f){
                CurvedPlate.mfingerRotationY = -10f;
            }

            //if(currentPerspectiveMode == MODE_ENDOSCOPE){
                if(CurvedPlate.mfingerRotationX > 55f){
                    CurvedPlate.mfingerRotationX = 55f;
                }
                if(CurvedPlate.mfingerRotationX < -55f){
                    CurvedPlate.mfingerRotationX = -55f;
                }
            //}else{  //currentPerspectiveMode == CameraViewport.MODE_OVER_LOOK
            //    if(CurvedPlate.mfingerRotationX > 15f){
            //        CurvedPlate.mfingerRotationX = 15f;
            //    }
            //    if(CurvedPlate.mfingerRotationX < -15f){
            //        CurvedPlate.mfingerRotationX = -15f;
            //    }
            //}
//            Log.w(TAG, "CurvedPlate mfingerRotationX : "+CurvedPlate.mfingerRotationX);
//            Log.w(TAG, "CurvedPlate mfingerRotationY : "+CurvedPlate.mfingerRotationY);
            CurvedPlate.mLastX = x;
            CurvedPlate.mLastY = y;
        }
    }



    //双击屏幕 切换视角
    public int currentPerspectiveMode = MODE_OVER_LOOK;
    public volatile boolean mTransforming = false;

    public void handleDoubleClick() {
        if(CurvedPlate==null) return;
        mTransforming = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean transforming = true;
                while(transforming){
                    if(currentPerspectiveMode == MODE_OVER_LOOK){
                        transforming = transformToEndoscope();
                    }else if(currentPerspectiveMode == MODE_ENDOSCOPE){
                        transforming = transformToOverlook();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.w(TAG,"current mViewMatrix: "+"\n"+
                        eye.cx+" "+eye.cy+" "+eye.cz+"\n"+
                        eye.tx+" "+eye.ty+" "+eye.tz+"\n"+
                        eye.upx+" "+eye.upy+" "+eye.upz+"\n");
                currentPerspectiveMode =
                        (currentPerspectiveMode == MODE_OVER_LOOK ? MODE_ENDOSCOPE:MODE_OVER_LOOK);
                mTransforming = false;
            }
        }).start();
    }

    private boolean transformToOverlook() {
        if(eye.cz > -2.4f){
            MatrixHelper.setCamera(eye.cx, eye.cy, eye.cz-=0.02f,
                    eye.tx, eye.ty, eye.tz,
                    eye.upx, eye.upy, eye.upz);
            return true;
        }
        return false;
    }

    private boolean transformToEndoscope() {
        if(eye.cz < -0.4f){
            MatrixHelper.setCamera(eye.cx, eye.cy, eye.cz+=0.02f,
                    eye.tx, eye.ty, eye.tz,
                    eye.upx, eye.upy, eye.upz);
            return true;
        }
        return false;
    }

    public void handleMultiTouch(float distance) {
        float dis = distance / 10;
        float scale;
        if(dis < 0 ){
            //小于0 两点距离比前一刻的两点距离短 在缩小
            scale = -0.1f;
            CurvedPlate.zoomTimes -= 0.1;
        } else {
            scale = 0.1f;
            CurvedPlate.zoomTimes += 0.1;
        }

        if(CurvedPlate.zoomTimes > PLATE_SCALE_MAX_VALUE) {
            scale = 0.0f;
            CurvedPlate.zoomTimes = PLATE_SCALE_MAX_VALUE;
        }
        if(CurvedPlate.zoomTimes < PLATE_SCALE_MIN_VALUE) {
            scale = 0.0f;
            CurvedPlate.zoomTimes = PLATE_SCALE_MIN_VALUE;
        }

        Matrix.translateM(MatrixHelper.mViewMatrix,0, 0f,0f,-scale);

        eye.setCameraVector(eye.cx,eye.cy,MatrixHelper.mViewMatrix[14]);
    }
}
