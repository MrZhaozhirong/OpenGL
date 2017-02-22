
uniform mat4 u_Matrix;  // view projection matrix
uniform float u_Time;   // current time

attribute vec3 a_Position;      //[in]
attribute vec3 a_Color;         //[in]
attribute vec3 a_DirectionVector;//[in]
attribute float a_ParticleStartTime;//[in]
//uniform float a_ParticleStartTime;//[in] 从开始到粒子被创建的时间间隔

varying vec3 v_Color;   //[out]
varying float v_ElapsedTime;//[out]

void main()
{
    v_Color = a_Color;
    v_ElapsedTime = u_Time - a_ParticleStartTime;
    float gravityFactor = v_ElapsedTime*v_ElapsedTime / 8.0;
    vec3 currentPosition = a_Position + (a_DirectionVector * v_ElapsedTime);
    currentPosition.y -= gravityFactor;
    // 探讨uniform 和 attribute的区别
    //vec3 currentPosition = a_Position + (a_DirectionVector * u_Time);
    //vec3 currentPosition = a_Position + (a_DirectionVector * a_ParticleStartTime);
    gl_Position = u_Matrix * vec4(currentPosition, 1.0);
    gl_PointSize = 10.0;
}
