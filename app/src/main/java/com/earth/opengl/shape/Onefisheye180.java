package com.earth.opengl.shape;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.earth.opengl.data.IndexBuffer;
import com.earth.opengl.data.VertexBuffer;
import com.earth.opengl.program.OneFishEye180ShaderProgram;
import com.earth.opengl.utils.MatrixHelper;
import com.earth.opengl.utils.TextureHelper;
import com.langtao.device.YUVFrame;
import com.langtao.fisheye.FishEyeProc;
import com.langtao.fisheye.OneFisheye180Param;
import com.langtao.fisheye.OneFisheyeOut;
import com.pixel.opengl.R;

import java.nio.ByteBuffer;


/**
 * Created by zzr on 2017/4/17.
 *  具体参照 http://blog.csdn.net/cassiepython/article/details/51620114
 */

public class Onefisheye180 {

    static{
        System.loadLibrary("one_fisheye");
        System.loadLibrary("LTFishEyeProc");
    }
    private static final String TAG = "Onefisheye180";
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
    public boolean isAutoCruise = true;
    //*****************************************************************
    private final Context context;
    private int numElements = 0;// 记录要画多少个三角形
    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COORDIANTE_COMPONENT_COUNT = 3; // 每个顶点的坐标数 x y z
    private static final int TEXTURE_COORDIANTE_COMPONENT_COUNT = 2; // 每个纹理坐标为 S T两个
    //*****************************************************************
    OneFishEye180ShaderProgram fishShader;
    private int drawElementType;
    private OneFisheyeOut out;
    private OneFisheye180Param outParam;
    private VertexBuffer verticesBuffer;
    private VertexBuffer texCoordsBuffer;
    private IndexBuffer indicesBuffer;
    private int mFrameWidth;
    private int mFrameHeight;
    private YUVFrame initFrame;
    private int[] _yuvTextureIDs;
    //***************************************************************
    public volatile boolean isInitialized = false;
    public volatile boolean initializing = false;

    public Onefisheye180(Context context,int frameWidth,int frameHeight,YUVFrame frame){
        this.context = context;
        this.mFrameWidth = frameWidth;
        this.mFrameHeight = frameHeight;
        this.initFrame = frame;
        initCurvedPlate180Param(frameWidth,frameHeight,frame);
    }

    public void initCurvedPlate180Param(int width, int height, YUVFrame frame){
        if(frame==null) return;
        initializing = true;
        createBufferData(width,height,frame);
        buildProgram();
        initTexture();
        setAttributeStatus();
        isInitialized = true;
        initializing = false;
    }


    private void createBufferData(int width,int height,YUVFrame frame) {
        if(out == null){
            try{
                //InputStream is = context.getResources().openRawResource(R.raw.img_20170725_forward);
                //byte[] dataArray = new byte[is.available()];
                //is.read(dataArray);
                outParam = new OneFisheye180Param();
                //int ret = FishEyeProc.getOneFisheye180Param(dataArray, 1280, 720, outParam);
                int ret = FishEyeProc.getOneFisheye180Param(frame.getYuvbyte(), width, height, outParam);
                if (ret != 0) {
                    return;
                }
                out = FishEyeProc.oneFisheye180Func(100);

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
        fishShader = new OneFishEye180ShaderProgram(context,
                R.raw.fisheye_180_vertex_shader,
                R.raw.fisheye_180_fragment_shader);
        GLES20.glUseProgram( fishShader.getShaderProgramId() );
    }


    private boolean initTexture() {
        //int[] yuvTextureIDs = TextureHelper.loadYUVTexture(context, R.raw.img_20170725_forward, 1280, 720);
        int[] yuvTextureIDs = TextureHelper.loadYUVTexture2(mFrameWidth, mFrameHeight,
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
        {
            //长宽没变，更新纹理，不重建
            TextureHelper.updateTexture2(_yuvTextureIDs[0], mFrameWidth, mFrameHeight, yDatabuffer);
            TextureHelper.updateTexture2(_yuvTextureIDs[1], mFrameWidth, mFrameHeight, uDatabuffer);
            TextureHelper.updateTexture2(_yuvTextureIDs[2], mFrameWidth, mFrameHeight, vDatabuffer);
        }
        //重新加载纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[0]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerY(), 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[1]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerU(), 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _yuvTextureIDs[2]);
        GLES20.glUniform1i(fishShader.getuLocationSamplerV(), 2);
        return true;
    }





    private float kColorConversion420[] = {
        1.0f, 1.0f, 1.0f,
        0.0f, -0.39465f, 2.03211f,
        1.13983f, -0.58060f, 0.0f
    };

    public void setAttributeStatus() {
        GLES20.glUniformMatrix3fv(fishShader.getuLocationCCM(), 1, false, kColorConversion420, 0);

        verticesBuffer.setVertexAttribPointer(fishShader.getaPositionLocation(),
                POSITION_COORDIANTE_COMPONENT_COUNT,
                POSITION_COORDIANTE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);

        texCoordsBuffer.setVertexAttribPointer(fishShader.getaTexCoordLocation(),
                TEXTURE_COORDIANTE_COMPONENT_COUNT,
                TEXTURE_COORDIANTE_COMPONENT_COUNT * BYTES_PER_FLOAT, 0);

        float width = (float)outParam.width;
        float height = (float)outParam.height;
        GLES20.glUniform1f(fishShader.getuLocationWidth(), width);
        GLES20.glUniform1f(fishShader.getuLocationHeight(), height);
        GLES20.glUniform1f(fishShader.getuLocationRectX(), outParam.rectX);
        GLES20.glUniform1f(fishShader.getuLocationRectY(), outParam.rectY);
        GLES20.glUniform1f(fishShader.getuLocationRectWidth(), outParam.rectWidth);
        GLES20.glUniform1f(fishShader.getuLocationRectHeight(), outParam.rectHeight);
    }


    public void draw(){
        //将最终变换矩阵写入
        GLES20.glUniformMatrix4fv(fishShader.getuMVPMatrixLocation(), 1, false, MatrixHelper.getFinalMatrix(),0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.getIndexBufferId());
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numElements, drawElementType, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}
