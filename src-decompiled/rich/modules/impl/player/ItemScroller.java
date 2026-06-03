package rich.modules.impl.player;

import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_465;
import org.lwjgl.glfw.GLFW;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.inventory.InventoryUtils;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public class ItemScroller extends ModuleStructure {
   private final StopWatch stopWatch = new StopWatch();
   private final SliderSettings scrollerSetting = new SliderSettings("Задержка", "Задержка прокрутки предметов").setValue(50.0F).range(0, 200);

   public ItemScroller() {
      super("ItemScroller", "Быстрое перемещение предметов", ModuleCategory.UTILITIES);
      this.settings(this.scrollerSetting);
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null && mc.field_1755 != null) {
         if (mc.field_1755 instanceof class_465 var2) {
            long var12 = mc.method_22683().method_4490();
            boolean var5 = PlayerInteractionHelper.isKey(mc.field_1690.field_1832);
            boolean var6 = GLFW.glfwGetMouseButton(var12, 0) == 1 || GLFW.glfwGetMouseButton(var12, 1) == 1;
            if (var5 && var6 && this.stopWatch.every(this.scrollerSetting.getValue())) {
               double var7 = mc.field_1729.method_1603() * mc.method_22683().method_4486() / mc.method_22683().method_4480();
               double var9 = mc.field_1729.method_1604() * mc.method_22683().method_4502() / mc.method_22683().method_4507();
               class_1735 var11 = var2.method_64240(var7, var9);
               if (var11 != null && var11.method_7681()) {
                  InventoryUtils.click(var11.field_7874, 0, class_1713.field_7794);
               }
            }
         }
      }
   }
}
