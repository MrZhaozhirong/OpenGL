//
//  Shader360.fsh
//  360单鱼眼像素着色器
//

precision highp float;

uniform sampler2D SamplerY;
uniform sampler2D SamplerU;
uniform sampler2D SamplerV;

varying highp vec2 v_textureCoordinate;

uniform mat3 colorConversionMatrix;


vec3 yuv2rgb(vec2 textureCoordinate)
{
    vec3 yuv;
    yuv.x = texture2D(SamplerY, textureCoordinate).r;
    yuv.y = texture2D(SamplerU, textureCoordinate).r - 0.5;
    yuv.z = texture2D(SamplerV, textureCoordinate).r - 0.5;
    
    return colorConversionMatrix * yuv;
}

void main() {
    highp vec3 rgb = yuv2rgb(v_textureCoordinate);

    gl_FragColor = vec4(rgb, 1);
}
