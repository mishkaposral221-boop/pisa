package rich.modules.impl.player;

import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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
      if (mc.player != null && mc.currentScreen != null) {
         if (mc.currentScreen instanceof HandledScreen var2) {
            long var12 = mc.getWindow().getHandle();
            boolean var5 = PlayerInteractionHelper.isKey(mc.options.sneakKey);
            boolean var6 = GLFW.glfwGetMouseButton(var12, 0) == 1 || GLFW.glfwGetMouseButton(var12, 1) == 1;
            if (var5 && var6 && this.stopWatch.every(this.scrollerSetting.getValue())) {
               double var7 = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
               double var9 = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
               Slot var11 = var2.getSlotAt(var7, var9);
               if (var11 != null && var11.hasStack()) {
                  InventoryUtils.click(var11.id, 0, SlotActionType.QUICK_MOVE);
               }
            }
         }
      }
   }
}
