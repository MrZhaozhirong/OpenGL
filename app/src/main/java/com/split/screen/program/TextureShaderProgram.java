package com.split.screen.program;

import android.content.Context;
import android.opengl.GLES20;

import com.earth.opengl.program.ShaderProgram;

/**
 * Created by ZZR on 2017/2/10.
 */

public class TextureShaderProgram extends ShaderProgram {


    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;

    protected static final String A_POSITION = "a_Position";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;


    public TextureShaderProgram(Context context,
                                int vertexShaderResourceId,
                                int fragmentShaderResourceId) {
        super(context, vertexShaderResourceId, fragmentShaderResourceId);

        uMatrixLocation = GLES20.glGetUniformLocation(programId, U_MATRIX);
        uTextureUnitLocation = GLES20.glGetUniformLocation(programId, U_TEXTURE_UNIT);

        aPositionLocation = GLES20.glGetAttribLocation(programId, A_POSITION);
        aTextureCoordinatesLocation =GLES20.glGetAttribLocation(programId, A_TEXTURE_COORDINATES);
    }

    public int getMatrixLocation() {
        return uMatrixLocation;
    }

    public int getTextureUnitLocation() {
        return uTextureUnitLocation;
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
}
