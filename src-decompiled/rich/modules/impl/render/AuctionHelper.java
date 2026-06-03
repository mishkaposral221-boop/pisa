package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2653;
import org.lwjgl.glfw.GLFW;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;

public class AuctionHelper extends ModuleStructure {
   private final BindSetting openWithItem = new BindSetting("Открыть с предметом в руке", "");
   private boolean openWithItemWasPressed = false;
   private class_1735 firstSlot = null;
   private class_1735 secondSlot = null;
   private class_1735 thirdSlot = null;
   private List<class_1735> thornsSlots = new ArrayList<>();
   private boolean needUpdate = false;
   private int updateDelay = 0;
   static final int GREEN_COLOR = -16711936;
   static final int ORANGE_COLOR = -29696;
   static final int RED_COLOR = -52429;

   public AuctionHelper() {
      super("AuctionHelper", "Auction Helper", ModuleCategory.VISUALS);
      this.settings(this.openWithItem);
   }

   @Override
   public void activate() {
      this.firstSlot = null;
      this.secondSlot = null;
      this.thirdSlot = null;
      this.needUpdate = false;
      this.updateDelay = 0;
   }

   @Override
   public void deactivate() {
      this.firstSlot = null;
      this.secondSlot = null;
      this.thirdSlot = null;
   }

   @EventHandler
   public void onPacket(PacketEvent var1) {
      if (var1.getPacket() instanceof class_2653) {
         this.needUpdate = true;
         this.updateDelay = 2;
      }
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int var2 = this.openWithItem.getKey();
         int var3 = this.openWithItem.getType();
         boolean var4 = false;
         if (var2 != -1) {
            if (var3 == 1) {
               var4 = GLFW.glfwGetKey(mc.method_22683().method_4490(), var2) == 1;
            } else if (var3 == 0) {
               var4 = GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), var2) == 1;
            }
         }

         if (var4) {
            if (!this.openWithItemWasPressed) {
               class_1799 var5 = mc.field_1724.method_6047();
               if (var5 != null && !var5.method_7960()) {
                  String var6 = var5.method_7964().getString();
                  mc.field_1724.field_3944.method_45730("ah search " + var6);
               }
            }

            this.openWithItemWasPressed = true;
         } else {
            this.openWithItemWasPressed = false;
         }
      }
   }

   public BindSetting getOpenWithItem() {
      return this.openWithItem;
   }

   public boolean isOpenWithItemWasPressed() {
      return this.openWithItemWasPressed;
   }

   public class_1735 getFirstSlot() {
      return this.firstSlot;
   }

   public class_1735 getSecondSlot() {
      return this.secondSlot;
   }

   public class_1735 getThirdSlot() {
      return this.thirdSlot;
   }

   public List<class_1735> getThornsSlots() {
      return this.thornsSlots;
   }

   public boolean isNeedUpdate() {
      return this.needUpdate;
   }

   public int getUpdateDelay() {
      return this.updateDelay;
   }
}
