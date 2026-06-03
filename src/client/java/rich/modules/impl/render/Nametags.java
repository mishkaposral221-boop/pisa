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
            float distAlpha = Math.max(0.25f, Math.min(1.0f, 1.0f - distance / 28.0f));
            this.renderNametag(guiGraphics, font, target, screenX, screenY, scale, distance, distAlpha);
        }
    }

    private static int mulAlpha(int argb, float m) {
        int a = (argb >>> 24) & 0xFF;
        a = (int)((float)a * Math.max(0.0f, Math.min(1.0f, m)));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private void renderNametag(DrawContext guiGraphics, TextRenderer font, PlayerEntity player, float cx, float cy, float scale, float distance, float alpha) {
        float health = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        float maxHp = Math.max(1.0f, player.getMaxHealth());
        float totalHealth = health + absorption;
        String name = player.getName().getString();
        String hpStr = String.format("%.0f", Float.valueOf(totalHealth));
        MutableText nameComp = Text.literal((String)name);
        MutableText hpComp = Text.literal((String)(" " + hpStr));
        int nameW = font.getWidth((StringVisitable)nameComp);
        int hpW = font.getWidth((StringVisitable)hpComp);
        int totalW = nameW + hpW;
        float bgPad = 6.0f;
        float bgW = Math.max((float)totalW + bgPad * 2.0f, 64.0f);
        float bgH = 9 + 11;
        float bgX = -bgW / 2.0f;
        float bgY = 0.0f;

        guiGraphics.getMatrices().pushMatrix();
        guiGraphics.getMatrices().translate(cx, cy);
        guiGraphics.getMatrices().scale(scale, scale);

        // shadow
        guiGraphics.fill((int)(bgX + 1.0f), (int)(bgY + 1.0f), (int)(bgX + bgW + 1.0f), (int)(bgY + bgH + 1.0f), mulAlpha(0x66000000, alpha));
        // background: top and bottom halves (gradient imitation)
        int half = (int)(bgY + bgH / 2.0f);
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), half, mulAlpha(0xE6121218, alpha));
        guiGraphics.fill((int)bgX, half, (int)(bgX + bgW), (int)(bgY + bgH), mulAlpha(0xE61C1C26, alpha));
        // top accent
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + 1.0f), mulAlpha(0x804AA0FF, alpha));
        // border
        int border = mulAlpha(0x40FFFFFF, alpha);
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + 1.0f), border);
        guiGraphics.fill((int)bgX, (int)(bgY + bgH - 1.0f), (int)(bgX + bgW), (int)(bgY + bgH), border);
        guiGraphics.fill((int)bgX, (int)bgY, (int)(bgX + 1.0f), (int)(bgY + bgH), border);
        guiGraphics.fill((int)(bgX + bgW - 1.0f), (int)bgY, (int)(bgX + bgW), (int)(bgY + bgH), border);

        int textY = (int)(bgY + 3.0f);
        guiGraphics.drawText(font, (Text)nameComp, -totalW / 2, textY, mulAlpha(0xFFFFFFFF, alpha), true);
        float hpPct = Math.min(1.0f, health / maxHp);
        int hpColor = hpPct > 0.6f ? -11141291 : (hpPct > 0.3f ? -171 : -43691);
        guiGraphics.drawText(font, (Text)hpComp, -totalW / 2 + nameW, textY, mulAlpha(hpColor, alpha), true);

        // HP bar
        float barX = bgX + 3.0f;
        float barY = bgY + 9.0f + 5.0f;
        float barW = bgW - 6.0f;
        float barH = 3.0f;
        guiGraphics.fill((int)(barX - 1.0f), (int)(barY - 1.0f), (int)(barX + barW + 1.0f), (int)(barY + barH + 1.0f), mulAlpha(0x90000000, alpha)); // border/track
        guiGraphics.fill((int)barX, (int)barY, (int)(barX + barW), (int)(barY + barH), mulAlpha(0x50202020, alpha));
        float fillW = barW * Math.min(1.0f, health / maxHp);
        if (fillW > 0.0f) {
            guiGraphics.fill((int)barX, (int)barY, (int)(barX + fillW), (int)(barY + barH), mulAlpha(hpColor, alpha));
        }
        if (absorption > 0.0f) {
            float absW = barW * Math.min(1.0f, absorption / maxHp);
            guiGraphics.fill((int)(barX + barW - absW), (int)barY, (int)(barX + barW), (int)(barY + barH), mulAlpha(-9166, alpha));
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
