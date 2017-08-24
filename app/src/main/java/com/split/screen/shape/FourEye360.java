package com.split.screen.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.WindowManager;

import com.earth.opengl.data.IndexBuffer;
import com.earth.opengl.data.VertexBuffer;
import com.earth.opengl.program.OneFishEye360ShaderProgram;
import com.earth.opengl.utils.MatrixHelper;
import com.earth.opengl.utils.TextureHelper;
import com.langtao.device.FishEyeDeviceDataSource;
import com.langtao.device.YUVFrame;
import com.langtao.fisheye.FishEyeProc;
import com.langtao.fisheye.OneFisheye360Param;
import com.langtao.fisheye.OneFisheyeOut;
import com.pixel.opengl.R;
import com.split.screen.data.FrameBuffer;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zzr on 2017/8/11.
 */

public class FourEye360 {

    static{
        System.loadLibrary("one_fisheye");
        System.loadLibrary("LTFishEyeProc");
    }
    private static final String TAG = "FourEye360";
    public final static double overture = 45;
    public float[] mProjectionMatrix = new float[16];// 4x4矩阵 存储投影矩阵
    public float[] mViewMatrix = new float[16]; // 摄像机位置朝向9参数矩阵
    public float[] mModelMatrix = new float[16];// 模型变换矩阵
    private float[] mMVPMatrix = new float[16];// 获取具体物体的总变换矩阵
    public float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    private int numElements = 0;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COORDINATE_COMPONENT_COUNT = 3; // 每个顶点的坐标数 x y z
    private static final int TEXTURE_COORDINATE_COMPONENT_COUNT = 2; // 每个纹理坐标为 S T两个
    private int drawElementType;
    private OneFisheyeOut out;
    private OneFishEye360ShaderProgram fishShader;
    private VertexBuffer verticesBuffer;
    private VertexBuffer texCoordsBuffer;
    private IndexBuffer indicesBuffer;
    private int[] _yuvTextureIDs;
    private volatile boolean isInitialized = false;
    private volatile boolean initializing = false;
    //***************************************************************
    private final Context context;
    private int screenWidth;
    private int screenHeight;
    private int mFrameWidth;
    private int mFrameHeight;
    private FishEyeDeviceDataSource fishEyeDevice;
    private SplitScreenCanvas splitScreenCanvas;



    public FourEye360(Context context,FishEyeDeviceDataSource fishEyeDevice) {
        this.context = context;
        this.fishEyeDevice = fishEyeDevice;
        Matrix.setIdentityM(this.mModelMatrix, 0);
        Matrix.setIdentityM(this.mProjectionMatrix, 0);
        Matrix.setIdentityM(this.mViewMatrix, 0);
        Matrix.setIdentityM(this.mMVPMatrix, 0);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
    }


    //================================建模视频帧相关==============================================================
    //================================建模视频帧相关==============================================================

    private void initFishEye360Param() {
        YUVFrame frame = fishEyeDevice.getInitFirstFrame();
        if(frame==null) return;
        initializing = true;
        createBufferData( frame.getWidth(), frame.getHeight(), frame);
        buildProgram();
        initTexture(frame.getWidth(), frame.getHeight(), frame);
        setAttributeStatus();
        isInitialized = true;
        initializing = false;
    }

    private void createBufferData(int width,int height,YUVFrame frame) {
        if(out == null){
            try{
                //InputStream is = context.getResources().openRawResource(R.raw.img_20170725_down);
                //byte[] dataArray = new byte[is.available()];
                //is.read(dataArray);
                OneFisheye360Param outParam = new OneFisheye360Param();
                //int ret = FishEyeProc.getOneFisheye360Param(dataArray, 1280, 1024, outParam);
                int ret = FishEyeProc.getOneFisheye360Param(frame.getYuvbyte(), width, height, outParam);
                if (ret != 0) {
                    return;
                }

                out = FishEyeProc.oneFisheye360Func(100, outParam);
            }catch ( Exception e){
                e.printStackTrace();
                return;
            }
        }

        verticesBuffer = new VertexBuffer(out.vertices);
        texCoordsBuffer = new VertexBuffer(out.texCoords);

        numElements = out.indices.length;
        if(numElements > Short.MAX_VALUE){
            short[] element_index = new short[numElements];
            for (int i = 0; i < out.indices.length; i++) {
                element_index[i] = (short) out.indices[i];
            }
            indicesBuffer = new IndexBuffer(element_index);
            drawElementType = GLES20.GL_UNSIGNED_SHORT;
        }else{
            int[] element_index = new int[numElements];
            System.arraycopy(out.indices, 0, element_index, 0, out.indices.length);
            indicesBuffer = new IndexBuffer(element_index);
            drawElementType = GLES20.GL_UNSIGNED_INT;
        }
    }

    private void buildProgram() {
        fishShader = new OneFishEye360ShaderProgram(context,
                R.raw.fisheye_360_vertex_shader,
                R.raw.fisheye_360_fragment_shader);
        GLES20.glUseProgram( fishShader.getShaderProgramId() );
    }

    private boolean initTexture(int width,int height,YUVFrame frame) {
        GLES20.glUseProgram( fishShader.getShaderProgramId() );
        int[] yuvTextureIDs = TextureHelper.loadYUVTexture2(width, height,
                frame.getYDatabuffer(),frame.getUDatabuffer(),frame.getVDatabuffer());
        if(yuvTextureIDs == null || yuvTextureIDs.length != 3) {
            Log.w(TAG,"yuvTextureIDs object's length not equals 3 !");
            return false;
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[0]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerY(), 0); // => GLES20.GL_TEXTURE0

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[1]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerU(), 1); // => GLES20.GL_TEXTURE1

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[2]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerV(), 2); // => GLES20.GL_TEXTURE2

        _yuvTextureIDs = yuvTextureIDs;
        mFrameWidth = width;
        mFrameHeight= height;
        return true;
    }

    private void setAttributeStatus() {
        GLES20.glUseProgram( fishShader.getShaderProgramId() );

        float kColorConversion420[] = {
                1.0f, 1.0f, 1.0f,
                0.0f, -0.39465f, 2.03211f,
                1.13983f, -0.58060f, 0.0f
        };

        GLES20.glUniformMatrix3fv(fishShader.getuLocationCCM(), 1, false, kColorConversion420, 0);

        verticesBuffer.setVertexAttribPointer(fishShader.getaPositionLocation(),
                POSITION_COORDINATE_COMPONENT_COUNT,
                POSITION_COORDINATE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);

        texCoordsBuffer.setVertexAttribPointer(fishShader.getaTexCoordLocation(),
                TEXTURE_COORDINATE_COMPONENT_COUNT,
                TEXTURE_COORDINATE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);
    }

    private void draw(){
        GLES20.glUseProgram( fishShader.getShaderProgramId() );
        //将最终变换矩阵写入
        GLES20.glUniformMatrix4fv(fishShader.getuMVPMatrixLocation(), 1, false, getFinalMatrix(),0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.getIndexBufferId());
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numElements, drawElementType, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void updateTexture() {
        YUVFrame yuvFrame = fishEyeDevice.getYUVFrame();
        if(yuvFrame==null) return;
        if(this.updateTexture(yuvFrame)){
            yuvFrame.release();
        }
    }

    private boolean updateTexture(YUVFrame yuvFrame ){
        if(yuvFrame==null) return false;
        int width = yuvFrame.getWidth();
        int height = yuvFrame.getHeight();
        ByteBuffer yDatabuffer = yuvFrame.getYDatabuffer();
        ByteBuffer uDatabuffer = yuvFrame.getUDatabuffer();
        ByteBuffer vDatabuffer = yuvFrame.getVDatabuffer();

        if(width != mFrameWidth || height!= mFrameHeight)
        {
            //先去掉旧的纹理
            GLES20.glDeleteTextures(_yuvTextureIDs.length, _yuvTextureIDs, 0);
            //重新加载数据
            int[] yuvTextureIDs = TextureHelper.loadYUVTexture2(width, height,
                    yDatabuffer, uDatabuffer, vDatabuffer);
            _yuvTextureIDs = yuvTextureIDs;
            mFrameWidth = width;
            mFrameHeight = height;
        }
        else
        {//长宽没变，更新纹理，不重建
            TextureHelper.updateTexture2(_yuvTextureIDs[0], mFrameWidth, mFrameHeight, yDatabuffer);
            TextureHelper.updateTexture2(_yuvTextureIDs[1], mFrameWidth, mFrameHeight, uDatabuffer);
            TextureHelper.updateTexture2(_yuvTextureIDs[2], mFrameWidth, mFrameHeight, vDatabuffer);
        }
        //重新加载纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[0]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerY(), 0); // => GLES20.GL_TEXTURE0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[1]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerU(), 1); // => GLES20.GL_TEXTURE1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[2]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerV(), 2); // => GLES20.GL_TEXTURE2
        return true;
    }

    private void updateBowlMatrix(float mOffsetFingerRotationX,
                                  float mOffsetFingerRotationY,
                                  float mOffsetFingerRotationZ) {

        Matrix.setIdentityM(this.mModelMatrix, 0);
        Matrix.scaleM(this.mModelMatrix,0,1.0f,1.0f,1.0f);

        Matrix.setIdentityM(this.mMatrixFingerRotationX, 0);
        Matrix.setIdentityM(this.mMatrixFingerRotationY, 0);
        Matrix.setIdentityM(this.mMatrixFingerRotationZ, 0);
        Matrix.rotateM(this.mMatrixFingerRotationZ, 0, this.mfingerRotationX+mOffsetFingerRotationX, 0, 0, 1);
        Matrix.rotateM(this.mMatrixFingerRotationX, 0, this.mfingerRotationY+mOffsetFingerRotationY, 1, 0, 0);

        Matrix.multiplyMM(this.mModelMatrix,0, this.mMatrixFingerRotationX,0, this.mMatrixFingerRotationZ,0 );
    }





    private FrameBuffer fbo1;
    public void onSurfaceCreate() {
        initFishEye360Param();
        splitScreenCanvas = new SplitScreenCanvas(context);
        fbo1 = new FrameBuffer();
        fbo1.setup(screenWidth, screenWidth);

        timer = new Timer();
        timer.schedule(autoScrollTimerTask, 5000, 10000);
    }

    public void onSurfaceChange(int width, int height) {
        float ratio = (float) width / (float) height;

        MatrixHelper.perspectiveM(this.mProjectionMatrix,
                (float) overture, ratio, 0.1f, 100f);
        MatrixHelper.perspectiveM(splitScreenCanvas.mProjectionMatrix,
                (float) overture/ratio, ratio, 0.1f, 100f);

        Matrix.setLookAtM(this.mViewMatrix, 0,
                0, 0, -1f, //摄像机位置
                0f, 0f, 0.0f, //摄像机目标视点
                0f, 1.0f, 0.0f);//摄像机头顶方向向量
        this.mfingerRotationY = 45.000f;
        Matrix.setLookAtM(splitScreenCanvas.mViewMatrix, 0,
                0, 0, -2.5f, //摄像机位置
                0f, 0f, 0.0f, //摄像机目标视点
                0f, 1.0f, 0.0f);//摄像机头顶方向向量
    }


    public void onDrawFrame() {
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //if(actionSwap)
        {
            fbo1.begin();
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glCullFace(GLES20.GL_BACK);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            if(fishEyeDevice.isInitedFishDevice() && this.isInitialized){
                updateTexture();
                updateBowlMatrix(0,0,0);
                if(isNeedAutoScroll){
                    autoRotated();
                }
                this.setAttributeStatus();
                this.draw();
            }
            fbo1.end();
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            splitScreenCanvas.setShaderAttribute(1);
            splitScreenCanvas.setDrawTexture(fbo1.getTextureId());
            splitScreenCanvas.draw();

            fbo1.begin();
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glCullFace(GLES20.GL_BACK);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            if(fishEyeDevice.isInitedFishDevice() && this.isInitialized){
                updateTexture();
                updateBowlMatrix(90f,0,0);
                if(isNeedAutoScroll){
                    autoRotated();
                }
                this.setAttributeStatus();
                this.draw();
            }
            fbo1.end();
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            splitScreenCanvas.setShaderAttribute(2);
            splitScreenCanvas.setDrawTexture(fbo1.getTextureId());
            splitScreenCanvas.draw();

            fbo1.begin();
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glCullFace(GLES20.GL_BACK);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            if(fishEyeDevice.isInitedFishDevice() && this.isInitialized){
                updateTexture();
                updateBowlMatrix(180f,0,0);
                if(isNeedAutoScroll){
                    autoRotated();
                }
                this.setAttributeStatus();
                this.draw();
            }
            fbo1.end();
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            splitScreenCanvas.setShaderAttribute(3);
            splitScreenCanvas.setDrawTexture(fbo1.getTextureId());
            splitScreenCanvas.draw();

            fbo1.begin();
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glCullFace(GLES20.GL_BACK);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            if(fishEyeDevice.isInitedFishDevice() && this.isInitialized){
                updateTexture();
                updateBowlMatrix(270f,0,0);
                if(isNeedAutoScroll){
                    autoRotated();
                }
                this.setAttributeStatus();
                this.draw();
            }
            fbo1.end();
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            splitScreenCanvas.setShaderAttribute(4);
            splitScreenCanvas.setDrawTexture(fbo1.getTextureId());
            splitScreenCanvas.draw();
        }
    }


    //================================操作封装==================================================================
    //================================操作封装==================================================================
    //***************************************************************
    private float mLastX;
    private float mLastY;
    private float mfingerRotationX = 0;
    private float mfingerRotationY = 0;
    private float mfingerRotationZ = 0;
    private float[] mMatrixFingerRotationX = new float[16];
    private float[] mMatrixFingerRotationY = new float[16];
    private float[] mMatrixFingerRotationZ = new float[16];
    private volatile boolean gestureInertia_isStop = true;
    private volatile boolean pullupInertia_isStop = true;
    private volatile boolean operating = false;
    private volatile boolean isNeedAutoScroll = false;
    public void resetStatus(){
        mfingerRotationX = 0;
        mfingerRotationY = 0;
        mfingerRotationZ = 0;
        Matrix.setIdentityM(this.mMatrixFingerRotationX, 0);
        Matrix.setIdentityM(this.mMatrixFingerRotationY, 0);
        Matrix.setIdentityM(this.mMatrixFingerRotationZ, 0);
    }
    //*****************************************************************
    //自动旋转相关
    private Timer timer;
    private TimerTask autoScrollTimerTask = new TimerTask(){
        @Override
        public void run() {
            isNeedAutoScroll = true;
            operating = false;
        }
    };
    private void autoRotated(){
        if(operating) return;
        this.mfingerRotationX -= 0.2f;
        if(this.mfingerRotationX > 360 || this.mfingerRotationX < -360){
            this.mfingerRotationX = this.mfingerRotationX % 360;
        }
    }

    public void handleTouchDown(float x, float y) {
        this.mLastX = x;
        this.mLastY = y;
        this.gestureInertia_isStop = true;
        operating = true;
        if(timer!=null){
            timer.purge();
        }
    }

    public void handleTouchUp(final float x, final float y,
                              final float xVelocity, final float yVelocity) {
        this.mLastX = 0;
        this.mLastY = 0;
        this.gestureInertia_isStop = false;

        if(this.mfingerRotationY > 35f){
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

        operating = true;
        pullupInertia_isStop = false;
        while(!this.pullupInertia_isStop){
            this.mfingerRotationY -= 0.1f;

            if(this.mfingerRotationY < 40f){
                pullupInertia_isStop = true;
                this.mfingerRotationY = 40f;
            }
            Thread.sleep(5);
        }
    }

    private void handleGestureInertia(float x, float y, float xVelocity, float yVelocity)
            throws InterruptedException {

            this.gestureInertia_isStop = false;
            float mXVelocity = xVelocity/8000f;
            float mYVelocity = yVelocity/8000f;
            while(!this.gestureInertia_isStop){
                double offsetX = -mXVelocity;

                this.mfingerRotationX -= offsetX;

                //----------------------------------------------------------------------------
                if(Math.abs(mXVelocity - 0.995f*mXVelocity) < 0.00000001f){
                    if(this.pullupInertia_isStop){
                        this.gestureInertia_isStop = true;
                    }
                }
                mYVelocity = 0.995f*mYVelocity;
                mXVelocity = 0.995f*mXVelocity;
                Thread.sleep(2);
                operating = true;
            }
    }
    public void handleTouchMove(float x, float y) {
        float offsetX = this.mLastX - x;
        float offsetY = this.mLastY - y;
        this.mfingerRotationX -= offsetX/10;
        this.mfingerRotationY -= offsetY/10;

        if(this.mfingerRotationY > 70){
            this.mfingerRotationY = 70;
        }
        if(this.mfingerRotationY < 30){
            this.mfingerRotationY = 30;
        }
        Log.w(TAG, "mfingerRotationY : "+this.mfingerRotationY);
        this.mLastX = x;
        this.mLastY = y;
    }
}
