package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import java.util.Arrays;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.network.Network;
import rich.util.repository.friend.FriendUtils;

public class AutoTpAccept extends ModuleStructure {
   private final String[] teleportMessages = new String[]{
      "has requested teleport", "просит телепортироваться", "хочет телепортироваться к вам", "просит к вам телепортироваться"
   };
   private boolean canAccept;
   private final BooleanSetting friendSetting = new BooleanSetting("Только друзья", "Будет принимать запросы только от друзей").setValue(true);

   public AutoTpAccept() {
      super("AutoTpAccept", "Auto Tp Accept", ModuleCategory.UTILITIES);
      this.settings(this.friendSetting);
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginMutation)
   public void onPacket(PacketEvent var1) {
      if (var1.getPacket() instanceof GameMessageS2CPacket var2) {
         String var5 = var2.content().getString();
         boolean var4 = !this.friendSetting.isValue() || FriendUtils.getFriends().stream().anyMatch(var1x -> var5.contains(var1x.getName()));
         if (this.isTeleportMessage(var5)) {
            this.canAccept = var4;
         }
      }
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginMutation)
   public void onTick(TickEvent var1) {
      if (!Network.isPvp() && this.canAccept) {
         mc.player.networkHandler.sendChatCommand("tpaccept");
         this.canAccept = false;
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private boolean isTeleportMessage(String var1) {
      return Arrays.stream(this.teleportMessages).map(String::toLowerCase).anyMatch(var1::contains);
   }
}
