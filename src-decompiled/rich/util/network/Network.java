package rich.util.network;

import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_2761;
import net.minecraft.class_3532;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import org.apache.commons.lang3.StringUtils;
import rich.IMinecraft;
import rich.events.impl.PacketEvent;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public final class Network implements IMinecraft {
   private static final StopWatch pvpWatch = new StopWatch();
   public static String server = "Vanilla";
   public static float TPS = 20.0F;
   public static long timestamp;
   public static int anarchy;
   public static boolean pvpEnd;

   public static void tick() {
      anarchy = getAnarchyMode();
      server = getServer();
      pvpEnd = inPvpEnd();
      if (inPvp()) {
         pvpWatch.reset();
      }
   }

   public static void packet(PacketEvent var0) {
      switch (var0.getPacket()) {
         case class_2761 var3:
            long var4 = System.nanoTime();
            float var6 = 20.0F;
            float var7 = var6 * (1.0E9F / (float)(var4 - timestamp));
            TPS = class_3532.method_15363(var7, 0.0F, var6);
            timestamp = var4;
         default:
      }
   }

   public static String getServer() {
      if (!PlayerInteractionHelper.nullCheck()
         && mc.method_1562() != null
         && mc.method_1562().method_45734() != null
         && mc.method_1562().method_52790() != null) {
         String var0 = mc.method_1562().method_45734().field_3761.toLowerCase();
         String var1 = mc.method_1562().method_52790().toLowerCase();
         if (var1.contains("botfilter")) {
            return "FunTime";
         } else if (var1.contains("§6spooky§ccore")) {
            return "SpookyTime";
         } else if (var0.contains("funtime") || var0.contains("skytime") || var0.contains("space-times") || var0.contains("funsky")) {
            return "CopyTime";
         } else if (var1.contains("holyworld") || var1.contains("vk.com/idwok")) {
            return "HolyWorld";
         } else if (var0.contains("reallyworld")) {
            return "ReallyWorld";
         } else {
            return var0.contains("gulpvp") ? "GulPvP" : "Vanilla";
         }
      } else {
         return "Vanilla";
      }
   }

   private static int getAnarchyMode() {
      class_269 var0 = mc.field_1687.method_8428();
      class_266 var1 = var0.method_1189(class_8646.field_45157);
      switch (server) {
         case "FunTime":
            if (var1 != null) {
               String[] var8 = var1.method_1114().getString().split("-");
               if (var8.length > 1) {
                  return Integer.parseInt(var8[1]);
               }
            }
            break;
         case "HolyWorld":
            for (class_9011 var5 : var0.method_1184(var1)) {
               String var6 = class_268.method_1142(var0.method_1164(var5.comp_2127()), var5.method_55387()).getString();
               if (!var6.isEmpty()) {
                  String var7 = StringUtils.substringBetween(var6, "#", " -◆-");
                  if (var7 != null && !var7.isEmpty()) {
                     return Integer.parseInt(var7.replace(" (1.20)", ""));
                  }
               }
            }
      }

      return -1;
   }

   public static boolean isPvp() {
      return !pvpWatch.finished(500.0);
   }

   private static boolean inPvp() {
      return mc.field_1705
         .method_1740()
         .field_2060
         .values()
         .stream()
         .map(var0 -> var0.method_5414().getString().toLowerCase())
         .anyMatch(var0 -> var0.contains("pvp") || var0.contains("пвп"));
   }

   private static boolean inPvpEnd() {
      return mc.field_1705
         .method_1740()
         .field_2060
         .values()
         .stream()
         .map(var0 -> var0.method_5414().getString().toLowerCase())
         .anyMatch(var0 -> (var0.contains("pvp") || var0.contains("пвп")) && (var0.contains("0") || var0.contains("1")));
   }

   public static String getWorldType() {
      return mc.field_1687.method_27983().method_29177().method_12832();
   }

   public static boolean isCopyTime() {
      return server.equals("CopyTime") || server.equals("SpookyTime") || server.equals("FunTime");
   }

   public static boolean isFunTime() {
      return server.equals("FunTime");
   }

   public static boolean isReallyWorld() {
      return server.equals("ReallyWorld");
   }

   public static boolean isGulPvP() {
      return server.equals("GulPvP");
   }

   public static boolean isHolyWorld() {
      return server.equals("HolyWorld");
   }

   public static boolean isSpookyTime() {
      return server.equals("SpookyTime");
   }

   public static boolean isAresMine() {
      return server.equals("aresmine");
   }

   public static boolean isVanilla() {
      return server.equals("Vanilla");
   }

   private Network() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static int getAnarchy() {
      return anarchy;
   }

   public static boolean isPvpEnd() {
      return pvpEnd;
   }
}
