package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import net.minecraft.class_1657;
import net.minecraft.class_3966;
import rich.events.api.EventHandler;
import rich.events.impl.KeyEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.util.repository.friend.FriendUtils;

public class ClickFriend extends ModuleStructure {
   private final BindSetting friendBind = new BindSetting("Добавить друга", "Добавить/удалить друга");

   public ClickFriend() {
      super("ClickFriend", "Click Friend", ModuleCategory.UTILITIES);
      this.settings(this.friendBind);
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginMutation)
   public void onKey(KeyEvent var1) {
      if (var1.isKeyDown(this.friendBind.getKey()) && mc.field_1765 instanceof class_3966 var2 && var2.method_17782() instanceof class_1657 var3) {
         if (FriendUtils.isFriend(var3)) {
            FriendUtils.removeFriend(var3);
         } else {
            FriendUtils.addFriend(var3);
         }
      }
   }
}
