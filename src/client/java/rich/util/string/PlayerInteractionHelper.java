package rich.util.string;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.Entity;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.EntityPose;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.client.util.InputUtil.Type;
import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.module.setting.implement.BindSetting;

public final class PlayerInteractionHelper implements IMinecraft {
   public static void sendSequencedPacket(SequencedPacketCreator var0) {
      mc.interactionManager.sendSequencedPacket(mc.world, var0);
   }

   public static void interactItem(Hand var0) {
      interactItem(var0, MathAngle.cameraAngle());
   }

   public static void interactItem(Hand var0, Angle var1) {
      sendSequencedPacket(var2 -> new PlayerInteractItemC2SPacket(var0, var2, var1.getYaw(), var1.getPitch()));
   }

   public static void interactEntity(Entity var0) {
      mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interactAt(var0, false, Hand.MAIN_HAND, var0.getBoundingBox().getCenter()));
      mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(var0, false, Hand.MAIN_HAND));
   }

   public static void startFallFlying() {
      mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.START_FALL_FLYING));
      mc.player.startGliding();
   }

   public static void sendPacketWithOutEvent(Packet<?> var0) {
      mc.getNetworkHandler().getConnection().send(var0, null);
   }

   public static void grimSuperBypass$$$(double var0, Angle var2) {
      mc.player
         .networkHandler
         .sendPacket(
            new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full(
               mc.player.getX(),
               mc.player.getY() + var0,
               mc.player.getZ(),
               var2.getYaw(),
               var2.getPitch(),
               mc.player.isOnGround(),
               mc.player.horizontalCollision
            )
         );
   }

   public static String getHealthString(float var0) {
      return String.format("%.1f", var0).replace(",", ".").replace(".0", "");
   }

   public static void jump() {
      if (mc.player.isSprinting()) {
         float var0 = mc.player.getYaw() * (float) (Math.PI / 180.0);
         mc.player.addVelocityInternal(new Vec3d(-MathHelper.sin(var0) * 0.2F, 0.0, MathHelper.cos(var0) * 0.2F));
      }

      mc.player.velocityDirty = true;
   }

   public static List<BlockPos> getCube(BlockPos var0, float var1) {
      return getCube(var0, var1, var1, true);
   }

   public static List<BlockPos> getCube(BlockPos var0, float var1, float var2) {
      return getCube(var0, var1, var2, true);
   }

   public static List<BlockPos> getCube(BlockPos var0, float var1, float var2, boolean var3) {
      ArrayList var4 = new ArrayList();
      int var5 = var0.getX();
      int var6 = var0.getY();
      int var7 = var0.getZ();
      int var8 = var3 ? var6 - (int)var2 : var6;

      for (int var9 = var5 - (int)var1; var9 <= var5 + var1; var9++) {
         for (int var10 = var7 - (int)var1; var10 <= var7 + var1; var10++) {
            for (int var11 = var8; var11 <= var6 + var2; var11++) {
               var4.add(new BlockPos(var9, var11, var10));
            }
         }
      }

      return var4;
   }

   public static List<BlockPos> getCube(BlockPos var0, BlockPos var1) {
      ArrayList var2 = new ArrayList();

      for (int var3 = var0.getX(); var3 <= var1.getX(); var3++) {
         for (int var4 = var0.getZ(); var4 <= var1.getZ(); var4++) {
            for (int var5 = var0.getY(); var5 <= var1.getY(); var5++) {
               var2.add(new BlockPos(var3, var5, var4));
            }
         }
      }

      return var2;
   }

   public static net.minecraft.client.util.InputUtil.Type getKeyType(int var0) {
      return var0 < 8 ? net.minecraft.client.util.InputUtil.Type.MOUSE : net.minecraft.client.util.InputUtil.Type.KEYSYM;
   }

   public static Stream<Entity> streamEntities() {
      return StreamSupport.stream(mc.world.getEntities().spliterator(), false);
   }

   public static boolean canChangeIntoPose(EntityPose var0, Vec3d var1) {
      return mc.player.getEntityWorld().isSpaceEmpty(mc.player, mc.player.getDimensions(var0).getBoxAt(var1).contract(1.0E-7));
   }

   public static boolean isPotionActive(RegistryEntry<StatusEffect> var0) {
      return mc.player.getActiveStatusEffects().containsKey(var0);
   }

   public static boolean isPlayerInBlock(Block var0) {
      return isBoxInBlock(mc.player.getBoundingBox().expand(-0.001), var0);
   }

   public static boolean isBoxInBlock(Box var0, Block var1) {
      return isBox(var0, var1x -> mc.world.getBlockState(var1x).getBlock().equals(var1));
   }

   public static boolean isBoxInBlocks(Box var0, List<Block> var1) {
      return isBox(var0, var1x -> var1.contains(mc.world.getBlockState(var1x).getBlock()));
   }

   public static boolean isBox(Box var0, Predicate<BlockPos> var1) {
      return BlockPos.stream(var0).anyMatch(var1);
   }

   public static boolean isKey(BindSetting var0) {
      int var1 = var0.getKey();
      return mc.currentScreen == null && var0.isVisible() && isKey(getKeyType(var1), var1);
   }

   public static boolean isKey(KeyBinding var0) {
      return isKey(var0.getDefaultKey().getCategory(), var0.getDefaultKey().getCode());
   }

   public static boolean isKey(net.minecraft.client.util.InputUtil.Type var0, int var1) {
      if (var1 != -1) {
         switch (var0) {
            case KEYSYM:
               return GLFW.glfwGetKey(mc.getWindow().getHandle(), var1) == 1;
            case MOUSE:
               return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), var1) == 1;
         }
      }

      return false;
   }

   public static boolean isAir(BlockPos var0) {
      return isAir(mc.world.getBlockState(var0));
   }

   public static boolean isAir(BlockState var0) {
      return var0.isAir() || var0.getBlock().equals(Blocks.CAVE_AIR) || var0.getBlock().equals(Blocks.VOID_AIR);
   }

   public static boolean isChat(Screen var0) {
      return var0 instanceof ChatScreen;
   }

   public static boolean nullCheck() {
      return mc.player == null || mc.world == null;
   }

   private PlayerInteractionHelper() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
