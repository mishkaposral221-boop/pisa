package rich.modules.impl.render;

import java.util.ArrayList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
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
import rich.theme.ClientTheme;
import rich.util.c;
import rich.util.render.Render2D;
import rich.util.render.Render3D;
import rich.util.render.font.Fonts;

public class Nametags extends ModuleStructure {
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Display player armor above the tag").setValue(true);
    public SliderSettings armorDistance = new SliderSettings("ArmorDistance", "Max distance to draw armor (blocks)").range(8.0F, 64.0F).setValue(32.0F);

    private final Vector4f reusablePos = new Vector4f();
    private final float[] reuseScreen = new float[2];
    private final ArrayList<ItemStack> armorItems = new ArrayList<>(6);

    public Nametags() {
        super("Nametags", "Display player names above heads", ModuleCategory.VISUALS);
        this.settings(this.showArmor, this.armorDistance);
    }

    public static Nametags getInstance() {
        return c.a(Nametags.class);
    }

    public boolean showArmor() {
        return this.showArmor.isValue();
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        try {
            this.renderInternal(event.getDrawContext());
        } catch (Exception exception) {
            // ignore
        }
    }

    private boolean worldToScreen(double wx, double wy, double wz, float sw, float sh) {
        Vec3d cam = Render3D.lastCameraPos;
        if (cam == null) {
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
        float sw = Render2D.getFixedScaledWidth();
        float sh = Render2D.getFixedScaledHeight();
        float td = Render3D.lastTickDelta;
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player || entity.isSpectator()) {
                continue;
            }
            double dist2 = mc.player.squaredDistanceTo(entity);
            if (dist2 > 4096.0) {
                continue;
            }
            Vec3d p = entity.getLerpedPos(td);
            double headY = p.y + (double)entity.getHeight() + 0.45;
            if (!this.worldToScreen(p.x, headY, p.z, sw, sh)) {
                continue;
            }
            float cx = this.reuseScreen[0];
            float cy = this.reuseScreen[1];
            float distance = (float)Math.sqrt(dist2);
            float scale = Math.max(0.7f, Math.min(1.2f, 1.15f - distance * 0.012f));
            this.renderTag(g, entity, cx, cy, scale, distance);
        }
    }

    private void renderTag(DrawContext g, PlayerEntity player, float cx, float cy, float scale, float distance) {
        float health = player.getHealth();
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        String name = player.getName().getString();
        String hpStr = Integer.toString((int)Math.ceil(health));

        int ping = 0;
        try {
            PlayerListEntry entry = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) : null;
            if (entry != null) {
                ping = entry.getLatency();
            }
        } catch (Exception e) {
            ping = 0;
        }

        float hpPct = Math.max(0.0f, Math.min(1.0f, health / maxHp));
        int hpColor = hpPct > 0.6f ? 0xFF55FF55 : (hpPct > 0.3f ? 0xFFFFAA00 : 0xFFFF5555);
        int pingColor = ping < 80 ? 0xFF7CFF7C : (ping < 150 ? 0xFFFFC04C : 0xFFFF6B6B);

        String hpPart = "  " + hpStr + " hp";
        String pingPart = "  " + ping + "ms";

        float fontSize = 6.2f * scale;
        float pad = 4.0f * scale;
        float gap = 2.5f * scale;
        float textH = fontSize;
        float barH = Math.max(2.0f, 3.0f * scale);

        float nameW = Fonts.BOLD.getWidth(name, fontSize);
        float hpW = Fonts.BOLD.getWidth(hpPart, fontSize);
        float pingW = Fonts.BOLD.getWidth(pingPart, fontSize);
        float contentW = Math.max(nameW + hpW + pingW, 36.0f * scale);

        float panelW = contentW + pad * 2.0f;
        float panelH = pad * 2.0f + textH + gap + barH;
        float panelX = cx - panelW / 2.0f;
        float panelY = cy - panelH - 2.0f * scale;
        float radius = Math.max(2.5f, 4.0f * scale);

        int panelAlpha = 225;
        Render2D.gradientRect(panelX, panelY, panelW, panelH, ClientTheme.bgGradient(panelAlpha), radius);
        Render2D.outline(panelX, panelY, panelW, panelH, 0.4f * scale, ClientTheme.outline(panelAlpha), radius);

        float tx = panelX + pad;
        float ty = panelY + pad;
        int shadow = 0xC0000000;
        Fonts.BOLD.draw(name, tx + 0.4f, ty + 0.4f, fontSize, shadow);
        Fonts.BOLD.draw(name, tx, ty, fontSize, 0xFFFFFFFF);
        Fonts.BOLD.draw(hpPart, tx + nameW + 0.4f, ty + 0.4f, fontSize, shadow);
        Fonts.BOLD.draw(hpPart, tx + nameW, ty, fontSize, hpColor);
        Fonts.BOLD.draw(pingPart, tx + nameW + hpW + 0.4f, ty + 0.4f, fontSize, shadow);
        Fonts.BOLD.draw(pingPart, tx + nameW + hpW, ty, fontSize, pingColor);

        float barX = panelX + pad;
        float barY = ty + textH + gap;
        float barW = panelW - pad * 2.0f;
        float barRadius = barH / 2.0f;
        Render2D.rect(barX, barY, barW, barH, 0x90101014, barRadius);
        if (hpPct > 0.001f) {
            Render2D.rect(barX, barY, Math.max(barH, barW * hpPct), barH, hpColor, barRadius);
        }

        if (this.showArmor() && distance <= this.armorDistance.getValue()) {
            this.armorItems.clear();
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
            ItemStack mainHand = player.getEquippedStack(EquipmentSlot.MAINHAND);
            ItemStack offHand = player.getEquippedStack(EquipmentSlot.OFFHAND);
            if (!mainHand.isEmpty()) this.armorItems.add(mainHand);
            if (!helmet.isEmpty()) this.armorItems.add(helmet);
            if (!chest.isEmpty()) this.armorItems.add(chest);
            if (!legs.isEmpty()) this.armorItems.add(legs);
            if (!boots.isEmpty()) this.armorItems.add(boots);
            if (!offHand.isEmpty()) this.armorItems.add(offHand);
            if (!this.armorItems.isEmpty()) {
                float itemScale = scale * 0.85f;
                float itemSize = 16.0f * itemScale;
                float spacing = itemSize + 1.5f;
                float totalW = (float)this.armorItems.size() * spacing - 1.5f;
                float startX = cx - totalW / 2.0f;
                float itemY = panelY - itemSize - 2.0f * scale;
                for (int i = 0; i < this.armorItems.size(); i++) {
                    ItemStack item = this.armorItems.get(i);
                    float itemX = startX + (float)i * spacing;
                    try {
                        g.getMatrices().pushMatrix();
                        g.getMatrices().translate(itemX, itemY);
                        g.getMatrices().scale(itemScale, itemScale);
                        g.drawItem(item, 0, 0);
                        g.getMatrices().popMatrix();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
