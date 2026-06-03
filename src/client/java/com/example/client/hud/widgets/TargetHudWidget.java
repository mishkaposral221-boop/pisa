package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.entity.player.SkinTextures;

public class TargetHudWidget
extends HudWidget {
    private LivingEntity target = null;
    private int showTicks = 0;
    private static final int FADE_TICKS = 60;

    public TargetHudWidget(float x, float y) {
        super(x, y, 120, 42);
    }

    public void setTarget(LivingEntity entity) {
        this.target = entity;
        this.showTicks = 60;
    }

    @Override
    public void tick() {
        Entity Entity2;
        if (this.showTicks > 0) {
            --this.showTicks;
        }
        if (this.showTicks <= 0) {
            this.target = null;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && (Entity2 = mc.targetedEntity) instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)Entity2;
            if (mc.player.getAttackCooldownProgress(0.0f) < 0.9f && living != mc.player) {
                this.setTarget(living);
            }
        }
        if (this.target != null && (!this.target.isAlive() || this.target.isRemoved())) {
            this.target = null;
            this.showTicks = 0;
        }
        this.visible = this.target != null && this.showTicks > 0;
    }

    @Override
    public void renderBackground(float alpha) {
    }

    @Override
    public void renderText(DrawContext g, float alpha) {
        Object name;
        LivingEntity LivingEntity2;
        float absorb;
        int b;
        int gr;
        int r;
        if (this.target == null) {
            return;
        }
        float a = alpha * Math.min(1.0f, (float)this.showTicks / 10.0f);
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int ix = (int)this.x;
        int iy = (int)this.y;
        int iAlpha = (int)(180.0f * a);
        g.fill(ix, iy, ix + this.width, iy + this.height, iAlpha << 24 | 0x140A28);
        int barX = ix + 4;
        int barY = iy + this.height - 8;
        int barW = this.width - 8;
        int barH = 4;
        g.fill(barX, barY, barX + barW, barY + barH, (int)(180.0f * a) << 24 | 0x14141E);
        float hp = this.target.getHealth();
        float maxHp = this.target.getMaxHealth();
        float pct = Math.min(1.0f, hp / maxHp);
        int fillW = (int)((float)barW * pct);
        if (pct > 0.5f) {
            r = (int)(255.0f * (1.0f - pct) * 2.0f);
            gr = 255;
            b = 50;
        } else if (pct > 0.25f) {
            r = 255;
            gr = (int)(255.0f * (pct - 0.25f) * 4.0f);
            b = 50;
        } else {
            r = 255;
            gr = 50;
            b = 50;
        }
        if (fillW > 0) {
            g.fill(barX, barY, barX + fillW, barY + barH, (int)(220.0f * a) << 24 | r << 16 | gr << 8 | b);
        }
        if ((absorb = this.target.getAbsorptionAmount()) > 0.0f) {
            int absW = (int)((float)barW * Math.min(1.0f, absorb / maxHp));
            g.fill(barX, barY - 2, barX + absW, barY, (int)(180.0f * a) << 24 | 0xFFDC32);
        }
        if ((LivingEntity2 = this.target) instanceof PlayerEntity) {
            PlayerListEntry info;
            PlayerEntity player = (PlayerEntity)LivingEntity2;
            MinecraftClient mc = MinecraftClient.getInstance();
            PlayerListEntry ServerSamplerSource = info = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) : null;
            if (info != null) {
                PlayerSkinDrawer.draw((DrawContext)g, (SkinTextures)info.getSkinTextures(), (int)(ix + 4), (int)(iy + 4), (int)19);
            }
        }
        if (((String)(name = this.target.getName().getString())).length() > 14) {
            name = ((String)name).substring(0, 14) + "..";
        }
        g.drawText(font, (String)name, ix + 26, iy + 3, TargetHudWidget.applyA(-1, a), true);
        String hpText = String.format("%.1f", Float.valueOf(this.target.getHealth())) + " HP";
        int hpColor = this.target.getHealth() > this.target.getMaxHealth() * 0.5f ? -11141291 : -43691;
        g.drawText(font, hpText, ix + this.width - 4 - font.getWidth(hpText), iy + 3, TargetHudWidget.applyA(hpColor, a), true);
        LivingEntity LivingEntity3 = this.target;
        if (LivingEntity3 instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)LivingEntity3;
            ItemStack[] armor = new ItemStack[]{player.getEquippedStack(EquipmentSlot.HEAD), player.getEquippedStack(EquipmentSlot.CHEST), player.getEquippedStack(EquipmentSlot.LEGS), player.getEquippedStack(EquipmentSlot.FEET), player.getEquippedStack(EquipmentSlot.MAINHAND), player.getEquippedStack(EquipmentSlot.OFFHAND)};
            int ax = ix + 4;
            int ay = iy + 16;
            for (ItemStack stack : armor) {
                if (stack.isEmpty()) continue;
                g.drawItem(stack, ax, ay);
                ax += 18;
            }
        }
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }
}

