#version 150

layout(std140) uniform WheelSegData {
    vec4 screen;      // x=screenW, y=screenH, z=guiScale, w=unused
    vec4 segment;     // x=cx, y=cy, z=innerR, w=outerR
    vec4 angles;      // x=startAngle(rad), y=endAngle(rad), z=unused, w=unused
    vec4 color;       // rgba
};

void main() {
    vec2 positions[6] = vec2[](
        vec2(0.0, 0.0),
        vec2(1.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 0.0),
        vec2(1.0, 1.0),
        vec2(0.0, 1.0)
    );

    vec2 pos = positions[gl_VertexID];
    vec2 ndcPos = pos * 2.0 - 1.0;
    ndcPos.y = -ndcPos.y;
    gl_Position = vec4(ndcPos, 0.0, 1.0);
}
