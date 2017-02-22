package com.particles.opengl.objects;

import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import com.particles.opengl.programs.ParticleShaderProgram;
import com.pixel.opengl.data.VertexArray;
import com.pixel.opengl.util.Geometry;

import static com.pixel.opengl.Contants.BYTES_PER_FLOAT;

/**
 * Created by ZZR on 2017/2/20.
 */

public class ParticleSystem {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int VECTOR_COMPONENT_COUNT = 3;
    private static final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT+
                    COLOR_COMPONENT_COUNT+VECTOR_COMPONENT_COUNT+PARTICLE_START_TIME_COMPONENT_COUNT;

    private static final int STRIDE = TOTAL_COMPONENT_COUNT*BYTES_PER_FLOAT;

    private final float[] particles;
    private final VertexArray vertexArray;

    private final int maxParticleCount;
    private int currentParticleCount;

    private int nextParticle;//存储粒子编号

    public ParticleSystem(int maxParticleCount){
        particles = new float[maxParticleCount * TOTAL_COMPONENT_COUNT];
        vertexArray = new VertexArray(particles);
        this.maxParticleCount = maxParticleCount;
    }

    public void addParticle(Geometry.Point position, int color, Geometry.Vector direction,
                            float particleStartTime){

        final int particleOffset = nextParticle * TOTAL_COMPONENT_COUNT;
        int currentOffset = particleOffset;
        nextParticle++;

        if(currentParticleCount < maxParticleCount){
            currentParticleCount ++;
        }

        if(nextParticle == maxParticleCount){
            // start over at the beginning, but keep currentParticleCount
            // so that all the other particles still get drawn.
            nextParticle = 0;
            //到了数组结尾处就从0开始，以便回收旧的粒子
        }

        particles[currentOffset++] = position.x;
        particles[currentOffset++] = position.y;
        particles[currentOffset++] = position.z;

        particles[currentOffset++] = Color.red(color)/255f;
        particles[currentOffset++] = Color.green(color)/255f;
        particles[currentOffset++] = Color.blue(color)/255f;

        particles[currentOffset++] = direction.x;
        particles[currentOffset++] = direction.y;
        particles[currentOffset++] = direction.z;

        particles[currentOffset++] = particleStartTime;

        vertexArray.updateBuffer(particles, particleOffset, TOTAL_COMPONENT_COUNT);
    }


    public void bindData(ParticleShaderProgram particleShaderProgram){
        int dataOffset = 0;
        vertexArray.setVertexArrtibutePointer(dataOffset,
                particleShaderProgram.getPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        dataOffset += POSITION_COMPONENT_COUNT;

        vertexArray.setVertexArrtibutePointer(dataOffset,
                particleShaderProgram.getColorLocation(),
                COLOR_COMPONENT_COUNT, STRIDE);
        dataOffset += COLOR_COMPONENT_COUNT;

        vertexArray.setVertexArrtibutePointer(dataOffset,
                particleShaderProgram.getDirectionVectorLocation(),
                VECTOR_COMPONENT_COUNT, STRIDE);
        dataOffset += VECTOR_COMPONENT_COUNT;

        vertexArray.setVertexArrtibutePointer(dataOffset,
                particleShaderProgram.getParticleStartTimeLocation(),
                PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE);
    }

    public void draw(){
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, currentParticleCount);
    }

}
