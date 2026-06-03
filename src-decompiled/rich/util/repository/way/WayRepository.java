package rich.util.repository.way;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.DrawEvent;
import rich.util.config.impl.way.WayConfig;
import rich.util.math.Projection;
import rich.util.render.Render2D;
import rich.util.render.font.Font;
import rich.util.render.font.Fonts;

public class WayRepository implements IMinecraft {
   private static WayRepository instance;
   private final List<Way> wayList = new ArrayList<>();

   public WayRepository() {
      instance = this;
   }

   public static WayRepository getInstance() {
      if (instance == null) {
         instance = new WayRepository();
      }

      return instance;
   }

   public void init() {
      EventManager.register(this);
      WayConfig.getInstance().load();
   }

   public boolean isEmpty() {
      return this.wayList.isEmpty();
   }

   public void addWay(String var1, class_2338 var2, String var3) {
      this.wayList.add(new Way(var1, var2, var3));
   }

   public void addWayAndSave(String var1, class_2338 var2, String var3) {
      this.addWay(var1, var2, var3);
      WayConfig.getInstance().save();
   }

   public boolean hasWay(String var1) {
      return this.wayList.stream().anyMatch(var1x -> var1x.name().equalsIgnoreCase(var1));
   }

   public Optional<Way> getWay(String var1) {
      return this.wayList.stream().filter(var1x -> var1x.name().equalsIgnoreCase(var1)).findFirst();
   }

   public void deleteWay(String var1) {
      this.wayList.removeIf(var1x -> var1x.name().equalsIgnoreCase(var1));
   }

   public void deleteWayAndSave(String var1) {
      this.deleteWay(var1);
      WayConfig.getInstance().save();
   }

   public void clearList() {
      this.wayList.clear();
   }

   public void clearListAndSave() {
      this.clearList();
      WayConfig.getInstance().save();
   }

   public int size() {
      return this.wayList.size();
   }

   public List<String> getWayNames() {
      return this.wayList.stream().map(Way::name).collect(Collectors.toList());
   }

   public List<String> getWayNamesForServer(String var1) {
      return this.wayList.stream().filter(var1x -> var1x.server().equalsIgnoreCase(var1)).map(Way::name).collect(Collectors.toList());
   }

   public void setWays(List<Way> var1) {
      this.wayList.clear();
      this.wayList.addAll(var1);
   }

   public String getCurrentServer() {
      return mc.method_1562() != null && mc.method_1562().method_45734() != null ? mc.method_1562().method_45734().field_3761 : "";
   }

   private boolean isInFrontOfCamera(class_243 var1) {
      class_4184 var2 = mc.field_1773.method_19418();
      if (var2 != null && var2.method_19332()) {
         class_243 var3 = var2.method_71156();
         class_243 var4 = var1.method_1020(var3);
         float var5 = var2.method_19330();
         float var6 = var2.method_19329();
         double var7 = Math.toRadians(var5);
         double var9 = Math.toRadians(var6);
         double var11 = -Math.sin(var7) * Math.cos(var9);
         double var13 = -Math.sin(var9);
         double var15 = Math.cos(var7) * Math.cos(var9);
         class_243 var17 = new class_243(var11, var13, var15);
         return var17.method_1026(var4) > 0.0;
      } else {
         return false;
      }
   }

   @EventHandler
   public void onRender2D(DrawEvent var1) {
      if (!this.isEmpty() && mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.method_1562() != null && mc.method_1562().method_45734() != null) {
            String var2 = this.getCurrentServer();

            for (Way var4 : this.wayList) {
               if (var4.server().equalsIgnoreCase(var2)) {
                  class_243 var5 = var4.pos().method_46558();
                  if (this.isInFrontOfCamera(var5)) {
                     class_243 var6 = Projection.worldSpaceToScreenSpace(var5);
                     if (!(var6.field_1350 <= 0.0) && !(var6.field_1350 >= 1.0)) {
                        double var7 = mc.field_1724.method_73189().method_1022(var5);
                        String var9 = var4.name() + " - " + String.format("%.1f", var7) + "m";
                        Font var10 = Fonts.BOLD;
                        float var11 = 6.0F;
                        float var12 = var10.getWidth(var9, var11);
                        float var13 = var10.getHeight(var11);
                        float var14 = 3.0F;
                        float var15 = (float)var6.field_1352 - var12 / 2.0F;
                        float var16 = (float)var6.field_1351 - var13 / 2.0F;
                        Render2D.rect(var15 - var14, var16 - var14 + 0.5F, var12 + var14 * 2.0F, var13 + var14 * 2.0F, -535620843, 2.0F);
                        var10.drawCentered(var9, (float)var6.field_1352, var16 + 1.0F, var11, -1);
                     }
                  }
               }
            }
         }
      }
   }

   public List<Way> getWayList() {
      return this.wayList;
   }
}
