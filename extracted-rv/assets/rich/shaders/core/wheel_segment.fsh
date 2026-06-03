#version 150

layout(std140) uniform WheelSegData {
    vec4 screen;      // x=screenW, y=screenH, z=guiScale, w=unused
    vec4 segment;     // x=cx, y=cy, z=innerR, w=outerR
    vec4 angles;      // x=startAngle(rad), y=endAngle(rad), z=unused, w=unused
    vec4 color;       // rgba
};

out vec4 fragColor;

void main() {
    // Пиксельные координаты в screen space (fixed scale)
    vec2 fragPos = gl_FragCoord.xy;
    // gl_FragCoord.y идёт снизу, переворачиваем
    fragPos.y = screen.y - fragPos.y;

    vec2 center = segment.xy;
    float innerR = segment.z;
    float outerR = segment.w;

    vec2 delta = fragPos - center;
    float dist = length(delta);

    // Проверяем что пиксель в кольце
    if (dist < innerR || dist > outerR) {
        discard;
    }

    // Угол пикселя (от -PI до PI, 0 = вправо)
    float angle = atan(delta.y, delta.x);

    float startA = angles.x;
    float endA   = angles.y;

    // Нормализуем угол в диапазон [startA, startA + 2PI]
    float a = angle;
    while (a < startA) a += 6.28318530718;
    while (a > startA + 6.28318530718) a -= 6.28318530718;

    if (a > endA) {
        discard;
    }

    // Сглаживание краёв кольца
    float guiScale = screen.z;
    float edgeSmooth = 1.0 / guiScale;

    float outerAlpha = smoothstep(outerR + edgeSmooth, outerR - edgeSmooth, dist);
    float innerAlpha = smoothstep(innerR - edgeSmooth, innerR + edgeSmooth, dist);

    float angleSmooth = edgeSmooth / max(dist, 1.0);
    float startAlpha = smoothstep(startA - angleSmooth, startA + angleSmooth, a);
    float endAlpha   = smoothstep(endA + angleSmooth, endA - angleSmooth, a);

    float alpha = outerAlpha * innerAlpha * startAlpha * endAlpha;

    if (alpha < 0.01) discard;

    fragColor = vec4(color.rgb, color.a * alpha);
}
