package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
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
   private Slot firstSlot = null;
   private Slot secondSlot = null;
   private Slot thirdSlot = null;
   private List<Slot> thornsSlots = new ArrayList<>();
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
      if (var1.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket) {
         this.needUpdate = true;
         this.updateDelay = 2;
      }
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.player != null && mc.world != null) {
         int var2 = this.openWithItem.getKey();
         int var3 = this.openWithItem.getType();
         boolean var4 = false;
         if (var2 != -1) {
            if (var3 == 1) {
               var4 = GLFW.glfwGetKey(mc.getWindow().getHandle(), var2) == 1;
            } else if (var3 == 0) {
               var4 = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), var2) == 1;
            }
         }

         if (var4) {
            if (!this.openWithItemWasPressed) {
               ItemStack var5 = mc.player.getMainHandStack();
               if (var5 != null && !var5.isEmpty()) {
                  String var6 = var5.getName().getString();
                  mc.player.networkHandler.sendChatCommand("ah search " + var6);
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

   public Slot getFirstSlot() {
      return this.firstSlot;
   }

   public Slot getSecondSlot() {
      return this.secondSlot;
   }

   public Slot getThirdSlot() {
      return this.thirdSlot;
   }

   public List<Slot> getThornsSlots() {
      return this.thornsSlots;
   }

   public boolean isNeedUpdate() {
      return this.needUpdate;
   }

   public int getUpdateDelay() {
      return this.updateDelay;
   }
}
