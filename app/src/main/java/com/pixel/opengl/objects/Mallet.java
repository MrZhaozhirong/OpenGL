package com.pixel.opengl.objects;

import android.opengl.GLES20;

import com.pixel.opengl.Contants;
import com.pixel.opengl.data.VertexArray;
import com.pixel.opengl.programs.ColorShaderProgram;
import com.pixel.opengl.util.Geometry;

import java.util.List;

/**
 * Created by ZZR on 2017/2/10.
 */

public class Mallet {
    private static final int POSITION_COMPONENT_COUNT = 3;

    public final float radius;
    public final float height;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawCommandList;

    public Mallet(float radius, float height, int numPointsAroundMallet){
        ObjectBuilder.GenerateData mallet = ObjectBuilder.createMallet(
                new Geometry.Point(0f, 0f, 0f), radius, height, numPointsAroundMallet);
        this.radius = radius;
        this.height = height;

        vertexArray = new VertexArray(mallet.vertexData);
        drawCommandList = mallet.drawList;
    }

    public void bindData(ColorShaderProgram colorShaderProgram){
        vertexArray.setVertexArrtibutePointer(0,
                colorShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,0);
    }

    public void draw(){
        for (ObjectBuilder.DrawCommand drawCommand : drawCommandList){
            drawCommand.draw();
        }
    }
}
