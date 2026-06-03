package rich.modules.impl.render;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_5498;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.a;
import rich.util.c;
import rich.util.render.Render2D;

public class CustomCrosshair extends ModuleStructure {
   private final SelectSetting preset = new SelectSetting("Шаблон", "Готовые пресеты прицела").value("Legit", "Legit", "Dot", "Wide", "Circle", "Custom");
   private final SelectSetting customType = new SelectSetting("Тип", "Тип кастомного прицела")
      .value("Plus", "Plus", "T", "Circle")
      .visible(() -> this.preset.isSelected("Custom"));
   private final ColorSetting color = new ColorSetting("Цвет", "Цвет прицела").setColor(new Color(255, 255, 255, 255).getRGB());
   private final SliderSettings alpha = new SliderSettings("Прозрачность", "Прозрачность прицела").range(30, 255).setValue(230.0F);
   private final SliderSettings size = new SliderSettings("Длина", "Длина линий прицела")
      .range(2.0F, 14.0F)
      .setValue(6.0F)
      .visible(() -> this.preset.isSelected("Custom"));
   private final SliderSettings gap = new SliderSettings("Отступ", "Отступ от центра")
      .range(0.0F, 8.0F)
      .setValue(3.0F)
      .visible(() -> this.preset.isSelected("Custom"));
   private final SliderSettings thickness = new SliderSettings("Толщина", "Толщина линий")
      .range(1.0F, 4.0F)
      .setValue(1.5F)
      .visible(() -> this.preset.isSelected("Custom"));
   private final BooleanSetting outline = new BooleanSetting("Обводка", "Черная подложка для читаемости").setValue(true);

   public static CustomCrosshair getInstance() {
      return c.a(CustomCrosshair.class);
   }

   public CustomCrosshair() {
      super("CustomCrosshair", "Полностью заменяет ванильный прицел", ModuleCategory.VISUALS);
      this.settings(this.preset, this.customType, this.color, this.alpha, this.size, this.gap, this.thickness, this.outline);
   }

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (this.isState() && mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1755 == null) {
            if (mc.field_1690.method_31044() == class_5498.field_26664) {
               class_332 var2 = var1.getDrawContext();
               float var3 = mc.method_22683().method_4486() / 2.0F;
               float var4 = mc.method_22683().method_4502() / 2.0F;
               CustomCrosshair.CrosshairConfig var5 = this.resolveConfig();
               int var6 = a.h(this.color.getColor(), (int)this.alpha.getValue());
               if (var5.circle) {
                  this.drawCircle(var3, var4, var5.size, var5.thickness, var6, var5.outline);
               } else {
                  this.drawCross(var2, var3, var4, var5, var6, var5.outline);
               }
            }
         }
      }
   }

   private CustomCrosshair.CrosshairConfig resolveConfig() {
      if (this.preset.isSelected("Legit")) {
         return new CustomCrosshair.CrosshairConfig(5.0F, 2.0F, 1.25F, false, true, false);
      }

      if (this.preset.isSelected("Dot")) {
         return new CustomCrosshair.CrosshairConfig(1.5F, 0.0F, 1.5F, false, true, false);
      }

      if (this.preset.isSelected("Wide")) {
         return new CustomCrosshair.CrosshairConfig(8.0F, 4.0F, 1.5F, false, true, false);
      }

      if (this.preset.isSelected("Circle")) {
         return new CustomCrosshair.CrosshairConfig(4.5F, 0.0F, 1.5F, true, true, false);
      }

      boolean var1 = this.customType.isSelected("T");
      boolean var2 = this.customType.isSelected("Circle");
      return new CustomCrosshair.CrosshairConfig(this.size.getValue(), this.gap.getValue(), this.thickness.getValue(), var2, this.outline.isValue(), var1);
   }

   private void drawCross(class_332 var1, float var2, float var3, CustomCrosshair.CrosshairConfig var4, int var5, boolean var6) {
      float var7 = var4.size;
      float var8 = var4.gap;
      float var9 = var4.thickness;
      if (var6) {
         int var10 = -1442840576;
         this.drawLineRect(var2 - var8 - var7, var3 - var9 / 2.0F, var7, var9, var10);
         this.drawLineRect(var2 + var8, var3 - var9 / 2.0F, var7, var9, var10);
         if (!var4.tShape) {
            this.drawLineRect(var2 - var9 / 2.0F, var3 - var8 - var7, var9, var7, var10);
         }

         this.drawLineRect(var2 - var9 / 2.0F, var3 + var8, var9, var7, var10);
      }

      this.drawLineRect(var2 - var8 - var7, var3 - var9 / 2.0F, var7, var9, var5);
      this.drawLineRect(var2 + var8, var3 - var9 / 2.0F, var7, var9, var5);
      if (!var4.tShape) {
         this.drawLineRect(var2 - var9 / 2.0F, var3 - var8 - var7, var9, var7, var5);
      }

      this.drawLineRect(var2 - var9 / 2.0F, var3 + var8, var9, var7, var5);
   }

   private void drawCircle(float var1, float var2, float var3, float var4, int var5, boolean var6) {
      if (var6) {
         Render2D.arc(var1 - var3, var2 - var3, var3 * 2.0F, var4 + 1.0F, 360.0F, 0.0F, -1442840576);
      }

      Render2D.arc(var1 - var3, var2 - var3, var3 * 2.0F, var4, 360.0F, 0.0F, var5);
   }

   private void drawLineRect(float var1, float var2, float var3, float var4, int var5) {
      Render2D.rect(var1, var2, var3, var4, var5, 0.0F);
   }

   public boolean shouldReplaceVanillaCrosshair() {
      return this.isState() && mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null;
   }

   private record CrosshairConfig() {
      private final float size;
      private final float gap;
      private final float thickness;
      private final boolean circle;
      private final boolean outline;
      private final boolean tShape;

      private CrosshairConfig(float var1, float var2, float var3, boolean var4, boolean var5, boolean var6) {
         this.size = var1;
         this.gap = var2;
         this.thickness = var3;
         this.circle = var4;
         this.outline = var5;
         this.tShape = var6;
      }
   }
}
