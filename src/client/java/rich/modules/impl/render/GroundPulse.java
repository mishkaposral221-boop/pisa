package rich.modules.impl.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.JumpEvent;
import rich.events.impl.WorldLoadEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.color.ColorUtil;
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
      if (mc.player != null && this.pulseCooldown.finished(200.0)) {
         if (this.waves.size() < 2) {
            this.pulseCooldown.reset();
            BlockPos var2 = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
            this.waves.add(new GroundPulse.WaveEffect(var2, System.currentTimeMillis()));
         }
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      if (!this.waves.isEmpty() && mc.world != null) {
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
      final BlockPos centerPos;
      final long startTime;
      long[] blockKeys;
      int[] blockDists;
      int blockCount = 0;
      boolean calculated = false;

      WaveEffect(BlockPos var2, long var3) {
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
            BlockPos var4 = this.centerPos;
            if (IMinecraft.mc.world.getBlockState(var4).isAir()) {
               for (int var5 = 1; var5 <= 5; var5++) {
                  BlockPos var6 = var4.down(var5);
                  if (!IMinecraft.mc.world.getBlockState(var6).isAir()) {
                     var4 = var6;
                     break;
                  }
               }
            }

            var3.add(var4);
            var2.put(var4.asLong(), 0);

            while (!var3.isEmpty()) {
               BlockPos var22 = (BlockPos)var3.poll();
               int var23 = (Integer)var2.get(var22.asLong());
               if (var23 <= 6) {
                  BlockState var7 = IMinecraft.mc.world.getBlockState(var22);
                  if (!var7.isAir()) {
                     VoxelShape var8 = var7.getOutlineShape(IMinecraft.mc.world, var22);
                     if (!var8.isEmpty() && this.blockCount < this.blockKeys.length) {
                        this.blockKeys[this.blockCount] = var22.asLong();
                        this.blockDists[this.blockCount] = var23;
                        this.blockCount++;
                     }
                  }

                  for (Direction var11 : Direction.values()) {
                     BlockPos var12 = var22.offset(var11);
                     if (IMinecraft.mc.world.isInBuildLimit(var12)) {
                        int var13 = var23 + 1;
                        if (var13 <= 6) {
                           long var14 = var12.asLong();
                           if (!var2.containsKey(var14) || (Integer)var2.get(var14) > var13) {
                              BlockState var16 = IMinecraft.mc.world.getBlockState(var12);
                              if (!var16.isAir()) {
                                 var2.put(var14, var13);
                                 var3.add(var12);
                              } else {
                                 BlockPos var17 = var12.down();
                                 if (IMinecraft.mc.world.isInBuildLimit(var17) && !IMinecraft.mc.world.getBlockState(var17).isAir()) {
                                    long var18 = var17.asLong();
                                    if (!var2.containsKey(var18) || (Integer)var2.get(var18) > var13) {
                                       var2.put(var18, var13);
                                       var3.add(var17);
                                    }
                                 }

                                 BlockPos var25 = var12.up();
                                 if (IMinecraft.mc.world.isInBuildLimit(var25) && !IMinecraft.mc.world.getBlockState(var25).isAir()) {
                                    long var19 = var25.asLong();
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
         if (IMinecraft.mc.world != null) {
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
                     BlockPos var10 = BlockPos.fromLong(this.blockKeys[var8]);
                     BlockState var11 = IMinecraft.mc.world.getBlockState(var10);
                     if (!var11.isAir()) {
                        VoxelShape var12 = var11.getOutlineShape(IMinecraft.mc.world, var10);
                        if (!var12.isEmpty()) {
                           float var13 = Math.max(0.0F, 1.0F - Math.abs(var9 - var4) / 2.0F) * var5;
                           if (!(var13 < 0.02F)) {
                              int var14 = ColorUtil.h(var6, (int)(var13 * 75.0F));

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
