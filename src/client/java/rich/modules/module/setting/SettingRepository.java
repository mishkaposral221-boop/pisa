package rich.modules.module.setting;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

public class SettingRepository implements Setupable {
   private final List<Setting> settings = Lists.newArrayList();

   @Override
   public final void settings(Setting... var1) {
      this.settings.addAll(Arrays.asList(var1));
   }

   public Setting get(String var1) {
      return this.settings.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).findFirst().orElse(null);
   }

   public List<Setting> settings() {
      return this.settings;
   }
}
