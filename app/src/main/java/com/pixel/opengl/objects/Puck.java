package com.pixel.opengl.objects;

import com.pixel.opengl.data.VertexArray;
import com.pixel.opengl.programs.ColorShaderProgram;
import com.pixel.opengl.util.Geometry;

import java.util.List;

/**
 * Created by ZZR on 2017/2/13.
 */

public class Puck {
    private static final int POSITION_COMPONENT_COUNT = 3;

    public final float radius ,height;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawCommandList;


    public Puck(float radius, float height, int numPointsAroundPuck){
        ObjectBuilder.GenerateData generateData = ObjectBuilder.createPuck(
                new Geometry.Cylinder(new Geometry.Point(0f, 0f, 0f), radius, height),
                numPointsAroundPuck);

        this.radius = radius;
        this.height = height;

        vertexArray = new VertexArray(generateData.vertexData);
        drawCommandList = generateData.drawList;
    }

    public void bindData(ColorShaderProgram colorShaderProgram){
        vertexArray.setVertexArrtibutePointer(0,
                colorShaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }

    public void draw(){
        for (ObjectBuilder.DrawCommand drawCommand : drawCommandList){
            drawCommand.draw();
        }
    }
}
