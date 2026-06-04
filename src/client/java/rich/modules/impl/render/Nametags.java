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
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
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

public class Nametags extends ModuleStructure {
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Display player armor info").setValue(true);
    public SliderSettings armorDistance = new SliderSettings("ArmorDistance", "\u041c\u0430\u043a\u0441. \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043e\u0442\u0440\u0438\u0441\u043e\u0432\u043a\u0438 \u0431\u0440\u043e\u043d\u0438 (\u0431\u043b\u043e\u043a\u0438)").range(8.0F, 64.0F).setValue(32.0F);

    // \u041f\u0435\u0440\u0435\u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u043c\u044b\u0435 \u043e\u0431\u044a\u0435\u043a\u0442\u044b \u2014 \u043d\u0435 \u0441\u043e\u0437\u0434\u0430\u0451\u043c \u043c\u0443\u0441\u043e\u0440 \u043a\u0430\u0436\u0434\u044b\u0439 \u043a\u0430\u0434\u0440\n    private final Vector4f reusablePos = new Vector4f();
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

    private float[] worldToScreen(double wx, double wy, double wz, Vec3d camPos, Matrix4f viewMatrix, Matrix4f projMatrix, int sw, int sh) {
        float dx = (float)(wx - camPos.x);
        float dy = (float)(wy - camPos.y);
        float dz = (float)(wz - camPos.z);
        Vector4f pos = this.reusablePos.set(dx, dy, dz, 1.0f);
        viewMatrix.transform(pos);
        projMatrix.transform(pos);
        if (pos.w() <= 0.001f) {
            return null;
        }
        float ndcX = pos.x() / pos.w();
        float ndcY = pos.y() / pos.w();
        float screenX = (ndcX + 1.0f) * 0.5f * (float)sw;
        float screenY = (-ndcY + 1.0f) * 0.5f * (float)sh;
        return new float[]{screenX, screenY};
    }

    private void renderInternal(DrawContext guiGraphics, float partialTick) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        Quaternionf quat = new Quaternionf((Quaternionfc)mc.gameRenderer.getCamera().getRotation()).conjugate();
        Matrix4f viewMatrix = new Matrix4f().rotation((Quaternionfc)quat);
        float fov = ((Integer)mc.options.getFov().getValue()).intValue();
        Matrix4f projMatrix = mc.gameRenderer.getBasicProjectionMatrix(fov);
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
        TextRenderer font = mc.textRenderer;
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player || entity.isSpectator()) continue;
            PlayerEntity target = entity;
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > 4096.0) continue;
            double lerpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * (double)partialTick;
            double lerpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * (double)partialTick;
            double lerpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * (double)partialTick;
            double headY = lerpY + (double)entity.getHeight() + 0.5;
            float[] screen = this.worldToScreen(lerpX, headY, lerpZ, camPos, viewMatrix, projMatrix, sw, sh);
            if (screen == null) continue;
            float screenX = Math.round(screen[0]);
            float screenY = Math.round(screen[1]);
            float distance = (float)Math.sqrt(dist);
            float scale = Math.max(0.5f, Math.min(1.0f, 1.0f - distance / 20.0f));
            this.renderNametag(guiGraphics, font, target, screenX, screenY, scale, distance);
        }
    }

    private void renderNametag(DrawContext guiGraphics, TextRenderer font, PlayerEntity player, float cx, float cy, float scale, float distance) {
        float health = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        String name = player.getName().getString();
        String hpStr = Integer.toString((int)health);
        
        // \u041f\u0438\u043d\u0433\n        int ping = 0;
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
        
        // \u0421\u043e\u0431\u0438\u0440\u0430\u0435\u043c \u0431\u0440\u043e\u043d\u044e (\u0442\u043e\u043b\u044c\u043a\u043e \u0432\u0431\u043b\u0438\u0437\u0438 \u2014 3D-\u043c\u043e\u0434\u0435\u043b\u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u0434\u043e\u0440\u043e\u0433\u0438\u0435)
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
        
        // \u0420\u0430\u0441\u0447\u0451\u0442 \u0440\u0430\u0437\u043c\u0435\u0440\u043e\u0432\n        float hpPct = Math.min(1.0f, health / maxHp);
        int hpColor = hpPct > 0.6f ? 0xFF55FF55 : (hpPct > 0.3f ? 0xFFFFAA00 : 0xFFFF5555);
        int pingColor = ping < 80 ? 0xFF55FF55 : (ping < 150 ? 0xFFFFAA00 : 0xFFFF5555);
        
        MutableText nameText = Text.literal(name);
        MutableText hpText = Text.literal(" " + hpStr);
        MutableText pingText = Text.literal(" " + ping + "ms");
        
        int nameW = font.getWidth(nameText);
        int hpW = font.getWidth(hpText);
        int pingW = font.getWidth(pingText);
        int textRowW = nameW + hpW + pingW + 12;
        
        float armorRowH = this.armorItems.isEmpty() ? 0 : 22;
        
        float bgPad = 4.0f;
        float bgW = Math.max((float)textRowW + bgPad * 2.0f, 50.0f);
        float bgH = 11 + 3;
        
        float bgX = -bgW / 2.0f;
        float bgY = -(armorRowH > 0 ? armorRowH + 2 : 0);
        
        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().translate(cx, cy);
        guiGraphics.getMatrices().scale(scale, scale);
        
        // === \u0412\u0415\u0420\u0425\u041d\u0418\u0419 \u0420\u042f\u0414: \u0418\u041a\u041e\u041d\u041a\u0418 \u0411\u0420\u041e\u041d\u0418 ===
        if (!this.armorItems.isEmpty()) {
            float totalArmorW = (float)this.armorItems.size() * 18.0f - 2.0f;
            float armorStartX = -totalArmorW / 2.0f;
            for (int i = 0; i < this.armorItems.size(); i++) {
                ItemStack item = this.armorItems.get(i);
                float itemX = armorStartX + (float)(i * 18);
                guiGraphics.drawItem(item, (int)itemX, (int)(bgY - 18));
            }
        }
        
        // === \u0424\u041e\u041d\u041e\u0412\u042b\u0419 \u041f\u0420\u042f\u041c\u041e\u0423\u0413\u041e\u041b\u042c\u041d\u0418\u041a ===
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + bgH), 0xC0000000);
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + 1), 0xFF4080FF);
        
        // === \u0421\u0422\u0420\u041e\u041a\u0410 \u0422\u0415\u041a\u0421\u0422\u0410 ===
        int textY = (int)(bgY + 2);
        
        int totalTextW = 10 + nameW + hpW + pingW;
        int textStartX = (int)(bgX + bgW / 2.0f - totalTextW / 2.0f);
        
        this.drawHeart(guiGraphics, textStartX + 1, textY + 1, 0xFFFF5555);
        
        guiGraphics.drawText(font, nameText, textStartX + 10, textY, 0xFFFFFFFF, true);
        guiGraphics.drawText(font, hpText, textStartX + 10 + nameW, textY, hpColor, true);
        guiGraphics.drawText(font, pingText, textStartX + 10 + nameW + hpW, textY, pingColor, true);
        
        // === HP \u0411\u0410\u0420 ===
        float barX = bgX + 2.0f;
        float barY = bgY + bgH - 2.5f;
        float barW = bgW - 4.0f;
        float barH = 2.0f;
        
        guiGraphics.fill((int)barX, (int)barY, (int)(barX + barW), (int)(barY + barH), 0x50000000);
        
        float fillW = barW * hpPct;
        if (fillW > 0.0f) {
            int barColor = hpPct > 0.6f ? 0xFF55FF55 : (hpPct > 0.3f ? 0xFFFFAA00 : 0xFFFF5555);
            guiGraphics.fill((int)barX, (int)barY, (int)(barX + fillW), (int)(barY + barH), barColor);
        }
        
        if (absorption > 0.0f) {
            float absW = barW * Math.min(1.0f, absorption / maxHp);
            guiGraphics.fill((int)(barX + barW - absW), (int)barY, (int)(barX + barW), (int)(barY + barH), 0xFFFFAA00);
        }
        
        guiGraphics.getMatrices().popMatrix();
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
