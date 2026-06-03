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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

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
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null || !this.shouldRender()) {
                return;
            }
            Vec3d camPos = client.gameRenderer.getCamera().getCameraPos();
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
                for (Entity entity : client.world.getEntities()) {
                    RenderColor fillBot;
                    RenderColor fillTop;
                    RenderColor outlineBot;
                    RenderColor outlineTop;
                    double dist;
                    LivingEntity living;
                    if (entity == client.player || entity.isSpectator() || !(entity instanceof LivingEntity) || (living = (LivingEntity)entity).isInvisible() && !this.showInvisibles() || (!(entity instanceof PlayerEntity) ? !this.showMobs() : !this.showPlayers()) || (dist = client.player.squaredDistanceTo(entity)) > 4096.0) continue;
                    float partialTick = client.getRenderTickCounter().getTickProgress(false);
                    Box box = entity.getBoundingBox();
                    Vec3d entityPos = entity.getLerpedPos(partialTick);
                    float minX = (float)(entityPos.x + (box.minX - entity.getX()) - camPos.x);
                    float minY = (float)(entityPos.y + (box.minY - entity.getY()) - camPos.y);
                    float minZ = (float)(entityPos.z + (box.minZ - entity.getZ()) - camPos.z);
                    float maxX = (float)(entityPos.x + (box.maxX - entity.getX()) - camPos.x);
                    float maxY = (float)(entityPos.y + (box.maxY - entity.getY()) - camPos.y);
                    float maxZ = (float)(entityPos.z + (box.maxZ - entity.getZ()) - camPos.z);
                    float pulse = (float)(0.85 + 0.15 * Math.sin((double)System.currentTimeMillis() / 400.0));
                    float distFade = Math.max(0.3f, 1.0f - (float)(Math.sqrt(dist) / 64.0));
                    float alphaM = pulse * distFade;
                    float hpPct = living.getHealth() / living.getMaxHealth();
                    if (living.hurtTime > 0) {
                        outlineTop = RenderColor.of((int)255, (int)130, (int)130, (int)((int)(200.0f * alphaM)));
                        outlineBot = RenderColor.of((int)255, (int)50, (int)50, (int)((int)(180.0f * alphaM)));
                        fillTop = RenderColor.of((int)255, (int)40, (int)40, (int)((int)(22.0f * alphaM)));
                        fillBot = RenderColor.of((int)200, (int)20, (int)20, (int)((int)(14.0f * alphaM)));
                    } else if (entity instanceof PlayerEntity) {
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
        builder.vertex(x2, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{bottomColor});
        builder.vertex(x2, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{bottomColor});
        builder.vertex(x1, y1, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{bottomColor});
        builder.vertex(x1, y1, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{bottomColor});
        builder.vertex(x1, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{topColor});
        builder.vertex(x1, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{topColor});
        builder.vertex(x2, y2, z2).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{topColor});
        builder.vertex(x2, y2, z1).element("Color", CustomVertexElementTypes.RENDER_COLOR, new RenderColor[]{topColor});
    }
}

