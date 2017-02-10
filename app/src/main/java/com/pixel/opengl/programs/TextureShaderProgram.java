package com.pixel.opengl.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.pixel.opengl.R;

/**
 * Created by ZZR on 2017/2/10.
 */

public class TextureShaderProgram extends ShaderProgram {


    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;

    protected static final String A_POSITION = "a_Position";
    //protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;


    public TextureShaderProgram(Context context) {
        super(context, R.raw.texture_vertext_shader, R.raw.texture_fragment_shader);

        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT);

        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    public void setUniforms(float[] matrix,int textureId){
        //传递矩阵
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0);
        //使用纹理单元保存纹理 把活动的纹理单元设置为单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //把纹理绑定到活动单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //把选定的单元0传递給片段着色器
        GLES20.glUniform1i(uTextureUnitLocation, 0);
    }


    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
