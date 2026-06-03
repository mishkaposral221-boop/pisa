package rich.modules.impl.render;

import net.minecraft.class_238;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldLoadEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.a;
import rich.util.c;
import rich.util.render.Render3D;

public class GhostTrail extends ModuleStructure {
   public final ColorSetting color = new ColorSetting("Цвет", "Цвет призрака").value(1617212927);
   private final SliderSettings fadeDuration = new SliderSettings("Скорость затухания", "Как долго призрак затухает (мс)")
      .range(300.0F, 2000.0F)
      .setValue(1000.0F);
   private final BooleanSetting fillSetting = new BooleanSetting("Заполнение", "Заполнять части тела").setValue(false);
   private GhostTrail.Ghost ghost = null;
   private boolean wasInAir = false;

   public static GhostTrail getInstance() {
      return c.a(GhostTrail.class);
   }

   public GhostTrail() {
      super("Ghost Trail", "Оставляет призрачные копии позади игрока", ModuleCategory.VISUALS);
      this.settings(this.color, this.fadeDuration, this.fillSetting);
   }

   @Override
   public void deactivate() {
      this.ghost = null;
      this.wasInAir = false;
      super.deactivate();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent var1) {
      this.ghost = null;
      this.wasInAir = false;
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null) {
         long var2 = System.currentTimeMillis();
         if (this.ghost != null && (float)(var2 - this.ghost.spawnTime()) >= this.ghost.fadeDurationMs()) {
            this.ghost = null;
         }

         boolean var4 = !mc.field_1724.method_24828();
         if (var4 && !this.wasInAir) {
            float var5 = mc.field_1724.field_42108.method_48566();
            float var6 = mc.field_1724.field_42108.method_48572(1.0F) * (float) (Math.PI * 2);
            float var7 = (float)(Math.sin(var6) * 1.4F * var5);
            float var8 = (float)(-Math.sin(var6) * 1.4F * var5);
            float var9 = (float)(-Math.sin(var6) * 1.4F * var5 * 0.5) - (float)(Math.sin(mc.field_1724.field_6251 * Math.PI) * 1.2F);
            float var10 = (float)(Math.sin(var6) * 1.4F * var5 * 0.5);
            this.ghost = new GhostTrail.Ghost(
               mc.field_1724.method_23317(),
               mc.field_1724.method_23318(),
               mc.field_1724.method_23321(),
               mc.field_1724.field_6283,
               var8,
               var7,
               var10,
               var9,
               (float)Math.toRadians(mc.field_1724.method_36455()),
               (float)Math.toRadians(mc.field_1724.method_5791() - mc.field_1724.field_6283),
               var2,
               this.fadeDuration.getValue()
            );
         }

         this.wasInAir = var4;
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      if (mc.field_1724 != null && this.ghost != null) {
         long var2 = System.currentTimeMillis();
         float var4 = (float)(var2 - this.ghost.spawnTime()) / this.ghost.fadeDurationMs();
         float var5 = 1.0F - var4 * var4;
         if (!(var5 <= 0.01F)) {
            int var6 = a.a(this.color.getColor(), var5);
            int var7 = this.fillSetting.isValue() ? a.a(var6, 0.25F) : 0;
            this.renderGhost(this.ghost, var6, var7);
         }
      }
   }

   private void renderGhost(GhostTrail.Ghost var1, int var2, int var3) {
      boolean var4 = this.fillSetting.isValue();
      double var5 = var1.x();
      double var7 = var1.y();
      double var9 = var1.z();
      double var11 = Math.sin(Math.toRadians(var1.yaw()));
      double var13 = Math.cos(Math.toRadians(var1.yaw()));
      this.box(var5, var7, var9, var11, var13, -0.25, 0.75, -0.125, 0.25, 1.5, 0.125, 0.0F, 0.0F, 0.75, var2, var3, var4);
      this.box(var5, var7, var9, var11, var13, -0.25, 1.5, -0.25, 0.25, 2.0, 0.25, var1.headPitch(), var1.headYaw(), 1.5, var2, var3, var4);
      this.limb(var5, var7, var9, var11, var13, -0.25, 0.0, -0.125, 0.0, 0.75, 0.125, -0.125, 0.75, var1.lLeg(), var2, var3, var4);
      this.limb(var5, var7, var9, var11, var13, 0.0, 0.0, -0.125, 0.25, 0.75, 0.125, 0.125, 0.75, var1.rLeg(), var2, var3, var4);
      this.limb(var5, var7, var9, var11, var13, -0.5, 0.75, -0.125, -0.25, 1.5, 0.125, -0.375, 1.5, var1.lArm(), var2, var3, var4);
      this.limb(var5, var7, var9, var11, var13, 0.25, 0.75, -0.125, 0.5, 1.5, 0.125, 0.375, 1.5, var1.rArm(), var2, var3, var4);
   }

   private void limb(
      double var1,
      double var3,
      double var5,
      double var7,
      double var9,
      double var11,
      double var13,
      double var15,
      double var17,
      double var19,
      double var21,
      double var23,
      double var25,
      float var27,
      int var28,
      int var29,
      boolean var30
   ) {
      if (Math.abs(var27) < 0.001F) {
         this.box(var1, var3, var5, var7, var9, var11, var13, var15, var17, var19, var21, 0.0F, 0.0F, 0.0, var28, var29, var30);
      } else {
         double var31 = Math.sin(var27);
         double var33 = Math.cos(var27);
         double var35 = 1.0E9;
         double var37 = 1.0E9;
         double var39 = 1.0E9;
         double var41 = -1.0E9;
         double var43 = -1.0E9;
         double var45 = -1.0E9;

         for (int var47 = 0; var47 < 8; var47++) {
            double var48 = (var47 & 1) == 0 ? var11 : var17;
            double var50 = (var47 & 2) == 0 ? var13 : var19;
            double var52 = (var47 & 4) == 0 ? var15 : var21;
            double var54 = var50 - var25;
            double var56 = var54 * var33 - var52 * var31 + var25;
            double var58 = var54 * var31 + var52 * var33;
            if (var48 < var35) {
               var35 = var48;
            }

            if (var48 > var41) {
               var41 = var48;
            }

            if (var56 < var37) {
               var37 = var56;
            }

            if (var56 > var43) {
               var43 = var56;
            }

            if (var58 < var39) {
               var39 = var58;
            }

            if (var58 > var45) {
               var45 = var58;
            }
         }

         this.box(var1, var3, var5, var7, var9, var35, var37, var39, var41, var43, var45, 0.0F, 0.0F, 0.0, var28, var29, var30);
      }
   }

   private void box(
      double var1,
      double var3,
      double var5,
      double var7,
      double var9,
      double var11,
      double var13,
      double var15,
      double var17,
      double var19,
      double var21,
      float var23,
      float var24,
      double var25,
      int var27,
      int var28,
      boolean var29
   ) {
      double var30 = Math.sin(var23);
      double var32 = Math.cos(var23);
      double var34 = Math.sin(var24);
      double var36 = Math.cos(var24);
      double var38 = 1.0E9;
      double var40 = 1.0E9;
      double var42 = 1.0E9;
      double var44 = -1.0E9;
      double var46 = -1.0E9;
      double var48 = -1.0E9;

      for (int var50 = 0; var50 < 8; var50++) {
         double var51 = (var50 & 1) == 0 ? var11 : var17;
         double var53 = (var50 & 2) == 0 ? var13 : var19;
         double var55 = (var50 & 4) == 0 ? var15 : var21;
         double var57 = var53 - var25;
         double var59 = var57 * var32 - var55 * var30 + var25;
         double var61 = var57 * var30 + var55 * var32;
         double var63 = var51 * var36 - var61 * var34;
         double var65 = var51 * var34 + var61 * var36;
         double var67 = var63 * var9 - var65 * var7 + var1;
         double var69 = var59 + var3;
         double var71 = var63 * var7 + var65 * var9 + var5;
         if (var67 < var38) {
            var38 = var67;
         }

         if (var67 > var44) {
            var44 = var67;
         }

         if (var69 < var40) {
            var40 = var69;
         }

         if (var69 > var46) {
            var46 = var69;
         }

         if (var71 < var42) {
            var42 = var71;
         }

         if (var71 > var48) {
            var48 = var71;
         }
      }

      class_238 var73 = new class_238(var38, var40, var42, var44, var46, var48);
      if (var29) {
         Render3D.drawBox(var73, var28, 1.0F, false, true, false);
      }

      Render3D.drawBox(var73, var27, 1.5F, true, false, false);
   }

   private record Ghost() {
      private final double x;
      private final double y;
      private final double z;
      private final float yaw;
      private final float lLeg;
      private final float rLeg;
      private final float lArm;
      private final float rArm;
      private final float headPitch;
      private final float headYaw;
      private final long spawnTime;
      private final float fadeDurationMs;

      private Ghost(
         double var1, double var3, double var5, float var7, float var8, float var9, float var10, float var11, float var12, float var13, long var14, float var16
      ) {
         this.x = var1;
         this.y = var3;
         this.z = var5;
         this.yaw = var7;
         this.lLeg = var8;
         this.rLeg = var9;
         this.lArm = var10;
         this.rArm = var11;
         this.headPitch = var12;
         this.headYaw = var13;
         this.spawnTime = var14;
         this.fadeDurationMs = var16;
      }
   }
}
