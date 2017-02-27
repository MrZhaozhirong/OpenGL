
uniform mat4 u_MVMatrix;        //模型视图矩阵
uniform mat4 u_IT_MVMatrix;     //倒置矩阵的转置
uniform mat4 u_MVPMatrix;       //模型视图透视矩阵

//方向光
uniform vec3 u_VectorToLight;           //In eye space
uniform vec4 u_PointLightPositions[3];   //In eye space  总共三个点光源
uniform vec3 u_PointLightColors[3];
//用于存储位置指向方向的法向量，归一化
attribute vec3 a_Normal;

attribute vec4 a_Position;

varying vec3 v_Color;

vec3 materialColor;
vec4 eyeSpacePosition;
vec3 eyeSpaceNormal;

vec3 getAmbientLighting();      //环境光
vec3 getDirectionalLighting();  //方向光
vec3 getPointLighting();        //点光源

void main()
{
    materialColor = mix(vec3(0.180, 0.467, 0.153),    // A dark green
                        vec3(0.660, 0.670, 0.680),    // A stony gray
                        a_Position.y);


    eyeSpacePosition = u_MVMatrix * a_Position;

    eyeSpaceNormal = normailze(vec3(u_IT_MVMatrix * vec4(a_Normal,0.0)));

    v_Color = getAmbientLighting();
    v_Color += getDirectionalLighting();
    v_Color += getPointLighting();

    gl_Position = u_MVPMatrix * a_Position;
}

vec3 getAmbientLighting()
{
    return materialColor * 0.1;
}

vec3 getDirectionalLighting()
{
    return materialColor * 0.3
             * max(dot(eyeSpaceNormal, u_VectorToLight), 0.0);       
}

vec3 getPointLighting()
{
    vec3 lightingSum = vec3(0.0);

    for(int i=0; i<3; i++)
    {
        vec3 toPointLight = vec3(u_PointLightPositions[i]) - vec3(eyeSpacePosition);
        //先计算当前eyeSpacePosition到光源的向量
        float distance = length(toPointLight);
        //计算当前位置与光源的距离
        toPointLight = normalize(toPointLight);
        //归一化

        float cosine = max(dot(eyeSpaceNormal, toPointLight), 0.0);
        //求出点光与位置法向量的夹角cos值
        lightingSum += (materialColor * u_PointLightColors[i] * 10.0 * cosine) / distance;
        //朗伯体反射(*cosine)实现点光反射 *5.0扩大效果 /distance是模拟随距离而减少的效果
    }

    return lightingSum;
}