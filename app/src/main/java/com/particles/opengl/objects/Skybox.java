package com.particles.opengl.objects;

import android.opengl.GLES20;

import com.particles.opengl.programs.SkyboxShaderProgram;
import com.pixel.opengl.data.VertexArray;

import java.nio.ByteBuffer;

/**
 * Created by ZZR on 2017/2/21.
 */

public class Skybox {

    private static final int POSITION_COMPONENT_COUNT = 3;

    private final VertexArray vertexArray;
    private final ByteBuffer indexArray;

    public Skybox(){
        vertexArray = new VertexArray(new float[]{
                -1f, 1f, 1f,   // 0 left top near
                1f, 1f, 1f,    // 1 right top near
                -1f, -1f, 1f,  // 2 left bottom near
                1f, -1f, 1f,   // 3 right bottom near
                -1f, 1f, -1f,  // 4 left top far
                1f, 1f, -1f,   // 5 right top far
                -1f, -1f, -1f, // 6 left bottom far
                1f, -1f, -1f   // 7 right bottom far
        });

        indexArray = ByteBuffer.allocateDirect(6 * 2 * 3)
                .put(new byte[]{
                        //front
                        1, 3, 0,
                        0, 3, 2,
                        //back
                        4, 6, 5,
                        5, 6, 7,
                        //left
                        0, 2, 4,
                        4, 2, 6,
                        //right
                        5, 7, 1,
                        1, 7, 3,
                        //top
                        5, 1, 4,
                        4, 1, 0,
                        //bottom
                        6, 2, 7,
                        7, 2, 3
                });
        indexArray.position(0);
    }

    public void draw(){
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, indexArray);
    }

    public void bindData(SkyboxShaderProgram skyboxShaderProgram){
        vertexArray.setVertexArrtibutePointer(0,
                skyboxShaderProgram.getPositionLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }
}
