package com.particles.opengl.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.pixel.opengl.util.ShaderHelper;
import com.pixel.opengl.util.TextResourceReader;

/**
 * Created by ZZR on 2017/2/20.
 */

public class ShaderProgram {

    protected final int program;

    protected ShaderProgram(Context context, int vertexShaderResourceId,
                            int fragmentShaderResourceId){
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(context,vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(context,fragmentShaderResourceId));
    }

    public void useProgram(){
        GLES20.glUseProgram(program);
    }

}
