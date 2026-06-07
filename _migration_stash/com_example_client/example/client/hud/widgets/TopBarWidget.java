package com.example.client.hud.widgets;

import com.example.client.hud.HudWidget;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

public class TopBarWidget
extends HudWidget {
    private String fpsStr = "";
    private String pingStr = "";
    private String time = "";
    private static final int TC = -267521;
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    public TopBarWidget(float x, float y) {
        super(x, y, 10, 14);
    }

    @Override
    public void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        this.fpsStr = mc.getCurrentFps() + " Fps";
        ClientPlayNetworkHandler net = mc.getNetworkHandler();
        if (net != null && mc.player != null) {
            PlayerListEntry info = net.getPlayerListEntry(mc.player.getUuid());
            this.pingStr = (info != null ? info.getLatency() : 0) + " Ping";
        }
        this.time = LocalTime.now().format(TF);
        TextRenderer f = mc.textRenderer;
        int pad = 6;
        int sep = 8;
        this.width = pad + f.getWidth("Luminas") + sep + f.getWidth(this.fpsStr) + sep + f.getWidth(this.pingStr) + sep + f.getWidth(this.time) + pad;
        this.height = 14;
    }

    @Override
    public void renderBackground(float alpha) {
    }

    @Override
    public void renderText(DrawContext g, float alpha) {
        TextRenderer f = MinecraftClient.getInstance().textRenderer;
        int ix = (int)this.x;
        int iy = (int)this.y;
        int col = TopBarWidget.applyA(-267521, alpha);
        int pad = 6;
        int sep = 8;
        int ty = iy + 3;
        int cx = ix + pad;
        g.drawText(f, "Luminas", cx, ty, TopBarWidget.applyA(-38401, alpha), false);
        g.drawText(f, this.fpsStr, cx += f.getWidth("Luminas") + sep, ty, col, false);
        g.drawText(f, this.pingStr, cx += f.getWidth(this.fpsStr) + sep, ty, col, false);
        g.drawText(f, this.time, cx += f.getWidth(this.pingStr) + sep, ty, col, false);
    }

    private static int applyA(int c, float a) {
        return (int)((float)(c >> 24 & 0xFF) * a) << 24 | c & 0xFFFFFF;
    }
}

