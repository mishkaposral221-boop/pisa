package rich.modules.impl.render;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
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
   private Vec3d smoothPos = null;
   private Vec3d targetPos = null;
   private long lastFrameTime = 0L;

   public void applyThemeColor(int var1) {
      this.color.setColor(var1);
   }

   public static BlockOverlay getInstance() {
      return c.keyCodec(BlockOverlay.class);
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
      Vec3d var2 = null;
      if (mc.crosshairTarget instanceof BlockHitResult var3 && var3.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
         BlockPos var29 = var3.getBlockPos();
         var2 = new Vec3d(var29.getX(), var29.getY(), var29.getZ());
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
            this.smoothPos = new Vec3d(
               this.smoothPos.x + (this.targetPos.x - this.smoothPos.x) * var6,
               this.smoothPos.y + (this.targetPos.y - this.smoothPos.y) * var6,
               this.smoothPos.z + (this.targetPos.z - this.smoothPos.z) * var6
            );
            int var7 = this.color.getColor();
            float var8 = this.brightness.getValue();
            int var9 = Math.min(255, (int)((var7 >> 16 & 0xFF) * var8));
            int var10 = Math.min(255, (int)((var7 >> 8 & 0xFF) * var8));
            int var11 = Math.min(255, (int)((var7 & 0xFF) * var8));
            int var12 = var7 >> 24 & 0xFF;
            int var13 = (int)(var12 * 0.15F * var8) << 24 | var9 << 16 | var10 << 8 | var11;
            float var14 = this.lineWidth.getValue();
            double var15 = this.smoothPos.x - 0.005;
            double var17 = this.smoothPos.y - 0.005;
            double var19 = this.smoothPos.z - 0.005;
            double var21 = this.smoothPos.x + 1.0 + 0.005;
            double var23 = this.smoothPos.y + 1.0 + 0.005;
            double var25 = this.smoothPos.z + 1.0 + 0.005;
            Box var27 = new Box(var15, var17, var19, var21, var23, var25);
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
