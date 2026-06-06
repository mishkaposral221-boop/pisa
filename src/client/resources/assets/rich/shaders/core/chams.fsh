#version 150

// Chams fragment shader - full-brightness texture, no lightmap.
// Samples the entity/armor texture, discards transparent pixels,
// applies hit-flash overlay, outputs at full brightness.

uniform sampler2D Sampler0;  // entity / armor texture

in vec4 overlayColor;
in vec2 texCoord0;
in float vertexAlpha;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    // Discard transparent pixels (cutout behavior)
    if (color.a < 0.1) {
        discard;
    }

    // Apply hit-flash overlay (red when damaged, white when invincible, etc.)
    color.rgb = mix(color.rgb, overlayColor.rgb, overlayColor.a);

    // Apply entity-level alpha (for semi-transparent entities) but keep full RGB brightness.
    // We intentionally skip lightmap multiplication here - that is the fix.
    color.a *= vertexAlpha;

    fragColor = color;
}
