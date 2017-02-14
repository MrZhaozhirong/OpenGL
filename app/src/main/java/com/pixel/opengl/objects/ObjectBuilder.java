package com.pixel.opengl.objects;

import android.opengl.GLES20;
import android.util.FloatMath;

import com.pixel.opengl.util.Geometry;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.angle;

/**
 * Created by ZZR on 2017/2/13.
 */

public class ObjectBuilder {
    private static final int FLOATS_PER_VERTEX = 3;
    private final float[] vertexData;
    private int offset = 0;

    public ObjectBuilder(int sizeInVertices){
        vertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    }

    /**
     * 计算圆柱体顶部 点数量，中心1+第一个围绕中心需要重复闭合1+numPoints
     * @param numPoints
     * @return
     */
    private static int sizeOfCircleInVertices(int numPoints){
        return 1+ (numPoints + 1);
    }

    /**
     * 圆柱侧面是由长方形：一个三角形带构造的，围着顶部圆的每个点都需要两个顶点，且1st和2rd都需要闭合
     */
    private static int sizeOfOpenCylinderInVertices(int numPoints){
        return (numPoints + 1) * 2;
    }

    /**
     * 创建冰球(扁平圆柱体)的静态方法
     * @param puck
     * @param numPoints
     * @return
     */
    static GenerateData createPuck(Geometry.Cylinder puck, int numPoints){
        int size = sizeOfCircleInVertices(numPoints)
                + sizeOfOpenCylinderInVertices(numPoints);
        ObjectBuilder builder = new ObjectBuilder(size);
        Geometry.Circle puckTop = new Geometry.Circle(
                puck.center.translateY(puck.height / 2),
                puck.radius
        );

        builder.appendCircle(puckTop, numPoints);
        builder.appendOpenCylinder(puck, numPoints);

        return builder.build();
    }

    static GenerateData createMallet(
            Geometry.Point center, float radius, float height, int numPoints){
        int size = sizeOfCircleInVertices(numPoints) * 2
                + sizeOfOpenCylinderInVertices(numPoints) * 2;

        ObjectBuilder builder = new ObjectBuilder(size);

        //First, generate the mallet base.
        float baseHeight = height * 0.25f;
        Geometry.Circle baseCircle = new Geometry.Circle(
                center.translateY(-height * 0.25f),
                radius);

        Geometry.Cylinder baseCylinder = new Geometry.Cylinder(
                baseCircle.center.translateY(-baseHeight / 2f),
                radius, baseHeight);

        builder.appendCircle(baseCircle,numPoints);
        builder.appendOpenCylinder(baseCylinder,numPoints);

        //Second, generate the handle
        float handleHeight = height * 0.75f;
        float handleRadius = radius / 3f;
        Geometry.Circle handleCircle = new Geometry.Circle(
                center.translateY(height * 0.5f),
                handleRadius);
        Geometry.Cylinder handleCylinder = new Geometry.Cylinder(
                handleCircle.center.translateY(-handleHeight / 2f),
                handleRadius, handleHeight);

        builder.appendCircle(handleCircle, numPoints);
        builder.appendOpenCylinder(handleCylinder, numPoints);

        return builder.build();
    }


    /**
     * 圆柱体顶部圆形图案
     * @param circle
     * @param numPoints
     */
    private void appendCircle(Geometry.Circle circle, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);

        vertexData[offset++] = circle.center.x;
        vertexData[offset++] = circle.center.y;
        vertexData[offset++] = circle.center.z;

        for (int i = 0 ;  i  <= numPoints; i++){
            float angleInRadians = ((float) i / (float)numPoints) * ((float)Math.PI * 2f);
            //一个点由x,y,z组成，这里不是三个点。请注意
            vertexData[offset++] = circle.center.x
                    + circle.radius * ((float) Math.cos(angleInRadians));
            vertexData[offset++] = circle.center.y;
            vertexData[offset++] = circle.center.z
                    + circle.radius * ((float) Math.sin(angleInRadians));
        }
        //GL_TRIANGLE_FAN 三角形扇是固定第一个三角形的第一点+前一个完整三角形最后点，和三点外加一个新的点构造新的三角形，直到重合
        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, startVertex, numVertices);
            }
        });
    }

    /**
     * 圆柱体侧面
     * @param cylinder
     * @param numPoints
     */
    private void appendOpenCylinder(Geometry.Cylinder cylinder, int numPoints) {
        final int startVertex = offset / FLOATS_PER_VERTEX;
        final int numVertices = sizeOfCircleInVertices(numPoints);
        final float yStart = cylinder.center.y - cylinder.height / 2f;
        final float yEnd = cylinder.center.y + cylinder.height / 2f;

        for (int i = 0 ;  i  <= numPoints; i++){
            float angleInRadians = ((float) i / (float)numPoints) * ((float)Math.PI * 2f);

            float xPosition = cylinder.center.x + cylinder.radius * ((float) Math.cos(angleInRadians));
            float zPosition = cylinder.center.z + cylinder.radius * ((float) Math.sin(angleInRadians));
            //一个点由x,y,z组成，这里只是是两个点。请注意
            vertexData[offset++] = xPosition;
            vertexData[offset++] = yStart;
            vertexData[offset++] = zPosition;

            vertexData[offset++] = xPosition;
            vertexData[offset++] = yEnd;
            vertexData[offset++] = zPosition;
        }
        //GL_TRIANGLE_STRIP 三角形带是第一个三角形的后两点，和三点外加一个新的点构造新的三角形
        drawList.add(new DrawCommand() {
            @Override
            public void draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, startVertex, numVertices);
            }
        });
    }




    private final List<DrawCommand> drawList = new ArrayList<DrawCommand>();

    static interface DrawCommand{
        void draw();
    }

    static class GenerateData{
        final float[] vertexData;
        final List<DrawCommand> drawList;
        //holder类，以便单个对象返回顶点数据和绘画命令列表
        GenerateData(float[] vertexData, List<DrawCommand> drawList){
            this.vertexData = vertexData;
            this.drawList = drawList;
        }
    }

    private GenerateData build(){
        return new GenerateData(vertexData, drawList);
    }



}
