package rich.mixin;

import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_4184;
import net.minecraft.class_761;
import net.minecraft.class_11282.class_12294;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.modules.impl.render.ChunkAnimator;
import rich.modules.impl.render.NoRender;

@Mixin(class_761.class)
public class WorldRendererMixin implements IMinecraft {
   @ModifyArg(method = "method_72157", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0), index = 0)
   private Object modifyChunkSectionsValue(Object var1) {
      if (var1 instanceof class_12294 var2) {
         ChunkAnimator var3 = ChunkAnimator.getInstance();
         if (var3 != null && var3.isState()) {
            float var4 = var2.comp_5186();
            float var5 = (1.0F - var4) * 100.0F;
            int var6 = var2.comp_5184() - (int)var5;
            return new class_12294(var2.comp_5182(), var2.comp_5183(), var6, var2.comp_5185(), var2.comp_5186(), var2.comp_5187(), var2.comp_5188());
         }
      }

      return var1;
   }

   @Inject(method = "method_43788", at = @At("HEAD"), cancellable = true)
   private void onHasBlindnessOrDarkness(class_4184 var1, CallbackInfoReturnable<Boolean> var2) {
      NoRender var3 = NoRender.getInstance();
      if (var3 != null && var3.isState()) {
         if (var1.method_19331() instanceof class_1309 var5) {
            boolean var6 = var5.method_6059(class_1294.field_5919);
            boolean var7 = var5.method_6059(class_1294.field_38092);
            if (var3.modeSetting.isSelected("Bad Effects") && var6 && !var7) {
               var2.setReturnValue(false);
            }

            if (var3.modeSetting.isSelected("Darkness") && var7 && !var6) {
               var2.setReturnValue(false);
            }

            if (var3.modeSetting.isSelected("Bad Effects") && var3.modeSetting.isSelected("Darkness")) {
               var2.setReturnValue(false);
            }
         }
      }
   }
}
