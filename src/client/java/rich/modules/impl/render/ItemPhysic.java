package rich.modules.impl.render;

import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.c;

public class ItemPhysic extends ModuleStructure {
   public final SelectSetting mode = new SelectSetting("Физика", "").value("Обычная").selected("Обычная");

   public static ItemPhysic getInstance() {
      return c.a(ItemPhysic.class);
   }

   public ItemPhysic() {
      super("ItemPhysic", "Item Physic", ModuleCategory.VISUALS);
   }

   @EventHandler
   public void onTick(TickEvent var1) {
   }
}
