package rich.util.repository.way;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
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

   public void addWay(String var1, BlockPos var2, String var3) {
      this.wayList.add(new Way(var1, var2, var3));
   }

   public void addWayAndSave(String var1, BlockPos var2, String var3) {
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
      return mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null ? mc.getNetworkHandler().getServerInfo().address : "";
   }

   private boolean isInFrontOfCamera(Vec3d var1) {
      Camera var2 = mc.gameRenderer.getCamera();
      if (var2 != null && var2.isReady()) {
         Vec3d var3 = var2.getCameraPos();
         Vec3d var4 = var1.subtract(var3);
         float var5 = var2.getYaw();
         float var6 = var2.getPitch();
         double var7 = Math.toRadians(var5);
         double var9 = Math.toRadians(var6);
         double var11 = -Math.sin(var7) * Math.cos(var9);
         double var13 = -Math.sin(var9);
         double var15 = Math.cos(var7) * Math.cos(var9);
         Vec3d var17 = new Vec3d(var11, var13, var15);
         return var17.dotProduct(var4) > 0.0;
      } else {
         return false;
      }
   }

   @EventHandler
   public void onRender2D(DrawEvent var1) {
      if (!this.isEmpty() && mc.player != null && mc.world != null) {
         if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null) {
            String var2 = this.getCurrentServer();

            for (Way var4 : this.wayList) {
               if (var4.server().equalsIgnoreCase(var2)) {
                  Vec3d var5 = var4.pos().toCenterPos();
                  if (this.isInFrontOfCamera(var5)) {
                     Vec3d var6 = Projection.worldSpaceToScreenSpace(var5);
                     if (!(var6.z <= 0.0) && !(var6.z >= 1.0)) {
                        double var7 = mc.player.getEntityPos().distanceTo(var5);
                        String var9 = var4.name() + " - " + String.format("%.1f", var7) + "m";
                        Font var10 = Fonts.BOLD;
                        float var11 = 6.0F;
                        float var12 = var10.getWidth(var9, var11);
                        float var13 = var10.getHeight(var11);
                        float var14 = 3.0F;
                        float var15 = (float)var6.x - var12 / 2.0F;
                        float var16 = (float)var6.y - var13 / 2.0F;
                        Render2D.rect(var15 - var14, var16 - var14 + 0.5F, var12 + var14 * 2.0F, var13 + var14 * 2.0F, -535620843, 2.0F);
                        var10.drawCentered(var9, (float)var6.x, var16 + 1.0F, var11, -1);
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
