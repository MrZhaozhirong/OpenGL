package com.particles.opengl.programs;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by ZZR on 2017/2/23.
 */

public class HightmapShaderProgram extends ShaderProgram {

    public static final String U_MATRIX= "u_Matrix";
    public static final String A_POSITION = "a_Position";

    private final int uMatrixLocation;
    private final int aPositionLocation;


    public HightmapShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        super(context, vertexShaderResourceId, fragmentShaderResourceId);

        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
    }

    public void setUniform(float[] matrix){
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }


    public int getMatrixLocation() {
        return uMatrixLocation;
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }
}
