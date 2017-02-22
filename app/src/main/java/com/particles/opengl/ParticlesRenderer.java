package com.particles.opengl;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;

import com.particles.opengl.objects.ParticleShooter;
import com.particles.opengl.objects.ParticleSystem;
import com.particles.opengl.objects.Skybox;
import com.particles.opengl.programs.ParticleShaderProgram;
import com.particles.opengl.programs.SkyboxShaderProgram;
import com.pixel.opengl.R;
import com.pixel.opengl.util.Geometry;
import com.pixel.opengl.util.MatrixHelper;
import com.pixel.opengl.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.R.attr.x;
import static android.R.attr.y;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Created by ZZR on 2017/2/17.
 */

public class ParticlesRenderer implements GLSurfaceView.Renderer {

    private final Context context;

    private final float[] projectionMartix = new float[16];
    private final float[] viewMartix = new float[16];
    private final float[] viewProjectionMartix = new float[16];

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

    public ParticlesRenderer(Context context) {
        this.context = context;
    }





    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //粒子重力加速度，较暗的粒子会覆盖之前较明亮的粒子，所以需要使能混合技术
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_ONE,GL_ONE);

        particleShaderProgram = new ParticleShaderProgram(context,
                R.raw.particles_vertex_shader, R.raw.particles_fragment_shader);
        particleSystem = new ParticleSystem(10000);
        globalStartTime = System.nanoTime();

        final Geometry.Vector particleDirection = new Geometry.Vector(0.0f, 1.0f, 0.0f);

        redParticleShooter = new ParticleShooter(
                new Geometry.Point(-1f, 0f, 0f),
                particleDirection, Color.rgb(255, 50, 5),
                angleVarianceInDegrees,speedVariance);
        greenParticleShooter = new ParticleShooter(
                new Geometry.Point(0f, 0f, 0f),
                particleDirection, Color.rgb(25, 255, 25),
                angleVarianceInDegrees,speedVariance);
        blueParticleShooter = new ParticleShooter(
                new Geometry.Point(1f, 0f, 0f),
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
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0,0,width,height);

        MatrixHelper.perspectiveM(projectionMartix, 45, (float)width/(float)height, 1f, 10f);
        //10章以前的，喷泉粒子没有背景前
//        setIdentityM(viewMartix, 0);
//        translateM(viewMartix, 0, 0f, -1.5f, -5f);
//        multiplyMM(viewProjectionMartix,0, projectionMartix,0, viewMartix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
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

        drawSkybox();
        drawParticles();
    }

    private void drawParticles() {
        float currentTime = (System.nanoTime() - globalStartTime) / 1000000000f;
        redParticleShooter.addParticles(particleSystem, currentTime, 5);
        greenParticleShooter.addParticles(particleSystem, currentTime, 5);
        blueParticleShooter.addParticles(particleSystem, currentTime, 5);

        setIdentityM(viewMartix, 0);
        //屏幕竖着划，镜头是要沿着x轴(水平基准)旋转。横划同理
        rotateM(viewMartix,0, -yRotation, 1f,0f,0f);
        rotateM(viewMartix,0, -xRotation, 0f,1f,0f);
        translateM(viewMartix, 0, 0f, -1.5f, -5f);
        multiplyMM(viewProjectionMartix,0, projectionMartix,0, viewMartix,0);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE,GL_ONE);

        particleShaderProgram.useProgram();
        particleShaderProgram.setUniforms(viewProjectionMartix, currentTime, particleTexture);
        particleSystem.bindData(particleShaderProgram);
        particleSystem.draw();
        glDisable(GL_BLEND);
    }

    private void drawSkybox() {
        setIdentityM(viewMartix, 0);
        //屏幕横着划，镜头是要沿着y轴(向上的方向)旋转。竖划同理
        rotateM(viewMartix,0, -yRotation, 1f,0f,0f);
        rotateM(viewMartix,0, -xRotation, 0f,1f,0f);
        multiplyMM(viewProjectionMartix,0, projectionMartix,0, viewMartix,0);
        skyboxShaderProgram.useProgram();
        skyboxShaderProgram.setUniforms(viewProjectionMartix, skyboxTexture);
        skybox.bindData(skyboxShaderProgram);
        skybox.draw();
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
    }
}
