package com.earth.opengl.program;

import android.content.Context;

import com.earth.opengl.utils.ShaderHelper;
import com.earth.opengl.utils.TextResourceReader;


/**
 * Created by ZZR on 2017/2/20.
 */

public class ShaderProgram {

    protected final int programId;

    protected ShaderProgram(Context context, int vertexShaderResourceId,
                            int fragmentShaderResourceId){
        programId = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(context,vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(context,fragmentShaderResourceId));
    }

    public int getShaderProgramId() {
        return programId;
    }

}
