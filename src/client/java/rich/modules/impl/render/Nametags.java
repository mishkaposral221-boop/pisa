package rich.modules.impl.render;

import java.util.ArrayList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector4f;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;
import rich.util.render.font.Fonts;

public class Nametags extends ModuleStructure {
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Display player armor info").setValue(true);
    public SliderSettings armorDistance = new SliderSettings("ArmorDistance", "\u041c\u0430\u043a\u0441. \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043e\u0442\u0440\u0438\u0441\u043e\u0432\u043a\u0438 \u0431\u0440\u043e\u043d\u0438 (\u0431\u043b\u043e\u043a\u0438)").range(8.0F, 64.0F).setValue(32.0F);

    private final Vector4f reusablePos = new Vector4f();
    private final Quaternionf reuseQuat = new Quaternionf();
    private final Matrix4f reuseView = new Matrix4f();
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
            this.renderInternal(event.getDrawContext(), event.getPartialTicks());
        } catch (Exception exception) {
            // ignore
        }
    }

    private boolean worldToScreen(double wx, double wy, double wz, Vec3d camPos, Matrix4f viewMatrix, Matrix4f projMatrix, int sw, int sh) {
        float dx = (float)(wx - camPos.x);
        float dy = (float)(wy - camPos.y);
        float dz = (float)(wz - camPos.z);
        Vector4f pos = this.reusablePos.set(dx, dy, dz, 1.0f);
        viewMatrix.transform(pos);
        projMatrix.transform(pos);
        if (pos.w() <= 0.001f) {
            return false;
        }
        float ndcX = pos.x() / pos.w();
        float ndcY = pos.y() / pos.w();
        this.reuseScreen[0] = (ndcX + 1.0f) * 0.5f * (float)sw;
        this.reuseScreen[1] = (-ndcY + 1.0f) * 0.5f * (float)sh;
        return true;
    }

    private void renderInternal(DrawContext guiGraphics, float partialTick) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        Quaternionfc camRot = (Quaternionfc)mc.gameRenderer.getCamera().getRotation();
        this.reuseQuat.set(camRot).conjugate();
        Matrix4f viewMatrix = this.reuseView.rotation((Quaternionfc)this.reuseQuat);
        float fov = ((Integer)mc.options.getFov().getValue()).intValue();
        Matrix4f projMatrix = mc.gameRenderer.getBasicProjectionMatrix(fov);
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player || entity.isSpectator()) continue;
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > 4096.0) continue;
            double lerpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * (double)partialTick;
            double lerpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * (double)partialTick;
            double lerpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * (double)partialTick;
            double headY = lerpY + (double)entity.getHeight() + 0.5;
            if (!this.worldToScreen(lerpX, headY, lerpZ, camPos, viewMatrix, projMatrix, sw, sh)) continue;
            float screenX = Math.round(this.reuseScreen[0]);
            float screenY = Math.round(this.reuseScreen[1]);
            if (screenX < -80.0f || screenX > (float)sw + 80.0f || screenY < -80.0f || screenY > (float)sh + 80.0f) continue;
            float distance = (float)Math.sqrt(dist);
            float scale = Math.max(0.6f, Math.min(1.0f, 1.0f - distance / 24.0f));
            this.renderNametag(guiGraphics, entity, screenX, screenY, scale, distance);
        }
    }

    private void renderNametag(DrawContext guiGraphics, PlayerEntity player, float cx, float cy, float scale, float distance) {
        float health = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        String name = player.getName().getString();
        String hpStr = Integer.toString((int)health);

        int ping = 0;
        try {
            net.minecraft.client.network.PlayerListEntry entry = mc.getNetworkHandler() != null
                    ? mc.getNetworkHandler().getPlayerListEntry(player.getUuid())
                    : null;
            if (entry != null) {
                ping = entry.getLatency();
            }
        } catch (Exception e) {
            ping = 0;
        }

        this.armorItems.clear();
        if (this.showArmor() && distance <= this.armorDistance.getValue()) {
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
            ItemStack mainHand = player.getEquippedStack(EquipmentSlot.MAINHAND);
            ItemStack offHand = player.getEquippedStack(EquipmentSlot.OFFHAND);
            if (!helmet.isEmpty()) this.armorItems.add(helmet);
            if (!chest.isEmpty()) this.armorItems.add(chest);
            if (!legs.isEmpty()) this.armorItems.add(legs);
            if (!boots.isEmpty()) this.armorItems.add(boots);
            if (!mainHand.isEmpty()) this.armorItems.add(mainHand);
            if (!offHand.isEmpty()) this.armorItems.add(offHand);
        }

        float hpPct = Math.min(1.0f, health / maxHp);
        int hpColor = hpPct > 0.6f ? 0xFF55FF55 : (hpPct > 0.3f ? 0xFFFFAA00 : 0xFFFF5555);
        int pingColor = ping < 80 ? 0xFF55FF55 : (ping < 150 ? 0xFFFFAA00 : 0xFFFF5555);

        String hpPart = " " + hpStr;
        String pingPart = " " + ping + "ms";

        float fontSize = 7.5f * scale;
        float nameW = Fonts.BOLD.getWidth(name, fontSize);
        float hpW = Fonts.BOLD.getWidth(hpPart, fontSize);
        float pingW = Fonts.BOLD.getWidth(pingPart, fontSize);
        float heartW = 9.0f * scale;
        float totalW = heartW + nameW + hpW + pingW;

        float itemScale = scale * 0.9f;
        float itemSize = 16.0f * itemScale;
        float armorRowH = this.armorItems.isEmpty() ? 0.0f : itemSize + 3.0f;

        float textY = cy - armorRowH;
        float startX = cx - totalW / 2.0f;

        if (!this.armorItems.isEmpty()) {
            float totalArmorW = (float)this.armorItems.size() * (itemSize + 2.0f) - 2.0f;
            float armorStartX = cx - totalArmorW / 2.0f;
            for (int i = 0; i < this.armorItems.size(); i++) {
                ItemStack item = this.armorItems.get(i);
                float itemX = armorStartX + (float)i * (itemSize + 2.0f);
                float itemY = textY - itemSize - 2.0f;
                guiGraphics.getMatrices().pushMatrix();
                guiGraphics.getMatrices().translate(itemX, itemY);
                guiGraphics.getMatrices().scale(itemScale, itemScale);
                guiGraphics.drawItem(item, 0, 0);
                guiGraphics.getMatrices().popMatrix();
            }
        }

        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().translate(startX, textY - 0.5f * scale);
        guiGraphics.getMatrices().scale(scale, scale);
        this.drawHeart(guiGraphics, 0, 0, 0xFFFF5555);
        guiGraphics.getMatrices().popMatrix();

        float tx = startX + heartW;
        this.drawFont(name, tx, textY, fontSize, 0xFFFFFFFF);
        this.drawFont(hpPart, tx + nameW, textY, fontSize, hpColor);
        this.drawFont(pingPart, tx + nameW + hpW, textY, fontSize, pingColor);

        float barX = cx - totalW / 2.0f;
        float barY = textY + fontSize + 2.0f * scale;
        float barW = totalW;
        float barH = Math.max(1.5f, 2.5f * scale);

        guiGraphics.fill((int)(barX - 1.0f), (int)(barY - 1.0f), (int)(barX + barW + 1.0f), (int)(barY + barH + 1.0f), 0xC0000000);
        guiGraphics.fill((int)barX, (int)barY, (int)(barX + barW), (int)(barY + barH), 0x80000000);

        float fillW = barW * hpPct;
        if (fillW > 0.0f) {
            guiGraphics.fill((int)barX, (int)barY, (int)(barX + fillW), (int)(barY + barH), hpColor);
        }

        if (absorption > 0.0f) {
            float absW = barW * Math.min(1.0f, absorption / maxHp);
            guiGraphics.fill((int)(barX + barW - absW), (int)barY, (int)(barX + barW), (int)(barY + barH), 0xFFFFAA00);
        }
    }

    private void drawFont(String s, float x, float y, float size, int color) {
        int outline = 0xFF000000;
        Fonts.BOLD.draw(s, x - 0.8f, y, size, outline);
        Fonts.BOLD.draw(s, x + 0.8f, y, size, outline);
        Fonts.BOLD.draw(s, x, y - 0.8f, size, outline);
        Fonts.BOLD.draw(s, x, y + 0.8f, size, outline);
        Fonts.BOLD.draw(s, x - 0.6f, y - 0.6f, size, outline);
        Fonts.BOLD.draw(s, x + 0.6f, y - 0.6f, size, outline);
        Fonts.BOLD.draw(s, x - 0.6f, y + 0.6f, size, outline);
        Fonts.BOLD.draw(s, x + 0.6f, y + 0.6f, size, outline);
        Fonts.BOLD.draw(s, x, y, size, color);
    }

    private int getMaxEnchantmentLevel(ItemStack item) {
        try {
            ItemEnchantmentsComponent enchComp = item.get(DataComponentTypes.ENCHANTMENTS);
            if (enchComp == null) return 0;

            int maxLevel = 0;
            try {
                for (RegistryEntry<Enchantment> ench : enchComp.getEnchantments()) {
                    int level = enchComp.getLevel(ench);
                    if (level > maxLevel) maxLevel = level;
                }
            } catch (Exception e2) {
                return 0;
            }
            return maxLevel;
        } catch (Exception e) {
            return 0;
        }
    }

    private void drawHeart(DrawContext g, int x, int y, int color) {
        g.fill(x + 1, y,     x + 3, y + 1, color);
        g.fill(x + 4, y,     x + 6, y + 1, color);
        g.fill(x,     y + 1, x + 7, y + 3, color);
        g.fill(x + 1, y + 3, x + 6, y + 4, color);
        g.fill(x + 2, y + 4, x + 5, y + 5, color);
        g.fill(x + 3, y + 5, x + 4, y + 6, color);
    }
}
