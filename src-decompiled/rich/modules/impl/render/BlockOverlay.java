package rich.modules.impl.render;

import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;
import rich.util.render.Render3D;

public class BlockOverlay extends ModuleStructure {
   private static final double BOX_EXPAND = 0.005;
   private final ColorSetting color = new ColorSetting("Цвет", "Цвет обводки").setColor(-922746881);
   private final SliderSettings brightness = new SliderSettings("Яркость", "Яркость цвета заливки").range(0.1F, 5.0F).setValue(1.0F);
   private final SliderSettings lineWidth = new SliderSettings("Толщина", "Толщина линии обводки").range(1, 5).setValue(1.0F);
   private final BooleanSetting outline = new BooleanSetting("Обводка", "Показывать обводку блока");
   private final BooleanSetting filled = new BooleanSetting("Заливка", "Заливать блок цветом");
   private final SliderSettings animationSpeed = new SliderSettings("Скорость", "Плавность перемещения бокса").range(1.0F, 30.0F).setValue(10.0F);
   private class_243 smoothPos = null;
   private class_243 targetPos = null;
   private long lastFrameTime = 0L;

   public void applyThemeColor(int var1) {
      this.color.setColor(var1);
   }

   public static BlockOverlay getInstance() {
      return c.a(BlockOverlay.class);
   }

   public BlockOverlay() {
      super("BlockOverlay", "Block Overlay", ModuleCategory.VISUALS);
      this.outline.setValue(true);
      this.filled.setValue(true);
      this.settings(this.color, this.brightness, this.lineWidth, this.outline, this.filled, this.animationSpeed);
   }

   @Override
   public void deactivate() {
      this.smoothPos = null;
      this.targetPos = null;
      this.lastFrameTime = 0L;
      super.deactivate();
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      class_243 var2 = null;
      if (mc.field_1765 instanceof class_3965 var3 && var3.method_17783() == class_240.field_1332) {
         class_2338 var29 = var3.method_17777();
         var2 = new class_243(var29.method_10263(), var29.method_10264(), var29.method_10260());
      }

      if (var2 == null) {
         this.smoothPos = null;
         this.targetPos = null;
         this.lastFrameTime = 0L;
      } else {
         this.targetPos = var2;
         if (this.smoothPos == null) {
            this.smoothPos = this.targetPos;
            this.lastFrameTime = System.nanoTime();
         } else {
            long var28 = System.nanoTime();
            float var5 = Math.min((float)((var28 - this.lastFrameTime) / 1.0E9), 0.1F);
            this.lastFrameTime = var28;
            float var6 = 1.0F - (float)Math.exp(-this.animationSpeed.getValue() * var5);
            this.smoothPos = new class_243(
               this.smoothPos.field_1352 + (this.targetPos.field_1352 - this.smoothPos.field_1352) * var6,
               this.smoothPos.field_1351 + (this.targetPos.field_1351 - this.smoothPos.field_1351) * var6,
               this.smoothPos.field_1350 + (this.targetPos.field_1350 - this.smoothPos.field_1350) * var6
            );
            int var7 = this.color.getColor();
            float var8 = this.brightness.getValue();
            int var9 = Math.min(255, (int)((var7 >> 16 & 0xFF) * var8));
            int var10 = Math.min(255, (int)((var7 >> 8 & 0xFF) * var8));
            int var11 = Math.min(255, (int)((var7 & 0xFF) * var8));
            int var12 = var7 >> 24 & 0xFF;
            int var13 = (int)(var12 * 0.15F * var8) << 24 | var9 << 16 | var10 << 8 | var11;
            float var14 = this.lineWidth.getValue();
            double var15 = this.smoothPos.field_1352 - 0.005;
            double var17 = this.smoothPos.field_1351 - 0.005;
            double var19 = this.smoothPos.field_1350 - 0.005;
            double var21 = this.smoothPos.field_1352 + 1.0 + 0.005;
            double var23 = this.smoothPos.field_1351 + 1.0 + 0.005;
            double var25 = this.smoothPos.field_1350 + 1.0 + 0.005;
            class_238 var27 = new class_238(var15, var17, var19, var21, var23, var25);
            if (this.outline.isValue()) {
               Render3D.drawBox(var27, var7, var14, true, false, false);
            }

            if (this.filled.isValue()) {
               Render3D.drawBox(var27, var13, var14, false, true, false);
            }
         }
      }
   }
}
