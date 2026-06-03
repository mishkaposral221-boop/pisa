package rich.modules.impl.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.online.OnlineTracker;
import rich.util.math.Projection;
import rich.util.render.Render2D;

public class UsersModule extends ModuleStructure {
   private static UsersModule instance;
   private static final Identifier ICON = Identifier.of("rich", "icon.png");
   private static final float ICON_SIZE = 12.0F;

   public UsersModule() {
      super("Users", "Показывает иконку RunTime перед ником игрока", ModuleCategory.UTILITIES);
      instance = this;
   }

   public static UsersModule getInstance() {
      return instance;
   }

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (mc.player != null && mc.world != null) {
         for (PlayerEntity var3 : mc.world.getPlayers()) {
            if (var3 != mc.player && OnlineTracker.getInstance().isRuntimeUser(var3.getGameProfile().name())) {
               Vec3d var4 = var3.getLerpedPos(var1.getPartialTicks()).add(0.0, var3.getHeight() + 0.4, 0.0);
               Vec3d var5 = Projection.worldSpaceToScreenSpace(var4);
               if (!(var5.z < 0.0) && !(var5.z > 1.0)) {
                  float var6 = (float)var5.x - 6.0F;
                  float var7 = (float)var5.y - 6.0F;
                  Render2D.texture(ICON, var6, var7, 12.0F, 12.0F, -1);
               }
            }
         }
      }
   }
}
