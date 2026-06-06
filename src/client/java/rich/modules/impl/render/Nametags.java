package rich.modules.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;
import rich.util.profiler.FrameProfiler;
import rich.util.render.Render2D;
import rich.util.render.Render3D;
import rich.util.render.font.Fonts;

public class Nametags extends ModuleStructure {
    // SAFE defaults: после краша убраны drawItem/armor и тяжёлая логика. Сначала стабильно запускаемся, потом точечно докрутим.
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Disabled by default for FPS/stability").setValue(false);
    public SliderSettings renderDistance = new SliderSettings("Distance", "Max distance to draw tags (blocks)").range(8.0F, 64.0F).setValue(32.0F);
    public SliderSettings armorDistance = new SliderSettings("ArmorDistance", "Unused in safe mode").range(8.0F, 48.0F).setValue(12.0F);
    public SliderSettings maxTags = new SliderSettings("MaxTags", "Hard cap on tags drawn per frame").range(5.0F, 100.0F).setValue(20.0F);
    public SliderSettings armorBudget = new SliderSettings("ArmorBudget", "Unused in safe mode").range(0.0F, 20.0F).setValue(0.0F);

    private static final float SCREEN_MARGIN = 100.0f;
    private final Vector4f reusablePos = new Vector4f();
    private final float[] reuseScreen = new float[2];

    public Nametags() {
        super("Nametags", "Display player names above heads", ModuleCategory.VISUALS);
        this.settings(this.showArmor, this.renderDistance, this.armorDistance, this.maxTags, this.armorBudget);
    }

    public static Nametags getInstance() {
        return c.a(Nametags.class);
    }

    public boolean showArmor() {
        return false;
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        FrameProfiler profiler = FrameProfiler.getInstance();
        boolean prof = profiler.isEnabled();
        if (prof) profiler.begin("Nametags/safeRender");
        try {
            this.renderInternal(event.getDrawContext());
        } catch (Throwable ignored) {
            // Важно: nametags не должны крашить весь клиент.
        } finally {
            if (prof) profiler.end();
        }
    }

    private boolean worldToScreen(double wx, double wy, double wz, float sw, float sh) {
        Vec3d cam = Render3D.lastCameraPos;
        if (cam == null || Render3D.lastWorldSpaceMatrix == null || Render3D.lastProjMat == null) {
            return false;
        }

        float dx = (float)(wx - cam.x);
        float dy = (float)(wy - cam.y);
        float dz = (float)(wz - cam.z);
        Vector4f v = this.reusablePos.set(dx, dy, dz, 1.0f);
        Render3D.lastWorldSpaceMatrix.transform(v);
        Render3D.lastProjMat.transform(v);

        float w = v.w();
        if (w <= 0.05f) {
            return false;
        }

        float inv = 1.0f / w;
        this.reuseScreen[0] = (1.0f + v.x() * inv) * 0.5f * sw;
        this.reuseScreen[1] = (1.0f - v.y() * inv) * 0.5f * sh;
        return true;
    }

    private void renderInternal(DrawContext g) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        PlayerEntity self = mc.player;
        float sw = Render2D.getFixedScaledWidth();
        float sh = Render2D.getFixedScaledHeight();
        float td = Render3D.lastTickDelta;
        float maxDist = this.renderDistance.getValue();
        double maxDist2 = maxDist * maxDist;
        int tagsLeft = (int)this.maxTags.getValue();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (tagsLeft <= 0) {
                break;
            }
            if (player == self || player.isSpectator() || !player.isAlive()) {
                continue;
            }

            double dist2 = self.squaredDistanceTo(player);
            if (dist2 > maxDist2) {
                continue;
            }

            Vec3d p = player.getLerpedPos(td);
            double headY = p.y + (double)player.getHeight() + 0.4;
            if (!this.worldToScreen(p.x, headY, p.z, sw, sh)) {
                continue;
            }

            float cx = this.reuseScreen[0];
            float cy = this.reuseScreen[1];
            if (cx < -SCREEN_MARGIN || cx > sw + SCREEN_MARGIN || cy < -SCREEN_MARGIN || cy > sh + SCREEN_MARGIN) {
                continue;
            }

            this.renderSimpleTag(player, cx, cy, (float)Math.sqrt(dist2));
            tagsLeft--;
        }
    }

    private void renderSimpleTag(PlayerEntity player, float cx, float cy, float distance) {
        String name = player.getName().getString();
        int hp = (int)Math.ceil(player.getHealth());
        String text = name + " " + hp;
        float scale = Math.max(0.72f, Math.min(1.0f, 1.05f - distance * 0.012f));
        float fontSize = 6.0f * scale;
        float padX = 3.0f * scale;
        float padY = 2.0f * scale;
        float width = Fonts.BOLD.getWidth(text, fontSize);
        float panelW = width + padX * 2.0f;
        float panelH = fontSize + padY * 2.0f;
        float x = cx - panelW / 2.0f;
        float y = cy - panelH;

        Render2D.rect(x, y, panelW, panelH, 0x88101015, Math.max(2.0f, 3.0f * scale));
        Fonts.BOLD.draw(text, x + padX, y + padY, fontSize, 0xFFFFFFFF);
    }
}
