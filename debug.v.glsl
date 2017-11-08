#version 140

uniform mat4 projectionMatrix;
in vec3 position;
in float colour;
out float fColour;

void main()
{
    gl_Position = projectionMatrix*vec4(position, 1.0);
    fColour = colour;
}
