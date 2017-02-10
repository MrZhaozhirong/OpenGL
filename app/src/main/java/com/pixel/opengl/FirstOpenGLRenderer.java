package com.pixel.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.pixel.opengl.objects.Mallet;
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
import static android.opengl.Matrix.translateM;
import static com.pixel.opengl.Contants.BYTES_PER_FLOAT;

/**
 * Created by ZZR on 2017/2/6.
 */

public class FirstOpenGLRenderer implements GLSurfaceView.Renderer {
//    int parseColor = Color.parseColor("#0099CC");
//    float red = Color.red(parseColor) / 255;
//    float green = Color.green(parseColor) / 255;
//    float blue = Color.blue(parseColor) / 255;

    private final Context context;
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private Table table;
    private Mallet mallet;

    private ColorShaderProgram colorShaderProgram;
    private TextureShaderProgram textureShaderProgram;

    private int textureId;

    public FirstOpenGLRenderer(Context context){
        this.context = context;
    }






    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f,0.0f,0.0f,0.0f);

        colorShaderProgram = new ColorShaderProgram(context);
        textureShaderProgram=new TextureShaderProgram(context);

        table = new Table();
        mallet= new Mallet();

        textureId = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface_low_res);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0,0,width,height);

        //透视投影矩阵
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float)width/(float)height, 1f, 10f);
        //透视投影的修正矩阵
        setIdentityM(modelMatrix,0);
        translateM(modelMatrix, 0, 0f,0f,-3f);
        rotateM(modelMatrix, 0, -30f, 1f, 0f, 0f);

        final float[] temp = new float[16];
        multiplyMM(temp,0, projectionMatrix,0, modelMatrix,0);

        System.arraycopy(temp,0, projectionMatrix,0,temp.length);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GL_COLOR_BUFFER_BIT);

        textureShaderProgram.useProgram();
        textureShaderProgram.setUniforms(projectionMatrix, textureId);
        table.bindData(textureShaderProgram);
        table.draw();

        colorShaderProgram.useProgram();
        colorShaderProgram.setUniforms(projectionMatrix);
        mallet.bindData(colorShaderProgram);
        mallet.draw();
    }
}
