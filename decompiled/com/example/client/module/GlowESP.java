/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ferra13671.cometrenderer.CometRenderer
 *  com.ferra13671.cometrenderer.glsl.GlProgram
 *  com.ferra13671.cometrenderer.minecraft.CRM
 *  com.ferra13671.cometrenderer.minecraft.CustomDrawMode
 *  com.ferra13671.cometrenderer.minecraft.CustomVertexElementTypes
 *  com.ferra13671.cometrenderer.minecraft.CustomVertexFormats
 *  com.ferra13671.cometrenderer.minecraft.RenderColor
 *  com.ferra13671.cometrenderer.vertex.DrawMode
 *  com.ferra13671.cometrenderer.vertex.format.VertexFormat
 *  com.ferra13671.cometrenderer.vertex.mesh.IMesh
 *  com.ferra13671.cometrenderer.vertex.mesh.Mesh
 *  com.ferra13671.cometrenderer.vertex.mesh.MeshBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL14
 *  org.lwjgl.opengl.GL20
 */
package com.example.client.module;

import com.example.client.menu.data.ModModule;
import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.glsl.GlProgram;
import com.ferra13671.cometrenderer.minecraft.CRM;
import com.ferra13671.cometrenderer.minecraft.CustomDrawMode;
import com.ferra13671.cometrenderer.minecraft.CustomVertexElementTypes;
import com.ferra13671.cometrenderer.minecraft.CustomVertexFormats;
import com.ferra13671.cometrenderer.minecraft.RenderColor;
import com.ferra13671.cometrenderer.vertex.DrawMode;
import com.ferra13671.cometrenderer.vertex.format.VertexFormat;
import com.ferra13671.cometrenderer.vertex.mesh.IMesh;
import com.ferra13671.cometrenderer.vertex.mesh.Mesh;
import com.ferra13671.cometrenderer.vertex.mesh.MeshBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

@Environment(value=EnvType.CLIENT)
public class GlowESP {
    private ModModule moduleRef;
    private static GlowESP INSTANCE;

    public GlowESP() {
        INSTANCE = this;
    }

    public static GlowESP getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public boolean shouldRender() {
        return this.isEnabled();
    }

    public boolean showPlayers() {
        if (this.moduleRef == null || this.moduleRef.settings.length < 1) {
            return true;
        }
        return this.moduleRef.settings[0].boolValue;
    }

    public boolean showMobs() {
        if (this.moduleRef == null || this.moduleRef.settings.length < 2) {
            return false;
        }
        return this.moduleRef.settings[1].boolValue;
    }

    public boolean showInvisibles() {
        if (this.moduleRef == null || this.moduleRef.settings.length < 3) {
            return true;
        }
        return this.moduleRef.settings[2].boolValue;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderWorld() {
        block20: {
            class_310 client = class_310.method_1551();
            if (client.field_1687 == null || client.field_1724 == null || !this.shouldRender()) {
                return;
            }
            class_243 camPos = client.field_1773.method_19418().method_71156();
            int prevProgram = GL11.glGetInteger((int)35725);
            boolean prevBlend = GL11.glIsEnabled((int)3042);
            boolean prevDepth = GL11.glIsEnabled((int)2929);
            try {
                CometRenderer.setCurrentProgram((GlProgram)CRM.getPrograms().POSITION_COLOR);
                CometRenderer.applyShaderColorUniform();
                CRM.applyMatrixUniform();
                GL11.glEnable((int)3042);
                GL14.glBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
                GL11.glDisable((int)2929);
                GL11.glDepthMask((boolean)false);
                GL11.glLineWidth((float)1.5f);
                MeshBuilder outlineBuilder = Mesh.builder((DrawMode)CustomDrawMode.CUBE_OUTLINE, (VertexFormat)CustomVertexFormats.POSITION_COLOR);
                MeshBuilder fillBuilder = Mesh.builder((DrawMode)CustomDrawMode.CUBE, (VertexFormat)CustomVertexFormats.POSITION_COLOR);
                boolean hasGeometry = false;
                for (class_1297 entity : client.field_1687.method_18112()) {
                    RenderColor fillBot;
                    RenderColor fillTop;
                    RenderColor outlineBot;
                    RenderColor outlineTop;
                    double dist;
                    class_1309 living;
                    if (entity == client.field_1724 || entity.method_7325() || !(entity instanceof class_1309) || (living = (class_1309)entity).method_5767() && !this.showInvisibles() || (!(entity instanceof class_1657) ? !this.showMobs() : !this.showPlayers()) || (dist = client.field_1724.method_5858(entity)) > 4096.0) continue;
                    float partialTick = client.method_61966().method_60637(false);
                    class_238 box = entity.method_5829();
                    class_243 entityPos = entity.method_30950(partialTick);
                    float minX = (float)(entityPos.field_1352 + (box.field_1323 - entity.method_23317()) - camPos.field_1352);
                    float minY = (float)(entityPos.field_1351 + (box.field_1322 - entity.method_23318()) - camPos.field_1351);
                    float minZ = (float)(entityPos.field_1350 + (box.field_1321 - entity.method_23321()) - camPos.field_1350);
                    float maxX = (float)(entityPos.field_1352 + (box.field_1320 - entity.method_23317()) - camPos.field_1352);
                    float maxY = (float)(entityPos.field_1351 + (box.field_1325 - entity.method_23318()) - camPos.field_1351);
                    float maxZ = (float)(entityPos.field_1350 + (box.field_1324 - entity.method_23321()) - camPos.field_1350);
                    float pulse = (float)(0.85 + 0.15 * Math.sin((double)System.currentTimeMillis() / 400.0));
                    float distFade = Math.max(0.3f, 1.0f - (float)(Math.sqrt(dist) / 64.0));
                    float alphaM = pulse * distFade;
                    float hpPct = living.method_6032() / living.method_6063();
                    if (living.field_6235 > 0) {
                        outlineTop = RenderColor.of((int)255, (int)130, (int)130, (int)((int)(200.0f * alphaM)));
                        outlineBot = RenderColor.of((int)255, (int)50, (int)50, (int)((int)(180.0f * alphaM)));
                        fillTop = RenderColor.of((int)255, (int)40, (int)40, (int)((int)(22.0f * alphaM)));
                        fillBot = RenderColor.of((int)200, (int)20, (int)20, (int)((int)(14.0f * alphaM)));
                    } else if (entity instanceof class_1657) {
                        outlineTop = RenderColor.of((int)200, (int)180, (int)255, (int)((int)(200.0f * alphaM)));
                        outlineBot = RenderColor.of((int)120, (int)60, (int)255, (int)((int)(170.0f * alphaM)));
                        fillTop = RenderColor.of((int)120, (int)40, (int)255, (int)((int)(18.0f * alphaM)));
                        fillBot = RenderColor.of((int)60, (int)15, (int)150, (int)((int)(12.0f * alphaM)));
                    } else {
                        outlineTop = RenderColor.of((int)100, (int)255, (int)200, (int)((int)(180.0f * alphaM)));
                        outlineBot = RenderColor.of((int)40, (int)200, (int)150, (int)((int)(150.0f * alphaM)));
                        fillTop = RenderColor.of((int)40, (int)200, (int)150, (int)((int)(14.0f * alphaM)));
                        fillBot = RenderColor.of((int)20, (int)120, (int)80, (int)((int)(10.0f * alphaM)));
                    }
                    this.addBoxVerticesGradient(outlineBuilder, minX, minY, minZ, maxX, maxY, maxZ, outlineBot, outlineTop);
                    this.addBoxVerticesGradient(fillBuilder, minX, minY, minZ, maxX, maxY, maxZ, fillBot, fillTop);
                    hasGeometry = true;
                }
                if (!hasGeometry) break block20;
                Mesh fill = fillBuilder.buildNullable();
                Mesh outline = outlineBuilder.buildNullable();
                try {
                    if (fill != null) {
                        CometRenderer.draw((IMesh)fill);
                    }
                    if (outline != null) {
                        CometRenderer.draw((IMesh)outline);
                    }
                }
                finally {
                    if (fill != null) {
                        fill.close();
                    }
                    if (outline != null) {
                        outline.close();
                    }
                }
            }
            finally {
                GL11.glDepthMask((boolean)true);
                if (prevDepth) {
                    GL11.glEnable((int)2929);
                } else {
                    GL11.glDisable((int)2929);
                }
                if (prevBlend) {
                    GL11.glEnable((int)3042);
                } else {
                    GL11.glDisable((int)3042);
                }
                GL11.glLineWidth((float)1.0f);
                GL20.glUseProgram((int)prevProgram);
            }
        }
    }

    private void addBoxVerticesGradient(MeshBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2, RenderColor bottomColor, RenderColor topColor) {
        builder.vertex(x2, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{bottomColor});
        builder.vertex(x2, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{bottomColor});
        builder.vertex(x1, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{bottomColor});
        builder.vertex(x1, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{bottomColor});
        builder.vertex(x1, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{topColor});
        builder.vertex(x1, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{topColor});
        builder.vertex(x2, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{topColor});
        builder.vertex(x2, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, (Object[])new RenderColor[]{topColor});
    }
}

