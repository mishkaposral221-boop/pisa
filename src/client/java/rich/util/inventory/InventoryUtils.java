package rich.util.inventory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.util.Hand;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.component.DataComponentTypes;
import rich.mixin.ClientWorldAccessor;

public final class InventoryUtils {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
   private static int savedSlot = -1;
   private static int silentSlot = -1;

   private InventoryUtils() {
   }

   public static int findItemInHotbar(Item var0) {
      if (mc.player == null) {
         return -1;
      }

      for (int var1 = 0; var1 < 9; var1++) {
         ItemStack var2 = mc.player.getInventory().getStack(var1);
         if (!var2.isEmpty() && var2.getItem() == var0) {
            return var1;
         }
      }

      return -1;
   }

   public static int findItemInInventory(Item var0) {
      if (mc.player == null) {
         return -1;
      }

      for (int var1 = 9; var1 < 36; var1++) {
         ItemStack var2 = mc.player.getInventory().getStack(var1);
         if (!var2.isEmpty() && var2.getItem() == var0) {
            return var1;
         }
      }

      return -1;
   }

   public static int findItemAnywhere(Item var0) {
      int var1 = findItemInHotbar(var0);
      return var1 != -1 ? var1 : findItemInInventory(var0);
   }

   public static InventoryResult find(Item var0) {
      return find(var1 -> var1.getItem() == var0);
   }

   public static InventoryResult find(Item... var0) {
      return find(Arrays.asList(var0));
   }

   public static InventoryResult find(List<Item> var0) {
      return find(var1 -> var0.contains(var1.getItem()));
   }

   public static boolean hasElytra() {
      return mc.player == null ? false : mc.player.getEquippedStack(EquipmentSlot.CHEST).get(DataComponentTypes.GLIDER) != null;
   }

   public static int findHotbarItem(Item var0) {
      if (mc.player == null) {
         return -1;
      }

      for (int var1 = 0; var1 < 9; var1++) {
         if (mc.player.getInventory().getStack(var1).getItem() == var0) {
            return var1;
         }
      }

      return -1;
   }

   public static int findElytraSlot() {
      if (mc.player == null) {
         return -1;
      }

      for (int var0 = 0; var0 < 46; var0++) {
         if (mc.player.getInventory().getStack(var0).getItem() == Items.ELYTRA) {
            return var0;
         }
      }

      return -1;
   }

   public static int findChestArmorSlot() {
      if (mc.player == null) {
         return -1;
      }

      for (int var0 = 0; var0 < 46; var0++) {
         ItemStack var1 = mc.player.getInventory().getStack(var0);
         EquippableComponent var2 = (EquippableComponent)var1.get(DataComponentTypes.EQUIPPABLE);
         if (var2 != null && var2.slot() == EquipmentSlot.CHEST && var1.getItem() != Items.ELYTRA) {
            return var0;
         }
      }

      return -1;
   }

   public static InventoryResult find(ItemSearcher var0) {
      if (mc.player == null) {
         return InventoryResult.notFound();
      }

      for (EquipmentSlot var4 : ARMOR_SLOTS) {
         ItemStack var5 = mc.player.getEquippedStack(var4);
         if (isValid(var5) && var0.matches(var5)) {
            return new InventoryResult(-2, true, var5);
         }
      }

      for (int var6 = 35; var6 >= 0; var6--) {
         ItemStack var7 = mc.player.getInventory().getStack(var6);
         if (isValid(var7) && var0.matches(var7)) {
            int var8 = var6 < 9 ? var6 + 36 : var6;
            return InventoryResult.of(var8, var7);
         }
      }

      return InventoryResult.notFound();
   }

   public static InventoryResult findHotbar(Item var0) {
      return findHotbar(var1 -> var1.getItem() == var0);
   }

   public static InventoryResult findHotbar(ItemSearcher var0) {
      if (mc.player == null) {
         return InventoryResult.notFound();
      }

      for (int var1 = 0; var1 < 9; var1++) {
         ItemStack var2 = mc.player.getInventory().getStack(var1);
         if (isValid(var2) && var0.matches(var2)) {
            return InventoryResult.of(var1, var2);
         }
      }

      return InventoryResult.notFound();
   }

   public static Slot findSlot(Item var0) {
      return findSlot(var1 -> var1.getStack().getItem() == var0, null);
   }

   public static Slot findSlot(Predicate<Slot> var0) {
      return findSlot(var0, null);
   }

   public static Slot findSlot(Predicate<Slot> var0, Comparator<Slot> var1) {
      if (mc.player == null) {
         return null;
      }

      Stream var2 = mc.player.currentScreenHandler.slots.stream().filter(var0);
      return var1 != null ? (Slot)var2.max(var1).orElse(null) : (Slot)var2.findFirst().orElse(null);
   }

   public static Slot findSlot(Item var0, Predicate<Slot> var1, Comparator<Slot> var2) {
      Predicate var3 = var2x -> var2x.getStack().getItem() == var0 && var1.test(var2x);
      return findSlot(var3, var2);
   }

   public static Slot findSlotInHotbar(Item var0) {
      if (mc.player == null) {
         return null;
      }

      for (int var1 = 36; var1 <= 44; var1++) {
         Slot var2 = mc.player.playerScreenHandler.getSlot(var1);
         if (var2 != null && !var2.getStack().isEmpty() && var2.getStack().getItem() == var0) {
            return var2;
         }
      }

      return null;
   }

   public static Slot findSlotInInventory(Item var0) {
      if (mc.player == null) {
         return null;
      }

      for (int var1 = 9; var1 <= 35; var1++) {
         Slot var2 = mc.player.playerScreenHandler.getSlot(var1);
         if (var2 != null && !var2.getStack().isEmpty() && var2.getStack().getItem() == var0) {
            return var2;
         }
      }

      return null;
   }

   public static Slot findSlotAnywhere(Item var0) {
      Slot var1 = findSlotInHotbar(var0);
      return var1 != null ? var1 : findSlotInInventory(var0);
   }

   public static Slot findRegularTotemSlot() {
      if (mc.player == null) {
         return null;
      }

      for (int var0 = 36; var0 <= 44; var0++) {
         Slot var1 = mc.player.playerScreenHandler.getSlot(var0);
         if (var1 != null
            && !var1.getStack().isEmpty()
            && var1.getStack().getItem() == Items.TOTEM_OF_UNDYING
            && !var1.getStack().hasEnchantments()) {
            return var1;
         }
      }

      for (int var2 = 9; var2 <= 35; var2++) {
         Slot var3 = mc.player.playerScreenHandler.getSlot(var2);
         if (var3 != null
            && !var3.getStack().isEmpty()
            && var3.getStack().getItem() == Items.TOTEM_OF_UNDYING
            && !var3.getStack().hasEnchantments()) {
            return var3;
         }
      }

      return null;
   }

   public static Slot findEnchantedTotemSlot() {
      if (mc.player == null) {
         return null;
      }

      for (int var0 = 36; var0 <= 44; var0++) {
         Slot var1 = mc.player.playerScreenHandler.getSlot(var0);
         if (var1 != null && !var1.getStack().isEmpty() && var1.getStack().getItem() == Items.TOTEM_OF_UNDYING && var1.getStack().hasEnchantments()
            )
          {
            return var1;
         }
      }

      for (int var2 = 9; var2 <= 35; var2++) {
         Slot var3 = mc.player.playerScreenHandler.getSlot(var2);
         if (var3 != null && !var3.getStack().isEmpty() && var3.getStack().getItem() == Items.TOTEM_OF_UNDYING && var3.getStack().hasEnchantments()
            )
          {
            return var3;
         }
      }

      return null;
   }

   public static Slot findTotemSlot(boolean var0) {
      if (mc.player == null) {
         return null;
      }

      Slot var1 = null;
      Slot var2 = null;

      for (int var3 = 36; var3 <= 44; var3++) {
         Slot var4 = mc.player.playerScreenHandler.getSlot(var3);
         if (var4 != null && !var4.getStack().isEmpty() && var4.getStack().getItem() == Items.TOTEM_OF_UNDYING) {
            if (!var4.getStack().hasEnchantments()) {
               if (var1 == null) {
                  var1 = var4;
               }
            } else if (var2 == null) {
               var2 = var4;
            }
         }
      }

      for (int var5 = 9; var5 <= 35; var5++) {
         Slot var6 = mc.player.playerScreenHandler.getSlot(var5);
         if (var6 != null && !var6.getStack().isEmpty() && var6.getStack().getItem() == Items.TOTEM_OF_UNDYING) {
            if (!var6.getStack().hasEnchantments()) {
               if (var1 == null) {
                  var1 = var6;
               }
            } else if (var2 == null) {
               var2 = var6;
            }
         }
      }

      if (var0) {
         return var1 != null ? var1 : var2;
      } else {
         return var1 != null ? var1 : var2;
      }
   }

   public static boolean hasEnchantedTotemInOffhand() {
      if (mc.player == null) {
         return false;
      }

      ItemStack var0 = mc.player.getOffHandStack();
      return var0.getItem() == Items.TOTEM_OF_UNDYING && var0.hasEnchantments();
   }

   public static boolean hasRegularTotemInOffhand() {
      if (mc.player == null) {
         return false;
      }

      ItemStack var0 = mc.player.getOffHandStack();
      return var0.getItem() == Items.TOTEM_OF_UNDYING && !var0.hasEnchantments();
   }

   public static void swap(int var0, int var1) {
      click(var0, 0, SlotActionType.PICKUP);
      click(var1, 0, SlotActionType.PICKUP);
      click(var0, 0, SlotActionType.PICKUP);
   }

   public static void swapHotbar(int var0, int var1) {
      click(var0, var1, SlotActionType.SWAP);
   }

   public static void swapToOffhand(int var0) {
      click(var0, 40, SlotActionType.SWAP);
   }

   public static void swapToOffhand(Slot var0) {
      if (var0 != null) {
         click(var0.id, 40, SlotActionType.SWAP);
      }
   }

   public static void swapOffhandWithSlot(int var0) {
      if (mc.player != null && mc.interactionManager != null) {
         int var1 = mc.player.playerScreenHandler.syncId;
         mc.interactionManager.clickSlot(var1, var0, 40, SlotActionType.SWAP, mc.player);
      }
   }

   public static void moveToSlot(int var0, int var1) {
      swap(var0, var1);
   }

   public static void click(int var0, int var1, SlotActionType var2) {
      if (mc.player != null && mc.interactionManager != null && var0 != -1) {
         mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, var0, var1, var2, mc.player);
      }
   }

   public static void selectSlot(int var0) {
      if (mc.player != null && var0 >= 0 && var0 <= 8) {
         if (mc.player.getInventory().getSelectedSlot() != var0) {
            mc.player.getInventory().setSelectedSlot(var0);
         }
      }
   }

   public static void selectSlotSilent(int var0) {
      if (mc.player != null && mc.getNetworkHandler() != null && var0 >= 0 && var0 <= 8) {
         mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(var0));
      }
   }

   public static void saveSlot() {
      if (mc.player != null) {
         savedSlot = mc.player.getInventory().getSelectedSlot();
      }
   }

   public static void restoreSlot() {
      if (savedSlot != -1) {
         selectSlot(savedSlot);
         savedSlot = -1;
      }
   }

   public static void restoreSlotSilent() {
      if (savedSlot != -1) {
         selectSlotSilent(savedSlot);
         savedSlot = -1;
      }
   }

   public static void silentUseHotbarItem(int var0) {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         int var1 = mc.player.getInventory().getSelectedSlot();
         if (var0 != var1) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(var0));
         }

         sendUsePacket(Hand.MAIN_HAND);
         if (var0 != var1) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(var1));
         }
      }
   }

   public static void silentSwapUseAndReturn(int var0) {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         int var1 = mc.player.getInventory().getSelectedSlot();
         click(var0, var1, SlotActionType.SWAP);
         sendUsePacket(Hand.MAIN_HAND);
         click(var0, var1, SlotActionType.SWAP);
      }
   }

   public static void silentUseItem(Item var0) {
      if (mc.player != null) {
         int var1 = findItemInHotbar(var0);
         if (var1 != -1) {
            silentUseHotbarItem(var1);
         } else {
            int var2 = findItemInInventory(var0);
            if (var2 != -1) {
               int var3 = wrapSlot(var2);
               silentSwapUseAndReturn(var3);
               closeScreen();
            }
         }
      }
   }

   public static void sendUsePacket(Hand var0) {
      if (mc.player != null && mc.getNetworkHandler() != null && mc.world != null) {
         try {
            ClientWorldAccessor var1 = (ClientWorldAccessor)mc.world;
            PendingUpdateManager var2 = var1.getPendingUpdateManager().incrementSequence();
            int var3 = var2.getSequence();
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(var0, var3, mc.player.getYaw(), mc.player.getPitch()));
            var2.close();
         } catch (Exception var4) {
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(var0, 0, mc.player.getYaw(), mc.player.getPitch()));
         }
      }
   }

   public static void use(Hand var0) {
      if (mc.player != null && mc.interactionManager != null) {
         mc.interactionManager.interactItem(mc.player, var0);
      }
   }

   public static void closeScreen() {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
      }
   }

   public static boolean isScreenOpen() {
      return mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen);
   }

   public static int wrapSlot(int var0) {
      return var0 < 9 ? var0 + 36 : var0;
   }

   public static int currentSlot() {
      return mc.player != null ? mc.player.getInventory().getSelectedSlot() : 0;
   }

   public static ItemStack offhandStack() {
      return mc.player != null ? mc.player.getOffHandStack() : ItemStack.EMPTY;
   }

   public static ItemStack mainhandStack() {
      return mc.player != null ? mc.player.getMainHandStack() : ItemStack.EMPTY;
   }

   public static boolean hasTotemInOffhand() {
      return mc.player != null && mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
   }

   public static Item getOffhandItem() {
      return mc.player != null ? mc.player.getOffHandStack().getItem() : Items.AIR;
   }

   private static boolean isValid(ItemStack var0) {
      return !var0.isEmpty() && var0.getDamage() < var0.getMaxDamage() - 10;
   }
}
