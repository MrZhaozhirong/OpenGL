//
//  Created by  on 11/8/15.
//  Copyright © 2015 Hanton. All rights reserved.
//


attribute vec4 position;
attribute vec2 texCoord;
attribute vec2 texFuse;
uniform mat4 modelViewProjectionMatrix;

varying vec2 v_textureCoordinate;
varying vec2 v_textureFuse;


void main() {
    v_textureCoordinate = texCoord;
    v_textureFuse = texFuse;
    gl_Position = modelViewProjectionMatrix * position;
}
