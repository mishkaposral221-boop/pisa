package rich.modules.impl.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.JumpEvent;
import rich.events.impl.WorldLoadEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.a;
import rich.util.c;
import rich.util.render.Render3D;
import rich.util.timer.StopWatch;

public class GroundPulse extends ModuleStructure {
   public ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет волны").value(-10177025);
   private final List<GroundPulse.WaveEffect> waves = new ArrayList<>(4);
   private final StopWatch pulseCooldown = new StopWatch();

   public static GroundPulse getInstance() {
      return c.a(GroundPulse.class);
   }

   public GroundPulse() {
      super("Ground Pulse", "Волны блоков при прыжке", ModuleCategory.VISUALS);
      this.settings(this.colorSetting);
   }

   @Override
   public void deactivate() {
      this.waves.clear();
      super.deactivate();
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent var1) {
      this.waves.clear();
   }

   @EventHandler
   public void onJump(JumpEvent var1) {
      if (mc.field_1724 != null && this.pulseCooldown.finished(200.0)) {
         if (this.waves.size() < 2) {
            this.pulseCooldown.reset();
            class_2338 var2 = class_2338.method_49637(mc.field_1724.method_23317(), mc.field_1724.method_23318() - 0.1, mc.field_1724.method_23321());
            this.waves.add(new GroundPulse.WaveEffect(var2, System.currentTimeMillis()));
         }
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      if (!this.waves.isEmpty() && mc.field_1687 != null) {
         long var2 = System.currentTimeMillis();
         this.waves.removeIf(var2x -> var2 - var2x.startTime > 500L);
         int var4 = 0;

         for (int var5 = this.waves.size(); var4 < var5; var4++) {
            this.waves.get(var4).render(var2);
         }
      }
   }

   private class WaveEffect {
      static final long DURATION = 500L;
      static final int MAX_RADIUS = 6;
      static final float WAVE_WIDTH = 2.0F;
      static final int MAX_PER_FRAME = 200;
      final class_2338 centerPos;
      final long startTime;
      long[] blockKeys;
      int[] blockDists;
      int blockCount = 0;
      boolean calculated = false;

      WaveEffect(class_2338 var2, long var3) {
         this.centerPos = var2;
         this.startTime = var3;
      }

      private void calculateReachableBlocks() {
         if (!this.calculated) {
            this.calculated = true;
            int var1 = 13;
            var1 = var1 * var1 * var1;
            this.blockKeys = new long[var1];
            this.blockDists = new int[var1];
            HashMap var2 = new HashMap(var1);
            ArrayDeque var3 = new ArrayDeque(var1 / 4);
            class_2338 var4 = this.centerPos;
            if (IMinecraft.mc.field_1687.method_8320(var4).method_26215()) {
               for (int var5 = 1; var5 <= 5; var5++) {
                  class_2338 var6 = var4.method_10087(var5);
                  if (!IMinecraft.mc.field_1687.method_8320(var6).method_26215()) {
                     var4 = var6;
                     break;
                  }
               }
            }

            var3.add(var4);
            var2.put(var4.method_10063(), 0);

            while (!var3.isEmpty()) {
               class_2338 var22 = (class_2338)var3.poll();
               int var23 = (Integer)var2.get(var22.method_10063());
               if (var23 <= 6) {
                  class_2680 var7 = IMinecraft.mc.field_1687.method_8320(var22);
                  if (!var7.method_26215()) {
                     class_265 var8 = var7.method_26218(IMinecraft.mc.field_1687, var22);
                     if (!var8.method_1110() && this.blockCount < this.blockKeys.length) {
                        this.blockKeys[this.blockCount] = var22.method_10063();
                        this.blockDists[this.blockCount] = var23;
                        this.blockCount++;
                     }
                  }

                  for (class_2350 var11 : class_2350.values()) {
                     class_2338 var12 = var22.method_10093(var11);
                     if (IMinecraft.mc.field_1687.method_24794(var12)) {
                        int var13 = var23 + 1;
                        if (var13 <= 6) {
                           long var14 = var12.method_10063();
                           if (!var2.containsKey(var14) || (Integer)var2.get(var14) > var13) {
                              class_2680 var16 = IMinecraft.mc.field_1687.method_8320(var12);
                              if (!var16.method_26215()) {
                                 var2.put(var14, var13);
                                 var3.add(var12);
                              } else {
                                 class_2338 var17 = var12.method_10074();
                                 if (IMinecraft.mc.field_1687.method_24794(var17) && !IMinecraft.mc.field_1687.method_8320(var17).method_26215()) {
                                    long var18 = var17.method_10063();
                                    if (!var2.containsKey(var18) || (Integer)var2.get(var18) > var13) {
                                       var2.put(var18, var13);
                                       var3.add(var17);
                                    }
                                 }

                                 class_2338 var25 = var12.method_10084();
                                 if (IMinecraft.mc.field_1687.method_24794(var25) && !IMinecraft.mc.field_1687.method_8320(var25).method_26215()) {
                                    long var19 = var25.method_10063();
                                    if (!var2.containsKey(var19) || (Integer)var2.get(var19) > var13) {
                                       var2.put(var19, var13);
                                       var3.add(var25);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      void render(long var1) {
         if (IMinecraft.mc.field_1687 != null) {
            this.calculateReachableBlocks();
            if (this.blockCount != 0) {
               float var3 = (float)(var1 - this.startTime) / 500.0F;
               float var4 = var3 * 6.0F;
               float var5 = (float)Math.sqrt(1.0F - var3);
               int var6 = GroundPulse.this.colorSetting.getColor();
               int var7 = 0;

               for (int var8 = 0; var8 < this.blockCount && var7 < 200; var8++) {
                  int var9 = this.blockDists[var8];
                  if (!(var9 < var4 - 2.0F) && !(var9 > var4 + 0.5F)) {
                     class_2338 var10 = class_2338.method_10092(this.blockKeys[var8]);
                     class_2680 var11 = IMinecraft.mc.field_1687.method_8320(var10);
                     if (!var11.method_26215()) {
                        class_265 var12 = var11.method_26218(IMinecraft.mc.field_1687, var10);
                        if (!var12.method_1110()) {
                           float var13 = Math.max(0.0F, 1.0F - Math.abs(var9 - var4) / 2.0F) * var5;
                           if (!(var13 < 0.02F)) {
                              int var14 = a.h(var6, (int)(var13 * 75.0F));

                              try {
                                 Render3D.drawShapeAlternative(var10, var12, var14, 1.0F, true, true);
                              } catch (Exception var16) {
                              }

                              var7++;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
