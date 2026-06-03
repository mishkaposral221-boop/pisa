package rich.modules.module.setting;

import java.util.List;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.screens.clickgui.impl.settingsrender.BindComponent;
import rich.screens.clickgui.impl.settingsrender.ButtonComponent;
import rich.screens.clickgui.impl.settingsrender.CheckboxComponent;
import rich.screens.clickgui.impl.settingsrender.ColorComponent;
import rich.screens.clickgui.impl.settingsrender.MultiSelectComponent;
import rich.screens.clickgui.impl.settingsrender.SelectComponent;
import rich.screens.clickgui.impl.settingsrender.SliderComponent;
import rich.screens.clickgui.impl.settingsrender.TextComponent;
import rich.util.interfaces.AbstractSettingComponent;

public class SettingComponentAdder {
   public void addSettingComponent(List<Setting> var1, List<AbstractSettingComponent> var2) {
      var1.forEach(var1x -> {
         if (var1x instanceof BooleanSetting var2x) {
            var2.add(new CheckboxComponent(var2x));
         }

         if (var1x instanceof BindSetting var3) {
            var2.add(new BindComponent(var3));
         }

         if (var1x instanceof ColorSetting var4) {
            var2.add(new ColorComponent(var4));
         }

         if (var1x instanceof TextSetting var5) {
            var2.add(new TextComponent(var5));
         }

         if (var1x instanceof SliderSettings var6) {
            var2.add(new SliderComponent(var6));
         }

         if (var1x instanceof ButtonSetting var7) {
            var2.add(new ButtonComponent(var7));
         }

         if (var1x instanceof SelectSetting var8) {
            var2.add(new SelectComponent(var8));
         }

         if (var1x instanceof MultiSelectSetting var9) {
            var2.add(new MultiSelectComponent(var9));
         }
      });
   }
}
