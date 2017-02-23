package com.particles.opengl;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView;

import com.particles.opengl.objects.Hightmap;
import com.particles.opengl.objects.ParticleShooter;
import com.particles.opengl.objects.ParticleSystem;
import com.particles.opengl.objects.Skybox;
import com.particles.opengl.programs.HightmapShaderProgram;
import com.particles.opengl.programs.ParticleShaderProgram;
import com.particles.opengl.programs.SkyboxShaderProgram;
import com.pixel.opengl.R;
import com.pixel.opengl.util.Geometry;
import com.pixel.opengl.util.MatrixHelper;
import com.pixel.opengl.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.scaleM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by ZZR on 2017/2/17.
 */

public class ParticlesRenderer implements GLSurfaceView.Renderer {

    private final Context context;

    private final float[] projectionMartix = new float[16];
    private final float[] viewMartix = new float[16];
    private final float[] viewMartixForSkybox = new float[16];

    private final float[] modelMatrix = new float[16];
    private final float[] tempMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];

    final float angleVarianceInDegrees = 8f;
    final float speedVariance = 1;

    private long globalStartTime;

    private ParticleShaderProgram particleShaderProgram;
    private ParticleSystem particleSystem;
    private ParticleShooter redParticleShooter;
    private ParticleShooter greenParticleShooter;
    private ParticleShooter blueParticleShooter;
    private int particleTexture;

    private SkyboxShaderProgram skyboxShaderProgram;
    private Skybox skybox;
    private int skyboxTexture;

    private Hightmap hightmap;
    private HightmapShaderProgram hightmapShaderProgram;

    public ParticlesRenderer(Context context) {
        this.context = context;
    }





    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);    //打开深度缓冲区功能
        glEnable(GL_CULL_FACE);     //关闭两面绘制，削减绘制开销，只绘制逆时针定义的三角形
        //粒子重力加速度，较暗的粒子会覆盖之前较明亮的粒子，所以需要使能混合技术
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_ONE,GL_ONE);

        particleShaderProgram = new ParticleShaderProgram(context,
                R.raw.particles_vertex_shader, R.raw.particles_fragment_shader);
        particleSystem = new ParticleSystem(10000);
        globalStartTime = System.nanoTime();

        final Geometry.Vector particleDirection = new Geometry.Vector(0.0f, 1.0f, 0.0f);

        redParticleShooter = new ParticleShooter(
                new Geometry.Point(-0.5f, 0f, 0f),
                particleDirection, Color.rgb(255, 50, 5),
                angleVarianceInDegrees,speedVariance);
        greenParticleShooter = new ParticleShooter(
                new Geometry.Point(0f, 0f, 0f),
                particleDirection, Color.rgb(25, 255, 25),
                angleVarianceInDegrees,speedVariance);
        blueParticleShooter = new ParticleShooter(
                new Geometry.Point(0.5f, 0f, 0f),
                particleDirection, Color.rgb(5, 50, 255),
                angleVarianceInDegrees,speedVariance);

        particleTexture = TextureHelper.loadTexture(context, R.drawable.particle_texture);

        skyboxShaderProgram = new SkyboxShaderProgram(context,
                R.raw.skybox_vertex_shader, R.raw.skybox_fragment_shader);
        skybox = new Skybox();
        skyboxTexture = TextureHelper.loadCubMap(context,
                new int[]{ R.drawable.left, R.drawable.right,
                        R.drawable.bottom, R.drawable.top,
                        R.drawable.front, R.drawable.back});

        BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.heightmap);
        hightmap = new Hightmap(drawable.getBitmap());
        hightmapShaderProgram = new HightmapShaderProgram(context,R.raw.hightmap_vertex_shader,R.raw.hightmap_fragment_shader);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0,0,width,height);

        MatrixHelper.perspectiveM(projectionMartix, 45, (float)width/(float)height, 1f, 100f);
        //10章以前的，喷泉粒子没有背景前
//        setIdentityM(viewMartix, 0);
//        translateM(viewMartix, 0, 0f, -1.5f, -5f);
//        multiplyMM(viewProjectionMartix,0, projectionMartix,0, viewMartix,0);
        updateViewMatrices();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //glClear(GL_COLOR_BUFFER_BIT);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //10章以前的，喷泉粒子没有背景前
//        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
//        redParticleShooter.addParticles(particleSystem, currentTime, 5);
//        greenParticleShooter.addParticles(particleSystem, currentTime, 5);
//        blueParticleShooter.addParticles(particleSystem, currentTime, 5);
//
//        particleShaderProgram.useProgram();
//        particleShaderProgram.setUniforms(viewProjectionMartix, currentTime, particleTexture);
//        particleSystem.bindData(particleShaderProgram);
//        particleSystem.draw();

        drawHeightmap();
        drawSkybox();
        drawParticles();
    }

    private void drawHeightmap() {
        setIdentityM(modelMatrix, 0);
        scaleM(modelMatrix,0, 100f,10f,100f);
        updateMvpMatrix();
        hightmapShaderProgram.useProgram();
        hightmapShaderProgram.setUniform(modelViewProjectionMatrix);
        hightmap.bindData(hightmapShaderProgram);
        hightmap.draw();
    }

    private void drawParticles() {
        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
        redParticleShooter.addParticles(particleSystem, currentTime, 5);
        greenParticleShooter.addParticles(particleSystem, currentTime, 5);
        blueParticleShooter.addParticles(particleSystem, currentTime, 5);

        //11章天空盒
        //屏幕竖着划，镜头是要沿着x轴(水平基准)旋转。横划同理
//        setIdentityM(viewMartix, 0);
//        rotateM(viewMartix,0, -yRotation, 1f,0f,0f);
//        rotateM(viewMartix,0, -xRotation, 0f,1f,0f);
//        translateM(viewMartix, 0, 0f, -1.5f, -5f);
//        multiplyMM(modelViewProjectionMatrix,0, projectionMartix,0, viewMartix,0);
        setIdentityM(modelMatrix, 0);
        updateMvpMatrix();
        //禁用深度更新，这样烟花就不会被地板阻挡，而已三色烟花也能正常混合了。
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE,GL_ONE);

        particleShaderProgram.useProgram();
        particleShaderProgram.setUniforms(modelViewProjectionMatrix, currentTime, particleTexture);
        particleSystem.bindData(particleShaderProgram);
        particleSystem.draw();
        glDisable(GL_BLEND);
        glDepthMask(true);
    }

    private void updateMvpMatrix(){
        multiplyMM(tempMatrix,0, viewMartix,0, modelMatrix,0);
        multiplyMM(modelViewProjectionMatrix,0, projectionMartix,0, tempMatrix,0);
    }

    private void drawSkybox() {
        //11章天空盒
        //屏幕横着划，镜头是要沿着y轴(向上的方向)旋转。竖划同理
//        setIdentityM(viewMartix, 0);
//        rotateM(viewMartix,0, -yRotation, 1f,0f,0f);
//        rotateM(viewMartix,0, -xRotation, 0f,1f,0f);
//        multiplyMM(viewProjectionMartix,0, projectionMartix,0, viewMartix,0);
        setIdentityM(modelMatrix, 0);
        updateMvpMatirxForSkybox();
        glDepthFunc(GL_LEQUAL);
        // This avoids problems with the skybox itself getting clipped. 避免了Skybox本身自动裁剪问题。
        // 问题现象表现为天空盒某个角度会出现黑边，这是因为GL_LESS模式自动裁剪了。
        skyboxShaderProgram.useProgram();
        skyboxShaderProgram.setUniforms(modelViewProjectionMatrix, skyboxTexture);
        skybox.bindData(skyboxShaderProgram);
        skybox.draw();
        glDepthFunc(GL_LESS);
    }

    private void updateMvpMatirxForSkybox(){
        multiplyMM(tempMatrix,0, viewMartixForSkybox,0, modelMatrix,0);
        multiplyMM(modelViewProjectionMatrix,0, projectionMartix,0, tempMatrix,0);
    }

    private float xRotation, yRotation;
    public void handleTouchDrag(float deltaX, float deltaY) {
        xRotation += deltaX / 16f;
        yRotation += deltaY / 16f;

        if(yRotation < -90){
            yRotation = -90;
        }else if(yRotation > 90){
            yRotation = 90;
        }
        //拖动的操作要更新视图矩阵(相机)
        updateViewMatrices();
    }

    private void updateViewMatrices(){
        setIdentityM(viewMartix, 0);
        rotateM(viewMartix,0, -yRotation, 1f,0f,0f);
        rotateM(viewMartix,0, -xRotation, 0f,1f,0f);
        System.arraycopy(viewMartix,0, viewMartixForSkybox,0, viewMartix.length);
        translateM(viewMartix,0, 0f,-1f,-3.5f);
    }
}
