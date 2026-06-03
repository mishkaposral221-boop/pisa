package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class InfoBarWidget
extends HudWidget {
    private String coords = "";
    private String bpsStr = "";
    private String ticksStr = "";
    private String playerName = "";
    private double lastX;
    private double lastY;
    private double lastZ;
    private static final int TC = -267521;

    public InfoBarWidget(float x, float y) {
        super(x, y, 10, 14);
    }

    @Override
    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            double dx = mc.player.getX() - this.lastX;
            double dy = mc.player.getY() - this.lastY;
            double dz = mc.player.getZ() - this.lastZ;
            double speed = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0;
            this.bpsStr = String.format("%.1f Bps", speed);
            this.lastX = mc.player.getX();
            this.lastY = mc.player.getY();
            this.lastZ = mc.player.getZ();
            this.coords = String.format("%d %d %d", (int)mc.player.getX(), (int)mc.player.getY(), (int)mc.player.getZ());
            this.playerName = mc.player.getName().getString();
            if (mc.world != null) {
                this.ticksStr = String.format("%.1f Ticks", Float.valueOf(mc.world.getTickManager().getTickRate()));
            }
        }
        TextRenderer f = mc.textRenderer;
        int p = 6;
        int s = 8;
        this.width = p + f.getWidth(this.coords) + s + f.getWidth(this.bpsStr) + s + f.getWidth(this.ticksStr) + s + f.getWidth(this.playerName) + p;
        this.height = 14;
    }

    @Override
    public void renderBackground(float alpha) {
    }

    @Override
    public void renderText(DrawContext g, float alpha) {
        TextRenderer f = MinecraftClient.getInstance().textRenderer;
        int col = InfoBarWidget.applyA(-267521, alpha);
        int ty = (int)this.y + 3;
        int cx = (int)this.x + 6;
        g.drawText(f, this.coords, cx, ty, col, false);
        g.drawText(f, this.bpsStr, cx += f.getWidth(this.coords) + 8, ty, col, false);
        g.drawText(f, this.ticksStr, cx += f.getWidth(this.bpsStr) + 8, ty, col, false);
        g.drawText(f, this.playerName, cx += f.getWidth(this.ticksStr) + 8, ty, col, false);
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }
}

