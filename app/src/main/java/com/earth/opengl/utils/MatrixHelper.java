package com.earth.opengl.utils;

import android.opengl.Matrix;

/**
 * Created by ZZR on 2017/2/9.
 */

public class MatrixHelper {

    public static float[] mProjectionMatrix = new float[16];// 4x4矩阵 存储投影矩阵
    public static float[] mViewMatrix = new float[16];// 摄像机位置朝向9参数矩阵
    public static float[] mModelMatrix = new float[16];// 模型变换矩阵

    static{
        //初始化为单位矩阵
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);
    }

    public static void translate(float x,float y,float z)
    {//设置沿xyz轴移动
        Matrix.translateM(mModelMatrix, 0, x, y, z);
    }

    //旋转变换
    public static void rotate(float angle, float x, float y, float z)
    {// 设置绕xyz轴移动
        Matrix.rotateM(mModelMatrix, 0, angle, x, y, z);
    }

    //缩放变换
    public static void scale(float x,float y,float z)
    {
        Matrix.scaleM(mModelMatrix,0, x, y, z);
    }

    // 设置摄像机
    public static void setCamera(float cx, // 摄像机位置x
                                 float cy, // 摄像机位置y
                                 float cz, // 摄像机位置z
                                 float tx, // 摄像机目标点x
                                 float ty, // 摄像机目标点y
                                 float tz, // 摄像机目标点z
                                 float upx, // 摄像机UP向量X分量
                                 float upy, // 摄像机UP向量Y分量
                                 float upz // 摄像机UP向量Z分量
    ) {
        Matrix.setLookAtM(mViewMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);
    }


    // 设置透视投影参数
    public static void setProjectFrustum(float left, // near面的left
                                         float right, // near面的right
                                         float bottom, // near面的bottom
                                         float top, // near面的top
                                         float near, // near面距离
                                         float far // far面距离
    ) {
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    // 获取具体物体的总变换矩阵
    static float[] mMVPMatrix = new float[16];

    public static float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    /**
     * 产生投影矩阵
     * @param m
     * @param yFovInDegrees
     * @param aspect
     * @param n
     * @param f
     */
    public static void perspectiveM(float[] m, float yFovInDegrees, float aspect, float n, float f){
        final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);
        final float a = (float) (1.0/Math.tan(angleInRadians / 2.0));
        //矩阵都是先列后行
        m[0] = a / aspect;  m[4] = 0f;  m[8] = 0f;              m[12] = 0f;
        m[1] = 0f;          m[5] = a;   m[9] = 0f;              m[13] = 0f;
        m[2] = 0f;          m[6] = 0f;  m[10] = -((f+n)/(f-n)); m[14] = -((2f*f*n)/(f-n));
        m[3] = 0f;          m[7] = 0f;  m[11] = -1f;            m[15] = 0f;
    }


    public static float[] rotateX(float mfingerRotationX) {
        float cos = (float) Math.cos(mfingerRotationX);
        float sin = (float) Math.sin(mfingerRotationX);
        float[] m = new float[16];
//                1.0f, 0.0f, 0.0f, 0.0f,
//                0.0f, cos, sin, 0.0f,
//                0.0f, -sin, cos, 0.0f,
//                0.0f, 0.0f, 0.0f, 1.0f
        m[0] = 1.0f;    m[4] = 0f;      m[8] = 0f;     m[12] = 0f;
        m[1] = 0f;      m[5] = cos;     m[9] = sin;    m[13] = 0f;
        m[2] = 0f;      m[6] = -sin;    m[10] = cos;   m[14] = 0.0f;
        m[3] = 0f;      m[7] = 0f;      m[11] = 0f;    m[15] = 1.0f;
        return m;
    }

    public static float[] rotateY(float mfingerRotationY) {
        float cos = (float) Math.cos(mfingerRotationY);
        float sin = (float) Math.sin(mfingerRotationY);
        float[] m = new float[16];
//                cos, 0.0f, -sin, 0.0f,
//                0.0f, 1.0f, 0.0f, 0.0f,
//                sin, 0.0f, cos, 0.0f,
//                0.0f, 0.0f, 0.0f, 1.0f
        m[0] = cos;    m[4] = 0f;      m[8] = -sin;     m[12] = 0f;
        m[1] = 0f;     m[5] = 1.0f;    m[9] = 0f;       m[13] = 0f;
        m[2] = sin;    m[6] = 0f;      m[10] = cos;     m[14] = 0.0f;
        m[3] = 0f;     m[7] = 0f;      m[11] = 0f;      m[15] = 1.0f;
        return m;
    }
}
