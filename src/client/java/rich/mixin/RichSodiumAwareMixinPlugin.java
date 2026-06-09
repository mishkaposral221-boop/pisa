package rich.mixin;

import java.util.List;
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * Конфиг-плагин для mixins.json.
 *
 * Отключает миксины, несовместимые с Sodium, когда Sodium присутствует в рантайме.
 * Sodium переписывает конвейер рендера мира/чанков, из-за чего инъекции во
 * внутренние точки (@At("INVOKE")/@Redirect) в WorldRenderer становятся невалидными
 * и роняют игру на этапе APPLY. Такие миксины здесь просто не применяются, если
 * загружен Sodium. Без Sodium всё работает как раньше.
 */
public class RichSodiumAwareMixinPlugin implements IMixinConfigPlugin {
   private static final boolean SODIUM_LOADED =
      FabricLoader.getInstance().isModLoaded("sodium");

   private static final Set<String> DISABLED_WITH_SODIUM = Set.of(
      "rich.mixin.WorldRendererChunkAnimatorMixin"
   );

   @Override
   public void onLoad(String mixinPackage) {
   }

   @Override
   public String getRefMapperConfig() {
      return null;
   }

   @Override
   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (SODIUM_LOADED && DISABLED_WITH_SODIUM.contains(mixinClassName)) {
         return false;
      }
      return true;
   }

   @Override
   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   @Override
   public List<String> getMixins() {
      return null;
   }

   @Override
   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   @Override
   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
