package rich.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_342;
import net.minecraft.class_4717;
import net.minecraft.class_5481;
import net.minecraft.class_4717.class_464;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.TabCompleteEvent;

@Mixin(class_4717.class)
public abstract class ChatInputSuggestorMixin {
   @Shadow
   @Final
   class_342 field_21599;
   @Shadow
   @Final
   private List<class_5481> field_21607;
   @Shadow
   private ParseResults<?> field_21610;
   @Shadow
   private CompletableFuture<Suggestions> field_21611;
   @Shadow
   private class_464 field_21612;
   @Shadow
   boolean field_21614;

   @Shadow
   public abstract void method_23920(boolean var1);

   @Inject(method = "method_23934", at = @At("HEAD"), cancellable = true)
   private void onRefresh(CallbackInfo var1) {
      String var2 = this.field_21599.method_1882();
      int var3 = this.field_21599.method_1881();
      String var4 = var2.substring(0, Math.min(var2.length(), var3));
      TabCompleteEvent var5 = new TabCompleteEvent(var4);
      EventManager.callEvent(var5);
      if (var5.isCancelled()) {
         var1.cancel();
      } else {
         if (var5.completions != null) {
            var1.cancel();
            this.field_21610 = null;
            if (this.field_21614) {
               return;
            }

            this.field_21599.method_1887(null);
            this.field_21612 = null;
            this.field_21607.clear();
            if (var5.completions.length == 0) {
               this.field_21611 = Suggestions.empty();
            } else {
               int var6 = var4.lastIndexOf(32);
               StringRange var7 = StringRange.between(var6 + 1, var4.length());
               List var8 = Stream.of(var5.completions).map(var1x -> new Suggestion(var7, var1x)).collect(Collectors.toList());
               Suggestions var9 = new Suggestions(var7, var8);
               this.field_21611 = new CompletableFuture<>();
               this.field_21611.complete(var9);
            }

            this.method_23920(true);
         }
      }
   }
}
