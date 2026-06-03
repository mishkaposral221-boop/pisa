package rich.util.interfaces;

import java.awt.Color;
import rich.modules.module.setting.Setting;

public abstract class AbstractSettingComponent extends AbstractComponent {
   private final Setting setting;
   protected float alphaMultiplier = 1.0F;

   public void setAlphaMultiplier(float var1) {
      this.alphaMultiplier = var1;
   }

   protected int applyAlpha(int var1, float var2) {
      int var3 = var1 >> 24 & 0xFF;
      int var4 = var1 >> 16 & 0xFF;
      int var5 = var1 >> 8 & 0xFF;
      int var6 = var1 & 0xFF;
      int var7 = Math.max(0, Math.min(255, (int)(var3 * this.alphaMultiplier * var2)));
      return var7 << 24 | var4 << 16 | var5 << 8 | var6;
   }

   protected int applyAlpha(int var1) {
      return this.applyAlpha(var1, 1.0F);
   }

   protected Color applyAlpha(Color var1) {
      int var2 = Math.max(0, Math.min(255, (int)(var1.getAlpha() * this.alphaMultiplier)));
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var2);
   }

   protected Color applyAlpha(Color var1, float var2) {
      int var3 = Math.max(0, Math.min(255, (int)(var1.getAlpha() * this.alphaMultiplier * var2)));
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var3);
   }

   public Setting getSetting() {
      return this.setting;
   }

   public float getAlphaMultiplier() {
      return this.alphaMultiplier;
   }

   public AbstractSettingComponent(Setting var1) {
      this.setting = var1;
   }
}
