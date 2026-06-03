package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import rich.events.api.EventHandler;
import rich.events.impl.EntityStatusEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.color.ColorUtil;
import rich.util.c;
import rich.util.render.Render3D;

public class KillEffect extends ModuleStructure {
   private final ColorSetting color = new ColorSetting("Цвет", "Цвет призрака").setColor(-3544833);
   private final SliderSettings duration = new SliderSettings("Длительность", "Время эффекта в мс").range(500.0F, 3000.0F).setValue(1400.0F);
   private final SliderSettings riseHeight = new SliderSettings("Подъем", "Насколько высоко поднимается дух").range(0.8F, 4.0F).setValue(2.2F);
   private final List<KillEffect.GhostEffect> ghosts = new ArrayList<>(8);
   private static final int RING_SEGMENTS = 16;
   private static final double[] RING_COS = new double[16];
   private static final double[] RING_SIN = new double[16];
   private final Vec3d[] ringPoints = new Vec3d[16];

   public static KillEffect getInstance() {
      return c.a(KillEffect.class);
   }

   public KillEffect() {
      super("KillEffect", "Эффекты при смерти игроков", ModuleCategory.VISUALS);

      for (int var1 = 0; var1 < 16; var1++) {
         this.ringPoints[var1] = Vec3d.ZERO;
      }

      this.settings(this.color, this.duration, this.riseHeight);
   }

   @Override
   public void deactivate() {
      this.ghosts.clear();
      super.deactivate();
   }

   @EventHandler
   public void onEntityStatus(EntityStatusEvent var1) {
      if (this.isState() && mc.player != null && mc.world != null) {
         if (var1.getEntity() instanceof PlayerEntity var2 && var2 != mc.player) {
            if (var1.getStatus() == 3 && this.ghosts.size() < 8) {
               this.ghosts
                  .add(new KillEffect.GhostEffect(new Vec3d(var2.getX(), var2.getY(), var2.getZ()), System.currentTimeMillis()));
            }
         }
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      if (!this.ghosts.isEmpty()) {
         long var2 = System.currentTimeMillis();
         long var4 = (long)this.duration.getValue();
         float var6 = this.riseHeight.getValue();
         int var7 = this.color.getColor();
         Iterator var8 = this.ghosts.iterator();

         while (var8.hasNext()) {
            KillEffect.GhostEffect var9 = (KillEffect.GhostEffect)var8.next();
            float var10 = (float)(var2 - var9.startTime) / (float)var4;
            if (var10 >= 1.0F) {
               var8.remove();
            } else {
               this.renderGhost(var9.startPos, var10, var6, var7);
            }
         }
      }
   }

   private void renderGhost(Vec3d var1, float var2, float var3, int var4) {
      float var5 = 1.0F - (float)Math.pow(1.0F - var2, 2.0);
      double var6 = var5 * var3;
      float var8 = 1.0F - var2;
      int var9 = ColorUtil.h(var4, Math.max(10, (int)(70.0F * var8)));
      int var10 = ColorUtil.h(var4, Math.max(20, (int)(180.0F * var8)));
      double var11 = var1.x;
      double var13 = var1.y + var6;
      double var15 = var1.z;
      Render3D.drawBox(new Box(var11 - 0.22, var13 + 0.02, var15 - 0.12, var11 + 0.22, var13 + 0.98, var15 + 0.12), var10, 1.7F, true, true, false);
      Render3D.drawBox(new Box(var11 - 0.16, var13 + 1.0, var15 - 0.2, var11 + 0.16, var13 + 1.34, var15 + 0.04), var10, 1.7F, true, true, false);
      Vec3d var17 = new Vec3d(var11 - 0.22, var13 + 0.78, var15);
      Vec3d var18 = new Vec3d(var11 + 0.22, var13 + 0.78, var15);
      Render3D.drawLine(var17, var18, var10, 1.5F, false);
      Render3D.drawLine(var17, var17.add(0.0, -0.42, 0.0), var10, 1.4F, false);
      Render3D.drawLine(var18, var18.add(0.0, -0.42, 0.0), var10, 1.4F, false);

      for (int var19 = 0; var19 < 3; var19++) {
         double var20 = var13 + 0.25 + var19 * 0.35;
         float var22 = 0.2F + var19 * 0.14F + var2 * 0.25F;
         this.drawRing(var11, var20, var15, var22, ColorUtil.h(var9, Math.max(8, 50 - var19 * 12)), 1.2F);
      }
   }

   private void drawRing(double var1, double var3, double var5, float var7, int var8, float var9) {
      for (int var10 = 0; var10 < 16; var10++) {
         this.ringPoints[var10] = new Vec3d(var1 + RING_COS[var10] * var7, var3, var5 + RING_SIN[var10] * var7);
      }

      for (int var11 = 0; var11 < 16; var11++) {
         Render3D.drawLine(this.ringPoints[var11], this.ringPoints[(var11 + 1) % 16], var8, var9, false);
      }
   }

   static {
      for (int var0 = 0; var0 < 16; var0++) {
         double var1 = (Math.PI * 2) * var0 / 16.0;
         RING_COS[var0] = Math.cos(var1);
         RING_SIN[var0] = Math.sin(var1);
      }
   }

   private record GhostEffect(Vec3d startPos, long startTime) {
   }
}
