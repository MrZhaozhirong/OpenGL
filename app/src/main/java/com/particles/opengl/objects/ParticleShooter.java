package com.particles.opengl.objects;

import com.pixel.opengl.util.Geometry;

import java.util.Random;

import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.setRotateEulerM;

/**
 * Created by ZZR on 2017/2/20.
 */

public class ParticleShooter {

    private final Geometry.Point position;
    private final Geometry.Vector direction;
    private final int color;

    private final float angleVariance;
    private final float speedVariance;
    private Random random = new Random();
    private float[] rotationMatrix = new float[16];
    private float[] directionVector = new float[4]; //
    private float[] resultVector = new float[4];

    public ParticleShooter(Geometry.Point position, Geometry.Vector direction, int color,
                           float angleVariance, float speedVariance){
        this.position = position;
        this.direction = direction;
        this.color = color;
        this.angleVariance = angleVariance;
        this.speedVariance = speedVariance;

        directionVector[0] = direction.x;
        directionVector[1] = direction.y;
        directionVector[2] = direction.z;
    }


    public void addParticles(ParticleSystem particleSystem, float curentTime, int count){
        for (int i=0; i<count; i++){
            setRotateEulerM(rotationMatrix,0,
                    (random.nextFloat()-0.5f)*angleVariance,
                    (random.nextFloat()-0.5f)*angleVariance,
                    (random.nextFloat()-0.5f)*angleVariance );

            multiplyMV(resultVector,0,
                    rotationMatrix,0, directionVector,0);

            float speedAdjustment = 1f+random.nextFloat()*speedVariance;

            Geometry.Vector thisDirectionVector = new Geometry.Vector(
                    resultVector[0] * speedAdjustment,
                    resultVector[1] * speedAdjustment,
                    resultVector[2] * speedAdjustment);

            particleSystem.addParticle(position, color, thisDirectionVector, curentTime);
        }
    }
}
