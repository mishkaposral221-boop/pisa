package com.example.client.menu;

import com.example.ExampleMod;
import com.example.client.menu.animation.AnimationController;
import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import com.example.client.menu.layout.MenuColors;
import com.example.client.menu.layout.MenuLayout;
import com.example.client.menu.util.ColorUtil;
import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.minecraft.RectColors;
import com.ferra13671.cometrenderer.minecraft.RenderColor;
import com.ferra13671.cometrenderer.minecraft.batch.impl.ColoredRectBatch;
import com.ferra13671.cometrenderer.minecraft.batch.impl.RoundedRectBatch;
import com.ferra13671.cometrenderer.scissor.ScissorRect;
import net.minecraft.client.MinecraftClient;

public class MenuRenderer {
    private static final int ROUNDED_BATCH_CAPACITY = 512;
    private static final int COLORED_BATCH_CAPACITY = 512;

    private final MenuLayout layout;
    private final AnimationController anim;

    public MenuRenderer(MenuLayout layout, AnimationController anim) {
        this.layout = layout;
        this.anim = anim;
    }

    public int getMenuX() {
        return (MinecraftClient.getInstance().getWindow().getScaledWidth() - this.layout.TOTAL_W()) / 2;
    }

    public int getMenuY() {
        return (MinecraftClient.getInstance().getWindow().getScaledHeight() - this.layout.CH()) / 2;
    }

    public void renderShapes() {
        try {
            this.renderShapesInternal();
        } catch (Exception e) {
            ExampleMod.LOGGER.error("Failed to render menu shapes", e);
        }
    }

    private void renderShapesInternal() {
        this.anim.updateOpenAnimation(true);
        if (!this.anim.shouldRender()) {
            return;
        }

        float alpha = this.anim.getAlpha();
        MinecraftClient client = MinecraftClient.getInstance();
        int baseX = this.getMenuX();
        int baseY = this.getMenuY();
        int cw = this.layout.CW();
        int ch = this.layout.CH();
        int p = this.layout.P();
        int hh = this.layout.HH();
        int ih = this.layout.IH();
        int gap = this.layout.GAP();
        int tw = this.layout.TW();
        int th = this.layout.TH();
        float tr = this.layout.TR();
        int guiScale = client.getWindow().getScaleFactor();

        RoundedRectBatch rounded = new RoundedRectBatch(ROUNDED_BATCH_CAPACITY);
        ColoredRectBatch colored = new ColoredRectBatch(COLORED_BATCH_CAPACITY);
        try {
            for (int col = 0; col < MenuData.CATEGORIES.length; ++col) {
                int colX = baseX + col * (cw + gap);
                Category cat = MenuData.CATEGORIES[col];
                RenderColor colTop = ColorUtil.mulAlpha(MenuColors.getColumnTop(), alpha);
                RenderColor colBot = ColorUtil.mulAlpha(MenuColors.getColumnBottom(), alpha);
                rounded.rectSized((float) colX, (float) baseY, (float) cw, (float) ch, this.layout.R(),
                        RectColors.verticalGradient(colTop, colBot));

                RenderColor sepColor = ColorUtil.mulAlpha(MenuColors.getHeaderSeparator(), alpha);
                colored.rectSized((float) colX, (float) (baseY + hh), (float) cw, 4.0f * this.layout.menuScale,
                        RectColors.oneColor(sepColor));

                int scrollY = (int) this.layout.scrollOffset[col];
                int yOff = 0;
                int totalContentH = 0;
                for (ModModule m : cat.modules) {
                    totalContentH += ih + (int) ((float) this.layout.expandH(m.settings.length) * m.expandAnim);
                }
                this.layout.clampScroll(col, totalContentH);
                scrollY = (int) this.layout.scrollOffset[col];
                int bodyTop = (int) ((float) (baseY + hh) + 4.0f * this.layout.menuScale);
                int scissorH = (int) (((float) (ch - hh) - 4.0f * this.layout.menuScale) * (float) guiScale);

                CometRenderer.getScissorStack().push(
                        new ScissorRect(colX * guiScale, bodyTop * guiScale, cw * guiScale, scissorH));
                try {
                    for (int i = 0; i < cat.modules.length; ++i) {
                        ModModule mod = cat.modules[i];
                        int itemH = ih + (int) ((float) this.layout.expandH(mod.settings.length) * mod.expandAnim);
                        int itemY = bodyTop + yOff + scrollY;
                        if (itemY + itemH < bodyTop || itemY > baseY + ch) {
                            yOff += itemH;
                            continue;
                        }
                        if (i > 0) {
                            colored.rectSized((float) colX, (float) itemY, (float) cw, this.layout.SEP(),
                                    RectColors.oneColor(ColorUtil.mulAlpha(MenuColors.SEPARATOR, alpha)));
                        }
                        if (mod.expandAnim > 0.01f) {
                            int expH = (int) ((float) this.layout.expandH(mod.settings.length) * mod.expandAnim);
                            float setAlpha = alpha * mod.expandAnim;
                            if (mod.expandAnim > 0.05f) {
                                int rowH = (int) (18.0f * this.layout.menuScale);
                                int sliderW = cw - p * 2;
                                int sliderH = Math.max(2, (int) (2.0f * this.layout.menuScale));
                                for (int si = 0; si < mod.settings.length; ++si) {
                                    Setting set = mod.settings[si];
                                    int setY = itemY + ih + (int) (3.0f * this.layout.menuScale) + si * rowH;
                                    if (setY + rowH > itemY + ih + expH) {
                                        break;
                                    }
                                    int setX = colX + p;
                                    if (si > 0) {
                                        colored.rectSized((float) colX, (float) setY, (float) cw, this.layout.SEP(),
                                                RectColors.oneColor(ColorUtil.mulAlpha(MenuColors.SEPARATOR, setAlpha)));
                                    }
                                    if (set.type == Setting.Type.SLIDER) {
                                        int trackY = setY + rowH - (int) (5.0f * this.layout.menuScale);
                                        rounded.rectSized((float) setX, (float) trackY, (float) sliderW, (float) sliderH,
                                                (float) sliderH / 2.0f,
                                                RectColors.oneColor(ColorUtil.mulAlpha(MenuColors.getSliderTrack(), setAlpha)));
                                        int filledW = Math.max(1, (int) ((double) sliderW * set.getNormalized()));
                                        rounded.rectSized((float) setX, (float) trackY, (float) filledW, (float) sliderH,
                                                (float) sliderH / 2.0f,
                                                RectColors.oneColor(ColorUtil.mulAlpha(MenuColors.getSliderFill(), setAlpha)));
                                        int thumbR = Math.max(3, (int) (3.0f * this.layout.menuScale));
                                        int thumbCX = setX + filledW;
                                        int thumbCY = trackY + sliderH / 2;
                                        rounded.rectSized((float) (thumbCX - thumbR), (float) (thumbCY - thumbR),
                                                (float) (thumbR * 2), (float) (thumbR * 2), (float) thumbR,
                                                RectColors.oneColor(ColorUtil.mulAlpha(MenuColors.getSliderThumb(), setAlpha)));
                                    } else if (set.type == Setting.Type.TOGGLE) {
                                        int tglW = tw;
                                        int tglH = th;
                                        int toggleSX = setX + sliderW - tglW;
                                        int toggleSY = setY + (rowH - tglH) / 2;
                                        float tglGlow = set.boolValue ? 1.0f : 0.0f;
                                        RenderColor tOffBg = ColorUtil.mulAlpha(MenuColors.getToggleOffBg(), setAlpha);
                                        RenderColor tOnBg = ColorUtil.mulAlpha(MenuColors.getToggleOnBg(), setAlpha);
                                        RenderColor tglBg = ColorUtil.blendRenderColor(tOffBg, tOnBg, tglGlow);
                                        rounded.rectSized((float) toggleSX, (float) toggleSY, (float) tglW, (float) tglH, tr,
                                                RectColors.oneColor(tglBg));
                                        int cD = Math.max(2, tglH - 2);
                                        int cR = cD / 2;
                                        int cTravel = tglW - cD - 2;
                                        int cX2 = toggleSX + 1 + (int) ((float) cTravel * tglGlow);
                                        int cY2 = toggleSY + 1;
                                        rounded.rectSized((float) cX2, (float) cY2, (float) cD, (float) cD, (float) cR,
                                                RectColors.oneColor(ColorUtil.mulAlpha(MenuColors.getToggleOnCircle(), setAlpha)));
                                    }
                                }
                            }
                        }
                        yOff += itemH;
                    }
                } finally {
                    CometRenderer.getScissorStack().pop();
                }
            }

            CometBatchUtil.flush(rounded);
            CometBatchUtil.flush(colored);
        } finally {
            CometBatchUtil.discard(rounded);
            CometBatchUtil.discard(colored);
        }
    }
}
