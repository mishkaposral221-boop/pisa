package rich.screens.clickgui.impl.module.handler;

import java.util.List;
import rich.modules.module.ModuleStructure;

public class ModuleFavoriteHandler {
   public void toggleFavorite(ModuleStructure var1, List<ModuleStructure> var2, ModuleAnimationHandler var3) {
      if (var1 != null) {
         var1.switchFavorite();
         int var4 = var2.indexOf(var1);

         for (ModuleStructure var6 : var2) {
            float var7 = var3.getPositionAnimations().getOrDefault(var6, 1.0F);
            if (var7 >= 0.99F) {
               var3.getPositionAnimations().put(var6, 0.0F);
            }

            if (!var3.getModuleAlphaAnimations().containsKey(var6)) {
               var3.getModuleAlphaAnimations().put(var6, 1.0F);
            }
         }

         var3.getModuleAlphaAnimations().put(var1, 0.0F);
      }
   }
}
