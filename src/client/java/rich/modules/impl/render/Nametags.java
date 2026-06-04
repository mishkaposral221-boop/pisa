package rich.modules.impl.render;

import java.util.ArrayList;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
import rich.util.c;

public class Nametags extends ModuleStructure {
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Display player armor info").setValue(true);

    public Nametags() {
        super("Nametags", "Display player names above heads", ModuleCategory.VISUALS);
        this.settings(this.showArmor);
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

    private static float[] worldToScreen(double wx, double wy, double wz, Vec3d camPos, Matrix4f viewMatrix, Matrix4f projMatrix, int sw, int sh) {
        float dx = (float)(wx - camPos.x);
        float dy = (float)(wy - camPos.y);
        float dz = (float)(wz - camPos.z);
        Vector4f pos = new Vector4f(dx, dy, dz, 1.0f);
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
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity.isSpectator() || !(entity instanceof PlayerEntity)) continue;
            PlayerEntity target = (PlayerEntity)entity;
            double dist = mc.player.squaredDistanceTo(entity);
            if (dist > 4096.0) continue;
            double lerpX = entity.lastRenderX + (entity.getX() - entity.lastRenderX) * (double)partialTick;
            double lerpY = entity.lastRenderY + (entity.getY() - entity.lastRenderY) * (double)partialTick;
            double lerpZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ) * (double)partialTick;
            double headY = lerpY + (double)entity.getHeight() + 0.5;
            float[] screen = Nametags.worldToScreen(lerpX, headY, lerpZ, camPos, viewMatrix, projMatrix, sw, sh);
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
        float totalHealth = health + absorption;
        String name = player.getName().getString();
        String hpStr = String.format("%.0f", Float.valueOf(totalHealth));

        // PING игрока (мс)
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
        String pingStr = " " + ping + "ms";

        MutableText nameComp = Text.literal((String)name);
        MutableText hpComp = Text.literal((String)(" " + hpStr));
        MutableText pingComp = Text.literal((String)pingStr);
        int nameW = font.getWidth((StringVisitable)nameComp);
        int hpW = font.getWidth((StringVisitable)hpComp);
        int pingW = font.getWidth((StringVisitable)pingComp);
        int totalW = nameW + hpW + pingW;
        float bgPad = 5.0f;
        float bgW = Math.max((float)totalW + bgPad * 2.0f, 60.0f);
        float bgH = 9 + 10;
        float bgX = -bgW / 2.0f;
        float bgY = 0.0f;
        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().translate(cx, cy);
        guiGraphics.getMatrices().scale(scale, scale);
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + bgH), -1879048192);
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + 1.0f), 1085564159);
        int textY = (int)(bgY + 3.0f);
        guiGraphics.drawText(font, (Text)nameComp, -totalW / 2, textY, -1, true);
        float hpPct = Math.min(1.0f, health / maxHp);
        int hpColor = hpPct > 0.6f ? -11141291 : (hpPct > 0.3f ? -171 : -43691);
        guiGraphics.drawText(font, (Text)hpComp, -totalW / 2 + nameW, textY, hpColor, true);
        // ping: зелёный <80, жёлтый <150, красный выше
        int pingColor = ping < 80 ? -11141291 : (ping < 150 ? -171 : -43691);
        guiGraphics.drawText(font, (Text)pingComp, -totalW / 2 + nameW + hpW, textY, pingColor, true);

        float barX = bgX + 3.0f;
        float barY = bgY + 9.0f + 5.0f;
        float barW = bgW - 6.0f;
        float barH = 2.5f;
        guiGraphics.fill((int)barX, (int)barY, (int)(barX + barW), (int)(barY + barH), 0x50000000);
        float fillW = barW * Math.min(1.0f, health / maxHp);
        if (fillW > 0.0f) {
            int barColor = hpPct > 0.6f ? -11141291 : (hpPct > 0.3f ? -171 : -43691);
            guiGraphics.fill((int)barX, (int)barY, (int)(barX + fillW), (int)(barY + barH), barColor);
        }
        if (absorption > 0.0f) {
            float absW = barW * Math.min(1.0f, absorption / maxHp);
            guiGraphics.fill((int)(barX + barW - absW), (int)barY, (int)(barX + barW), (int)(barY + barH), -9166);
        }
        if (this.showArmor()) {
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
            ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
            ItemStack legs = player.getEquippedStack(EquipmentSlot.LEGS);
            ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
            ItemStack mainHand = player.getEquippedStack(EquipmentSlot.MAINHAND);
            ItemStack offHand = player.getEquippedStack(EquipmentSlot.OFFHAND);
            if (!helmet.isEmpty()) items.add(helmet);
            if (!chest.isEmpty()) items.add(chest);
            if (!legs.isEmpty()) items.add(legs);
            if (!boots.isEmpty()) items.add(boots);
            if (!mainHand.isEmpty()) items.add(mainHand);
            if (!offHand.isEmpty()) items.add(offHand);
            if (!items.isEmpty()) {
                int count = items.size();
                float totalItemW = (float)(count - 1) * 18.0f + 16.0f;
                float startX = -totalItemW / 2.0f;
                for (int i = 0; i < count; ++i) {
                    int ix = (int)(startX + (float)(i * 18));
                    guiGraphics.drawItem((ItemStack)items.get(i), ix, -16);
                }
            }
        }
        guiGraphics.getMatrices().popMatrix();
    }
}
