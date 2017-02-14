package com.pixel.opengl.util;

import static android.R.attr.y;

/**
 * Created by ZZR on 2017/2/13.
 */

public class Geometry {


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

}
