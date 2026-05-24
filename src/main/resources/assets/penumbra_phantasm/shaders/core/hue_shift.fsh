#version 150

uniform sampler2D Sampler0;
uniform float HueTarget;
uniform float Strength;

in vec2 texCoord0;
out vec4 fragColor;

vec3 rgb2hsl(vec3 c) {
    float h, s, l;
    float minVal = min(min(c.r, c.g), c.b);
    float maxVal = max(max(c.r, c.g), c.b);
    float delta = maxVal - minVal;
    l = (maxVal + minVal) * 0.5;
    if (delta == 0.0) {
        h = 0.0;
        s = 0.0;
    } else {
        s = delta / (1.0 - abs(2.0 * l - 1.0));
        if (maxVal == c.r) {
            h = mod(((c.g - c.b) / delta), 6.0);
        } else if (maxVal == c.g) {
            h = (c.b - c.r) / delta + 2.0;
        } else {
            h = (c.r - c.g) / delta + 4.0;
        }
        h /= 6.0;
    }
    return vec3(h, s, l);
}

float hue2rgb(float p, float q, float t) {
    if (t < 0.0) t += 1.0;
    if (t > 1.0) t -= 1.0;
    if (t < 1.0/6.0) return p + (q - p) * 6.0 * t;
    if (t < 1.0/2.0) return q;
    if (t < 2.0/3.0) return p + (q - p) * (2.0/3.0 - t) * 6.0;
    return p;
}

vec3 hsl2rgb(vec3 hsl) {
    float h = hsl.x, s = hsl.y, l = hsl.z;
    if (s == 0.0) return vec3(l);
    float q = l < 0.5 ? l * (1.0 + s) : l + s - l * s;
    float p = 2.0 * l - q;
    return vec3(hue2rgb(p, q, h + 1.0/3.0), hue2rgb(p, q, h), hue2rgb(p, q, h - 1.0/3.0));
}

void main() {
    vec4 base = texture(Sampler0, texCoord0);
    vec3 rgb = clamp(base.rgb, 0.0, 1.0);
    vec3 hsl = rgb2hsl(rgb);

    // Give greys a minimum saturation so the hue shift becomes visible.
    // The minimum saturation scales with Strength, so when Strength=0 nothing changes.
    float minSat = Strength * 0.5;
    hsl.y = max(hsl.y, minSat);

    // Shift the hue toward the target hue, mixing strength.
    hsl.x = mix(hsl.x, HueTarget, Strength);

    vec3 shifted = hsl2rgb(hsl);
    fragColor = vec4(shifted, base.a);
}