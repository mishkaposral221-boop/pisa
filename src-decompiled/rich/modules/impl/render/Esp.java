package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
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
   private List<class_1297> players = new ArrayList<>(32);
   private long lastUpdateTime;
   private static final long CACHE_DURATION = 50L;
   private static final double MAX_RENDER_DIST_SQ = 65536.0;
   private static final double MIN_DIST_SQ = 1.0;
   private final HashMap<Long, Long> hitTimes = new HashMap<>();
   private static final long HIT_EFFECT_DURATION = 200L;
   private class_1297 hoveredEntity = null;
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
         if (mc.field_1687 != null) {
            class_243 var4 = mc.field_1773.method_19418().method_71156();

            for (class_1297 var6 : mc.field_1687.method_18112()) {
               if (var6 != mc.field_1724
                  && !var6.method_5767()
                  && !(var6.method_5707(var4) > 65536.0)
                  && (var6.method_5797() == null || !var6.method_5797().getString().startsWith("Ghost_"))) {
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
      if (mc.field_1724 == null) {
         this.hoveredEntity = null;
      } else {
         class_243 var1 = mc.field_1724.method_33571();
         class_243 var2 = var1.method_1019(mc.field_1724.method_5720().method_1021(6.0));
         class_1297 var3 = null;
         double var4 = Double.MAX_VALUE;
         int var6 = 0;

         for (int var7 = this.players.size(); var6 < var7; var6++) {
            class_1297 var8 = this.players.get(var6);
            if (var8.method_5829().method_992(var1, var2).isPresent()) {
               double var9 = var8.method_5858(mc.field_1724);
               if (var9 < var4) {
                  var4 = var9;
                  var3 = var8;
               }
            }
         }

         this.hoveredEntity = var3;
      }
   }

   public void onPlayerHit(class_1297 var1) {
      if (var1 != null && this.hitEffect.isValue()) {
         this.hitTimes.put(var1.method_5667().getMostSignificantBits(), System.currentTimeMillis());
      }
   }

   @EventHandler
   public void onAttack(AttackEvent var1) {
      if (this.hitEffect.isValue() && var1.getTarget() != null) {
         this.hitTimes.put(var1.getTarget().method_5667().getMostSignificantBits(), System.currentTimeMillis());
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      if (!this.players.isEmpty()) {
         float var2 = var1.getPartialTicks();
         class_243 var3 = mc.field_1773.method_19418().method_71156();
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
            class_1297 var16 = this.players.get(var14);
            if (var16 != null && var16 != mc.field_1724) {
               double var17 = class_3532.method_16436(var2, var16.field_6014, var16.method_23317());
               double var19 = class_3532.method_16436(var2, var16.field_6036, var16.method_23318());
               double var21 = class_3532.method_16436(var2, var16.field_5969, var16.method_23321());
               double var23 = var17 - var3.field_1352;
               double var25 = var19 - var3.field_1351;
               double var27 = var21 - var3.field_1350;
               if (!(var23 * var23 + var25 * var25 + var27 * var27 < 1.0)) {
                  boolean var29 = var16 instanceof class_1657 && FriendUtils.isFriend((class_1657)var16);
                  long var30 = var16.method_5667().getMostSignificantBits();
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

                  class_238 var40 = var16.method_5829().method_989(var17 - var16.method_23317(), var19 - var16.method_23318(), var21 - var16.method_23321());
                  Render3D.drawBox(var40, var37, 2.0F, true, true, true);
                  Render3D.drawBox(var40, var38, 2.0F, true, false, true);
               }
            }
         }
      }
   }
}
