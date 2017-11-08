#version 140

in vec3 oNormal;
in vec2 oUv;
in mat4 oProjectionMatrix;
in vec4 oVertexInLightViewspace;
in float intensity;
out vec4 fragColor;
uniform sampler2D texImage;
uniform sampler2D shadowImage;
uniform mat4 projectionMatrix;
float ShadowCalculation(vec4 fragPosLightSpace)
{
    // perform perspective divide
    vec3 projCoords = fragPosLightSpace.xyz ;
    // transform to [0,1] range
    projCoords = projCoords * 0.5 + 0.5;
    // get closest depth value from light's perspective (using [0,1] range fragPosLight as coords)
    float closestDepth = texture(shadowImage, projCoords.xy).r;
    // get depth of current fragment from light's perspective
    float currentDepth = projCoords.z;
    // check whether current frag pos is in shadow
    float bias = 0.001;
    float shadow = currentDepth-bias < closestDepth  ? 1.0 : 0.0;

    return shadow;
}
void main(void) {
    vec3 lightDir = vec3(0,0,-1);
    float intensity = clamp(dot(oNormal,lightDir),0.2,1);
    vec4 colour = texture(texImage,oUv);
    float shadow = ShadowCalculation(oVertexInLightViewspace);
    fragColor = vec4(colour.rgb*intensity*shadow,1.0);
}
