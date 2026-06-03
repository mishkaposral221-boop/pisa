package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import net.minecraft.class_2848;
import net.minecraft.class_2848.class_2849;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.c;

public class AutoSprint extends ModuleStructure {
   private static volatile boolean serverSprintState = false;
   private final BooleanSetting noReset = new BooleanSetting("Не сбрасывать спринт", "Don't reset sprint for crits").setValue(false);

   public static AutoSprint getInstance() {
      return c.a(AutoSprint.class);
   }

   public AutoSprint() {
      super("AutoSprint", ModuleCategory.UTILITIES);
      this.settings(this.noReset);
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginMutation)
   public void onPacket(PacketEvent var1) {
      if (var1.getType() == PacketEvent.Type.SEND) {
         if (var1.getPacket() instanceof class_2848 var2) {
            if (var2.method_12365() == class_2849.field_12981) {
               if (serverSprintState) {
                  var1.cancel();
                  return;
               }

               serverSprintState = true;
            } else if (var2.method_12365() == class_2849.field_12985) {
               if (!serverSprintState) {
                  var1.cancel();
                  return;
               }

               serverSprintState = false;
            }
         }
      }
   }

   public static boolean isServerSprinting() {
      return serverSprintState;
   }

   public static void resetServerState() {
      serverSprintState = false;
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null) {
         this.processSprint();
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private void processSprint() {
      boolean var1 = mc.field_1724.field_5976 && !mc.field_1724.field_34927;
      boolean var2 = mc.field_1724.method_5715() && !mc.field_1724.method_5681();
      boolean var3 = !var1 && mc.field_1724.field_6250 > 0.0F;
      if (!var2) {
         if (var3 && !mc.field_1724.method_5624()) {
            mc.field_1724.method_5728(true);
         }
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   @Override
   public void deactivate() {
      resetServerState();
   }

   public BooleanSetting getNoReset() {
      return this.noReset;
   }
}
