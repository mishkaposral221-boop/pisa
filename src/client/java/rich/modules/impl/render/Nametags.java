package rich.modules.impl.render;

import java.util.ArrayList;
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

    // worldToScreen \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442 \u0437\u0430\u0445\u0432\u0430\u0447\u0435\u043d\u043d\u044b\u0435 \u043c\u0430\u0442\u0440\u0438\u0446\u044b \u043c\u0438\u0440\u0430 (\u0444\u0438\u043a\u0441 \u0442\u0440\u044f\u0441\u043a\u0438)
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
            double headY = p.y + (double)entity.getHeight() + 0.4;
            if (!this.worldToScreen(p.x, headY, p.z, sw, sh)) {
                continue;
            }
            float cx = this.reuseScreen[0];
            float cy = this.reuseScreen[1];
            float distance = (float)Math.sqrt(dist2);
            float scale = Math.max(0.75f, Math.min(1.1f, 1.1f - distance * 0.010f));
            this.renderTag(g, entity, cx, cy, scale, distance);
        }
    }

    // \u041c\u0418\u041d\u0418\u041c\u0410\u041b\u0418\u0417\u041c: \u0447\u0438\u0441\u0442\u043e\u0435 \u0438\u043c\u044f + \u0442\u043e\u043d\u043a\u0430\u044f \u043f\u043e\u043b\u043e\u0441\u043a\u0430 HP \u043f\u043e\u0434 \u043d\u0438\u043c. \u0411\u0435\u0437 \u043f\u0438\u043d\u0433\u0430/\u043d\u0430\u0434\u043f\u0438\u0441\u0438 "hp".
    private void renderTag(DrawContext g, PlayerEntity player, float cx, float cy, float scale, float distance) {
        float health = player.getHealth();
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        String name = player.getName().getString();
        float hpPct = Math.max(0.0f, Math.min(1.0f, health / maxHp));
        int hpColor = hpPct > 0.6f ? 0xFF63E6A0 : (hpPct > 0.3f ? 0xFFF2C14E : 0xFFF26D6D);

        float fontSize = 6.0f * scale;
        float padX = 3.5f * scale;
        float padY = 2.5f * scale;
        float textH = fontSize;
        float barGap = 2.0f * scale;
        float barH = Math.max(1.5f, 2.0f * scale);

        float nameW = Fonts.BOLD.getWidth(name, fontSize);
        float panelW = nameW + padX * 2.0f;
        float panelH = padY * 2.0f + textH + barGap + barH;
        float panelX = cx - panelW / 2.0f;
        float panelY = cy - panelH;
        float radius = Math.max(2.0f, 3.0f * scale);

        // \u0442\u043e\u043d\u043a\u0430\u044f \u043f\u043e\u043b\u0443\u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u0430\u044f \u0442\u0430\u0431\u043b\u0435\u0442\u043a\u0430 + \u0435\u043b\u0432\u0430 \u0437\u0430\u043c\u0435\u0442\u043d\u044b\u0439 \u043a\u043e\u043d\u0442\u0443\u0440
        Render2D.rect(panelX, panelY, panelW, panelH, 0x80101015, radius);
        Render2D.outline(panelX, panelY, panelW, panelH, 0.35f * scale, 0x40FFFFFF, radius);

        // \u0438\u043c\u044f \u043f\u043e \u0446\u0435\u043d\u0442\u0440\u0443
        float tx = cx - nameW / 2.0f;
        float ty = panelY + padY;
        Fonts.BOLD.draw(name, tx + 0.4f, ty + 0.4f, fontSize, 0xC0000000);
        Fonts.BOLD.draw(name, tx, ty, fontSize, 0xFFFFFFFF);

        // \u0442\u043e\u043d\u043a\u0430\u044f \u0437\u0430\u043a\u0440\u0443\u0433\u043b\u0451\u043d\u043d\u0430\u044f \u043f\u043e\u043b\u043e\u0441\u043a\u0430 HP
        float barX = panelX + padX;
        float barY = ty + textH + barGap;
        float barW = panelW - padX * 2.0f;
        float barRadius = barH / 2.0f;
        Render2D.rect(barX, barY, barW, barH, 0x80000000, barRadius);
        if (hpPct > 0.001f) {
            Render2D.rect(barX, barY, Math.max(barH, barW * hpPct), barH, hpColor, barRadius);
        }

        if (this.showArmor() && distance <= this.armorDistance.getValue()) {
            this.armorItems.clear();
            ItemStack mainHand = player.getEquippedStack(EquipmentSlot.MAINHAND);
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
            ItemStack offHand = player.getEquippedStack(EquipmentSlot.OFFHAND);
            if (!mainHand.isEmpty()) this.armorItems.add(mainHand);
            if (!helmet.isEmpty()) this.armorItems.add(helmet);
            if (!chest.isEmpty()) this.armorItems.add(chest);
            if (!legs.isEmpty()) this.armorItems.add(legs);
            if (!boots.isEmpty()) this.armorItems.add(boots);
            if (!offHand.isEmpty()) this.armorItems.add(offHand);
            if (!this.armorItems.isEmpty()) {
                float itemScale = scale * 0.8f;
                float itemSize = 16.0f * itemScale;
                float spacing = itemSize + 1.0f;
                float totalW = (float)this.armorItems.size() * spacing - 1.0f;
                float startX = cx - totalW / 2.0f;
                float itemY = panelY - itemSize - 1.5f * scale;
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
