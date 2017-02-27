package com.particles.opengl.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.pixel.opengl.util.Geometry;

/**
 * Created by ZZR on 2017/2/23.
 */

public class HightmapShaderProgram extends ShaderProgram {

    public static final String U_MATRIX= "u_Matrix";
    public static final String A_POSITION = "a_Position";
    public static final String A_NORMAL = "a_Normal";
    public static final String U_VECTOR_TO_LIGHT = "u_VectorToLight";

    private final int uMatrixLocation;
    private final int aPositionLocation;
    private final int uVectorToLightLocation;
    private final int aNormalLocation;

    public HightmapShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        super(context, vertexShaderResourceId, fragmentShaderResourceId);

        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);

        uVectorToLightLocation=GLES20.glGetUniformLocation(program, U_VECTOR_TO_LIGHT);
        aNormalLocation=GLES20.glGetAttribLocation(program, A_NORMAL);
    }

    public void setUniform(float[] matrix){
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public void setUniforms(float[] matrix, Geometry.Vector vectorToLight){
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        GLES20.glUniform3f(uVectorToLightLocation,
                vectorToLight.x, vectorToLight.y, vectorToLight.z);
    }

    public int getMatrixLocation() {
        return uMatrixLocation;
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getNormalAttributeLocation() {
        return aNormalLocation;
    }

}
