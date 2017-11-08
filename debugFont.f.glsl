#version 140

in vec2 oUv;
out vec4 fragColor;
uniform sampler2D texImage;

void main(void) {
    float intensity = texture(texImage,oUv).a;
    float solid = smoothstep(0.35,0.5,intensity);
    float border = smoothstep(0.30,0.35,intensity)-solid;
	fragColor = vec4(
        border,
        border,
        1.0,
        border
    );
}
