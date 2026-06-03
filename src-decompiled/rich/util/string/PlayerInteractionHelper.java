package rich.util.string;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1297;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2680;
import net.minecraft.class_2824;
import net.minecraft.class_2848;
import net.minecraft.class_2886;
import net.minecraft.class_304;
import net.minecraft.class_3532;
import net.minecraft.class_4050;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_6880;
import net.minecraft.class_7204;
import net.minecraft.class_2828.class_2830;
import net.minecraft.class_2848.class_2849;
import net.minecraft.class_3675.class_307;
import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.module.setting.implement.BindSetting;

public final class PlayerInteractionHelper implements IMinecraft {
   public static void sendSequencedPacket(class_7204 var0) {
      mc.field_1761.method_41931(mc.field_1687, var0);
   }

   public static void interactItem(class_1268 var0) {
      interactItem(var0, MathAngle.cameraAngle());
   }

   public static void interactItem(class_1268 var0, Angle var1) {
      sendSequencedPacket(var2 -> new class_2886(var0, var2, var1.getYaw(), var1.getPitch()));
   }

   public static void interactEntity(class_1297 var0) {
      mc.field_1724.field_3944.method_52787(class_2824.method_34208(var0, false, class_1268.field_5808, var0.method_5829().method_1005()));
      mc.field_1724.field_3944.method_52787(class_2824.method_34207(var0, false, class_1268.field_5808));
   }

   public static void startFallFlying() {
      mc.field_1724.field_3944.method_52787(new class_2848(mc.field_1724, class_2849.field_12982));
      mc.field_1724.method_23669();
   }

   public static void sendPacketWithOutEvent(class_2596<?> var0) {
      mc.method_1562().method_48296().method_10752(var0, null);
   }

   public static void grimSuperBypass$$$(double var0, Angle var2) {
      mc.field_1724
         .field_3944
         .method_52787(
            new class_2830(
               mc.field_1724.method_23317(),
               mc.field_1724.method_23318() + var0,
               mc.field_1724.method_23321(),
               var2.getYaw(),
               var2.getPitch(),
               mc.field_1724.method_24828(),
               mc.field_1724.field_5976
            )
         );
   }

   public static String getHealthString(float var0) {
      return String.format("%.1f", var0).replace(",", ".").replace(".0", "");
   }

   public static void jump() {
      if (mc.field_1724.method_5624()) {
         float var0 = mc.field_1724.method_36454() * (float) (Math.PI / 180.0);
         mc.field_1724.method_45319(new class_243(-class_3532.method_15374(var0) * 0.2F, 0.0, class_3532.method_15362(var0) * 0.2F));
      }

      mc.field_1724.field_64356 = true;
   }

   public static List<class_2338> getCube(class_2338 var0, float var1) {
      return getCube(var0, var1, var1, true);
   }

   public static List<class_2338> getCube(class_2338 var0, float var1, float var2) {
      return getCube(var0, var1, var2, true);
   }

   public static List<class_2338> getCube(class_2338 var0, float var1, float var2, boolean var3) {
      ArrayList var4 = new ArrayList();
      int var5 = var0.method_10263();
      int var6 = var0.method_10264();
      int var7 = var0.method_10260();
      int var8 = var3 ? var6 - (int)var2 : var6;

      for (int var9 = var5 - (int)var1; var9 <= var5 + var1; var9++) {
         for (int var10 = var7 - (int)var1; var10 <= var7 + var1; var10++) {
            for (int var11 = var8; var11 <= var6 + var2; var11++) {
               var4.add(new class_2338(var9, var11, var10));
            }
         }
      }

      return var4;
   }

   public static List<class_2338> getCube(class_2338 var0, class_2338 var1) {
      ArrayList var2 = new ArrayList();

      for (int var3 = var0.method_10263(); var3 <= var1.method_10263(); var3++) {
         for (int var4 = var0.method_10260(); var4 <= var1.method_10260(); var4++) {
            for (int var5 = var0.method_10264(); var5 <= var1.method_10264(); var5++) {
               var2.add(new class_2338(var3, var5, var4));
            }
         }
      }

      return var2;
   }

   public static class_307 getKeyType(int var0) {
      return var0 < 8 ? class_307.field_1672 : class_307.field_1668;
   }

   public static Stream<class_1297> streamEntities() {
      return StreamSupport.stream(mc.field_1687.method_18112().spliterator(), false);
   }

   public static boolean canChangeIntoPose(class_4050 var0, class_243 var1) {
      return mc.field_1724.method_73183().method_8587(mc.field_1724, mc.field_1724.method_18377(var0).method_30757(var1).method_1011(1.0E-7));
   }

   public static boolean isPotionActive(class_6880<class_1291> var0) {
      return mc.field_1724.method_6088().containsKey(var0);
   }

   public static boolean isPlayerInBlock(class_2248 var0) {
      return isBoxInBlock(mc.field_1724.method_5829().method_1014(-0.001), var0);
   }

   public static boolean isBoxInBlock(class_238 var0, class_2248 var1) {
      return isBox(var0, var1x -> mc.field_1687.method_8320(var1x).method_26204().equals(var1));
   }

   public static boolean isBoxInBlocks(class_238 var0, List<class_2248> var1) {
      return isBox(var0, var1x -> var1.contains(mc.field_1687.method_8320(var1x).method_26204()));
   }

   public static boolean isBox(class_238 var0, Predicate<class_2338> var1) {
      return class_2338.method_29715(var0).anyMatch(var1);
   }

   public static boolean isKey(BindSetting var0) {
      int var1 = var0.getKey();
      return mc.field_1755 == null && var0.isVisible() && isKey(getKeyType(var1), var1);
   }

   public static boolean isKey(class_304 var0) {
      return isKey(var0.method_1429().method_1442(), var0.method_1429().method_1444());
   }

   public static boolean isKey(class_307 var0, int var1) {
      if (var1 != -1) {
         switch (var0) {
            case field_1668:
               return GLFW.glfwGetKey(mc.method_22683().method_4490(), var1) == 1;
            case field_1672:
               return GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), var1) == 1;
         }
      }

      return false;
   }

   public static boolean isAir(class_2338 var0) {
      return isAir(mc.field_1687.method_8320(var0));
   }

   public static boolean isAir(class_2680 var0) {
      return var0.method_26215() || var0.method_26204().equals(class_2246.field_10543) || var0.method_26204().equals(class_2246.field_10243);
   }

   public static boolean isChat(class_437 var0) {
      return var0 instanceof class_408;
   }

   public static boolean nullCheck() {
      return mc.field_1724 == null || mc.field_1687 == null;
   }

   private PlayerInteractionHelper() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
