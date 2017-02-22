package com.particles.opengl.programs;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Created by ZZR on 2017/2/20.
 */

public class ParticleShaderProgram extends ShaderProgram {


    public static final String U_MATRIX= "u_Matrix";
    public static final String U_TIME = "u_Time";
    public static final String A_POSITION = "a_Position";
    public static final String A_COLOR = "a_Color";
    public static final String A_DIRECTION_VECTOR = "a_DirectionVector";
    public static final String A_PARTICLE_START_TIME = "a_ParticleStartTime";
    public static final String U_TEXTURE_UNIT="u_TextureUnit";
    //uniform locations
    private final int uMatrixLocation;
    private final int uTimeLocation;
    //attribute locations
    private final int aColorLocation;
    private final int aPositionLocation;
    private final int aDirectionVectorLocation;
    private final int aParticleStartTimeLocation;

    private final int uTextureUnitLocation;


    public ParticleShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        super(context, vertexShaderResourceId, fragmentShaderResourceId);
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        uTimeLocation = GLES20.glGetUniformLocation(program, U_TIME);
        aColorLocation=GLES20.glGetAttribLocation(program, A_COLOR);
        aPositionLocation=GLES20.glGetAttribLocation(program, A_POSITION);
        aDirectionVectorLocation=GLES20.glGetAttribLocation(program, A_DIRECTION_VECTOR);
        aParticleStartTimeLocation=GLES20.glGetAttribLocation(program, A_PARTICLE_START_TIME);
        //aParticleStartTimeLocation=GLES20.glGetUniformLocation(program, A_PARTICLE_START_TIME);
        uTextureUnitLocation=GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT);
    }

    public void setUniforms(float[] matrix, float elapsedTime, int textureId){
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        GLES20.glUniform1f(uTimeLocation, elapsedTime);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
        GLES20.glUniform1i(uTextureUnitLocation,0);
    }

    public void setUniforms(float[] matrix, float elapsedTime){
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
        GLES20.glUniform1f(uTimeLocation, elapsedTime);
    }

    public int getColorLocation() {
        return aColorLocation;
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getDirectionVectorLocation() {
        return aDirectionVectorLocation;
    }

    public int getParticleStartTimeLocation() {
        return aParticleStartTimeLocation;
    }
}
