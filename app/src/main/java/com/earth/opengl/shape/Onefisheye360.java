package com.earth.opengl.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.earth.opengl.data.IndexBuffer;
import com.earth.opengl.data.VertexBuffer;
import com.earth.opengl.program.OneFishEye360ShaderProgram;
import com.earth.opengl.utils.MatrixHelper;
import com.earth.opengl.utils.TextureHelper;
import com.langtao.device.YUVFrame;
import com.langtao.fisheye.FishEyeProc;
import com.langtao.fisheye.OneFisheye360Param;
import com.langtao.fisheye.OneFisheyeOut;
import com.pixel.opengl.R;

import java.nio.ByteBuffer;


/**
 * Created by zzr on 2017/4/17.
 *  具体参照 http://blog.csdn.net/cassiepython/article/details/51620114
 */

public class Onefisheye360 {

    static{
        System.loadLibrary("one_fisheye");
        System.loadLibrary("LTFishEyeProc");
    }
    private static final String TAG = "Onefisheye360";
    //*****************************************************************
    //** 单手双手操作相关
    public float mLastX;
    public float mLastY;
    public float mfingerRotationX = 0;
    public float mfingerRotationY = 0;
    public float mfingerRotationZ = 0;
    public float[] mMatrixFingerRotationX = new float[16];
    public float[] mMatrixFingerRotationY = new float[16];
    public float[] mMatrixFingerRotationZ = new float[16];
    public float zoomTimes = 0.0f;
    //** 惯性自转标志
    public volatile boolean gestureInertia_isStop = true;
    public volatile boolean pullupInertia_isStop = true;
    public int mFrameWidth;
    public int mFrameHeight;
    private int[] _yuvTextureIDs;
    //*****************************************************************
    private final Context context;
    private int numElements = 0;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COORDINATE_COMPONENT_COUNT = 3; // 每个顶点的坐标数 x y z
    private static final int TEXTURE_COORDINATE_COMPONENT_COUNT = 2; // 每个纹理坐标为 S T两个
    //**************************坐标改成索引***************************************
    private OneFishEye360ShaderProgram fishShader;
    private int drawElementType;
    private OneFisheyeOut out;
    private VertexBuffer verticesBuffer;
    private VertexBuffer texCoordsBuffer;
    private IndexBuffer indicesBuffer;

    private int frameWidth;
    private int frameHeight;
    private YUVFrame initFrame;
    //***************************************************************
    public volatile boolean isInitialized = false;
    public volatile boolean initializing = false;


    public Onefisheye360(Context context,int frameWidth,int frameHeight,YUVFrame frame){
        this.context = context;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.initFrame = frame;
        initFishEye360Param(frameWidth, frameHeight, frame);
    }

    public void initFishEye360Param(int width, int height, YUVFrame frame){
        if(frame==null) return;
        initializing = true;
        createBufferData( width, height, frame);
        buildProgram();
        initTexture();
        setAttributeStatus();
        isInitialized = true;
        initializing = false;
    }

    private void createBufferData(int width,int height,YUVFrame frame) {
        if(out == null){
            try{
                //InputStream is = context.getResources().openRawResource(R.raw.img_20170725_down);
                //byte[] dataArray = new byte[is.available()];
                //is.read(dataArray);
                OneFisheye360Param outParam = new OneFisheye360Param();
                //int ret = FishEyeProc.getOneFisheye360Param(dataArray, 1280, 1024, outParam);
                int ret = FishEyeProc.getOneFisheye360Param(frame.getYuvbyte(), width, height, outParam);
                if (ret != 0) {
                    return;
                }

                out = FishEyeProc.oneFisheye360Func(100, outParam);
            }catch ( Exception e){
                e.printStackTrace();
                return;
            }
        }

        verticesBuffer = new VertexBuffer(out.vertices);
        texCoordsBuffer = new VertexBuffer(out.texCoords);

        numElements = out.indices.length;
        if(numElements > Short.MAX_VALUE){
            short[] element_index = new short[numElements];
            for (int i = 0; i < out.indices.length; i++) {
                element_index[i] = (short) out.indices[i];
            }
            indicesBuffer = new IndexBuffer(element_index);
            drawElementType = GLES20.GL_UNSIGNED_SHORT;
        }else{
            int[] element_index = new int[numElements];
            System.arraycopy(out.indices, 0, element_index, 0, out.indices.length);
            indicesBuffer = new IndexBuffer(element_index);
            drawElementType = GLES20.GL_UNSIGNED_INT;
        }
    }

    private void buildProgram() {
        fishShader = new OneFishEye360ShaderProgram(context,
                R.raw.fisheye_360_vertex_shader,
                R.raw.fisheye_360_fragment_shader);
        GLES20.glUseProgram( fishShader.getShaderProgramId() );
    }

    private boolean initTexture() {
        int[] yuvTextureIDs = TextureHelper.loadYUVTexture2(frameWidth, frameHeight,
                initFrame.getYDatabuffer(),initFrame.getUDatabuffer(),initFrame.getVDatabuffer());
        if(yuvTextureIDs == null || yuvTextureIDs.length != 3) {
            Log.w(TAG,"yuvTextureIDs object's length not equals 3 !");
            return false;
        }
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[0]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerY(), 0); // => GLES20.GL_TEXTURE0

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[1]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerU(), 1); // => GLES20.GL_TEXTURE1

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextureIDs[2]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerV(), 2); // => GLES20.GL_TEXTURE2

        _yuvTextureIDs = yuvTextureIDs;
        return true;
    }

    public boolean updateTexture(YUVFrame yuvFrame ){
        if(yuvFrame==null) return false;
        int width = yuvFrame.getWidth();
        int height = yuvFrame.getHeight();
        ByteBuffer yDatabuffer = yuvFrame.getYDatabuffer();
        ByteBuffer uDatabuffer = yuvFrame.getUDatabuffer();
        ByteBuffer vDatabuffer = yuvFrame.getVDatabuffer();

        if(width != mFrameWidth || height!= mFrameHeight)
        {
            //先去掉旧的纹理
            GLES20.glDeleteTextures(_yuvTextureIDs.length, _yuvTextureIDs, 0);
            //重新加载数据
            int[] yuvTextureIDs = TextureHelper.loadYUVTexture2(width, height,
                    yDatabuffer, uDatabuffer, vDatabuffer);
            _yuvTextureIDs = yuvTextureIDs;
            mFrameWidth = width;
            mFrameHeight = height;
        }
        else
        {//长宽没变，更新纹理，不重建
            TextureHelper.updateTexture2(_yuvTextureIDs[0], mFrameWidth, mFrameHeight, yDatabuffer);
            TextureHelper.updateTexture2(_yuvTextureIDs[1], mFrameWidth, mFrameHeight, uDatabuffer);
            TextureHelper.updateTexture2(_yuvTextureIDs[2], mFrameWidth, mFrameHeight, vDatabuffer);
        }

        //重新加载纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[0]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerY(), 0); // => GLES20.GL_TEXTURE0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[1]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerU(), 1); // => GLES20.GL_TEXTURE1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[2]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerV(), 2); // => GLES20.GL_TEXTURE2
        return true;
    }

    private float kColorConversion420[] = {
            1.0f, 1.0f, 1.0f,
            0.0f, -0.39465f, 2.03211f,
            1.13983f, -0.58060f, 0.0f
    };
    private void setAttributeStatus() {
        GLES20.glUniformMatrix3fv(fishShader.getuLocationCCM(), 1, false, kColorConversion420, 0);

        verticesBuffer.setVertexAttribPointer(fishShader.getaPositionLocation(),
                POSITION_COORDINATE_COMPONENT_COUNT,
                POSITION_COORDINATE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);

        texCoordsBuffer.setVertexAttribPointer(fishShader.getaTexCoordLocation(),
                TEXTURE_COORDINATE_COMPONENT_COUNT,
                TEXTURE_COORDINATE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);
    }


    public void draw(){
        //将最终变换矩阵写入
        GLES20.glUniformMatrix4fv(fishShader.getuMVPMatrixLocation(), 1, false, MatrixHelper.getFinalMatrix(),0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.getIndexBufferId());
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numElements, drawElementType, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}
