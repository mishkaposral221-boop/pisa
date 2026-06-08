package rich.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import rich.modules.impl.render.ChunkAnimator;

/**
 * Анимация секций чанков (ChunkAnimator).
 *
 * Эта инъекция использует ХРУПКУЮ точку @At("INVOKE") внутри renderBlockLayers
 * (method_72157) класса WorldRenderer. Sodium своим LevelRendererMixin
 * переписывает/мёрджит этот метод, из-за чего целевой вызов List.add(...) исчезает,
 * а Mixin бросает InvalidInjectionException и роняет игру на этапе APPLY.
 *
 * Поэтому инъекция вынесена в отдельный миксин, который АВТОМАТИЧЕСКИ ОТКЛЮЧАЕТСЯ,
 * когда загружен Sodium (см. RichSodiumAwareMixinPlugin). Под Sodium анимация чанков
 * через ванильный путь всё равно невозможна — Sodium не использует
 * DynamicUniforms.ChunkSectionsValue, у него собственный конвейер рендера секций.
 *
 * Без Sodium миксин применяется как обычно и фича работает.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererChunkAnimatorMixin {
   @ModifyArg(method = "renderBlockLayers", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), index = 0, require = 0)
   private Object modifyChunkSectionsValue(Object var1) {
      if (var1 instanceof net.minecraft.client.gl.DynamicUniforms.ChunkSectionsValue var2) {
         ChunkAnimator var3 = ChunkAnimator.getInstance();
         if (var3 != null && var3.isState()) {
            float var4 = var2.visibility();
            if (var4 >= 1.0F) {
               return var1;
            }

            int var5 = (int)((1.0F - var4) * 100.0F);
            if (var5 == 0) {
               return var1;
            }

            int var6 = var2.y() - var5;
            return new net.minecraft.client.gl.DynamicUniforms.ChunkSectionsValue(var2.modelView(), var2.x(), var6, var2.z(), var2.visibility(), var2.textureAtlasWidth(), var2.textureAtlasHeight());
         }
      }

      return var1;
   }
}
