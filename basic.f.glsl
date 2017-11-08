#version 140

in vec3 oNormal;
in vec2 oUv;
in float intensity;
out vec4 fragColor;
uniform sampler2D texImage;

void main(void) {
//    vec3 lightDir = vec3(0,0,-1);
//    float intensity = clamp(dot(oNormal,lightDir),0,1);
    vec4 colour = texture(texImage,oUv);
    fragColor = vec4(colour.rgb*intensity,1.0);
}
