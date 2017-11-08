#version 140

uniform mat4 projectionMatrix;
in vec3 position;
in vec2 uv;
out vec2 oUv;

void main()
{
    gl_Position = projectionMatrix*vec4(position, 1.0);
    oUv = uv;
}
