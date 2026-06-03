package rich.modules.impl.misc;

import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.online.OnlineTracker;
import rich.util.math.Projection;
import rich.util.render.Render2D;

public class UsersModule extends ModuleStructure {
   private static UsersModule instance;
   private static final class_2960 ICON = class_2960.method_60655("rich", "icon.png");
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
      if (mc.field_1724 != null && mc.field_1687 != null) {
         for (class_1657 var3 : mc.field_1687.method_18456()) {
            if (var3 != mc.field_1724 && OnlineTracker.getInstance().isRuntimeUser(var3.method_7334().name())) {
               class_243 var4 = var3.method_30950(var1.getPartialTicks()).method_1031(0.0, var3.method_17682() + 0.4, 0.0);
               class_243 var5 = Projection.worldSpaceToScreenSpace(var4);
               if (!(var5.field_1350 < 0.0) && !(var5.field_1350 > 1.0)) {
                  float var6 = (float)var5.field_1352 - 6.0F;
                  float var7 = (float)var5.field_1351 - 6.0F;
                  Render2D.texture(ICON, var6, var7, 12.0F, 12.0F, -1);
               }
            }
         }
      }
   }
}
