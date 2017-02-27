package com.particles.opengl.objects;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;

import com.particles.opengl.data.IndexBuffer;
import com.particles.opengl.data.VertexBuffer;
import com.particles.opengl.programs.HightmapEyespaceShaderProgram;
import com.particles.opengl.programs.HightmapShaderProgram;
import com.pixel.opengl.util.Geometry;

import static com.pixel.opengl.Contants.BYTES_PER_FLOAT;

/**
 * Created by ZZR on 2017/2/22.
 */

public class Hightmap {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT;
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + NORMAL_COMPONENT_COUNT )* BYTES_PER_FLOAT;

    private final int width;
    private final int height;
    private final int numElements;
    private final VertexBuffer vertexBuffer;
    private final IndexBuffer indexBuffer;

    public Hightmap(Bitmap bitmap){
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        if(width * height > 65536){
            throw new RuntimeException("Heightmap is too large for index buffer");
        }

        numElements = calculateNumElements();

        vertexBuffer = new VertexBuffer(loadBitmapData(bitmap));
        indexBuffer = new IndexBuffer(createIndexData()); 
    }

    private int calculateNumElements() {
        //针对高度图中，一个像素点有4个顶点构成一组，每组生成2个三角形，每个三角形需要3个索引点
        //ps 3*3的图像 = 2*2=4个矩阵*每2个矩阵三角形=8*每个三角形3个索引点
        return (width-1) * (height - 1) * 2 * 3;
    }

    private float[] loadBitmapData(Bitmap bitmap){
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0,0,width,height);
        bitmap.recycle();

//        final float[] heightmapVertices = new float[width*height*POSITION_COMPONENT_COUNT];
        /*13增加法线和位置*/
        final float[] heightmapVertices = new float[width*height*TOTAL_COMPONENT_COUNT];
        int offset = 0;

        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                //每个索引点都需要xyz分量表示位置，其中需要归一化处理，垂直于xz平面
//                final float xRotation = ((float)col / (float)(width-1)) - 0.5f;
//                final float yRotation =
//                        (float)Color.red(pixels[(row*height + col)]) / (float)255;
//                final float zRotation = ((float)row / (float)(height-1)) - 0.5f;
//
//                heightmapVertices[offset++] = xRotation;
//                heightmapVertices[offset++] = yRotation;
//                heightmapVertices[offset++] = zRotation;
                /*13章 更新代码组成*/
                final Geometry.Point point = getPoint(pixels,row,col);
                heightmapVertices[offset++] = point.x;
                heightmapVertices[offset++] = point.y;
                heightmapVertices[offset++] = point.z;
                /*增加法线和位置 */
                final Geometry.Point top = getPoint(pixels, row - 1, col);
                final Geometry.Point left = getPoint(pixels, row, col - 1);
                final Geometry.Point right = getPoint(pixels, row, col + 1);
                final Geometry.Point bottom = getPoint(pixels, row + 1, col);
                final Geometry.Vector rightToLeft = Geometry.vectorBetween(right, left);
                final Geometry.Vector topToBottom = Geometry.vectorBetween(top, bottom);
                final Geometry.Vector normal = rightToLeft.crossProduct(topToBottom).normalize();
                heightmapVertices[offset++] = normal.x;
                heightmapVertices[offset++] = normal.y;
                heightmapVertices[offset++] = normal.z;
            }
        }
        return heightmapVertices;
    }

    private Geometry.Point getPoint(int[] pixels, int row, int col) {
        final float x = ((float)col / (float)(width-1)) - 0.5f;
        final float z = ((float)row / (float)(height-1)) - 0.5f;

        row = clamp(row, 0, width-1);
        col = clamp(col, 0, height-1);

        final float y =
                (float)Color.red(pixels[(row*height + col)]) / (float)255;
        return new Geometry.Point(x,y,z);
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(val,max));
    }

    private short[] createIndexData(){
        final short[] indexData = new short[numElements];
        int offset = 0;
        for(int row = 0; row < height-1; row++){
            for(int col = 0; col < width-1; col++){
                short topLeftIndexNum = (short) (row * width + col);
                short topRightIndexNum = (short) (row * width + col + 1);
                short bottomLeftIndexNum = (short) ((row+1) * width + col);
                short bottomRightIndexNum = (short) ((row+1) * width + col + 1);
                //calculateNumElements对应点的索引值，还是基于3*3的例子就容易明白了
                indexData[offset++] = topLeftIndexNum;
                indexData[offset++] = bottomLeftIndexNum;
                indexData[offset++] = topRightIndexNum;

                indexData[offset++] = topRightIndexNum;
                indexData[offset++] = bottomLeftIndexNum;
                indexData[offset++] = bottomRightIndexNum;
            }
        }
        return indexData;
    }


    public void bindData(HightmapEyespaceShaderProgram hightmapShaderProgram){
        vertexBuffer.setVertexAttribPointer(0,
                hightmapShaderProgram.getPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        vertexBuffer.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT,
                hightmapShaderProgram.getNormalAttributeLocation(),
                NORMAL_COMPONENT_COUNT, STRIDE);
    }

    public void bindData(HightmapShaderProgram hightmapShaderProgram){
        vertexBuffer.setVertexAttribPointer(0,
                hightmapShaderProgram.getPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        vertexBuffer.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT,
                hightmapShaderProgram.getNormalAttributeLocation(),
                NORMAL_COMPONENT_COUNT, STRIDE);
    }

    public void draw(){
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getIndexBufferId());

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numElements, GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}
