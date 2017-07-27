package com.earth.opengl.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.earth.opengl.data.IndexBuffer;
import com.earth.opengl.data.VertexBuffer;
import com.earth.opengl.program.PanoramaBallShaderProgram;
import com.earth.opengl.utils.MatrixHelper;
import com.earth.opengl.utils.TextureHelper;
import com.langtao.ltpanorama.PanoramaIn;
import com.langtao.ltpanorama.PanoramaOut;
import com.langtao.ltpanorama.PanoramaProc;
import com.pixel.opengl.R;


/**
 * Created by zzr on 2017/4/17.
 *  具体参照 http://blog.csdn.net/cassiepython/article/details/51620114
 */

public class PANORAMA_Ball {

    static{
        System.loadLibrary("panorama");
        System.loadLibrary("LTPanoramaProc");
    }
    private static final String TAG = "PANORAMA_Ball";
    //*****************************************************************
    //** 单手双手操作相关
    public float mLastX;
    public float mLastY;
    public float mfingerRotationX = 0;
    public float mfingerRotationY = 0;
    public float[] mMatrixFingerRotationX = new float[16];
    public float[] mMatrixFingerRotationY = new float[16];
    public float zoomTimes = 0.0f;
    //** 惯性自滚标志
    public boolean gestureInertia_isStop = true;
    //** 纵角度限制相关
    public BallRollBoundaryDirection boundaryDirection = BallRollBoundaryDirection.NORMAL;
    public double moving_count_auto_return = 0.0f;
    //*****************************************************************
    private final Context context;
    private int numElements = 0;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COORDINATE_COMPONENT_COUNT = 3; // 每个顶点的坐标数 x y z
    private static final int TEXTURE_COORDINATE_COMPONENT_COUNT = 2; // 每个纹理坐标为 S T两个
    //**************************坐标改成索引***************************************
    private PanoramaBallShaderProgram pbShader;
    private PanoramaOut out;
    private VertexBuffer verticesBuffer;
    private VertexBuffer texCoordsBuffer;
    private VertexBuffer texFuseBuffer;
    private IndexBuffer indicesBuffer;

    public PANORAMA_Ball(Context context){
        this.context = context;

        createBufferData();

        buildProgram();

        initTexture();

        setAttributeStatus();
    }


    private void createBufferData() {
        if(out == null){
            try{
                PanoramaIn panoIn = new PanoramaIn();
                panoIn.width = 3040;
                panoIn.height = 1520;
                panoIn.circleCenter1X = 760.0f;
                panoIn.circleCenter1Y = 750.0f;
                panoIn.horizontalRadius1 = 750.0f;
                panoIn.verticalRadius1 = 750.0f;
                panoIn.circleCenter2X = 2276.0f;
                panoIn.circleCenter2Y = 754.0f;
                panoIn.horizontalRadius2 = 750.0f;
                panoIn.verticalRadius2 = 750.0f;
                panoIn.percent = 0.044f;
                out = PanoramaProc.panoramaSphere(0, 200, panoIn);
            }catch ( Exception e){
                e.printStackTrace();
                return;
            }
        }

        verticesBuffer = new VertexBuffer(out.vertices);
        texCoordsBuffer = new VertexBuffer(out.texCoords);
        texFuseBuffer = new VertexBuffer(out.texFuse);

        numElements = out.indices.length;
        if(numElements > Short.MAX_VALUE){
            short[] element_index = new short[numElements];
            for (int i = 0; i < out.indices.length; i++) {
                element_index[i] = (short) out.indices[i];
            }
            indicesBuffer = new IndexBuffer(element_index);
        }else{
            int[] element_index = new int[numElements];
            System.arraycopy(out.indices, 0, element_index, 0, out.indices.length);
            indicesBuffer = new IndexBuffer(element_index);
        }
    }

    private void buildProgram() {
        pbShader = new PanoramaBallShaderProgram(context,
                R.raw.panorama_ball_vertex_shader,
                R.raw.panorama_ball_fragment_shader);
        GLES20.glUseProgram( pbShader.getShaderProgramId() );
    }


    private boolean initTexture() {
        int[] yuvTextureIDs = TextureHelper.loadYUVTexture(context, R.raw.img_20170519_002, 3040, 1520);
        if(yuvTextureIDs == null || yuvTextureIDs.length != 3) {
            Log.w(TAG,"yuvTextureIDs object's length not equals 3 !");
            return false;
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[0]);
        GLES20.glUniform1i(pbShader.getuLocationSamplerY(), 0); // => GLES20.GL_TEXTURE0

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[1]);
        GLES20.glUniform1i(pbShader.getuLocationSamplerU(), 1); // => GLES20.GL_TEXTURE1

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[2]);
        GLES20.glUniform1i(pbShader.getuLocationSamplerV(), 2); // => GLES20.GL_TEXTURE2
        return true;
    }

    private float kColorConversion420[] = {
        1, 1, 1,
        0, -0.39465f, 2.03211f,
        1.13983f, -0.58060f, 0
    };
    private void setAttributeStatus() {
        GLES20.glUniformMatrix3fv(pbShader.getuLocationCCM(), 1, false, kColorConversion420, 0);

        verticesBuffer.setVertexAttribPointer(pbShader.getaPositionLocation(),
                POSITION_COORDINATE_COMPONENT_COUNT,
                POSITION_COORDINATE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);

        texCoordsBuffer.setVertexAttribPointer(pbShader.getaTexCoordLocation(),
                TEXTURE_COORDINATE_COMPONENT_COUNT,
                TEXTURE_COORDINATE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);

        texFuseBuffer.setVertexAttribPointer(pbShader.getaTexFuseLocation(),
                TEXTURE_COORDINATE_COMPONENT_COUNT,
                TEXTURE_COORDINATE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);

        GLES20.glUniform1i(pbShader.getuLocationImageMode(), 0);
        GLES20.glUniform1f(pbShader.getuLocationPercent(), out.percent);

        GLES20.glUniform1f(pbShader.getuLocationFactorA11(), out.factorA11);
        GLES20.glUniform1f(pbShader.getuLocationFactorB11(), out.factorB11);
        GLES20.glUniform1f(pbShader.getuLocationFactorA12(), out.factorA12);
        GLES20.glUniform1f(pbShader.getuLocationFactorB12(), out.factorB12);
        GLES20.glUniform1f(pbShader.getuLocationFactorA21(), out.factorA21);
        GLES20.glUniform1f(pbShader.getuLocationFactorB21(), out.factorB21);
        GLES20.glUniform1f(pbShader.getuLocationFactorA22(), out.factorA22);
        GLES20.glUniform1f(pbShader.getuLocationFactorB22(), out.factorB22);
    }


    public void draw(){
        //将最终变换矩阵写入
        GLES20.glUniformMatrix4fv(pbShader.getuMVPMatrixLocation(), 1, false, MatrixHelper.getFinalMatrix(),0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.getIndexBufferId());
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numElements, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}
