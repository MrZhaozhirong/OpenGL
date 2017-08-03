package com.earth.opengl.utils;

/**
 * Created by zzr on 2017/7/28.
 */

public class BowlViewport {
    public static int MODE_OVER_LOOK = 0;
    public static int MODE_ENDOSCOPE = 1;
    public int currentPerspectiveMode = MODE_OVER_LOOK;

    public float cx; // 摄像机位置x
    public float cy; // 摄像机位置y
    public float cz; // 摄像机位置z
    public float tx; // 摄像机目标点x
    public float ty; // 摄像机目标点y
    public float tz; // 摄像机目标点z
    public float upx;// 摄像机UP向量X分量
    public float upy;// 摄像机UP向量Y分量
    public float upz;// 摄像机UP向量Z分量

    public BowlViewport setPerspectiveMode(int mode){
        this.currentPerspectiveMode = mode;
        return this;
    }

    public BowlViewport setCameraVector(float cx,float cy,float cz){
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
        return this;
    }

    public BowlViewport setTargetViewVector(float tx,float ty,float tz){
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        return this;
    }

    public BowlViewport setCameraUpVector(float upx,float upy,float upz){
        this.upx = upx;
        this.upy = upy;
        this.upz = upz;
        return this;
    }


}
