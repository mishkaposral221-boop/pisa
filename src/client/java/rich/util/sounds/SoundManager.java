package rich.util.sounds;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.Registries;
import rich.IMinecraft;
import rich.util.string.PlayerInteractionHelper;

public final class SoundManager implements IMinecraft {
   public static SoundEvent KOLOKOLNIA_KILL = SoundEvent.of(Identifier.of("rich:kolokolnia_kill"));
   public static SoundEvent MOAN1 = SoundEvent.of(Identifier.of("rich:moan1"));
   public static SoundEvent MOAN2 = SoundEvent.of(Identifier.of("rich:moan2"));
   public static SoundEvent MOAN3 = SoundEvent.of(Identifier.of("rich:moan3"));
   public static SoundEvent MOAN4 = SoundEvent.of(Identifier.of("rich:moan4"));
   public static SoundEvent MODULE_DISABLE = SoundEvent.of(Identifier.of("rich:module_disable"));
   public static SoundEvent MODULE_ENABLE = SoundEvent.of(Identifier.of("rich:module_enable"));
   public static SoundEvent OFF = SoundEvent.of(Identifier.of("rich:off"));
   public static SoundEvent ON = SoundEvent.of(Identifier.of("rich:on"));
   public static SoundEvent CRIME = SoundEvent.of(Identifier.of("rich:crime"));
   public static SoundEvent METALLIC = SoundEvent.of(Identifier.of("rich:metallic"));
   public static SoundEvent WELCOME = SoundEvent.of(Identifier.of("rich:welcome"));

   public static void init() {
      Registry.register(Registries.SOUND_EVENT, KOLOKOLNIA_KILL.id(), KOLOKOLNIA_KILL);
      Registry.register(Registries.SOUND_EVENT, MOAN1.id(), MOAN1);
      Registry.register(Registries.SOUND_EVENT, MOAN2.id(), MOAN2);
      Registry.register(Registries.SOUND_EVENT, MOAN3.id(), MOAN3);
      Registry.register(Registries.SOUND_EVENT, MOAN4.id(), MOAN4);
      Registry.register(Registries.SOUND_EVENT, MODULE_DISABLE.id(), MODULE_DISABLE);
      Registry.register(Registries.SOUND_EVENT, MODULE_ENABLE.id(), MODULE_ENABLE);
      Registry.register(Registries.SOUND_EVENT, OFF.id(), OFF);
      Registry.register(Registries.SOUND_EVENT, ON.id(), ON);
      Registry.register(Registries.SOUND_EVENT, CRIME.id(), CRIME);
      Registry.register(Registries.SOUND_EVENT, METALLIC.id(), METALLIC);
      Registry.register(Registries.SOUND_EVENT, WELCOME.id(), WELCOME);
   }

   public static void playSound(SoundEvent var0) {
      playSound(var0, 1.0F, 1.0F);
   }

   public static void playSound(SoundEvent var0, float var1, float var2) {
      if (!PlayerInteractionHelper.nullCheck()) {
         mc.world.playSound(mc.player, mc.player.getBlockPos(), var0, SoundCategory.BLOCKS, var1, var2);
      }
   }

   public static void playSoundDirect(SoundEvent var0, float var1, float var2) {
      mc.getSoundManager().play(PositionedSoundInstance.ui(var0, var2, var1));
   }

   private SoundManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
