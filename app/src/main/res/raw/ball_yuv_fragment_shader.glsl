precision mediump float;
varying vec4 vPosition;
uniform sampler2D SamplerY;
uniform sampler2D SamplerU;
uniform sampler2D SamplerV;

varying vec2 v_TextureCoordinates;

const mediump mat3 yuv2rgb = mat3(1, 1, 1,
                                  0, -0.39465, 2.03211,
                                  1.13983, -0.58060, 0);

void main()
{
    vec3 yuv;
    vec3 rgb;

    yuv.x = texture2D(SamplerY, v_TextureCoordinates).r;
    yuv.y = texture2D(SamplerU, v_TextureCoordinates).r - 0.5;
    yuv.z = texture2D(SamplerV, v_TextureCoordinates).r - 0.5;

    rgb = yuv2rgb * yuv;

    gl_FragColor = vec4(rgb, 1);
}