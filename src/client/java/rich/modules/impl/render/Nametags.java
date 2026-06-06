package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
    // drawItem очень дорогой, поэтому дефолты теперь безопаснее для FPS.
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Draw held item + armor icons over the tag (close range only; turn off if FPS drops)").setValue(true);
    public SliderSettings renderDistance = new SliderSettings("Distance", "Max distance to draw tags (blocks)").range(8.0F, 64.0F).setValue(36.0F);
    public SliderSettings armorDistance = new SliderSettings("ArmorDistance", "Max distance to draw the icon row (blocks)").range(8.0F, 48.0F).setValue(16.0F);
    public SliderSettings maxTags = new SliderSettings("MaxTags", "Hard cap on tags drawn per frame (protects FPS in crowds)").range(5.0F, 100.0F).setValue(25.0F);
    public SliderSettings armorBudget = new SliderSettings("ArmorBudget", "Max players that get the equipment icon row per frame (drawItem is expensive)").range(0.0F, 20.0F).setValue(4.0F);

    private static final long CANDIDATE_CACHE_MS = 100L;
    private static final long TEXT_CACHE_CLEAR_MS = 2_000L;
    private static final float SCREEN_MARGIN = 120.0f;
    private static final float COMPACT_DISTANCE = 26.0f;

    private final Vector4f reusablePos = new Vector4f();
    private final float[] reuseScreen = new float[2];
    private final ArrayList<ItemStack> armorItems = new ArrayList<>(6);
    private final ArrayList<PlayerEntity> candidates = new ArrayList<>();
    private final Map<String, Float> textWidthCache = new HashMap<>(128);

    private long lastCandidateUpdate = 0L;
    private long lastTextCacheClear = 0L;

    public Nametags() {
        super("Nametags", "Display player names above heads", ModuleCategory.VISUALS);
        this.settings(this.showArmor, this.renderDistance, this.armorDistance, this.maxTags, this.armorBudget);
    }

    public static Nametags getInstance() {
        return c.a(Nametags.class);
    }

    public boolean showArmor() {
        return this.showArmor.isValue();
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        FrameProfiler profiler = FrameProfiler.getInstance();
        boolean prof = profiler.isEnabled();
        if (prof) profiler.begin("Nametags/renderInternal");
        try {
            this.renderInternal(event.getDrawContext());
        } catch (Exception exception) {
            // ignore
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
        float ndcX = v.x() * inv;
        float ndcY = v.y() * inv;
        this.reuseScreen[0] = (1.0f + ndcX) * 0.5f * sw;
        this.reuseScreen[1] = (1.0f - ndcY) * 0.5f * sh;
        return true;
    }

    private void renderInternal(DrawContext g) {
        if (mc.world == null || mc.player == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - this.lastTextCacheClear > TEXT_CACHE_CLEAR_MS) {
            this.textWidthCache.clear();
            this.lastTextCacheClear = now;
        }

        PlayerEntity self = mc.player;
        float sw = Render2D.getFixedScaledWidth();
        float sh = Render2D.getFixedScaledHeight();
        float td = Render3D.lastTickDelta;
        float maxDist = this.renderDistance.getValue();
        float maxDist2 = maxDist * maxDist;
        boolean armorOn = this.showArmor();
        float armorMax = this.armorDistance.getValue();
        float armorFullDist = armorMax * 0.5f;
        int tagsLeft = (int)this.maxTags.getValue();
        int armorLeft = (int)this.armorBudget.getValue();

        this.updateCandidates(now, self, maxDist2);
        if (this.candidates.isEmpty()) {
            return;
        }

        Vec3d eye = self.getEyePos();
        Vec3d look = self.getRotationVector();

        for (int idx = 0; idx < this.candidates.size(); idx++) {
            if (tagsLeft <= 0) {
                break;
            }

            PlayerEntity entity = this.candidates.get(idx);
            if (entity == null || entity == self || entity.isSpectator() || !entity.isAlive()) {
                continue;
            }

            double dist2 = self.squaredDistanceTo(entity);
            if (dist2 > maxDist2) {
                continue;
            }

            // Очень дешёвый pre-cull: если игрок явно сзади камеры, не делаем projection/font/drawItem.
            Vec3d to = entity.getPos().subtract(eye);
            double lenSq = to.lengthSquared();
            if (lenSq > 9.0) {
                double dot = to.dotProduct(look) / Math.sqrt(lenSq);
                if (dot < -0.35) {
                    continue;
                }
            }

            Vec3d p = entity.getLerpedPos(td);
            double headY = p.y + (double)entity.getHeight() + 0.4;
            if (!this.worldToScreen(p.x, headY, p.z, sw, sh)) {
                continue;
            }

            float cx = this.reuseScreen[0];
            float cy = this.reuseScreen[1];
            if (cx < -SCREEN_MARGIN || cx > sw + SCREEN_MARGIN || cy < -SCREEN_MARGIN || cy > sh + SCREEN_MARGIN) {
                continue;
            }

            float distance = (float)Math.sqrt(dist2);
            float scale = Math.max(0.72f, Math.min(1.05f, 1.08f - distance * 0.011f));
            boolean compact = distance > COMPACT_DISTANCE;
            boolean drawArmor = !compact && armorOn && distance <= armorMax && armorLeft > 0;
            boolean armorFull = distance <= armorFullDist;

            if (this.renderTag(g, entity, cx, cy, scale, distance, compact, drawArmor, armorFull)) {
                armorLeft--;
            }
            tagsLeft--;
        }
    }

    private void updateCandidates(long now, PlayerEntity self, float maxDist2) {
        if (now - this.lastCandidateUpdate < CANDIDATE_CACHE_MS && !this.candidates.isEmpty()) {
            return;
        }

        this.lastCandidateUpdate = now;
        this.candidates.clear();
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == self || entity.isSpectator() || !entity.isAlive()) {
                continue;
            }
            if (self.squaredDistanceTo(entity) <= maxDist2) {
                this.candidates.add(entity);
            }
        }

        if (this.candidates.size() > 1) {
            this.candidates.sort((a, b) -> Double.compare(self.squaredDistanceTo(a), self.squaredDistanceTo(b)));
        }
    }

    private float cachedWidth(String text, float fontSize) {
        int sizeKey = Math.round(fontSize * 10.0f);
        String key = text + "|" + sizeKey;
        Float cached = this.textWidthCache.get(key);
        if (cached != null) {
            return cached;
        }
        float width = Fonts.BOLD.getWidth(text, fontSize);
        if (this.textWidthCache.size() > 256) {
            this.textWidthCache.clear();
        }
        this.textWidthCache.put(key, width);
        return width;
    }

    private boolean renderTag(DrawContext g, PlayerEntity player, float cx, float cy, float scale, float distance, boolean compact, boolean drawArmor, boolean armorFull) {
        float health = player.getHealth();
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        String name = player.getName().getString();
        int hp = (int)Math.ceil(health);
        String hpStr = Integer.toString(hp);
        float hpPct = Math.max(0.0f, Math.min(1.0f, health / maxHp));
        int hpColor = hpPct > 0.6f ? 0xFF63E6A0 : (hpPct > 0.3f ? 0xFFF2C14E : 0xFFF26D6D);

        float fontSize = 6.0f * scale;
        float padX = 3.2f * scale;
        float padY = 2.2f * scale;
        float textH = fontSize;
        float barGap = 2.0f * scale;
        float barH = Math.max(1.4f, 1.8f * scale);
        float hpGap = 3.5f * scale;

        float nameW = this.cachedWidth(name, fontSize);
        float hpW = this.cachedWidth(hpStr, fontSize);
        float contentW = nameW + hpGap + hpW;
        float panelW = contentW + padX * 2.0f;
        float panelH = compact ? padY * 2.0f + textH : padY * 2.0f + textH + barGap + barH;
        float panelX = cx - panelW / 2.0f;
        float panelY = cy - panelH;
        float radius = Math.max(2.0f, 3.0f * scale);

        Render2D.rect(panelX, panelY, panelW, panelH, compact ? 0x88101015 : 0x99101015, radius);

        float tx = panelX + padX;
        float ty = panelY + padY;
        // На дальних тегах оставляем только один слой текста — меньше font draw calls в толпе.
        if (!compact) {
            Fonts.BOLD.draw(name, tx + 0.4f, ty + 0.4f, fontSize, 0xC0000000);
        }
        Fonts.BOLD.draw(name, tx, ty, fontSize, 0xFFFFFFFF);
        float hx = tx + nameW + hpGap;
        if (!compact) {
            Fonts.BOLD.draw(hpStr, hx + 0.4f, ty + 0.4f, fontSize, 0xC0000000);
        }
        Fonts.BOLD.draw(hpStr, hx, ty, fontSize, hpColor);

        if (!compact) {
            float barX = panelX + padX;
            float barY = ty + textH + barGap;
            float barW = panelW - padX * 2.0f;
            float barRadius = barH / 2.0f;
            Render2D.rect(barX, barY, barW, barH, 0x80000000, barRadius);
            if (hpPct > 0.001f) {
                Render2D.rect(barX, barY, Math.max(barH, barW * hpPct), barH, hpColor, barRadius);
            }
        }

        boolean drewArmor = false;
        if (drawArmor) {
            this.armorItems.clear();
            ItemStack mainHand = player.getEquippedStack(EquipmentSlot.MAINHAND);
            if (!mainHand.isEmpty()) this.armorItems.add(mainHand);
            if (armorFull) {
                ItemStack offHand = player.getEquippedStack(EquipmentSlot.OFFHAND);
                ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
                ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
                ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
                ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
                if (!offHand.isEmpty()) this.armorItems.add(offHand);
                if (!helmet.isEmpty()) this.armorItems.add(helmet);
                if (!chest.isEmpty()) this.armorItems.add(chest);
                if (!legs.isEmpty()) this.armorItems.add(legs);
                if (!boots.isEmpty()) this.armorItems.add(boots);
            }

            int n = this.armorItems.size();
            if (n > 0) {
                drewArmor = true;
                float itemScale = scale * 0.72f;
                float itemSize = 16.0f * itemScale;
                float spacing = itemSize + 1.0f;
                float totalW = (float)n * spacing - 1.0f;
                float startX = cx - totalW / 2.0f;
                float itemY = panelY - itemSize - 1.5f * scale;
                boolean pushed = false;
                try {
                    g.getMatrices().pushMatrix();
                    pushed = true;
                    g.getMatrices().translate(startX, itemY);
                    g.getMatrices().scale(itemScale, itemScale);
                    float step = spacing / itemScale;
                    for (int i = 0; i < n; i++) {
                        g.drawItem(this.armorItems.get(i), 0, 0);
                        g.getMatrices().translate(step, 0.0f);
                    }
                } catch (Exception ignored) {
                } finally {
                    if (pushed) {
                        try {
                            g.getMatrices().popMatrix();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        return drewArmor;
    }
}
