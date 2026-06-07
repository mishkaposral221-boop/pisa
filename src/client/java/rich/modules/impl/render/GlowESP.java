package rich.modules.impl.render;

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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.c;

public class GlowESP extends ModuleStructure {
    private static final double MAX_DISTANCE_SQ = 32.0 * 32.0;
    private static final int MAX_ENTITIES_PER_FRAME = 16;

    public BooleanSetting showPlayers = new BooleanSetting("ShowPlayers", "Render player glow boxes").setValue(true);
    public BooleanSetting showMobs = new BooleanSetting("ShowMobs", "Render mob glow boxes").setValue(false);
    public BooleanSetting showInvisibles = new BooleanSetting("ShowInvisibles", "Show invisible entities").setValue(true);

    public static GlowESP getInstance() {
        return c.a(GlowESP.class);
    }

    public GlowESP() {
        super("GlowESP", "Glow outline renderer with gradient", ModuleCategory.VISUALS);
        this.settings(this.showPlayers, this.showMobs, this.showInvisibles);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        this.renderWorld();
    }

    private void renderWorld() {
        try {
            this.renderWorldInternal();
        } catch (Exception e) {
            // Silent catch
        }
    }

    private void renderWorldInternal() {
        if (mc.world == null || mc.player == null) {
            return;
        }
        // Не рисуем GlowESP поверх GUI/ClickGui: в логах это давало отдельные 29ms спайки.
        if (mc.currentScreen != null) {
            return;
        }

        Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
        int prevProgram = GL11.glGetInteger(35725);
        boolean prevBlend = GL11.glIsEnabled(3042);
        boolean prevDepth = GL11.glIsEnabled(2929);
        try {
            CometRenderer.setCurrentProgram((GlProgram)CRM.getPrograms().POSITION_COLOR);
            CometRenderer.applyShaderColorUniform();
            CRM.applyMatrixUniform();
            GL11.glEnable(3042);
            GL14.glBlendFuncSeparate(770, 771, 1, 0);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glLineWidth(2.0f);
            MeshBuilder outlineBuilder = Mesh.builder((DrawMode)CustomDrawMode.CUBE_OUTLINE, (VertexFormat)CustomVertexFormats.POSITION_COLOR);
            MeshBuilder fillBuilder = Mesh.builder((DrawMode)CustomDrawMode.CUBE, (VertexFormat)CustomVertexFormats.POSITION_COLOR);
            boolean hasGeometry = false;
            int rendered = 0;
            float partialTick = mc.getRenderTickCounter().getTickProgress(false);
            float pulse = (float)(0.85 + 0.15 * Math.sin((double)System.currentTimeMillis() / 400.0));
            for (Entity entity : mc.world.getEntities()) {
                if (rendered >= MAX_ENTITIES_PER_FRAME) {
                    break;
                }
                LivingEntity living;
                if (entity == mc.player || entity.isSpectator() || !(entity instanceof LivingEntity)) continue;
                living = (LivingEntity)entity;
                if (living.isInvisible() && !this.showInvisibles.isValue()) continue;
                if (entity instanceof PlayerEntity ? !this.showPlayers.isValue() : !this.showMobs.isValue()) continue;
                double dist = mc.player.squaredDistanceTo(entity);
                if (dist > MAX_DISTANCE_SQ) continue;
                Box box = entity.getBoundingBox();
                Vec3d entityPos = entity.getLerpedPos(partialTick);
                float minX = (float)(entityPos.x + (box.minX - entity.getX()) - camPos.x);
                float minY = (float)(entityPos.y + (box.minY - entity.getY()) - camPos.y);
                float minZ = (float)(entityPos.z + (box.minZ - entity.getZ()) - camPos.z);
                float maxX = (float)(entityPos.x + (box.maxX - entity.getX()) - camPos.x);
                float maxY = (float)(entityPos.y + (box.maxY - entity.getY()) - camPos.y);
                float maxZ = (float)(entityPos.z + (box.maxZ - entity.getZ()) - camPos.z);
                float distFade = Math.max(0.35f, 1.0f - (float)(Math.sqrt(dist) / 32.0));
                float alphaM = pulse * distFade;
                RenderColor fillBot;
                RenderColor fillTop;
                RenderColor outlineBot;
                RenderColor outlineTop;
                if (living.hurtTime > 0) {
                    outlineTop = RenderColor.of(255, 130, 130, (int)(190.0f * alphaM));
                    outlineBot = RenderColor.of(255, 50, 50, (int)(170.0f * alphaM));
                    fillTop = RenderColor.of(255, 40, 40, (int)(18.0f * alphaM));
                    fillBot = RenderColor.of(200, 20, 20, (int)(10.0f * alphaM));
                } else if (entity instanceof PlayerEntity) {
                    float hpPct = Math.min(1.0f, living.getHealth() / Math.max(1.0f, living.getMaxHealth()));
                    int r = (int)(255 * (1.0f - hpPct) + 90 * hpPct);
                    int g = (int)(70 * (1.0f - hpPct) + 230 * hpPct);
                    int b = (int)(120 * (1.0f - hpPct) + 160 * hpPct);
                    outlineTop = RenderColor.of(Math.min(255, r + 50), Math.min(255, g + 40), Math.min(255, b + 60), (int)(210.0f * alphaM));
                    outlineBot = RenderColor.of(r, g, b, (int)(180.0f * alphaM));
                    fillTop = RenderColor.of(r, g, b, (int)(20.0f * alphaM));
                    fillBot = RenderColor.of(r / 2, g / 2, b / 2, (int)(12.0f * alphaM));
                } else {
                    outlineTop = RenderColor.of(100, 255, 200, (int)(170.0f * alphaM));
                    outlineBot = RenderColor.of(40, 200, 150, (int)(140.0f * alphaM));
                    fillTop = RenderColor.of(40, 200, 150, (int)(12.0f * alphaM));
                    fillBot = RenderColor.of(20, 120, 80, (int)(8.0f * alphaM));
                }
                this.addBoxVerticesGradient(outlineBuilder, minX, minY, minZ, maxX, maxY, maxZ, outlineBot, outlineTop);
                this.addBoxVerticesGradient(fillBuilder, minX, minY, minZ, maxX, maxY, maxZ, fillBot, fillTop);
                hasGeometry = true;
                rendered++;
            }
            if (hasGeometry) {
                Mesh fill = fillBuilder.buildNullable();
                Mesh outline = outlineBuilder.buildNullable();
                try {
                    if (fill != null) {
                        CometRenderer.draw((IMesh)fill);
                    }
                    if (outline != null) {
                        CometRenderer.draw((IMesh)outline);
                    }
                } finally {
                    if (fill != null) {
                        fill.close();
                    }
                    if (outline != null) {
                        outline.close();
                    }
                }
            }
        } finally {
            GL11.glDepthMask(true);
            if (prevDepth) {
                GL11.glEnable(2929);
            } else {
                GL11.glDisable(2929);
            }
            if (prevBlend) {
                GL11.glEnable(3042);
            } else {
                GL11.glDisable(3042);
            }
            GL11.glLineWidth(1.0f);
            GL20.glUseProgram(prevProgram);
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

