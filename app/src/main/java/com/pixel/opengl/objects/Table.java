package com.pixel.opengl.objects;

import android.opengl.GLES20;

import com.pixel.opengl.Contants;
import com.pixel.opengl.data.VertexArray;
import com.pixel.opengl.programs.TextureShaderProgram;

/**
 * Created by ZZR on 2017/2/10.
 */

public class Table {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int  STRRID = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT)
                                        * Contants.BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            //x,    y,      s,      t
            0f,     0f,     0.5f,   0.5f,
            -0.5f,  -0.8f,  0f,     0.9f,
            0.5f,   -0.8f,  1f,     0.9f,
            0.5f,   0.8f,   1f,     0.1f,
            -0.5f,  0.8f,   0f,     0.1f,
            -0.5f,  -0.8f,  0f,     0.9f,
    };

    private final VertexArray vertexArray;

    public Table(){
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(TextureShaderProgram textureProgram){
        vertexArray.setVertexArrtibutePointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRRID
        );

        vertexArray.setVertexArrtibutePointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRRID
        );
    }

    public void draw(){
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);
    }
}
