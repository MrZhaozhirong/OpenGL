//
//  Shader180.fsh
//  180单鱼眼像素着色器
//

precision highp float;

uniform sampler2D SamplerY;
uniform sampler2D SamplerU;
uniform sampler2D SamplerV;

varying highp vec2 v_textureCoordinate;

uniform mat3 colorConversionMatrix;


uniform float width;
uniform float height;
uniform float rectX;
uniform float rectY;
uniform float rectWidth;
uniform float rectHeight;


vec3 yuv2rgb(vec2 textureCoordinate)
{
    vec3 yuv;
    yuv.x = texture2D(SamplerY, textureCoordinate).r;
    yuv.y = texture2D(SamplerU, textureCoordinate).r - 0.5;
    yuv.z = texture2D(SamplerV, textureCoordinate).r - 0.5;
    
    return colorConversionMatrix * yuv;
}

void main() {
    
    highp vec3 rgb;
    
    if (rectWidth > rectHeight) {
        float heightBegin = ((rectWidth - rectHeight) * 0.5) / rectWidth;
        float heightEnd = 1.0 - heightBegin;
        if (v_textureCoordinate.y < heightBegin || v_textureCoordinate.y > heightEnd)
        {
            rgb = vec3(0.0, 0.0, 0.0);
        }
        else
        {
            float dHeight = rectHeight * (v_textureCoordinate.y - heightBegin) / (heightEnd - heightBegin);
            float u = (rectX + rectWidth * v_textureCoordinate.x) / width;
            float v = (rectY + dHeight) / height;
            rgb = yuv2rgb(vec2(u, v));
        }
    }
    else if (rectWidth < rectHeight) {
        float widthBegin = ((rectHeight - rectWidth) * 0.5) / rectHeight;
        float widthEnd = 1.0 - widthBegin;
        if (v_textureCoordinate.x < widthBegin || v_textureCoordinate.x > widthEnd)
        {
            rgb = vec3(0.0, 0.0, 0.0);
        }
        else
        {
            float dWidth = rectWidth * (v_textureCoordinate.x - widthBegin) / (widthEnd - widthBegin);
            float u = (rectX + dWidth) / width;
            float v = (rectY + rectHeight * v_textureCoordinate.y) / height;
            rgb = yuv2rgb(vec2(u, v));
        }
    }
    else {
        float u = (rectX + rectWidth * v_textureCoordinate.x) / width;
        float v = (rectY + rectHeight * v_textureCoordinate.y) / height;
        rgb = yuv2rgb(vec2(u, v));
    }

    gl_FragColor = vec4(rgb, 1.0);
}
