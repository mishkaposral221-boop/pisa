#version 150

// Chams vertex shader - flat-shaded entity, no lightmap multiplication.
// ROOT CAUSE of black leggings:
//   ENTITY_SNIPPET shader does: color *= vertexColor * lightMapColor
//   Inner leg geometry (HUMANOID_LEGGINGS / layer_2) has UV2=(0,0) at the
//   moment armor features enqueue draw calls, so lightMapColor = (0,0,0,1).
//   Result: texture * anything * (0,0,0,1) = black.
// FIX: Ignore lightmap entirely. Sample texture as-is, full brightness.

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;  // overlay texture
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 overlayColor;
out vec2 texCoord0;
out float vertexAlpha;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // Fetch hit-flash overlay (UV1 is in texels, not normalized)
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;
    // Pass alpha only - we do NOT apply vertex RGB (it is lightmap-darkened)
    vertexAlpha = Color.a;
}
