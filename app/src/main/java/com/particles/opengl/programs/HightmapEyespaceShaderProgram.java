package com.particles.opengl.programs;

import android.content.Context;
import android.opengl.GLES20;


/**
 * Created by ZZR on 2017/2/23.
 */

public class HightmapEyespaceShaderProgram extends ShaderProgram {

    public static final String U_MV_MATRIX= "u_MVMatrix";
    public static final String U_IT_MV_MATRIX= "u_IT_MVMatrix";
    public static final String U_MVP_MATRIX= "u_MVPMatrix";
    public static final String U_POINT_LIGHT_POSITIONS= "u_PointLightPositions";
    public static final String U_POINT_LIGHT_COLORS= "u_PointLightColors";

    public static final String A_POSITION = "a_Position";
    public static final String A_NORMAL = "a_Normal";
    public static final String U_VECTOR_TO_LIGHT = "u_VectorToLight";

    private final int uMVMatrixLocation;
    private final int uIT_MVMatrixLocation;
    private final int uMVPMatrixLocation;
    private final int uPointLightPositionsLocation;
    private final int uPointLightColorsLocation;
    private final int uVectorToLightLocation;

    private final int aNormalLocation;
    private final int aPositionLocation;

    public HightmapEyespaceShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        super(context, vertexShaderResourceId, fragmentShaderResourceId);

        uMVMatrixLocation = GLES20.glGetUniformLocation(program, U_MV_MATRIX);
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, U_MVP_MATRIX);
        uIT_MVMatrixLocation = GLES20.glGetUniformLocation(program, U_IT_MV_MATRIX);

        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);

        uVectorToLightLocation=GLES20.glGetUniformLocation(program, U_VECTOR_TO_LIGHT);
        aNormalLocation=GLES20.glGetAttribLocation(program, A_NORMAL);

        uPointLightPositionsLocation = GLES20.glGetUniformLocation(program, U_POINT_LIGHT_POSITIONS);
        uPointLightColorsLocation = GLES20.glGetUniformLocation(program, U_POINT_LIGHT_COLORS);
    }


    public void setUniforms(float[] mvMatrix,
                            float[] it_mvMatrix,
                            float[] mvpMatrix,
                            float[] vectorToDirectionalLight,
                            float[] pointLightPositions,
                            float[] pointLightColors){
        GLES20.glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        GLES20.glUniformMatrix4fv(uIT_MVMatrixLocation, 1, false, it_mvMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        GLES20.glUniform3fv(uVectorToLightLocation, 1, vectorToDirectionalLight, 0);
        GLES20.glUniform4fv(uPointLightPositionsLocation, 3, pointLightPositions, 0);
        GLES20.glUniform3fv(uPointLightColorsLocation, 3, pointLightColors, 0);
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getNormalAttributeLocation() {
        return aNormalLocation;
    }
}
