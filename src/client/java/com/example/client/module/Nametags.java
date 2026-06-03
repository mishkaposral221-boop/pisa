package com.example.client.module;

import com.example.client.menu.data.ModModule;
import java.util.ArrayList;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector4f;

public class Nametags {
    private ModModule moduleRef;
    private static Nametags INSTANCE;
    private static final Identifier GEOLOGICA_FONT_ID;
    private static final Style GEOLOGICA_STYLE;

    public Nametags() {
        INSTANCE = this;
    }

    public static Nametags getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public boolean showArmor() {
        if (this.moduleRef == null || this.moduleRef.settings.length < 1) {
            return true;
        }
        return this.moduleRef.settings[0].boolValue;
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

    public void render(DrawContext guiGraphics, float partialTick) {
        try {
            this.renderInternal(guiGraphics, partialTick);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void renderInternal(DrawContext guiGraphics, float partialTick) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || !this.isEnabled()) {
            return;
        }
        Quaternionf quat = new Quaternionf((Quaternionfc)client.gameRenderer.getCamera().getRotation()).conjugate();
        Matrix4f viewMatrix = new Matrix4f().rotation((Quaternionfc)quat);
        float fov = ((Integer)client.options.getFov().getValue()).intValue();
        Matrix4f projMatrix = client.gameRenderer.getBasicProjectionMatrix(fov);
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        Vec3d camPos = client.gameRenderer.getCamera().getCameraPos();
        TextRenderer font = client.textRenderer;
        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player || entity.isSpectator() || !(entity instanceof PlayerEntity)) continue;
            PlayerEntity target = (PlayerEntity)entity;
            double dist = client.player.squaredDistanceTo(entity);
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
        MutableText nameComp = Text.literal((String)name).fillStyle(GEOLOGICA_STYLE);
        MutableText hpComp = Text.literal((String)(" " + hpStr)).fillStyle(GEOLOGICA_STYLE);
        int nameW = font.getWidth((StringVisitable)nameComp);
        int hpW = font.getWidth((StringVisitable)hpComp);
        int totalW = nameW + hpW;
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
            if (!helmet.isEmpty()) {
                items.add(helmet);
            }
            if (!chest.isEmpty()) {
                items.add(chest);
            }
            if (!legs.isEmpty()) {
                items.add(legs);
            }
            if (!boots.isEmpty()) {
                items.add(boots);
            }
            if (!mainHand.isEmpty()) {
                items.add(mainHand);
            }
            if (!offHand.isEmpty()) {
                items.add(offHand);
            }
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

    static {
        GEOLOGICA_FONT_ID = Identifier.of((String)"modid", (String)"geologica");
        GEOLOGICA_STYLE = Style.EMPTY.withFont(new StyleSpriteSource.Font(GEOLOGICA_FONT_ID));
    }
}

