package rich.util;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;

public class d {
   public static final SoundEvent a = a("hitsound.awp");
   public static final SoundEvent b = a("hitsound.litvin");
   public static final SoundEvent c = a("hitsound.pay");
   public static final SoundEvent d = a("hitsound.uwu");

   private static SoundEvent a(String var0) {
      Identifier var1 = Identifier.of("rich", var0);
      return Registry.register(Registries.SOUND_EVENT, var1, SoundEvent.of(var1));
   }

   public static void a() {
   }
}
