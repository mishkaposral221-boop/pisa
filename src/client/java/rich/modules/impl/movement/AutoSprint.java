package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.AutoSwap;
import rich.modules.impl.combat.Triggerbot;
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
         if (var1.getPacket() instanceof ClientCommandC2SPacket var2) {
            if (var2.getMode() == net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.START_SPRINTING) {
               if (serverSprintState) {
                  var1.cancel();
                  return;
               }

               serverSprintState = true;
            } else if (var2.getMode() == net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
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
      if (mc.player != null) {
         this.processSprint();
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private void processSprint() {
      // Во время свапа из инвентаря НИКОГДА не ре-включаем спринт: иначе START_SPRINTING
      // уйдёт рядом с инвентарным кликом/закрытием экрана и сервер посчитает это читами (Inventory).
      if (AutoSwap.SUPPRESS_SPRINT) {
         return;
      }

      // Yield to the Triggerbot while it is dropping sprint to land a crit. Without this, holding W
      // (forwardSpeed > 0) makes us re-enable sprint the same tick the Triggerbot cleared it, so the
      // server never sees STOP_SPRINTING before the attack and the hit is not a crit. When the user
      // turns on "Don't reset sprint" (noReset), we keep the old always-sprint behaviour instead.
      if (!this.noReset.isValue() && Triggerbot.SUPPRESS_SPRINT) {
         return;
      }

      boolean var1 = mc.player.horizontalCollision && !mc.player.collidedSoftly;
      boolean var2 = mc.player.isSneaking() && !mc.player.isSwimming();
      boolean var3 = !var1 && mc.player.forwardSpeed > 0.0F;
      if (!var2) {
         if (var3 && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
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
