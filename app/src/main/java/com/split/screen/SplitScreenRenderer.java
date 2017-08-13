package com.split.screen;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.earth.opengl.utils.CameraViewport;
import com.earth.opengl.utils.MatrixHelper;
import com.langtao.device.FishEyeDeviceDataSource2;
import com.langtao.device.YUVFrame;
import com.split.screen.shape.Twofisheye360;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by zzr on 2017/8/10.
 */

public class SplitScreenRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "SplitScreenRenderer";
    public final static double overture = 45;

    private Context context;
    private int frameWidth;
    private int frameHeight;
    private YUVFrame initFrame;
    public Twofisheye360 bowl;
    public FishEyeDeviceDataSource2 fishEyeDevice;
    public CameraViewport eye;

    public SplitScreenRenderer(Context context, FishEyeDeviceDataSource2
            fishEyeDevice, int frameWidth, int frameHeight, YUVFrame frame) {
        this.context = context;

        eye = new CameraViewport();
        this.fishEyeDevice = fishEyeDevice;

        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.initFrame = frame;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f,0.0f,0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        bowl = new Twofisheye360(context,frameWidth,frameHeight,initFrame);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
        float ratio = (float) width / (float) height;
        MatrixHelper.perspectiveM(MatrixHelper.mProjectionMatrix,
                (float) overture, ratio, 0.1f, 100f);

        MatrixHelper.setCamera(0, 0, -3f, //摄像机位置
                            0f, 0f, 0.0f, //摄像机目标视点
                            0f, 1.0f, 0.0f);//摄像机头顶方向向量
        eye.setCameraVector(0, 0, -3f);
        eye.setTargetViewVector(0f, 0f, 0.0f);
        eye.setCameraUpVector(0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        long startTime = System.nanoTime();
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        if(fishEyeDevice.isInitedFishDevice() && bowl.isInitialized){
            updateTexture();
            updateBowlMatrix();
            bowl.draw();
        }
        long estimatedTime = System.nanoTime() - startTime;
        //Log.w(TAG,"estimatedTime : "+estimatedTime);
    }

    private void updateTexture() {
        YUVFrame yuvFrame = fishEyeDevice.getYUVFrame();
        if(yuvFrame==null) return;
        if(bowl.updateTexture(yuvFrame)){
            yuvFrame.release();
        }
    }

    private void updateBowlMatrix() {

        Matrix.setIdentityM(MatrixHelper.mModelMatrix, 0);
        Matrix.scaleM(MatrixHelper.mModelMatrix,0,1.0f,1.0f,1.0f);

        Matrix.setIdentityM(bowl.mMatrixFingerRotationX, 0);
        Matrix.setIdentityM(bowl.mMatrixFingerRotationY, 0);
        Matrix.setIdentityM(bowl.mMatrixFingerRotationZ, 0);

        Matrix.rotateM(bowl.mMatrixFingerRotationZ, 0, bowl.mfingerRotationX, 0, 0, 1);
        Matrix.rotateM(bowl.mMatrixFingerRotationX, 0, bowl.mfingerRotationY, 1, 0, 0);
        Matrix.multiplyMM(MatrixHelper.mModelMatrix,0, bowl.mMatrixFingerRotationX,0, bowl.mMatrixFingerRotationZ,0 );
    }


    //*****************************************************************************
    public void resume(){
        fishEyeDevice.startCollectFrame();
    }
    public void pause(){
        fishEyeDevice.stopCollectFrame();
    }
}
