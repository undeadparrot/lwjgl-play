#version 140

out vec4 fragColor;
uniform sampler2D texImage;

void main(void) {
    gl_FragDepth = gl_FragCoord.z;
}
