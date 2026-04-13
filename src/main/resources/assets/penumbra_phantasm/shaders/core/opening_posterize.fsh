#version 150

uniform sampler2D Sampler0;
uniform float Strength;
uniform float WhiteLevel;
uniform float Threshold;
uniform float AvgMip;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 base = texture(Sampler0, texCoord0);
    vec3 rgb = clamp(base.rgb, 0.0, 1.0);
    float l = dot(rgb, vec3(0.2126, 0.7152, 0.0722));
    float avgL = dot(textureLod(Sampler0, vec2(0.5), AvgMip).rgb, vec3(0.2126, 0.7152, 0.0722));
    avgL = max(avgL, 0.02);
    float thr = clamp(Threshold * avgL, 0.03, 0.55);
    float bw = step(thr, l);
    vec3 twoTone = vec3(WhiteLevel * bw);
    fragColor = vec4(mix(rgb, twoTone, Strength), 1.0);
}
