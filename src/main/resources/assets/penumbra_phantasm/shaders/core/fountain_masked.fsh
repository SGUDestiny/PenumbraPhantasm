#version 150

uniform sampler2D Sampler0; // mask
uniform sampler2D Sampler1; // texture
uniform float Time;
uniform vec4 TintColor;

in vec4 texProj0;
in vec2 vUv;
in vec4 vColor;

out vec4 fragColor;
void main() {
    // smaller number = bigger scale (ik, not very intuitive)
    float scale = 4.0;
    //speed of the moving textures
    float speed = 0.12f;

    mat4 scaleMat = mat4(
    scale, 0.0,   0.0,   0.0,
    0.0,   scale, 0.0,   0.0,
    0.0,   0.0,   1.0,   0.0,
    0.0,   0.0,   0.0,   1.0
    );



    mat4 scrollFront = mat4(
    1.0, 0.0, 0.0,  Time * speed,
    0.0, 1.0, 0.0, -Time * speed,
    0.0, 0.0, 1.0,  0.0,
    0.0, 0.0, 0.0,  1.0
    );

    mat4 scrollBehind = mat4(
    1.0, 0.0, 0.0, -Time * speed,
    0.0, 1.0, 0.0,  Time * speed,
    0.0, 0.0, 1.0,  0.0,
    0.0, 0.0, 0.0,  1.0
    );

    //flowing texture, aka moving
    vec4 flowFront = textureProj(Sampler1, texProj0 * scaleMat * scrollFront);

    // offset for the behind flowing texture
    mat4 offsetRight = mat4(
    1.0, 0.0, 0.0, 0.5,
    0.0, 1.0, 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
    );

    vec4 flowBehind = textureProj(
    Sampler1,
    texProj0 * scaleMat * scrollBehind * offsetRight
    );

   //base black background
    vec3 color = vec3(0.0);

   //behind layer that is slightly darker (0.5x)
    color += flowBehind.rgb * flowBehind.a * 0.5;

   //brighter front layer of the moving texture
    color += flowFront.rgb * flowFront.a;

    //color clamp
    color = clamp(color, 0.0, 1.0);

   // applying mask here
    float mask = texture(Sampler0, vUv).a;
    mask = pow(mask, 2.0);
    mask = smoothstep(0.1, 0.9, mask);

    if (mask < 0.01)
    discard;

    vec3 finalColor = color * vColor.rgb * TintColor.rgb;
    float finalAlpha = mask * vColor.a * TintColor.a;

    fragColor = vec4(finalColor, finalAlpha);


}
