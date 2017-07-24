//
//  Created by  on 11/8/15.
//  Copyright © 2015 Hanton. All rights reserved.
//

precision highp float;

uniform sampler2D SamplerY;
uniform sampler2D SamplerU;
uniform sampler2D SamplerV;

uniform mat3 colorConversionMatrix;

uniform int   imageMode;
uniform float percent;
uniform float factorA11;
uniform float factorB11;
uniform float factorA12;
uniform float factorB12;
uniform float factorA21;
uniform float factorB21;
uniform float factorA22;
uniform float factorB22;

#define  pi 3.14159265358979323846

varying highp vec2 v_textureCoordinate;
varying highp vec2 v_textureFuse;

vec3 yuv2rgb(vec2 textureCoordinate)
{
    vec3 yuv;
    yuv.x = texture2D(SamplerY, textureCoordinate).r;
    yuv.y = texture2D(SamplerU, textureCoordinate).r - 0.5;
    yuv.z = texture2D(SamplerV, textureCoordinate).r - 0.5;

    return colorConversionMatrix * yuv;
}

vec2 getCoor(vec2 pos, float dr)
{
    float r = 0.5 + dr;
    float f = r * 2.0 / pi;
    float rf = 1.0 / f;

    float row = pos.y;
    float col = pos.x;
    float colTmp = 2.0 * r - row;
    float rowTmp = col;

    float sita = colTmp * rf;
    float fai = rowTmp * rf;
    float sinSita = sin(sita);
    float xT = sinSita * cos(fai);
    float zT = sinSita * sin(fai);

    float theta = acos(zT);
    float tmp = xT / sin(theta);
    if (tmp > 1.0)
    {
        tmp = 1.0;
    }
    if (tmp < -1.0)
    {
        tmp = -1.0;
    }
    float fiAngle = acos(tmp);

    float v = 0.0;
    float u = 0.0;
    if (!(theta < 0.000000000001 && theta > -0.000000000001))
    {
        u = f * theta * sin(fiAngle);
        v = f * theta * tmp;
        u = (colTmp > r) ? -u : u;
    }

    vec2 coor;
    coor.x = r - v;
    coor.y = r + u;
    return coor;
}

vec3 process1(vec2 pos, float dr)
{
    float dr_v = 2.0 * dr;
    vec2 pos_v = pos;
    pos_v.x = dr_v + 1.0 + 2.0 * pos.x;
    pos_v.y += dr_v;

    vec2 coor = getCoor(pos_v, dr_v);
    vec2 coor_v;
    if (imageMode == 1) {
        coor_v.x = factorA21 * coor.y / (0.5 + dr_v) + factorB21;
        coor_v.y = factorA22 * coor.x / (0.5 + dr_v) + factorB22;
    } else {
        coor_v.x = factorA21 * coor.x / (0.5 + dr_v) + factorB21;
        coor_v.y = factorA22 * coor.y / (0.5 + dr_v) + factorB22;
    }
    vec3 rightRgb = yuv2rgb(v_textureCoordinate);
    vec3 leftRgb = yuv2rgb(coor_v);
    return 0.5 * (leftRgb * (dr - pos.x) + rightRgb * (dr + pos.x)) / dr;

}

vec3 process2(vec2 pos, float dr)
{
    float dr_v = 2.0 * dr;
    vec2 pos_v = pos;
    pos_v.x = dr_v + 2.0 * pos.x - 1.0;
    pos_v.y += dr_v;

    vec2 coor = getCoor(pos_v, dr_v);
    vec2 coor_v;
    if (imageMode == 1) {
        coor_v.x = factorA21 * coor.y / (0.5 + dr_v) + factorB21;
        coor_v.y = factorA22 * coor.x / (0.5 + dr_v) + factorB22;
    } else {
        coor_v.x = factorA21 * coor.x / (0.5 + dr_v) + factorB21;
        coor_v.y = factorA22 * coor.y / (0.5 + dr_v) + factorB22;
    }
    vec3 leftRgb = yuv2rgb(v_textureCoordinate);
    vec3 rightRgb = yuv2rgb(coor_v);
    return 0.5 * (leftRgb * (dr - pos.x + 0.5) + rightRgb * (dr + pos.x - 0.5)) / dr;
}

vec3 process3(vec2 pos, float dr)
{
    float dr_v = 2.0 * dr;
    vec2 pos_v = pos;
    pos_v.x = dr_v + 2.0 * pos.x;
    pos_v.y += dr_v;

    vec2 coor = getCoor(pos_v, dr_v);
    vec2 coor_v;
    if (imageMode == 1) {
        coor_v.x = factorA11 * coor.y / (0.5 + dr_v) + factorB11;
        coor_v.y = factorA12 * coor.x / (0.5 + dr_v) + factorB12;
    } else {
        coor_v.x = factorA11 * coor.x / (0.5 + dr_v) + factorB11;
        coor_v.y = factorA12 * coor.y / (0.5 + dr_v) + factorB12;
    }
    vec3 rightRgb = yuv2rgb(v_textureCoordinate);
    vec3 leftRgb = yuv2rgb(coor_v);
    return 0.5 * (leftRgb * (dr - pos.x + 0.5) + rightRgb * (dr + pos.x - 0.5)) / dr;
}

vec3 process4(vec2 pos, float dr)
{
    float dr_v = 2.0 * dr;
    vec2 pos_v = pos;
    pos_v.x = 2.0 * (dr + pos.x - 1.0);
    pos_v.y += dr_v;

    vec2 coor = getCoor(pos_v, dr_v);
    vec2 coor_v;
    if (imageMode == 1) {
        coor_v.x = factorA11 * coor.y / (0.5 + dr_v) + factorB11;
        coor_v.y = factorA12 * coor.x / (0.5 + dr_v) + factorB12;
    } else {
        coor_v.x = factorA11 * coor.x / (0.5 + dr_v) + factorB11;
        coor_v.y = factorA12 * coor.y / (0.5 + dr_v) + factorB12;
    }

    vec3 leftRgb = yuv2rgb(v_textureCoordinate);
    vec3 rightRgb = yuv2rgb(coor_v);
    return 0.5 * (leftRgb * (dr - pos.x + 1.0) + rightRgb * (dr + pos.x - 1.0)) / dr;
}

void main() {
    highp vec3 yuv;

    highp vec3 rgb;

    float x1 = 0.25 * percent / (1.0 - percent);
    float x2 = 0.5 - x1;
    float x3 = 0.5 + x1;
    float x4 = 1.0 - x1;


    if (v_textureFuse.x < x1) {
        rgb = process1(v_textureFuse, x1);
    } else if (v_textureFuse.x > x2 && v_textureFuse.x < 0.5) {
        rgb = process2(v_textureFuse, x1);
    } else if (v_textureFuse.x >= 0.5 && v_textureFuse.x < x3) {
        rgb = process3(v_textureFuse, x1);
    } else if (v_textureFuse.x > x4) {
        rgb = process4(v_textureFuse, x1);
    } else {
        rgb = yuv2rgb(v_textureCoordinate);
    }

    gl_FragColor = vec4(rgb, 1);
}
