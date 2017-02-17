package com.pixel.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.pixel.opengl.objects.Mallet;
import com.pixel.opengl.objects.Puck;
import com.pixel.opengl.objects.Table;
import com.pixel.opengl.programs.ColorShaderProgram;
import com.pixel.opengl.programs.TextureShaderProgram;
import com.pixel.opengl.util.Geometry;
import com.pixel.opengl.util.LoggerConfig;
import com.pixel.opengl.util.MatrixHelper;
import com.pixel.opengl.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.content.ContentValues.TAG;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

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
    //反转的视图投影矩阵，用于二维屏幕翻转投影到视椎体内的三位坐标
    private final float[] invertedViewProjectionMatrix = new float[16];

    private Table table;
    private Mallet mallet;
    private Puck puck;

    private ColorShaderProgram colorShaderProgram;
    private TextureShaderProgram textureShaderProgram;

    private int textureId;

    public OpenGLRenderer(Context context){
        this.context = context;
    }

    //边界
    private final float leftBound = -0.5f;
    private final float rightBound= 0.5f;
    private final float farBound  = -0.8f;
    private final float nearBound = 0.8f;
    //木桩是否被按压
    private boolean malletPressed = false;
    //木桩的位置存储在这里
    private Geometry.Point blueMalletPosition;
    private Geometry.Point previousBlueMalletPosition;
    //记录冰球的位置、速度和方向
    private Geometry.Point puckPosition;
    private Geometry.Vector puckVector;



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f,0.0f,0.0f,0.0f);

        colorShaderProgram = new ColorShaderProgram(context);
        textureShaderProgram=new TextureShaderProgram(context);

        table = new Table();
        mallet= new Mallet(0.1f, 0.15f, 32);
        puck = new Puck(0.07f, 0.02f, 32);

        textureId = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface);

        blueMalletPosition = new Geometry.Point(0f, mallet.height / 2f, 0.4f);

        puckPosition = new Geometry.Point(0f, puck.height/2f, 0f);
        puckVector = new Geometry.Vector(0f, 0f, 0f);
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

        multiplyMM(viewProjectionMatrix,0, projectionMatrix,0, viewMatrix,0);
        //创建反转矩阵,用于二维点转三维坐标时候使用
        invertM(invertedViewProjectionMatrix,0, viewProjectionMatrix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GL_COLOR_BUFFER_BIT);

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

        //positionObjectInScene(0f, mallet.height / 2f, 0.4f);
        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y, blueMalletPosition.z);
        colorShaderProgram.setUniforms(modelViewProjectionMatrix, 0f,0f,1f);
        //我们不需要定义两次object的数据，只需要在不同的位置和颜色重新画一个就好
        mallet.draw();

        //positionObjectInScene(0f, puck.height / 2f, 0f);
        puckPosition = puckPosition.translate(puckVector);
        //增加边缘碰撞测试
        if(puckPosition.x < leftBound + puck.radius ||
                puckPosition.x > rightBound - puck.radius){
            puckVector = new Geometry.Vector(-puckVector.x,puckVector.y,puckVector.z);
            //模拟摩擦阻尼
            puckVector = puckVector.scale(0.9f);
        }
        if(puckPosition.z < farBound + puck.radius ||
                puckPosition.z > nearBound - puck.radius){
            puckVector = new Geometry.Vector(puckVector.x, puckVector.y,-puckVector.z);
            //模拟摩擦阻尼
            puckVector = puckVector.scale(0.9f);
        }
        //加入木桩冰球的碰撞测试。这是我自己加上去的，非书本内容
        float distance = Geometry.vectorBetween(blueMalletPosition, puckPosition).length();
        if(distance < (puck.radius + mallet.radius)){
            //puckVector = new Geometry.Vector(-puckVector.x,puckVector.y,-puckVector.z);
            puckVector = Geometry.vectorCollisionAngle(puckPosition, blueMalletPosition, puckVector);
            //模拟摩擦阻尼
            puckVector = puckVector.scale(0.9f);
        }
        puckPosition = new Geometry.Point(
                clamp(puckPosition.x, leftBound+puck.radius, rightBound-puck.radius),
                puckPosition.y,
                clamp(puckPosition.z, farBound+puck.radius, nearBound-puck.radius) );
        positionObjectInScene(puckPosition.x, puckPosition.y, puckPosition.z);
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

    public void handleTouchPress(float normalizedX, float normalizedY) {
        if(LoggerConfig.ON){
            Log.w(TAG, "GLRenderer Press normalizedX:"+normalizedX+" ## normalizedY:"+normalizedY);
        }
        // 三维世界的几何射线，出发点+有向向量
        Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
        // 现在 创建一个包裹着槌的边界球
        Geometry.Sphere sphere = new Geometry.Sphere(
                new Geometry.Point(
                        blueMalletPosition.x,
                        blueMalletPosition.y,
                        blueMalletPosition.z),
                mallet.height / 2f);
        // 测试球与射线的是否相交
        malletPressed = Geometry.intersects(sphere, ray);
        if(LoggerConfig.ON){
            Log.w(TAG, "mallect touch press:"+malletPressed);
        }
    }

    /**
     * 将被触碰的二维屏幕点转为一个三维视锥体的射线光束
     * @param normalizedX
     * @param normalizedY
     */
    private Geometry.Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
        // 要实现二维点扩展三维直线，我们需要 取消 透视投影和透视除法的效果
        // 这时候我们就需要一个反转矩阵
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};
        // 我们选取近位面和远位面的两个点，画出一条线
        // 要做这个转换，第一步我们需要乘以反转矩阵，消除透视投影的影响
        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld  = new float[4];
        // 撤销透视投影的影响
        multiplyMV(nearPointWorld,0, invertedViewProjectionMatrix,0, nearPointNdc,0);
        multiplyMV(farPointWorld,0, invertedViewProjectionMatrix,0, farPointNdc,0);
        // 撤销透视除法的影响，有趣的是，反转矩阵实际已经还有了反转的w分量，直接拿xyz/w即可消除透视除法的影响
        divideByW(nearPointWorld);
        divideByW(farPointWorld);
        // 现在已经有两个三维世界的点了。
        Geometry.Point nearPointRay = new Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
        Geometry.Point farPointRay  = new Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);
        // 返回一个三维世界的几何射线，出发点+有向向量
        return new Geometry.Ray(nearPointRay, Geometry.vectorBetween(nearPointRay, farPointRay));
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }


    public void handleTouchDrag(float normalizedX, float normalizedY) {
        if(LoggerConfig.ON){
            Log.w(TAG, "GLRenderer Drag normalizedX:"+normalizedX+" ## normalizedY:"+normalizedY);
        }
        previousBlueMalletPosition = blueMalletPosition;
        if(malletPressed){
            Geometry.Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            // Define plane respresenting table
            Geometry.Plane plane =
                    new Geometry.Plane(new Geometry.Point(0f, 0f, 0f), new Geometry.Vector(0, 1, 0));
            Geometry.Point point = Geometry.intersectionPoint(ray, plane);

//            blueMalletPosition = new Geometry.Point(point.x, mallet.height/2f, point.z);
            blueMalletPosition = new Geometry.Point(
                    clamp(point.x, leftBound+mallet.radius, rightBound-mallet.radius),
                    mallet.height / 2f,
                    clamp(point.z, /*farBound+mallet.radius*/0+mallet.radius, nearBound-mallet.radius) );

            //加入木桩冰球的碰撞测试。
            float distance = Geometry.vectorBetween(blueMalletPosition, puckPosition).length();
            if(distance < (puck.radius + mallet.radius)){
                //记录方向
                puckVector = Geometry.vectorBetween(previousBlueMalletPosition, blueMalletPosition);
            }
        }
    }

    private float clamp(float value, float min, float max){
        return Math.min(max, Math.max(value,min));
    }


}
