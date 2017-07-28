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

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;



/**
 * Created by nicky on 2017/4/17.
 */
public class BowlRenderer implements GLSurfaceView.Renderer {
    public final static String TAG = "BowlRenderer";
    public final static float SCALE_MAX_VALUE=1.0f;
    public final static float SCALE_MIN_VALUE=-1.0f;
    public final static double overture = 45;


    private Context context;
    private Onefisheye360 bowl;
    private boolean isNeedAutoScroll = false;
    private BowlViewport eye;

    public BowlRenderer(Context context) {
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
        bowl = new Onefisheye360(context);
        eye = new BowlViewport();
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
                (float)width/(float)height, 0.1f, 400f);
        // 调用此方法产生摄像机9参数位置矩阵
        MatrixHelper.setCamera(0, 0, -4f, //摄像机位置
                                0f, 0f, 0.0f, //摄像机目标视点
                                0f, 1.0f, 0.0f);//摄像机头顶方向向量
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清除深度缓冲与颜色缓冲
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        updateBallMatrix();
        if(isNeedAutoScroll){
            autoRotated();
        }
        bowl.draw();
    }

    private void updateBallMatrix() {

        Matrix.setIdentityM(MatrixHelper.mModelMatrix, 0);
        Matrix.setIdentityM(bowl.mMatrixFingerRotationZ, 0);
        if(bowl.mfingerRotationZ > 360 || bowl.mfingerRotationZ < -360){
            bowl.mfingerRotationZ = bowl.mfingerRotationZ % 360;
        }
        Matrix.rotateM(MatrixHelper.mModelMatrix, 0, bowl.mfingerRotationZ, 0, 0, 1);
    }







    //---------------------各种操作-------------------------------------------------
    //双击屏幕
    public int currentPerspectiveMode = BowlViewport.MODE_OVER_LOOK;
    void handleDoubleClick(){
        //把放大缩小还原
        bowl.zoomTimes = 0;
        if(currentPerspectiveMode == BowlViewport.MODE_OVER_LOOK){
            MatrixHelper.setCamera(0, 0, -1.5f,
                    0f, 1.0f, 1.0f,
                    0f, 0.0f, -1.0f);
            currentPerspectiveMode = BowlViewport.MODE_ENDOSCOPE;

        }else if(currentPerspectiveMode == BowlViewport.MODE_ENDOSCOPE){
            MatrixHelper.setCamera(0, 0, -4f,
                    0f, 0f, 0.0f,
                    0f, 1.0f, 0.0f);
            currentPerspectiveMode = BowlViewport.MODE_OVER_LOOK;
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
        }
    }

    //滑动
    void handleTouchMove(float x, float y) {
        if(bowl != null){
            float offsetX = bowl.mLastX - x;

            bowl.mfingerRotationZ += offsetX/8;
            Log.w(TAG, "offsetX/8 = "+offsetX/8);
            bowl.mLastX = x;
            bowl.mLastY = y;
        }
    }


    //自动旋转相关
    private Timer timer = new Timer();
    TimerTask autoScrollTimerTask = new TimerTask(){
        @Override
        public void run() {
            isNeedAutoScroll = true;
        }
    };
    void autoRotated(){
        bowl.mfingerRotationZ += 0.1;
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
