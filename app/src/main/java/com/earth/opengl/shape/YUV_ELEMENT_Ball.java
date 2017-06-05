package com.earth.opengl.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.earth.opengl.data.IndexBuffer;
import com.earth.opengl.data.VertexBuffer;
import com.earth.opengl.utils.MatrixHelper;
import com.earth.opengl.utils.ShaderHelper;
import com.earth.opengl.utils.TextResourceReader;
import com.earth.opengl.utils.TextureHelper;
import com.pixel.opengl.R;

import java.util.ArrayList;


/**
 * Created by nicky on 2017/4/17.
 *  具体参照 http://blog.csdn.net/cassiepython/article/details/51620114
 */

public class YUV_ELEMENT_Ball {
    private static final String TAG = "YUV_ELEMENT_Ball";
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
    private final int angleSpan = 5;// 将球行单位进切分的角度
    private static final float UNIT_SIZE = 1.0f;
    private float radius = 0.8f;
    private int vCount = 0;     // 记录顶点个数，先初始化为0，一个顶点由xyz和st 5个float构成
    private int numElements = 0;// 记录要画多少个三角形
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COORDIANTE_COMPONENT_COUNT = 3; // 每个顶点的坐标数 x y z
    private static final int TEXTURE_COORDIANTE_COMPONENT_COUNT = 2; // 每个纹理坐标为 S T两个
    private int program;
    private static final String A_POSITION = "a_Position";
    private static final String U_MATRIX = "u_Matrix";
    private int uMatrixLocation;
    private int aPositionLocation;
    private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    private int aTextureCoordinates;
    //**************************增加YUV支持*************************************
    private static final String SAMPLER_Y = "SamplerY";
    private int uLocationSamplerY;
    private static final String SAMPLER_U = "SamplerU";
    private int uLocationSamplerU;
    private static final String SAMPLER_V = "SamplerV";
    private int uLocationSamplerV;
    //**************************坐标改成索引************************************
    private static final int STRIDE = (POSITION_COORDIANTE_COMPONENT_COUNT
            + TEXTURE_COORDIANTE_COMPONENT_COUNT)
            * BYTES_PER_FLOAT;
    private VertexBuffer vstBuffer;
    private IndexBuffer indexBuffer;

    public YUV_ELEMENT_Ball(Context context){
        this.context = context;

        createIndexAndVSTData();

        buildProgram();

        initShaderVariable();

        initTexture();

        setAttributeStatus();
    }


    private void createIndexAndVSTData() {
        ArrayList<Float> VSTList = new ArrayList<Float>();// V顶点坐标(xyz)，ST纹理坐标(st)
        ArrayList<Short> indexList = new ArrayList<Short>();// 顶点所以数组
        short offset = 0;
        for (int vAngle = 0; vAngle < 180; vAngle = vAngle + angleSpan)
        {
            for (int hAngle = 0; hAngle <= 360; hAngle = hAngle + angleSpan)
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

                float s0 = hAngle / 360.0f;
                float t0 = vAngle / 180.0f;
                float s1 = (hAngle + angleSpan)/360.0f ;
                float t1 = (vAngle + angleSpan) / 180.0f;

                //左下角
                VSTList.add(x0);
                VSTList.add(y0);
                VSTList.add(z0);
                VSTList.add(s0);
                VSTList.add(t0);

                //右下角
                VSTList.add(x1);
                VSTList.add(y1);
                VSTList.add(z1);
                VSTList.add(s1);
                VSTList.add(t0);

                //右上角
                VSTList.add(x2);
                VSTList.add(y2);
                VSTList.add(z2);
                VSTList.add(s1);
                VSTList.add(t1);

                //左上角
                VSTList.add(x3);
                VSTList.add(y3);
                VSTList.add(z3);
                VSTList.add(s0);
                VSTList.add(t1);

                indexList.add((short)(offset + 1));
                indexList.add((short)(offset + 3));
                indexList.add((short)(offset + 0));
                indexList.add((short)(offset + 1));
                indexList.add((short)(offset + 2));
                indexList.add((short)(offset + 3));

                offset += 4; // 4个顶点


            }
        }
        // *****************************************************************
        vCount = VSTList.size() / (POSITION_COORDIANTE_COMPONENT_COUNT + TEXTURE_COORDIANTE_COMPONENT_COUNT);
        numElements = indexList.size();// 每4个顶点构成2个三角形元素 * 3个索引点

        Log.w(TAG, "VSTList.size : "+VSTList.size()); //多少个float数据
        Log.w(TAG, "indexList.size : "+indexList.size());

        float[] element_VSTData = new float[VSTList.size()];
        for (int i = 0; i < VSTList.size(); i++) {
            element_VSTData[i] = VSTList.get(i);
        }
        vstBuffer = new VertexBuffer(element_VSTData);
        short[] element_index = new short[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            element_index[i] = indexList.get(i);
        }
        indexBuffer = new IndexBuffer(element_index);
    }

    private void initShaderVariable() {
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);
        aTextureCoordinates = GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES);
        uLocationSamplerY = GLES20.glGetUniformLocation(program, SAMPLER_Y);
        uLocationSamplerU = GLES20.glGetUniformLocation(program, SAMPLER_U);
        uLocationSamplerV = GLES20.glGetUniformLocation(program, SAMPLER_V);
    }

    private void buildProgram() {
        //获取顶点着色器文本
        String vertexShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.ball_vertex_shader);
        //获取片段着色器文本
        String fragmentShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.ball_yuv_fragment_shader);
        //获取program的id
        program = ShaderHelper.buildProgram(vertexShaderSource, fragmentShaderSource);
        GLES20.glUseProgram(program);
    }

    private boolean initTexture() {
        int[] yuvTextureIDs = TextureHelper.loadYUVTexture(context, R.raw.yuv_test_pic);
        if(yuvTextureIDs == null || yuvTextureIDs.length != 3) {
            Log.w(TAG,"yuvTextureIDs object's length not equals 3 !");
            return false;
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[0]);
        GLES20.glUniform1i(uLocationSamplerY, 0); // => GLES20.GL_TEXTURE0

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[1]);
        GLES20.glUniform1i(uLocationSamplerU, 1); // => GLES20.GL_TEXTURE1

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[2]);
        GLES20.glUniform1i(uLocationSamplerV, 2); // => GLES20.GL_TEXTURE2
        return true;
    }

    public void setAttributeStatus() {
        vstBuffer.setVertexAttribPointer(0,
                aPositionLocation,
                POSITION_COORDIANTE_COMPONENT_COUNT, STRIDE);
        vstBuffer.setVertexAttribPointer(
                POSITION_COORDIANTE_COMPONENT_COUNT * BYTES_PER_FLOAT,
                aTextureCoordinates,
                TEXTURE_COORDIANTE_COMPONENT_COUNT, STRIDE);
    }

    public void draw(){
        //将最终变换矩阵写入
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, MatrixHelper.getFinalMatrix(),0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getIndexBufferId());
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numElements, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}
