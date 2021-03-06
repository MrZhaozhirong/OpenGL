package com.earth.opengl.data;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by ZZR on 2017/2/22.
 */

public class IndexBuffer {

    public static final int BYTES_PER_SHORT = 2;
    public static final int BYTES_PER_INT = 4;

    private final int indexBufferId;

    public int getIndexBufferId() {
        return indexBufferId;
    }

    public IndexBuffer(short[] indexData) {
        //allocate a buffer
        final int buffers[] = new int[1];
        GLES20.glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new vertex buffer object");
        }
        indexBufferId = buffers[0];
        //bind to the buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

        //Transfer data to native memory.
        ShortBuffer indexArry = ByteBuffer.allocateDirect(indexData.length * BYTES_PER_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indexData);

        indexArry.position(0);

        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexArry.capacity() * BYTES_PER_SHORT,
                indexArry, GLES20.GL_STATIC_DRAW);

        //IMPORTANT! unbind the buffer when done with it
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }



    public IndexBuffer(int[] indexData){
        //allocate a buffer
        final int buffers[] = new int[1];
        GLES20.glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new vertex buffer object");
        }
        indexBufferId = buffers[0];
        //bind to the buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

        //Transfer data to native memory.
        IntBuffer indexArry = ByteBuffer.allocateDirect(indexData.length * BYTES_PER_INT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(indexData);

        indexArry.position(0);

        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexArry.capacity() * BYTES_PER_INT,
                indexArry, GLES20.GL_STATIC_DRAW);

        //IMPORTANT! unbind the buffer when done with it
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
