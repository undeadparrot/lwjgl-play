#version 140

uniform mat4 projectionMatrix;
in vec3 position;
in vec3 normal;
in vec2 uv;

out vec3 oNormal;
out float intensity;
out vec2 oUv;

void main()
{
    oNormal = normal;
    oUv = uv;
    vec3 lightDir = vec3(0,1,0);
    intensity = clamp(dot(oNormal,lightDir),0,1);
    gl_Position = projectionMatrix*vec4(position, 1.0);
}
