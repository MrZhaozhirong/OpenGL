package com.earth.opengl.shape;

import android.content.Context;
import android.opengl.GLES20;

import com.earth.opengl.utils.MatrixHelper;
import com.earth.opengl.utils.ShaderHelper;
import com.earth.opengl.utils.TextResourceReader;
import com.earth.opengl.utils.TextureHelper;
import com.pixel.opengl.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;


/**
 * Created by nicky on 2017/4/17.
 *  具体参照 http://blog.csdn.net/cassiepython/article/details/51620114
 */

public class Ball {

    //*****************************************************************
    //** 单手双手操作相关
    public float mLastX;
    public float mLastY;
    public float mfingerRotationX = 0;
    public float mfingerRotationY = 0;
    public float[] mMatrixFingerRotationX = new float[16];
    public float[] mMatrixFingerRotationY = new float[16];
    public final static float SCALE_MAX_VALUE=1.0f;
    public final static float SCALE_MIN_VALUE=-1.0f;
    public final static double overture = 45;
    public float zoomTimes = 0.0f;
    //** 惯性自滚标志
    public boolean gestureInertia_isStop = true;
    //** 纵角度限制相关
    public RollBoundaryDirection boundaryDirection = RollBoundaryDirection.NORMAL;
    public double moving_count_auto_return = 0.0f;
    //*****************************************************************
    private final Context context;
    final int angleSpan = 4;   // 将球行单位进切分的角度
    private static final float UNIT_SIZE = 1.0f;    // 单位尺寸
    private float radius = 0.8f;    // 球的半径
    private int vCount = 0;     // 记录顶点个数，先初始化为0
    private static final int BYTES_PER_FLOAT = 4;   // float类型的字节数
    private static final int COORDS_PER_VERTEX = 3; // 数组中每个顶点的坐标数
    private static final int TEXTURE_COORDIANTES_COMPONENT_COUNT = 2; // 每个纹理坐标为 S T两个
    private FloatBuffer vertexBuffer;// 顶点坐标
    private FloatBuffer textureBuffer;// 纹理坐标
    private int program;
    private static final String A_POSITION = "a_Position";
    private static final String U_MATRIX = "u_Matrix";
    private int uMatrixLocation;
    private int aPositionLocation;
    private int textureId;
    private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private int uTextureUnitLocation;
    private int aTextureCoordinates;


    public Ball(Context context){
        this.context = context;
        initVertexData();
        buildProgram();

        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        aTextureCoordinates = GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES);
        uTextureUnitLocation = GLES20.glGetAttribLocation(program, U_TEXTURE_UNIT);

        initTexture();

        //---------传入顶点坐标数据
        GLES20.glVertexAttribPointer(aPositionLocation, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        //---------传入纹理坐标数据
        GLES20.glVertexAttribPointer(aTextureCoordinates, TEXTURE_COORDIANTES_COMPONENT_COUNT,
                GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoordinates);
    }

    private void initVertexData() {
        ArrayList<Float> alVertix = new ArrayList<Float>();// 存放顶点坐标的ArrayList
        // ***************************************
        ArrayList<Float> textureVertix = new ArrayList<Float>();// 存放纹理坐标的ArrayList
        for (int vAngle = 0; vAngle < 180; vAngle = vAngle + angleSpan) //球面垂直方向180/angleSpan数量单位
        {
            for (int hAngle = 0; hAngle <= 360; hAngle = hAngle + angleSpan)//球面水平方向360/angleSpan数量单位
            {
                // 纵向横向各到一个角度后计算对应的此点在球面上的坐标
                float z0 = (float) (radius * UNIT_SIZE
                        * Math.sin(Math.toRadians(vAngle)) * Math.cos(Math
                        .toRadians(hAngle)));
                float x0 = (float) (radius * UNIT_SIZE
                        * Math.sin(Math.toRadians(vAngle)) * Math.sin(Math
                        .toRadians(hAngle)));
                float y0 = (float) (radius * UNIT_SIZE * Math.cos(Math
                        .toRadians(vAngle)));
                //Log.w("x0 y0 z0","" + x0 + "  "+y0+ "  " +z0);
                float z1 = (float) (radius * UNIT_SIZE
                        * Math.sin(Math.toRadians(vAngle)) * Math.cos(Math
                        .toRadians(hAngle + angleSpan)));
                float x1 = (float) (radius * UNIT_SIZE
                        * Math.sin(Math.toRadians(vAngle)) * Math.sin(Math
                        .toRadians(hAngle + angleSpan)));
                float y1 = (float) (radius * UNIT_SIZE * Math.cos(Math
                        .toRadians(vAngle)));
                //Log.w("x1 y1 z1","" + x1 + "  "+y1+ "  " +z1);
                float z2 = (float) (radius * UNIT_SIZE
                        * Math.sin(Math.toRadians(vAngle + angleSpan)) * Math
                        .cos(Math.toRadians(hAngle + angleSpan)));
                float x2 = (float) (radius * UNIT_SIZE
                        * Math.sin(Math.toRadians(vAngle + angleSpan)) * Math
                        .sin(Math.toRadians(hAngle + angleSpan)));
                float y2 = (float) (radius * UNIT_SIZE * Math.cos(Math
                        .toRadians(vAngle + angleSpan)));
                //Log.w("x2 y2 z2","" + x2 + "  "+y2+ "  " +z2);
                float z3 = (float) (radius * UNIT_SIZE
                        * Math.sin(Math.toRadians(vAngle + angleSpan)) * Math
                        .cos(Math.toRadians(hAngle)));
                float x3 = (float) (radius * UNIT_SIZE
                        * Math.sin(Math.toRadians(vAngle + angleSpan)) * Math
                        .sin(Math.toRadians(hAngle)));
                float y3 = (float) (radius * UNIT_SIZE * Math.cos(Math
                        .toRadians(vAngle + angleSpan)));
                //Log.w("x3 y3 z3","" + x3 + "  "+y3+ "  " +z3);
                // 一个矩形由两个三角形组成
                // 第一个三角形的三个点
                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                alVertix.add(x0);
                alVertix.add(y0);
                alVertix.add(z0);
                // 关于纹理坐标 Android Opengl的坐标 http://www.cnblogs.com/jenry/p/4083415.html
                float s0 = hAngle / 360.0f;
                float t0 = 1 - vAngle / 180.0f;
                float s1 = (hAngle + angleSpan)/360.0f ;
                float t1 = 1 - (vAngle + angleSpan) / 180.0f;

                textureVertix.add(s1);
                textureVertix.add(t0);
                textureVertix.add(s0);
                textureVertix.add(t1);
                textureVertix.add(s0);
                textureVertix.add(t0);
                // 第二个三角形的三个点
                alVertix.add(x1);
                alVertix.add(y1);
                alVertix.add(z1);
                alVertix.add(x2);
                alVertix.add(y2);
                alVertix.add(z2);
                alVertix.add(x3);
                alVertix.add(y3);
                alVertix.add(z3);
                // 第二个三角形对应的纹理坐标
                textureVertix.add(s1);
                textureVertix.add(t0);
                textureVertix.add(s1);
                textureVertix.add(t1);
                textureVertix.add(s0);
                textureVertix.add(t1);
            }
        }
        // *****************************************************************
        vCount = alVertix.size() / COORDS_PER_VERTEX;// 顶点的数量
        // 将alVertix中的坐标值转存到一个float数组中
        float vertices[] = new float[ alVertix.size() ];
        for (int i = 0; i < alVertix.size(); i++) {
            vertices[i] = alVertix.get(i);
        }
        vertexBuffer = ByteBuffer
                .allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        // 把坐标们加入FloatBuffer中
        vertexBuffer.put(vertices);
        // 设置buffer，从第一个坐标开始读
        vertexBuffer.position(0);
        // *****************************************************************
        float textures[] = new float[textureVertix.size()];
        for(int i=0;i<textureVertix.size();i++){
            textures[i] = textureVertix.get(i);
        }
        textureBuffer = ByteBuffer
                .allocateDirect(textures.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        // 把坐标们加入FloatBuffer中
        textureBuffer.put(textures);
        // 设置buffer，从第一个坐标开始读
        textureBuffer.position(0);
    }

    private void buildProgram() {
        //获取顶点着色器文本
        String vertexShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.vertex_shader_ball);
        //获取片段着色器文本
        String fragmentShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.fragment_shader_ball);
        //获取program的id
        program = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);
        GLES20.glUseProgram(program);
    }

    private void initTexture() {
        textureId = TextureHelper.loadTexture(context, R.mipmap.test);
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        GLES20.glUniform1i(uTextureUnitLocation, 0);
    }

    public void draw(){
        //将最终变换矩阵写入
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, MatrixHelper.getFinalMatrix(),0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
    }
}
