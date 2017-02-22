
uniform mat4 u_Matrix;
attribute vec3 a_Position;

varying vec3 v_Position;

void main()
{
    v_Position = a_Position;
    v_Position.z = -v_Position.z;  // 天空盒期望的是左手空间

    gl_Position = u_Matrix * vec4(a_Position, 1.0);
    gl_Position = gl_Position.xyww;
    // 确保天空盒的每一部分都将位于归一化坐标上，用w分量填充z分量，这样透视剔除法z/w=1，z最终就在值为1的远平面上了。
}
