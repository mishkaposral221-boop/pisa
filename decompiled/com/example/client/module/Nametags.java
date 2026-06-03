/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11719
 *  net.minecraft.class_11719$class_11721
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_5250
 *  net.minecraft.class_5348
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector4f
 */
package com.example.client.module;

import com.example.client.menu.data.ModModule;
import java.util.ArrayList;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11719;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_5250;
import net.minecraft.class_5348;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class Nametags {
    private ModModule moduleRef;
    private static Nametags INSTANCE;
    private static final class_2960 GEOLOGICA_FONT_ID;
    private static final class_2583 GEOLOGICA_STYLE;

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

    private static float[] worldToScreen(double wx, double wy, double wz, class_243 camPos, Matrix4f viewMatrix, Matrix4f projMatrix, int sw, int sh) {
        float dx = (float)(wx - camPos.field_1352);
        float dy = (float)(wy - camPos.field_1351);
        float dz = (float)(wz - camPos.field_1350);
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

    public void render(class_332 guiGraphics, float partialTick) {
        try {
            this.renderInternal(guiGraphics, partialTick);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void renderInternal(class_332 guiGraphics, float partialTick) {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null || client.field_1724 == null || !this.isEnabled()) {
            return;
        }
        Quaternionf quat = new Quaternionf((Quaternionfc)client.field_1773.method_19418().method_23767()).conjugate();
        Matrix4f viewMatrix = new Matrix4f().rotation((Quaternionfc)quat);
        float fov = ((Integer)client.field_1690.method_41808().method_41753()).intValue();
        Matrix4f projMatrix = client.field_1773.method_22973(fov);
        int sw = client.method_22683().method_4486();
        int sh = client.method_22683().method_4502();
        class_243 camPos = client.field_1773.method_19418().method_71156();
        class_327 font = client.field_1772;
        for (class_1297 entity : client.field_1687.method_18112()) {
            if (entity == client.field_1724 || entity.method_7325() || !(entity instanceof class_1657)) continue;
            class_1657 target = (class_1657)entity;
            double dist = client.field_1724.method_5858(entity);
            if (dist > 4096.0) continue;
            double lerpX = entity.field_6038 + (entity.method_23317() - entity.field_6038) * (double)partialTick;
            double lerpY = entity.field_5971 + (entity.method_23318() - entity.field_5971) * (double)partialTick;
            double lerpZ = entity.field_5989 + (entity.method_23321() - entity.field_5989) * (double)partialTick;
            double headY = lerpY + (double)entity.method_17682() + 0.5;
            float[] screen = Nametags.worldToScreen(lerpX, headY, lerpZ, camPos, viewMatrix, projMatrix, sw, sh);
            if (screen == null) continue;
            float screenX = Math.round(screen[0]);
            float screenY = Math.round(screen[1]);
            float distance = (float)Math.sqrt(dist);
            float scale = Math.max(0.5f, Math.min(1.0f, 1.0f - distance / 20.0f));
            this.renderNametag(guiGraphics, font, target, screenX, screenY, scale, distance);
        }
    }

    private void renderNametag(class_332 guiGraphics, class_327 font, class_1657 player, float cx, float cy, float scale, float distance) {
        float health = player.method_6032();
        float absorption = player.method_6067();
        float maxHp = Math.max(1.0f, player.method_6063());
        float totalHealth = health + absorption;
        String name = player.method_5477().getString();
        String hpStr = String.format("%.0f", Float.valueOf(totalHealth));
        class_5250 nameComp = class_2561.method_43470((String)name).method_27696(GEOLOGICA_STYLE);
        class_5250 hpComp = class_2561.method_43470((String)(" " + hpStr)).method_27696(GEOLOGICA_STYLE);
        int nameW = font.method_27525((class_5348)nameComp);
        int hpW = font.method_27525((class_5348)hpComp);
        int totalW = nameW + hpW;
        float bgPad = 5.0f;
        float bgW = Math.max((float)totalW + bgPad * 2.0f, 60.0f);
        Objects.requireNonNull(font);
        float bgH = 9 + 10;
        float bgX = -bgW / 2.0f;
        float bgY = 0.0f;
        guiGraphics.method_51448().pushMatrix();
        guiGraphics.method_51448().translate(cx, cy);
        guiGraphics.method_51448().scale(scale, scale);
        guiGraphics.method_25294((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + bgH), -1879048192);
        guiGraphics.method_25294((int)bgX, (int)bgY, (int)(bgX + bgW), (int)(bgY + 1.0f), 1085564159);
        int textY = (int)(bgY + 3.0f);
        guiGraphics.method_51439(font, (class_2561)nameComp, -totalW / 2, textY, -1, true);
        float hpPct = Math.min(1.0f, health / maxHp);
        int hpColor = hpPct > 0.6f ? -11141291 : (hpPct > 0.3f ? -171 : -43691);
        guiGraphics.method_51439(font, (class_2561)hpComp, -totalW / 2 + nameW, textY, hpColor, true);
        float barX = bgX + 3.0f;
        Objects.requireNonNull(font);
        float barY = bgY + 9.0f + 5.0f;
        float barW = bgW - 6.0f;
        float barH = 2.5f;
        guiGraphics.method_25294((int)barX, (int)barY, (int)(barX + barW), (int)(barY + barH), 0x50000000);
        float fillW = barW * Math.min(1.0f, health / maxHp);
        if (fillW > 0.0f) {
            int barColor = hpPct > 0.6f ? -11141291 : (hpPct > 0.3f ? -171 : -43691);
            guiGraphics.method_25294((int)barX, (int)barY, (int)(barX + fillW), (int)(barY + barH), barColor);
        }
        if (absorption > 0.0f) {
            float absW = barW * Math.min(1.0f, absorption / maxHp);
            guiGraphics.method_25294((int)(barX + barW - absW), (int)barY, (int)(barX + barW), (int)(barY + barH), -9166);
        }
        if (this.showArmor()) {
            ArrayList<class_1799> items = new ArrayList<class_1799>();
            class_1799 helmet = player.method_6118(class_1304.field_6169);
            class_1799 chest = player.method_6118(class_1304.field_6174);
            class_1799 legs = player.method_6118(class_1304.field_6172);
            class_1799 boots = player.method_6118(class_1304.field_6166);
            class_1799 mainHand = player.method_6118(class_1304.field_6173);
            class_1799 offHand = player.method_6118(class_1304.field_6171);
            if (!helmet.method_7960()) {
                items.add(helmet);
            }
            if (!chest.method_7960()) {
                items.add(chest);
            }
            if (!legs.method_7960()) {
                items.add(legs);
            }
            if (!boots.method_7960()) {
                items.add(boots);
            }
            if (!mainHand.method_7960()) {
                items.add(mainHand);
            }
            if (!offHand.method_7960()) {
                items.add(offHand);
            }
            if (!items.isEmpty()) {
                int count = items.size();
                float totalItemW = (float)(count - 1) * 18.0f + 16.0f;
                float startX = -totalItemW / 2.0f;
                for (int i = 0; i < count; ++i) {
                    int ix = (int)(startX + (float)(i * 18));
                    guiGraphics.method_51427((class_1799)items.get(i), ix, -16);
                }
            }
        }
        guiGraphics.method_51448().popMatrix();
    }

    static {
        GEOLOGICA_FONT_ID = class_2960.method_60655((String)"modid", (String)"geologica");
        GEOLOGICA_STYLE = class_2583.field_24360.method_27704((class_11719)new class_11719.class_11721(GEOLOGICA_FONT_ID));
    }
}

