package com.pixel.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.pixel.opengl.objects.Mallet;
import com.pixel.opengl.objects.Puck;
import com.pixel.opengl.objects.Table;
import com.pixel.opengl.programs.ColorShaderProgram;
import com.pixel.opengl.programs.TextureShaderProgram;
import com.pixel.opengl.util.LoggerConfig;
import com.pixel.opengl.util.MatrixHelper;
import com.pixel.opengl.util.ShaderHelper;
import com.pixel.opengl.util.TextResourceReader;
import com.pixel.opengl.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.R.attr.mode;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;
import static com.pixel.opengl.Contants.BYTES_PER_FLOAT;

/**
 * Created by ZZR on 2017/2/6.
 */

public class OpenGLRenderer implements GLSurfaceView.Renderer {
//    int parseColor = Color.parseColor("#0099CC");
//    float red = Color.red(parseColor) / 255;
//    float green = Color.green(parseColor) / 255;
//    float blue = Color.blue(parseColor) / 255;

    private final Context context;
    private final float[] viewMatrix = new float[16];               //视图矩阵
    private final float[] projectionMatrix = new float[16];         //投影矩阵
    private final float[] viewProjectionMatrix = new float[16];     //视图*投影矩阵
    private final float[] modelMatrix = new float[16];              //修正(旋转平移缩放)矩阵
    private final float[] modelViewProjectionMatrix = new float[16];//修正之后的视图投影矩阵

    private Table table;
    private Mallet mallet;
    private Puck puck;

    private ColorShaderProgram colorShaderProgram;
    private TextureShaderProgram textureShaderProgram;

    private int textureId;

    public OpenGLRenderer(Context context){
        this.context = context;
    }






    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f,0.0f,0.0f,0.0f);

        colorShaderProgram = new ColorShaderProgram(context);
        textureShaderProgram=new TextureShaderProgram(context);

        table = new Table();
        mallet= new Mallet(0.08f, 0.15f, 32);
        puck = new Puck(0.06f, 0.02f, 32);

        textureId = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0,0,width,height);

        //透视投影矩阵
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float)width/(float)height, 1f, 10f);
        //透视投影的修正矩阵
//        setIdentityM(modelMatrix,0);
//        translateM(modelMatrix, 0, 0f,0f,-3f);
//        rotateM(modelMatrix, 0, -30f, 1f, 0f, 0f);
//
//        final float[] temp = new float[16];
//        multiplyMM(temp,0, projectionMatrix,0, modelMatrix,0);
//
//        System.arraycopy(temp,0, projectionMatrix,0,temp.length);

        setLookAtM(viewMatrix, 0,   //setLookAtM会把结果从rm偏移值开始保存
                0f, 1.2f, 2.2f,     //眼镜所在位置，场景中所有东西看起来都像是这个点观察
                0f, 0f, 0f,         //眼镜正在看的点坐标
                0f, 1f, 0f);        //刚才讨论的是眼镜，这个就是你的头指向的地方，upY=1意味这你的头笔直指向上方
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GL_COLOR_BUFFER_BIT);
        multiplyMM(viewProjectionMatrix,0, projectionMatrix,0, viewMatrix,0);

        positionTableInScene();
        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(modelViewProjectionMatrix, textureId);
        table.bindData(textureShaderProgram);
        table.draw();

        positionObjectInScene(0f, mallet.height / 2f, -0.4f);
        colorShaderProgram.useProgram();
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 1f,0f,0f);
        mallet.bindData(colorShaderProgram);
        mallet.draw();

        positionObjectInScene(0f, mallet.height / 2f, 0.4f);
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f,0f,1f);
        //我们不需要定义两次object的数据，只需要在不同的位置和颜色重新画一个就好
        mallet.draw();

        positionObjectInScene(0f, puck.height / 2f, 0f);
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f);
        puck.bindData(colorShaderProgram);
        puck.draw();
    }

    private void positionObjectInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x,y,z);
        multiplyMM(modelViewProjectionMatrix,0,
                viewProjectionMatrix,0,
                modelMatrix,0);
    }

    private void positionTableInScene() {
        //The table is defined in terms of X&Y coordinates,
        // so we rotate it 90 degrees to lie flat on the XZ plane
        setIdentityM(modelMatrix, 0);
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix,0, viewProjectionMatrix,0, modelMatrix,0);
    }
}
