package com.pixel.opengl.util;


import android.util.FloatMath;
import android.util.Log;

import static android.R.attr.angle;
import static android.content.ContentValues.TAG;

/**
 * Created by ZZR on 2017/2/13.
 */

public class Geometry {

    /**
     * 平面
     */
    public static class Plane{
        public final Point point;
        /* 法向量 */
        public final Vector normalVertor;
        public Plane(Point point, Vector normalVertor){
            this.point = point;
            this.normalVertor = normalVertor;
        }
    }

    /**
     * 球体
     */
    public static class Sphere{
        public final Point center;
        public final float radius;
        public Sphere(Point center, float radius){
            this.center = center;
            this.radius = radius;
        }
    }

    /**
     * 几何射线
     */
    public static class Ray{
        public final Point point;
        public final Vector vector;
        public Ray(Point point,Vector vector){
            this.point = point;
            this.vector = vector;
        }
    }

    /**
     * 有向 向量
     */
    public static class Vector{
        public final float x,y,z;
        public Vector(float x,float y,float z){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * 向量A*向量B 交叉乘积
         * i, j, k, 分别代表是xyz轴方向的分量
         * |A*B| = (Ay*Bz-Az*By)i + (Az*Bx-Ax*Bz)j + (Ax*By-Ay*Bx)k
         * @param other vector
         */
        public Vector crossProduct(Vector other) {
            float Vx = (y * other.z) - (z * other.y);
            float Vy = (z * other.x) - (x * other.z);
            float Vz = (x * other.y) - (y * other.x);
            return new Vector(Vx,Vy,Vz);
        }

        public float length() {
            double sqrt = Math.sqrt(x * x + y * y + z * z);
            return (float) sqrt;
        }

        public float dotProduct(Vector other) {
            return x*other.x + y*other.y + z*other.z;
        }

        public Vector scale(float scale) {
            return new Vector(x*scale,y*scale,z*scale);
        }
    }

    /**
     * 中心点
     */
    public static class Point{
        public final float x,y,z;
        public Point(float x, float y, float z ){
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point translateY(float distance){
            return new Point(x, y+distance, z);
        }

        public Point translate(Vector vector) {
            return new Point(
                    x + vector.x,
                    y + vector.y,
                    z + vector.z);
        }
    }

    /**
     * 圆形 = 中心点+半径
     */
    public static class Circle{
        public final Point center;
        public final float radius;

        public Circle(Point center, float radius){
            this.center = center;
            this.radius = radius;
        }

        public Circle scale(float scale){
            return new Circle(center, radius * scale);
        }
    }


    /**
     * 圆柱
     */
    public static class Cylinder{
        public final Point center;
        public final float radius;
        public final float height;

        public Cylinder(Point center, float radius, float height){
            this.center = center;
            this.radius = radius;
            this.height = height;
        }
    }
    // ///////////////////////////////////////////
    // ///////////////////////////////////////////
    // 相关的几何计算方法
    public static Vector vectorBetween(Point from, Point to){
        return new Vector(
                to.x - from.x,
                to.y - from.y,
                to.z - from.z);
    }

    /**
     * 圆、球体 碰撞模型
     * @param active 主动去撞的
     * @param passive 被撞的
     * @return
     */
    public static Vector vectorCollisionAngle(Point active, Point passive, Vector activeVector){
        Vector normalVector = vectorBetween(passive, active);
        float dotProduct = activeVector.dotProduct(normalVector);
        float mathcos = dotProduct / (normalVector.length() * activeVector.length());
        double angle = Math.acos(mathcos) / ((float)Math.PI * 2f) *360 ;
        double v = dotProduct / Math.cos(angle) / activeVector.length();

        if(LoggerConfig.ON){
            Log.w(TAG, "angle : "+angle);
            Log.w(TAG, "v : "+v);
        }

        float x = activeVector.x;
        float z = activeVector.z;
        if(angle < 45){
            z = -z;
        }else if(angle < 135){
            x = -x;
        }else if(angle < 225){
            z = -z;
        }else if(angle < 315){
            x = -x;
        }else if(angle < 360){
            z = -z;
        }
        return new Vector(x,activeVector.y,z);
    }

    /**
     * 数学模型球体与射线是否相交的判断
     * @param sphere
     * @param ray
     * @return
     */
    public static boolean intersects(Sphere sphere, Ray ray){
        if(LoggerConfig.ON){
            Log.w(TAG, "sphere.radius : "+sphere.radius);
        }
        return distanceBetween(sphere.center, ray) < sphere.radius;
    }

    private static float distanceBetween(Point point, Ray ray) {
        // 中心点 到 射线两端的两个向量
        Vector p1ToPoint = vectorBetween(ray.point, point);
        Vector p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point);
        // 求出两个向量AB的交叉乘积，得出一个新的向量垂直于AB向量的平面，大小(长度)是AB向量定义的三角形的两倍
        Vector vectorP1crossP2 = p1ToPoint.crossProduct(p2ToPoint);
        // 即中心点到射线距离=2*area/射线长度大小=vectorP1crossP2.length / ray.length
        float distanceFromPointToRay = vectorP1crossP2.length() / ray.vector.length();
        if(LoggerConfig.ON){
            Log.w(TAG, "distanceFromPointToRay : "+distanceFromPointToRay);
        }
        return distanceFromPointToRay;
    }


    /**
     * 数学模型平面与射线是否相交的判断
     * @param ray
     * @param plane
     * @return
     */
    public static Point intersectionPoint(Ray ray, Plane plane){
        // 射线到平面的平行向量
        Vector rayToPlaneVector = vectorBetween(ray.point, plane.point);
        // 求出
        float scale = rayToPlaneVector.dotProduct(plane.normalVertor)
                / ray.vector.dotProduct(plane.normalVertor);
        Point intersectionPoint = ray.point.translate(ray.vector.scale(scale));
        return intersectionPoint;
    }
}
