package rich.util;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.registry.Registries;

public class d {
   public static final SoundEvent keyCodec = keyCodec("hitsound.awp");
   public static final SoundEvent elementCodec = keyCodec("hitsound.litvin");
   public static final SoundEvent c = keyCodec("hitsound.pay");
   public static final SoundEvent d = keyCodec("hitsound.uwu");

   private static SoundEvent keyCodec(String var0) {
      Identifier var1 = Identifier.of("rich", var0);
      return (SoundEvent)Registry.register(Registries.SOUND_EVENT, var1, SoundEvent.of(var1));
   }

   public static void keyCodec() {
   }
}
