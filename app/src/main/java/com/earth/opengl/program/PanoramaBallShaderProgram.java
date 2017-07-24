package com.earth.opengl.program;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by zzr on 2017/6/8.
 */

public class PanoramaBallShaderProgram extends ShaderProgram {


    private static final String A_POSITION = "position";
    private static final String A_TEXCOORD = "texCoord";
    private static final String A_TEXFUSE = "texFuse";
    private static final String U_MVP_MATRIX = "modelViewProjectionMatrix";
    private static final String SAMPLER_Y = "SamplerY";
    private static final String SAMPLER_U = "SamplerU";
    private static final String SAMPLER_V = "SamplerV";
    private static final String COLOR_CONVERSION_MATRIX = "colorConversionMatrix";
    private static final String IMAGE_MODE = "imageMode";
    private static final String PERCENT = "percent";
    private static final String FACTOR_A11 = "factorA11";
    private static final String FACTOR_B11 = "factorB11";
    private static final String FACTOR_A12 = "factorA12";
    private static final String FACTOR_B12 = "factorB12";
    private static final String FACTOR_A21 = "factorA21";
    private static final String FACTOR_B21 = "factorB21";
    private static final String FACTOR_A22 = "factorA22";
    private static final String FACTOR_B22 = "factorB22";
    private int aPositionLocation;
    private int aTexCoordLocation;
    private int aTexFuseLocation;
    private int uMVPMatrixLocation;
    private int uLocationSamplerY;
    private int uLocationSamplerU;
    private int uLocationSamplerV;
    private int uLocationCCM;
    private int uLocationImageMode;
    private int uLocationPercent;
    private int uLocationFactorA11;
    private int uLocationFactorB11;
    private int uLocationFactorA12;
    private int uLocationFactorB12;
    private int uLocationFactorA21;
    private int uLocationFactorB21;
    private int uLocationFactorA22;
    private int uLocationFactorB22;


    public PanoramaBallShaderProgram(Context context,
                                     int vertexShaderResourceId,
                                     int fragmentShaderResourceId) {
        super(context, vertexShaderResourceId, fragmentShaderResourceId);

        aPositionLocation = GLES20.glGetAttribLocation(programId, A_POSITION);
        aTexCoordLocation = GLES20.glGetAttribLocation(programId, A_TEXCOORD);
        aTexFuseLocation = GLES20.glGetAttribLocation(programId, A_TEXFUSE);
        uMVPMatrixLocation = GLES20.glGetUniformLocation(programId, U_MVP_MATRIX);

        uLocationSamplerY = GLES20.glGetUniformLocation(programId, SAMPLER_Y);
        uLocationSamplerU = GLES20.glGetUniformLocation(programId, SAMPLER_U);
        uLocationSamplerV = GLES20.glGetUniformLocation(programId, SAMPLER_V);

        uLocationImageMode = GLES20.glGetUniformLocation(programId, IMAGE_MODE);
        uLocationPercent = GLES20.glGetUniformLocation(programId, PERCENT);
        uLocationCCM = GLES20.glGetUniformLocation(programId, COLOR_CONVERSION_MATRIX);

        uLocationFactorA11 = GLES20.glGetUniformLocation(programId, FACTOR_A11);
        uLocationFactorB11 = GLES20.glGetUniformLocation(programId, FACTOR_B11);
        uLocationFactorA12 = GLES20.glGetUniformLocation(programId, FACTOR_A12);
        uLocationFactorB12 = GLES20.glGetUniformLocation(programId, FACTOR_B12);
        uLocationFactorA21 = GLES20.glGetUniformLocation(programId, FACTOR_A21);
        uLocationFactorB21 = GLES20.glGetUniformLocation(programId, FACTOR_B21);
        uLocationFactorA22 = GLES20.glGetUniformLocation(programId, FACTOR_A22);
        uLocationFactorB22 = GLES20.glGetUniformLocation(programId, FACTOR_B22);
    }

    public int getuLocationCCM() {
        return uLocationCCM;
    }

    public int getaPositionLocation() {
        return aPositionLocation;
    }

    public int getaTexCoordLocation() {
        return aTexCoordLocation;
    }

    public int getaTexFuseLocation() {
        return aTexFuseLocation;
    }

    public int getuMVPMatrixLocation() {
        return uMVPMatrixLocation;
    }

    public int getuLocationSamplerY() {
        return uLocationSamplerY;
    }

    public int getuLocationSamplerU() {
        return uLocationSamplerU;
    }

    public int getuLocationSamplerV() {
        return uLocationSamplerV;
    }

    public int getuLocationImageMode() {
        return uLocationImageMode;
    }

    public int getuLocationPercent() {
        return uLocationPercent;
    }

    public int getuLocationFactorA11() {
        return uLocationFactorA11;
    }

    public int getuLocationFactorB11() {
        return uLocationFactorB11;
    }

    public int getuLocationFactorA12() {
        return uLocationFactorA12;
    }

    public int getuLocationFactorB12() {
        return uLocationFactorB12;
    }

    public int getuLocationFactorA21() {
        return uLocationFactorA21;
    }

    public int getuLocationFactorB21() {
        return uLocationFactorB21;
    }

    public int getuLocationFactorA22() {
        return uLocationFactorA22;
    }

    public int getuLocationFactorB22() {
        return uLocationFactorB22;
    }
}
