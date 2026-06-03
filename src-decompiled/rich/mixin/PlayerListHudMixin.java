package rich.mixin;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_355;
import net.minecraft.class_5250;
import net.minecraft.class_640;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_355.class)
public class PlayerListHudMixin {
   private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
   private static final Map<String, UUID> UUID_CACHE = new HashMap<>();
   private static final long UUID_CACHE_CLEAR_TIME = 300000L;
   private static long lastCacheCleared = 0L;
   private static final class_2960 ICON_TEXTURE = class_2960.method_60655("rich", "icon.png");

   @Inject(method = "method_48213", at = @At("RETURN"), cancellable = true)
   private void addVanishedEntries(CallbackInfoReturnable<List<class_640>> var1) {
      class_310 var2 = class_310.method_1551();
      List var3 = (List)var1.getReturnValue();
      if (var2.field_1687 != null && var2.field_1724 != null) {
         class_269 var4 = var2.field_1687.method_8428();
         HashSet var5 = new HashSet();

         for (class_640 var7 : var2.field_1724.field_3944.method_2880()) {
            if (var7.method_2966() != null) {
               var5.add(var7.method_2966().name());
            }
         }

         ArrayList var16 = new ArrayList();
         ArrayList var17 = new ArrayList(var4.method_1159());
         var17.sort(Comparator.comparing(class_268::method_1197));

         for (class_268 var9 : var17) {
            Collection var10 = var9.method_1204();
            if (var10.size() == 1) {
               String var11 = (String)var10.iterator().next();
               if (NAME_PATTERN.matcher(var11).matches() && !var5.contains(var11)) {
                  UUID var12 = UUID_CACHE.computeIfAbsent(var11, var0 -> UUID.randomUUID());
                  class_5250 var13 = class_2561.method_43473()
                     .method_10852(class_2561.method_43470("[").method_27692(class_124.field_1080))
                     .method_10852(class_2561.method_43470("V").method_27692(class_124.field_1061))
                     .method_10852(class_2561.method_43470("] ").method_27692(class_124.field_1080))
                     .method_10852(var9.method_1144())
                     .method_10852(class_2561.method_43470(var11).method_27692(class_124.field_1080));
                  GameProfile var14 = new GameProfile(var12, var11);
                  class_640 var15 = new class_640(var14, var2.method_1542());
                  var15.method_2962(var13);
                  var15.method_62153(Integer.MIN_VALUE);
                  var16.add(var15);
               }
            }
         }

         long var18 = System.currentTimeMillis();
         if (var18 - lastCacheCleared > 300000L) {
            UUID_CACHE.clear();
            lastCacheCleared = var18;
         }

         ArrayList var19 = new ArrayList(var16.size() + var3.size());
         var19.addAll(var16);
         var19.addAll(var3);
         var1.setReturnValue(var19);
      } else {
         var1.setReturnValue(var3);
      }
   }
}
