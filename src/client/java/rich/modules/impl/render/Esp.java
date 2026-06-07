package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import rich.events.api.EventHandler;
import rich.events.impl.AttackEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldLoadEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;
import rich.util.render.Render3D;
import rich.util.repository.friend.FriendUtils;

public class Esp extends ModuleStructure {
   private List<Entity> players = new ArrayList<>(32);
   private long lastUpdateTime;
   private static final long CACHE_DURATION = 50L;
   private static final double MAX_RENDER_DIST_SQ = 4096.0;
   private static final double MIN_DIST_SQ = 1.0;
   private final HashMap<Long, Long> hitTimes = new HashMap<>();
   private static final long HIT_EFFECT_DURATION = 200L;
   private Entity hoveredEntity = null;
   private long lastHoverCheck = 0L;
   private static final long HOVER_CHECK_INTERVAL = 50L;
   public ColorSetting boxColor = new ColorSetting("Цвет бокса", "Цвет для отображения бокса").value(-22016);
   public ColorSetting friendColor = new ColorSetting("Цвет друна", "Цвет для отображения друнов").value(-16711936);
   public ColorSetting hitColor = new ColorSetting("Цвет удара", "Цвет хитбокса при попадании").value(-65536);
   public ColorSetting hoverColor = new ColorSetting("Цвет наведения", "Цвет бокса при наведении").value(-65536);
   public SliderSettings boxAlpha = new SliderSettings("Прозрачность", "Прозрачность бокса").setValue(1.0F).range(0.1F, 1.0F);
   public BooleanSetting hitEffect = new BooleanSetting("Эффект удара", "Менять цвет при ударе по игроку").setValue(true);
   public BooleanSetting hoverEffect = new BooleanSetting("Эффект наведения", "Меняет цвет при наведении на игрока").setValue(true);

   public static Esp getInstance() {
      return c.a(Esp.class);
   }

   public Esp() {
      super("Hitboxes", "Hitboxes", ModuleCategory.VISUALS);
      this.settings(this.boxColor, this.friendColor, this.hitColor, this.hoverColor, this.boxAlpha, this.hitEffect, this.hoverEffect);
   }

   @EventHandler
   public void onWorldLoad(WorldLoadEvent var1) {
      this.players.clear();
      this.hitTimes.clear();
      this.lastUpdateTime = 0L;
      this.hoveredEntity = null;
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      long var2 = System.currentTimeMillis();
      if (var2 - this.lastUpdateTime >= 50L || this.players.isEmpty()) {
         this.lastUpdateTime = var2;
         this.players.clear();
         if (mc.world != null) {
            Vec3d var4 = mc.gameRenderer.getCamera().getCameraPos();

            for (Entity var6 : mc.world.getEntities()) {
               if (var6 != mc.player
                  && !var6.isInvisible()
                  && !(var6.squaredDistanceTo(var4) > 4096.0)
                  && (var6.getCustomName() == null || !var6.getCustomName().getString().startsWith("Ghost_"))) {
                  this.players.add(var6);
               }
            }

            this.hitTimes.entrySet().removeIf(var2x -> var2 - var2x.getValue() > 200L);
            if (var2 - this.lastHoverCheck > 50L) {
               this.lastHoverCheck = var2;
               this.updateHoveredEntity();
            }
         }
      }
   }

   private void updateHoveredEntity() {
      if (mc.player == null) {
         this.hoveredEntity = null;
      } else {
         Vec3d var1 = mc.player.getEyePos();
         Vec3d var2 = var1.add(mc.player.getRotationVector().multiply(6.0));
         Entity var3 = null;
         double var4 = Double.MAX_VALUE;
         int var6 = 0;

         for (int var7 = this.players.size(); var6 < var7; var6++) {
            Entity var8 = this.players.get(var6);
            if (var8.getBoundingBox().raycast(var1, var2).isPresent()) {
               double var9 = var8.squaredDistanceTo(mc.player);
               if (var9 < var4) {
                  var4 = var9;
                  var3 = var8;
               }
            }
         }

         this.hoveredEntity = var3;
      }
   }

   public void onPlayerHit(Entity var1) {
      if (var1 != null && this.hitEffect.isValue()) {
         this.hitTimes.put(var1.getUuid().getMostSignificantBits(), System.currentTimeMillis());
      }
   }

   @EventHandler
   public void onAttack(AttackEvent var1) {
      if (this.hitEffect.isValue() && var1.getTarget() != null) {
         this.hitTimes.put(var1.getTarget().getUuid().getMostSignificantBits(), System.currentTimeMillis());
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      if (!this.players.isEmpty()) {
         if (mc.player == null) {
            return;
         }
         float var2 = var1.getPartialTicks();
         Vec3d var3 = mc.gameRenderer.getCamera().getCameraPos();
         Vec3d look = mc.player.getRotationVector();
         long var4 = System.currentTimeMillis();
         int var6 = this.boxColor.getColorNoAlpha();
         int var7 = this.friendColor.getColorNoAlpha();
         int var8 = this.hitColor.getColorNoAlpha();
         int var9 = this.hoverColor.getColorNoAlpha();
         int var10 = (int)(this.boxAlpha.getValue() * 255.0F);
         int var11 = var6 | 0xFF000000;
         boolean var12 = this.hitEffect.isValue();
         boolean var13 = this.hoverEffect.isValue();
         int var14 = 0;

         for (int var15 = this.players.size(); var14 < var15; var14++) {
            Entity var16 = this.players.get(var14);
            if (var16 != null && var16 != mc.player) {
               double var17 = MathHelper.lerp(var2, var16.lastX, var16.getX());
               double var19 = MathHelper.lerp(var2, var16.lastY, var16.getY());
               double var21 = MathHelper.lerp(var2, var16.lastZ, var16.getZ());
               double var23 = var17 - var3.x;
               double var25 = var19 - var3.y;
               double var27 = var21 - var3.z;
               if (!(var23 * var23 + var25 * var25 + var27 * var27 < 1.0)) {
                  // Отсекаем игроков, которые явно позади камеры — их не видно даже сквозь стены на экране
                  double lenFwd = Math.sqrt(var23 * var23 + var25 * var25 + var27 * var27);
                  if (lenFwd > 2.0 && (var23 * look.x + var25 * look.y + var27 * look.z) / lenFwd < -0.2) {
                     continue;
                  }

                  boolean var29 = var16 instanceof PlayerEntity && FriendUtils.isFriend((PlayerEntity)var16);
                  long var30 = var16.getUuid().getMostSignificantBits();
                  Long var32 = var12 ? this.hitTimes.get(var30) : null;
                  boolean var33 = var32 != null;
                  long var34 = var33 ? var4 - var32 : 200L;
                  boolean var36 = var13 && this.hoveredEntity == var16;
                  int var37;
                  int var38;
                  if (var33 && var34 < 200L) {
                     float var39 = 1.0F - (float)var34 / 200.0F;
                     var37 = var8 & 16777215 | (int)(var10 * var39) << 24;
                     var38 = var8 | 0xFF000000;
                  } else if (var36) {
                     var37 = var9 & 16777215 | var10 << 24;
                     var38 = var9 | 0xFF000000;
                  } else {
                     var37 = (var29 ? var7 : var6) & 16777215 | var10 << 24;
                     var38 = var11;
                  }

                  Box var40 = var16.getBoundingBox().offset(var17 - var16.getX(), var19 - var16.getY(), var21 - var16.getZ());
                  Render3D.drawBox(var40, var37, 2.0F, true, true, true);
                  Render3D.drawBox(var40, var38, 2.0F, true, false, true);
               }
            }
         }
      }
   }
}
