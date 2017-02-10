package com.pixel.opengl.objects;

import android.opengl.GLES20;

import com.pixel.opengl.Contants;
import com.pixel.opengl.data.VertexArray;
import com.pixel.opengl.programs.ColorShaderProgram;

/**
 * Created by ZZR on 2017/2/10.
 */

public class Mallet {
    private static final int POSITON_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE =
            (POSITON_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * Contants.BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            //xyrgb
            0f,-0.4f,   0f, 0f, 1f,
            0f,0.4f,    1f, 0f, 0f,
    };

    private final VertexArray vertexArray;

    public Mallet(){
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(ColorShaderProgram colorShaderProgram){
        vertexArray.setVertexArrtibutePointer(
                0,
                colorShaderProgram.getPositionAttributeLocation(),
                POSITON_COMPONENT_COUNT,
                STRIDE
        );

        vertexArray.setVertexArrtibutePointer(
                2,
                colorShaderProgram.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT,
                STRIDE
        );
    }

    public void draw(){
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 2);
    }
}
