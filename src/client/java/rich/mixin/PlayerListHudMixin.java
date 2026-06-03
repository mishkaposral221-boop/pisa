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
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
   private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
   private static final Map<String, UUID> UUID_CACHE = new HashMap<>();
   private static final long UUID_CACHE_CLEAR_TIME = 300000L;
   private static long lastCacheCleared = 0L;
   private static final Identifier ICON_TEXTURE = Identifier.of("rich", "icon.png");

   @Inject(method = "collectPlayerEntries", at = @At("RETURN"), cancellable = true)
   private void addVanishedEntries(CallbackInfoReturnable<List<PlayerListEntry>> var1) {
      MinecraftClient var2 = MinecraftClient.getInstance();
      List var3 = (List)var1.getReturnValue();
      if (var2.world != null && var2.player != null) {
         Scoreboard var4 = var2.world.getScoreboard();
         HashSet var5 = new HashSet();

         for (PlayerListEntry var7 : var2.player.networkHandler.getPlayerList()) {
            if (var7.getProfile() != null) {
               var5.add(var7.getProfile().name());
            }
         }

         ArrayList var16 = new ArrayList();
         ArrayList var17 = new ArrayList(var4.getTeams());
         var17.sort(Comparator.comparing(Team::getName));

         for (Team var9 : var17) {
            Collection var10 = var9.getPlayerList();
            if (var10.size() == 1) {
               String var11 = (String)var10.iterator().next();
               if (NAME_PATTERN.matcher(var11).matches() && !var5.contains(var11)) {
                  UUID var12 = UUID_CACHE.computeIfAbsent(var11, var0 -> UUID.randomUUID());
                  MutableText var13 = Text.empty()
                     .append(Text.literal("[").formatted(Formatting.GRAY))
                     .append(Text.literal("V").formatted(Formatting.RED))
                     .append(Text.literal("] ").formatted(Formatting.GRAY))
                     .append(var9.getPrefix())
                     .append(Text.literal(var11).formatted(Formatting.GRAY));
                  GameProfile var14 = new GameProfile(var12, var11);
                  PlayerListEntry var15 = new PlayerListEntry(var14, var2.isInSingleplayer());
                  var15.setDisplayName(var13);
                  var15.setListOrder(Integer.MIN_VALUE);
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
