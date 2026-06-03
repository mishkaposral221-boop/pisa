package rich.util.network;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.Hand;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PendingUpdateManager;
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

   public static void sendWithoutEvent(Packet<?> var0) {
      pauseEvents();
      send(var0);
      resumeEvents();
   }

   public static void send(Packet<?> var0) {
      if (mc.getNetworkHandler() != null) {
         if (var0 instanceof ClickSlotC2SPacket var1) {
            mc.interactionManager.clickSlot(var1.syncId(), var1.slot(), var1.button(), var1.actionType(), mc.player);
         } else {
            mc.getNetworkHandler().sendPacket(var0);
         }
      }
   }

   public static void sendInputPacket(boolean var0, boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6) {
      PlayerInput var7 = new PlayerInput(var0, var1, var2, var3, var4, var5, var6);
      mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(var7));
   }

   public static void sendOnlySneak(boolean var0) {
      PlayerInput var1 = mc.player.input.playerInput;
      sendInputPacket(var1.forward(), var1.backward(), var1.left(), var1.right(), var1.jump(), var0, var1.sprint());
   }

   public static void sendUse(Hand var0) {
      sendUse(var0, new Angle(mc.player.getYaw(), mc.player.getPitch()));
   }

   public static void sendUse(Hand var0, Angle var1) {
      PendingUpdateManager var2 = ((IClientWorld)mc.world).client$pending().incrementSequence();

      try {
         int var3 = var2.getSequence();
         PlayerInteractItemC2SPacket var4 = new PlayerInteractItemC2SPacket(var0, var3, var1.getYaw(), var1.getPitch());
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

   public static void sendUse(Hand var0, BlockHitResult var1) {
      PendingUpdateManager var2 = ((IClientWorld)mc.world).client$pending().incrementSequence();

      try {
         int var3 = var2.getSequence();
         PlayerInteractBlockC2SPacket var4 = new PlayerInteractBlockC2SPacket(var0, var1, var3);
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
      return mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null && mc.getNetworkHandler().getServerInfo().address.contains(var0);
   }

   public static void handleCPacket(Packet<?> var0) {
      if (var0 instanceof PlayerMoveC2SPacket var1) {
         NetworkUtility.PlayerState.lastGround = var1.isOnGround();
         NetworkUtility.PlayerState.lastVertical = mc.player.verticalCollision;
      }
   }

   public static void handleSPacket(Packet<?> var0) {
      if (var0 instanceof WorldTimeUpdateS2CPacket var1) {
         lastReceive = System.currentTimeMillis();
      }
   }

   public static void handlePacket(Packet<?> var0) {
      if (mc.getNetworkHandler() instanceof ClientPlayNetworkHandler var1) {
         if (mc.isOnThread()) {
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
