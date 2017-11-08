#version 140

uniform mat4 projectionMatrix;
uniform mat4 lightViewProjMatrix;
in vec3 position;
in vec3 normal;
in vec2 uv;

out vec3 oNormal;
out float intensity;
out vec2 oUv;
out mat4 oProjectionMatrix;
out vec4 oVertexInLightViewspace;

void main()
{
    oNormal = normal;
    oUv = uv;
    oProjectionMatrix = projectionMatrix;
    vec3 lightDir = vec3(0,1,0);
    intensity = clamp(dot(oNormal,lightDir),0,1);
    oVertexInLightViewspace = lightViewProjMatrix*vec4(position, 1.0);
    gl_Position = projectionMatrix*vec4(position, 1.0);
}
