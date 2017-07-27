package com.earth.opengl.program;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by zzr on 2017/7/26.
 */

public class OneFishEye180ShaderProgram extends ShaderProgram{

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
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String RECT_X = "rectX";
    private static final String RECT_Y = "rectY";
    private static final String RECT_WIDTH = "rectWidth";
    private static final String RECT_HEIGHT = "rectHeight";
    private int uLocationWidth;
    private int uLocationHeight;
    private int uLocationRectX;
    private int uLocationRectY;
    private int uLocationRectWidth;
    private int uLocationRectHeight;


    public OneFishEye180ShaderProgram(Context context,
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

        uLocationWidth = GLES20.glGetUniformLocation(programId, WIDTH);
        uLocationHeight = GLES20.glGetUniformLocation(programId, HEIGHT);
        uLocationRectX = GLES20.glGetUniformLocation(programId, RECT_X);
        uLocationRectY = GLES20.glGetUniformLocation(programId, RECT_Y);
        uLocationRectWidth = GLES20.glGetUniformLocation(programId, RECT_WIDTH);
        uLocationRectHeight = GLES20.glGetUniformLocation(programId, RECT_HEIGHT);
    }


    public int getaPositionLocation() {
        return aPositionLocation;
    }

    public int getaTexCoordLocation() {
        return aTexCoordLocation;
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

    public int getuLocationCCM() {
        return uLocationCCM;
    }

    public int getuLocationWidth() {
        return uLocationWidth;
    }

    public int getuLocationHeight() {
        return uLocationHeight;
    }

    public int getuLocationRectX() {
        return uLocationRectX;
    }

    public int getuLocationRectY() {
        return uLocationRectY;
    }

    public int getuLocationRectWidth() {
        return uLocationRectWidth;
    }

    public int getuLocationRectHeight() {
        return uLocationRectHeight;
    }
}
