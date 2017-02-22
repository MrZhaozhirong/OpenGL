package com.pixel.opengl.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;


/**
 * Created by ZZR on 2017/2/10.
 */

public class TextureHelper {

    private static final String TAG = "TextureHelper";

    public static int loadCubMap(Context context, int[] cubeResources){
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        if(textureObjectIds[0] == 0){
            if (LoggerConfig.ON){
                Log.w(TAG,"Could not generate a new OpenGL texture object!");
            }
            return 0;
        }
        int length = cubeResources.length;
        if(length < 6){
            if (LoggerConfig.ON){
                Log.w(TAG,"Not enough targets in cubeResource, at least 6");
            }
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap[] cubeBitmaps = new Bitmap[length];
        for(int i=0; i<length; i++){
            cubeBitmaps[i] = BitmapFactory.decodeResource(context.getResources(),
                    cubeResources[i], options);
            if(cubeBitmaps[i] == null){
                if(LoggerConfig.ON){
                    Log.w(TAG, "Resource ID "+cubeResources[i] + "could not be decode");
                }
                GLES20.glDeleteTextures(1, textureObjectIds, 0);
                return 0;
            }
        }
        //告诉OpenGL后面纹理操作是应用于哪个纹理对象
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureObjectIds[0]);
        //设置缩小的时候（GL_TEXTURE_MIN_FILTER）使用双线程过滤，而非使用mipmap三线程过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        //设置放大的时候（GL_TEXTURE_MAG_FILTER）使用双线程过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //左右
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0);
        //下上
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0);
        //前后
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        for(Bitmap bitmap : cubeBitmaps){
            bitmap.recycle();
        }

        return textureObjectIds[0];
    }


    /**
     * 从原生文件加载纹理图片
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadTexture(Context context, int resourceId){
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);

        if(textureObjectIds[0] == 0){
            if (LoggerConfig.ON){
                Log.w(TAG,"Could not generate a new OpenGL texture object!");
            }
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   //指定需要的是原始数据，非压缩数据
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if(bitmap == null){
            if(LoggerConfig.ON){
                Log.w(TAG, "Resource ID "+resourceId + "could not be decode");
            }
            GLES20.glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }

        //告诉OpenGL后面纹理调用应该是应用于哪个纹理对象
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);

        //设置缩小的时候（GL_TEXTURE_MIN_FILTER）使用mipmap三线程过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_TEXTURE_MIN_FILTER);
        //设置放大的时候（GL_TEXTURE_MAG_FILTER）使用双线程过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();

        //快速生成mipmap贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        //解除纹理操作的绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }
}
