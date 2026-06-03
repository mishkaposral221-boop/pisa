/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_408
 *  org.lwjgl.glfw.GLFW
 */
package com.example.client.hud;

import com.example.client.hud.HudWidget;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class HudManager {
    private static HudManager INSTANCE;
    private final List<HudWidget> widgets = new ArrayList<HudWidget>();
    private HudWidget dragTarget = null;

    public HudManager() {
        INSTANCE = this;
    }

    public static HudManager getInstance() {
        return INSTANCE;
    }

    public void addWidget(HudWidget w) {
        this.widgets.add(w);
    }

    public void tick() {
        for (HudWidget w : this.widgets) {
            if (!w.visible) continue;
            w.tick();
        }
        this.pollDrag();
    }

    private void pollDrag() {
        boolean lmb;
        class_310 mc = class_310.method_1551();
        if (!(mc.field_1755 instanceof class_408)) {
            if (this.dragTarget != null) {
                this.dragTarget.stopDrag();
                this.dragTarget = null;
            }
            return;
        }
        long window = mc.method_22683().method_4490();
        boolean bl = lmb = GLFW.glfwGetMouseButton((long)window, (int)0) == 1;
        if (this.dragTarget != null) {
            if (lmb) {
                double scale = mc.method_22683().method_4495();
                double[] xArr = new double[1];
                double[] yArr = new double[1];
                GLFW.glfwGetCursorPos((long)window, (double[])xArr, (double[])yArr);
                this.dragTarget.updateDrag(xArr[0] / scale, yArr[0] / scale);
            } else {
                this.dragTarget.stopDrag();
                this.dragTarget = null;
            }
        }
    }

    public void renderBackground(float alpha) {
        for (HudWidget w : this.widgets) {
            if (!w.visible) continue;
            w.renderBackground(alpha);
        }
    }

    public void renderText(class_332 g, float alpha) {
        for (HudWidget w : this.widgets) {
            if (!w.visible) continue;
            w.renderText(g, alpha);
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) {
            return false;
        }
        for (int i = this.widgets.size() - 1; i >= 0; --i) {
            HudWidget w = this.widgets.get(i);
            if (!w.visible || !w.isHovered(mx, my)) continue;
            w.startDrag(mx, my);
            this.dragTarget = w;
            return true;
        }
        return false;
    }
}

