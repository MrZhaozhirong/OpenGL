package com.split.screen.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.earth.opengl.data.VertexBuffer;
import com.pixel.opengl.R;
import com.split.screen.program.TextureShaderProgram;

/**
 * Created by zzr on 2017/8/14.
 */

public class SplitScreenCanvas {

    public float[] mProjectionMatrix = new float[16];// 4x4矩阵 存储投影矩阵
    public float[] mViewMatrix = new float[16]; // 摄像机位置朝向9参数矩阵
    public float[] mModelMatrix = new float[16];// 模型变换矩阵
    private float[] mMVPMatrix = new float[16];// 获取具体物体的总变换矩阵

    public float[] getFinalMatrix() {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        return mMVPMatrix;
    }

    private TextureShaderProgram shader;
    private Context context;

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COORDINATE_COMPONENT_COUNT = 2; // 每个顶点的坐标数 x y
    private static final int TEXTURE_COORDINATE_COMPONENT_COUNT = 2; // 每个纹理坐标为 S T两个

    private final float[] sPos1 ={
            //左上
            0.0f,1.0f,      //右上角
            0.0f,0.0f,      //右下角
            1.0f,1.0f,      //左上角
            1.0f,1.0f,      //左上角
            0.0f,0.0f,      //右下角
            1.0f,0.0f,      //左下角
    };
    private VertexBuffer posArray1;

    private final float[] sPos2 ={
            //右上
            -1.0f,1.0f,
            -1.0f,0.0f,
            0.0f,1.0f,
            0.0f,1.0f,
            -1.0f,0.0f,
            0.0f,0.0f,
    };
    private VertexBuffer posArray2;

    private final float[] sPos3 ={
            //右下
            -1.0f,0.0f,
            -1.0f,-1.0f,
            0.0f,0.0f,
            0.0f,0.0f,
            -1.0f,-1.0f,
            0.0f,-1.0f,
    };
    private VertexBuffer posArray3;

    private final float[] sPos4 ={
            //左下
            0.0f,0.0f,
            0.0f,-1.0f,
            1.0f,0.0f,
            1.0f,0.0f,
            0.0f,-1.0f,
            1.0f,-1.0f,
    };
    private VertexBuffer posArray4;

    private final float[] sCoord={
            //纹理四个区间是一样的
            1.0f,1.0f,  //右上角
            1.0f,0.0f,  //右下角
            0.0f,1.0f,  //左上角
            0.0f,1.0f,  //左上角
            1.0f,0.0f,  //右下角
            0.0f,0.0f,  //左下角
    };
    private VertexBuffer coordArray;

    public SplitScreenCanvas(Context context){
        this.context = context;
        shader = new TextureShaderProgram(context,R.raw.texture_vertext_shader, R.raw.texture_fragment_shader);
        intiVertexData();

        Matrix.setIdentityM(this.mModelMatrix, 0);
        Matrix.setIdentityM(this.mProjectionMatrix, 0);
        Matrix.setIdentityM(this.mViewMatrix, 0);
        Matrix.setIdentityM(this.mMVPMatrix, 0);
    }

    private void intiVertexData() {
        posArray1 = new VertexBuffer(sPos1);
        posArray2 = new VertexBuffer(sPos2);
        posArray3 = new VertexBuffer(sPos3);
        posArray4 = new VertexBuffer(sPos4);
        coordArray = new VertexBuffer(sCoord);
    }

    public void setShaderAttribute(int pos) {
        GLES20.glUseProgram( shader.getShaderProgramId() );

        //传入顶点坐标
        switch (pos){
            case 1:
                posArray1.setVertexAttribPointer(shader.getPositionAttributeLocation(),
                        POSITION_COORDINATE_COMPONENT_COUNT, 0, 0);
                break;
            case 2:
                posArray2.setVertexAttribPointer(shader.getPositionAttributeLocation(),
                        POSITION_COORDINATE_COMPONENT_COUNT, 0, 0);
                break;
            case 3:
                posArray3.setVertexAttribPointer(shader.getPositionAttributeLocation(),
                        POSITION_COORDINATE_COMPONENT_COUNT, 0, 0);
                break;
            case 4:
                posArray4.setVertexAttribPointer(shader.getPositionAttributeLocation(),
                        POSITION_COORDINATE_COMPONENT_COUNT, 0, 0);
                break;
        }

        //传入纹理坐标
        coordArray.setVertexAttribPointer(shader.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATE_COMPONENT_COUNT, 0, 0);
    }


    public void setDrawTexture(int textureId){
        GLES20.glUseProgram( shader.getShaderProgramId() );
        GLES20.glUniformMatrix4fv(shader.getMatrixLocation(),1,false, getFinalMatrix(),0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(shader.getTextureUnitLocation(), 3);
    }

    public void draw(){
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0, sPos1.length/2);
    }
}
