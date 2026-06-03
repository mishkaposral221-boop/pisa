package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.api.events.render.TextFactoryEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.repository.friend.FriendUtils;

public class NameProtect extends ModuleStructure {
   private final TextSetting nameSetting = new TextSetting("Имя", "Никнейм, который будет заменен на ваш").setText("Protected").setMax(32);
   private final BooleanSetting friendsSetting = new BooleanSetting("Друзья", "Скрывает никнеймы друзей").setValue(true);

   public NameProtect() {
      super("NameProtect", "Name Protect", ModuleCategory.UTILITIES);
      this.settings(this.friendsSetting);
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onTextFactory(TextFactoryEvent var1) {
      var1.replaceText(mc.getSession().getUsername(), this.nameSetting.getText());
      if (this.friendsSetting.isValue()) {
         this.replaceFriendNames(var1);
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private void replaceFriendNames(TextFactoryEvent var1) {
      FriendUtils.getFriends().forEach(var2 -> var1.replaceText(var2.getName(), this.nameSetting.getText()));
   }
}
