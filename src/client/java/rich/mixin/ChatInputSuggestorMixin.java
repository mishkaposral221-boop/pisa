package rich.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.text.OrderedText;
import net.minecraft.client.gui.screen.ChatInputSuggestor.SuggestionWindow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.TabCompleteEvent;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
   @Shadow
   @Final
   TextFieldWidget textField;
   @Shadow
   @Final
   private List<OrderedText> messages;
   @Shadow
   private ParseResults<?> parse;
   @Shadow
   private CompletableFuture<Suggestions> pendingSuggestions;
   @Shadow
   private net.minecraft.client.gui.screen.ChatInputSuggestor.SuggestionWindow window;
   @Shadow
   boolean completingSuggestions;

   @Shadow
   public abstract void show(boolean var1);

   @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
   private void onRefresh(CallbackInfo var1) {
      String var2 = this.textField.getText();
      int var3 = this.textField.getCursor();
      String var4 = var2.substring(0, Math.min(var2.length(), var3));
      TabCompleteEvent var5 = new TabCompleteEvent(var4);
      EventManager.callEvent(var5);
      if (var5.isCancelled()) {
         var1.cancel();
      } else {
         if (var5.completions != null) {
            var1.cancel();
            this.parse = null;
            if (this.completingSuggestions) {
               return;
            }

            this.textField.setSuggestion(null);
            this.window = null;
            this.messages.clear();
            if (var5.completions.length == 0) {
               this.pendingSuggestions = Suggestions.empty();
            } else {
               int var6 = var4.lastIndexOf(32);
               StringRange var7 = StringRange.between(var6 + 1, var4.length());
               List var8 = Stream.of(var5.completions).map(var1x -> new Suggestion(var7, var1x)).collect(Collectors.toList());
               Suggestions var9 = new Suggestions(var7, var8);
               this.pendingSuggestions = new CompletableFuture<>();
               this.pendingSuggestions.complete(var9);
            }

            this.show(true);
         }
      }
   }
}
