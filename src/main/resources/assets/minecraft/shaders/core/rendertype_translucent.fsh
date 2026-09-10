#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D ImageDepth;
uniform sampler2D WhiteScreen;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float FountainTime;
uniform float FountainAspect;
uniform vec4 TintColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;
in vec4 texProj0;
in vec3 vPosition;
in vec4 vColor;
in vec2 vUv;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    vec4 outColor = baseColor;

    if (abs(vColor.a - 0.996078431372549) < 0.00001) {
        float scale = 4.0;
        float speed = 0.12;

        mat4 scaleMat = mat4(
        scale, 0.0, 0.0, 0.0,
        0.0, scale, 0.0, 0.0,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
        );

        mat4 scrollFront = mat4(
        1.0, 0.0, 0.0, FountainTime * speed,
        0.0, 1.0, 0.0, -FountainTime * speed,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
        );

        mat4 scrollBehind = mat4(
        1.0, 0.0, 0.0, -FountainTime * speed,
        0.0, 1.0, 0.0, FountainTime * speed,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
        );

        vec4 projFront = texProj0 * scaleMat * scrollFront;
        vec2 uvFront = projFront.xy / projFront.w;
        uvFront.x *= FountainAspect;
        vec4 flowFront = texture(ImageDepth, uvFront);

        mat4 offsetRight = mat4(
        1.0, 0.0, 0.0, 0.5,
        0.0, 1.0, 0.0, 0.0,
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
        );

        vec4 projBehind = texProj0 * scaleMat * scrollBehind * offsetRight;
        vec2 uvBehind = projBehind.xy / projBehind.w;
        uvBehind.x *= FountainAspect;
        vec4 flowBehind = texture(ImageDepth, uvBehind);

        vec3 customRgb = vec3(0.0);
        customRgb += flowBehind.rgb * flowBehind.a * 0.5;
        customRgb += flowFront.rgb * flowFront.a;
        customRgb = clamp(customRgb, 0.0, 1.0);

        vec3 dX = dFdx(vPosition);
        vec3 dY = dFdy(vPosition);
        vec3 faceNormal = abs(normalize(cross(dX, dY)));

        vec2 localUv;
        if (faceNormal.y > 0.5) {
            localUv = fract(vPosition.xz);
        } else if (faceNormal.x > 0.5) {
            localUv = fract(vPosition.yz);
        } else {
            localUv = fract(vPosition.xy);
        }

        float mask = texture(WhiteScreen, localUv).a;
        mask = pow(mask, 2.0);
        mask = smoothstep(0.1, 0.9, mask);

        vec3 finalColor = customRgb * TintColor.rgb;
        float finalAlpha = vColor.a * TintColor.a;

        outColor = vec4(finalColor, finalAlpha);
    }

    fragColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
}