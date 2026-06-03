package rich.util.sounds;

import net.minecraft.class_1109;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_7923;
import rich.IMinecraft;
import rich.util.string.PlayerInteractionHelper;

public final class SoundManager implements IMinecraft {
   public static class_3414 KOLOKOLNIA_KILL = class_3414.method_47908(class_2960.method_60654("rich:kolokolnia_kill"));
   public static class_3414 MOAN1 = class_3414.method_47908(class_2960.method_60654("rich:moan1"));
   public static class_3414 MOAN2 = class_3414.method_47908(class_2960.method_60654("rich:moan2"));
   public static class_3414 MOAN3 = class_3414.method_47908(class_2960.method_60654("rich:moan3"));
   public static class_3414 MOAN4 = class_3414.method_47908(class_2960.method_60654("rich:moan4"));
   public static class_3414 MODULE_DISABLE = class_3414.method_47908(class_2960.method_60654("rich:module_disable"));
   public static class_3414 MODULE_ENABLE = class_3414.method_47908(class_2960.method_60654("rich:module_enable"));
   public static class_3414 OFF = class_3414.method_47908(class_2960.method_60654("rich:off"));
   public static class_3414 ON = class_3414.method_47908(class_2960.method_60654("rich:on"));
   public static class_3414 CRIME = class_3414.method_47908(class_2960.method_60654("rich:crime"));
   public static class_3414 METALLIC = class_3414.method_47908(class_2960.method_60654("rich:metallic"));
   public static class_3414 WELCOME = class_3414.method_47908(class_2960.method_60654("rich:welcome"));

   public static void init() {
      class_2378.method_10230(class_7923.field_41172, KOLOKOLNIA_KILL.comp_3319(), KOLOKOLNIA_KILL);
      class_2378.method_10230(class_7923.field_41172, MOAN1.comp_3319(), MOAN1);
      class_2378.method_10230(class_7923.field_41172, MOAN2.comp_3319(), MOAN2);
      class_2378.method_10230(class_7923.field_41172, MOAN3.comp_3319(), MOAN3);
      class_2378.method_10230(class_7923.field_41172, MOAN4.comp_3319(), MOAN4);
      class_2378.method_10230(class_7923.field_41172, MODULE_DISABLE.comp_3319(), MODULE_DISABLE);
      class_2378.method_10230(class_7923.field_41172, MODULE_ENABLE.comp_3319(), MODULE_ENABLE);
      class_2378.method_10230(class_7923.field_41172, OFF.comp_3319(), OFF);
      class_2378.method_10230(class_7923.field_41172, ON.comp_3319(), ON);
      class_2378.method_10230(class_7923.field_41172, CRIME.comp_3319(), CRIME);
      class_2378.method_10230(class_7923.field_41172, METALLIC.comp_3319(), METALLIC);
      class_2378.method_10230(class_7923.field_41172, WELCOME.comp_3319(), WELCOME);
   }

   public static void playSound(class_3414 var0) {
      playSound(var0, 1.0F, 1.0F);
   }

   public static void playSound(class_3414 var0, float var1, float var2) {
      if (!PlayerInteractionHelper.nullCheck()) {
         mc.field_1687.method_8396(mc.field_1724, mc.field_1724.method_24515(), var0, class_3419.field_15245, var1, var2);
      }
   }

   public static void playSoundDirect(class_3414 var0, float var1, float var2) {
      mc.method_1483().method_4873(class_1109.method_4757(var0, var2, var1));
   }

   private SoundManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
