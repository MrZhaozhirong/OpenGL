//
//  Shader360.vsh
//  360单鱼眼顶点着色器
//
attribute vec4 position;
attribute vec2 texCoord;

varying vec2 v_textureCoordinate;

uniform mat4 modelViewProjectionMatrix;

void main() {
    v_textureCoordinate = texCoord;
    gl_Position = modelViewProjectionMatrix * position;
}
