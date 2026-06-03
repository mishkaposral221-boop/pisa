package rich.modules.module.setting.implement;

import java.awt.Color;
import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class ColorSetting extends Setting {
   private float hue = 0.0F;
   private float saturation = 1.0F;
   private float brightness = 1.0F;
   private float alpha = 1.0F;
   private int[] presets = new int[0];

   public ColorSetting(String var1, String var2) {
      super(var1, var2);
   }

   public ColorSetting value(int var1) {
      this.setColor(var1);
      return this;
   }

   public ColorSetting presets(int... var1) {
      this.presets = var1;
      return this;
   }

   public ColorSetting visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public int getColor() {
      int var1 = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
      int var2 = Math.round(this.alpha * 255.0F);
      return var2 << 24 | var1 & 16777215;
   }

   public int getColorWithAlpha() {
      return this.getColor();
   }

   public int getColorNoAlpha() {
      return Color.HSBtoRGB(this.hue, this.saturation, this.brightness) | 0xFF000000;
   }

   public ColorSetting setColor(int var1) {
      int var2 = var1 >> 16 & 0xFF;
      int var3 = var1 >> 8 & 0xFF;
      int var4 = var1 & 0xFF;
      int var5 = var1 >> 24 & 0xFF;
      float[] var6 = Color.RGBtoHSB(var2, var3, var4, null);
      this.hue = var6[0];
      this.saturation = var6[1];
      this.brightness = var6[2];
      this.alpha = var5 / 255.0F;
      return this;
   }

   public Color getAwtColor() {
      int var1 = this.getColor();
      return new Color(var1, true);
   }

   public ColorSetting setHue(float var1) {
      this.hue = Math.max(0.0F, Math.min(1.0F, var1));
      return this;
   }

   public ColorSetting setSaturation(float var1) {
      this.saturation = Math.max(0.0F, Math.min(1.0F, var1));
      return this;
   }

   public ColorSetting setBrightness(float var1) {
      this.brightness = Math.max(0.0F, Math.min(1.0F, var1));
      return this;
   }

   public ColorSetting setAlpha(float var1) {
      this.alpha = Math.max(0.0F, Math.min(1.0F, var1));
      return this;
   }

   public float getHue() {
      return this.hue;
   }

   public float getSaturation() {
      return this.saturation;
   }

   public float getBrightness() {
      return this.brightness;
   }

   public float getAlpha() {
      return this.alpha;
   }

   public int[] getPresets() {
      return this.presets;
   }

   public void setPresets(int[] var1) {
      this.presets = var1;
   }
}
