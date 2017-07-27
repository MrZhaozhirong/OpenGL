package com.earth.opengl.program;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by zzr on 2017/7/25.
 */

public class OneFishEye360ShaderProgram extends ShaderProgram {

    //vertex
    private static final String A_POSITION = "position";
    private static final String A_TEXCOORD = "texCoord";
    private static final String U_MVP_MATRIX = "modelViewProjectionMatrix";
    private int aPositionLocation;
    private int aTexCoordLocation;
    private int uMVPMatrixLocation;
    //fragment
    private static final String SAMPLER_Y = "SamplerY";
    private static final String SAMPLER_U = "SamplerU";
    private static final String SAMPLER_V = "SamplerV";
    private static final String COLOR_CONVERSION_MATRIX = "colorConversionMatrix";
    private int uLocationSamplerY;
    private int uLocationSamplerU;
    private int uLocationSamplerV;
    private int uLocationCCM;

    public OneFishEye360ShaderProgram(Context context,
                                         int vertexShaderResourceId,
                                         int fragmentShaderResourceId) {
        super(context, vertexShaderResourceId, fragmentShaderResourceId);

        aPositionLocation = GLES20.glGetAttribLocation(programId, A_POSITION);
        aTexCoordLocation = GLES20.glGetAttribLocation(programId, A_TEXCOORD);
        uMVPMatrixLocation = GLES20.glGetUniformLocation(programId, U_MVP_MATRIX);

        uLocationSamplerY = GLES20.glGetUniformLocation(programId, SAMPLER_Y);
        uLocationSamplerU = GLES20.glGetUniformLocation(programId, SAMPLER_U);
        uLocationSamplerV = GLES20.glGetUniformLocation(programId, SAMPLER_V);

        uLocationCCM = GLES20.glGetUniformLocation(programId, COLOR_CONVERSION_MATRIX);
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

    public int getuLocationCCM() {
        return uLocationCCM;
    }

    public int getuMVPMatrixLocation() {
        return uMVPMatrixLocation;
    }

    public int getaTexCoordLocation() {
        return aTexCoordLocation;
    }

    public int getaPositionLocation() {
        return aPositionLocation;
    }
}
