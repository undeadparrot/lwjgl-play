#version 140

in float fColour;
out vec4 fragColor;

void main(void) {
    int x = int(fColour);
	fragColor = vec4(
        float(x>>16&0xFF)/255.0,
        float(x>>8&0xFF)/255.0,
        float(x&0xFF)/255.0,
        1.0
    );
}
