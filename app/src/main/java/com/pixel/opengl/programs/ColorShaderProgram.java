package com.pixel.opengl.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.pixel.opengl.R;

/**
 * Created by ZZR on 2017/2/10.
 */

public class ColorShaderProgram extends ShaderProgram {

    protected static final String A_POSITION = "a_Position";
    private final int aPositionLocation;
    protected static final String A_COLOR = "a_Color";
    private final int aColorLocation;
    protected static final String U_MATRIX= "u_Matrix";
    private final int uMatrixLocation;

    public ColorShaderProgram(Context context){
        super(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader);

        aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR);
        aPositionLocation=GLES20.glGetAttribLocation(program,A_POSITION);
        uMatrixLocation =GLES20.glGetUniformLocation(program, U_MATRIX);

    }

    public void setUniforms(float[] matrix){
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public int getColorAttributeLocation() {
        return aColorLocation;
    }


    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }
}
