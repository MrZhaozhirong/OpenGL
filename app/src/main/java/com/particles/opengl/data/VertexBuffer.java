package com.particles.opengl.data;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static com.pixel.opengl.Contants.BYTES_PER_FLOAT;
import static com.pixel.opengl.Contants.BYTES_PER_SHORT;

/**
 * Created by ZZR on 2017/2/22.
 */

public class VertexBuffer {

    private final int bufferId;

    public VertexBuffer(float[] vertexData) {
        //allocate a buffer
        final int buffers[] = new int[1];
        GLES20.glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new vertex buffer object");
        }
        bufferId = buffers[0];
        //bind to the buffer
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);

        //Transfer data to native memory.
        FloatBuffer vertexArry = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);

        vertexArry.position(0);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexArry.capacity() * BYTES_PER_FLOAT,
                vertexArry, GLES20.GL_STATIC_DRAW);

        //IMPORTANT! unbind the buffer when done with it
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void setVertexAttribPointer(int dataOffset, int attributeLocation,
                                       int componentCount, int stride){
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId);

        GLES20.glVertexAttribPointer(attributeLocation, componentCount, GLES20.GL_FLOAT,
                false, stride, dataOffset);
        GLES20.glEnableVertexAttribArray(attributeLocation);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
}
