package com.particles.opengl.programs;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by ZZR on 2017/2/22.
 */

public class SkyboxShaderProgram extends ShaderProgram {

    public static final String U_MATRIX= "u_Matrix";
    public static final String A_POSITION = "a_Position";
    public static final String U_TEXTURE_UNIT = "u_TextureUnit";

    private final int uMatrixLocation;
    private final int uTextureUnitLocation;
    private final int aPositionLocation;

    public SkyboxShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        super(context, vertexShaderResourceId, fragmentShaderResourceId);

        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        uTextureUnitLocation = GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT);
    }

    public void setUniforms(float[] matrix, int textureId){
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
        GLES20.glUniform1i(uTextureUnitLocation, 0);
    }



    public int getMatrixLocation() {
        return uMatrixLocation;
    }

    public int getTextureUnitLocation() {
        return uTextureUnitLocation;
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }
}
