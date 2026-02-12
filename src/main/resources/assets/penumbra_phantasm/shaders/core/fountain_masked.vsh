#version 150

#moj_import <projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 texProj0;
out vec2 vUv;
out vec4 vColor;
void main() {
    vColor = Color;
    vec4 pos = ProjMat * ModelViewMat * vec4(Position, 1.0);
    gl_Position = pos;

    texProj0 = projection_from_position(pos);
    vUv = UV0;
}
