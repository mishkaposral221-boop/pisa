package rich.mixin;

import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_3675.class_307;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.KeyEvent;
import rich.screens.clickgui.ClickGui;
import rich.util.config.impl.bind.BindConfig;

@Mixin(class_309.class)
public class KeyboardMixin {
   @Final
   @Shadow
   private class_310 field_1678;

   @Inject(method = "method_1466", at = @At("HEAD"))
   private void onKey(long var1, int var3, class_11908 var4, CallbackInfo var5) {
      if (var4.comp_4795() != -1 && var1 == this.field_1678.method_22683().method_4490()) {
         if (var3 == 0 && var4.comp_4795() == BindConfig.getInstance().getBindKey() && this.canOpenClickGui()) {
            ClickGui.INSTANCE.openGui();
         }

         EventManager.callEvent(new KeyEvent(this.field_1678.field_1755, class_307.field_1668, var4.comp_4795(), var3));
      }
   }

   private boolean canOpenClickGui() {
      return this.field_1678.field_1687 == null || this.field_1678.field_1724 == null ? false : this.field_1678.field_1755 == null;
   }
}
