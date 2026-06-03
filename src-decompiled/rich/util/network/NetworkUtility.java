package rich.util.network;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.class_10185;
import net.minecraft.class_1268;
import net.minecraft.class_2596;
import net.minecraft.class_2761;
import net.minecraft.class_2813;
import net.minecraft.class_2828;
import net.minecraft.class_2851;
import net.minecraft.class_2885;
import net.minecraft.class_2886;
import net.minecraft.class_3965;
import net.minecraft.class_634;
import net.minecraft.class_7202;
import rich.IMinecraft;
import rich.mixin.ClientConnectionAccessor;
import rich.mixin.IClientWorld;
import rich.modules.impl.combat.aura.Angle;
import rich.util.timer.TimerUtil;

public final class NetworkUtility implements IMinecraft {
   private static boolean shouldTriggerEvent = true;
   private static boolean serverSprinting = false;
   private static float tpsFactor = 0.0F;
   private static int received = 0;
   private static long lastReceive = 0L;
   private static TimerUtil tpsTimer = new TimerUtil();

   public static void pauseEvents() {
      shouldTriggerEvent = false;
   }

   public static void resumeEvents() {
      shouldTriggerEvent = true;
   }

   public static boolean shouldTriggerEvent() {
      return shouldTriggerEvent;
   }

   public static void updateServerSprint(boolean var0) {
      serverSprinting = var0;
   }

   public static boolean serverSprinting() {
      return serverSprinting;
   }

   public static void sendWithoutEvent(Runnable var0) {
      pauseEvents();
      var0.run();
      resumeEvents();
   }

   public static void sendWithoutEvent(class_2596<?> var0) {
      pauseEvents();
      send(var0);
      resumeEvents();
   }

   public static void send(class_2596<?> var0) {
      if (mc.method_1562() != null) {
         if (var0 instanceof class_2813 var1) {
            mc.field_1761.method_2906(var1.comp_3842(), var1.comp_3844(), var1.comp_3845(), var1.comp_3846(), mc.field_1724);
         } else {
            mc.method_1562().method_52787(var0);
         }
      }
   }

   public static void sendInputPacket(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6) {
      class_10185 var7 = new class_10185(var0, var1, var2, var3, var4, var5, var6);
      mc.method_1562().method_52787(new class_2851(var7));
   }

   public static void sendOnlySneak(boolean var0) {
      class_10185 var1 = mc.field_1724.field_3913.field_54155;
      sendInputPacket(var1.comp_3159(), var1.comp_3160(), var1.comp_3161(), var1.comp_3162(), var1.comp_3163(), var0, var1.comp_3165());
   }

   public static void sendUse(class_1268 var0) {
      sendUse(var0, new Angle(mc.field_1724.method_36454(), mc.field_1724.method_36455()));
   }

   public static void sendUse(class_1268 var0, Angle var1) {
      class_7202 var2 = ((IClientWorld)mc.field_1687).client$pending().method_41937();

      try {
         int var3 = var2.method_41942();
         class_2886 var4 = new class_2886(var0, var3, var1.getYaw(), var1.getPitch());
         send(var4);
      } catch (Throwable var6) {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (var2 != null) {
         var2.close();
      }
   }

   public static void sendUse(class_1268 var0, class_3965 var1) {
      class_7202 var2 = ((IClientWorld)mc.field_1687).client$pending().method_41937();

      try {
         int var3 = var2.method_41942();
         class_2885 var4 = new class_2885(var0, var1, var3);
         send(var4);
      } catch (Throwable var6) {
         if (var2 != null) {
            try {
               var2.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (var2 != null) {
         var2.close();
      }
   }

   public static boolean is(String var0) {
      return mc.method_1562() != null && mc.method_1562().method_45734() != null && mc.method_1562().method_45734().field_3761.contains(var0);
   }

   public static void handleCPacket(class_2596<?> var0) {
      if (var0 instanceof class_2828 var1) {
         NetworkUtility.PlayerState.lastGround = var1.method_12273();
         NetworkUtility.PlayerState.lastVertical = mc.field_1724.field_5992;
      }
   }

   public static void handleSPacket(class_2596<?> var0) {
      if (var0 instanceof class_2761 var1) {
         lastReceive = System.currentTimeMillis();
      }
   }

   public static void handlePacket(class_2596<?> var0) {
      if (mc.method_1562() instanceof class_634 var1) {
         if (mc.method_18854()) {
            ClientConnectionAccessor.handlePacket(var0, var1);
         } else {
            mc.execute(() -> ClientConnectionAccessor.handlePacket(var0, var1));
         }
      }
   }

   public static UUID offlineUUID(String var0) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + var0).getBytes(StandardCharsets.UTF_8));
   }

   private NetworkUtility() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static float getTpsFactor() {
      return tpsFactor;
   }

   public static final class PlayerState {
      public static boolean lastGround = false;
      public static boolean lastVertical = false;
      public static int lastTp = 0;

      private PlayerState() {
         throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
      }
   }
}
