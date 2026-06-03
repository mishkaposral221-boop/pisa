package rich.util.render.gif;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_1011.class_1012;
import rich.util.config.impl.profile.ProfileConfig;

public class ProfileMediaLoader {
   private static final List<class_2960> customAvatarFrames = new ArrayList<>();
   private static final List<class_2960> customBannerFrames = new ArrayList<>();
   private static String loadedAvatarPath = null;
   private static String loadedBannerPath = null;
   private static int avatarFrameIndex = 0;
   private static int bannerFrameIndex = 0;
   private static long lastAvatarTime = 0L;
   private static long lastBannerTime = 0L;
   private static long avatarDelay = 50L;
   private static long bannerDelay = 50L;
   private static int textureCounter = 0;

   public static void tick() {
      ProfileConfig var0 = ProfileConfig.getInstance();
      String var1 = var0.hasCustomAvatar() ? var0.getAvatarPath() : "";
      String var2 = var0.hasCustomBanner() ? var0.getBannerPath() : "";
      if (!var1.equals(loadedAvatarPath)) {
         unregisterFrames(customAvatarFrames);
         customAvatarFrames.clear();
         loadedAvatarPath = var1;
         if (!var1.isEmpty()) {
            loadMedia(var1, customAvatarFrames, true);
         }
      }

      if (!var2.equals(loadedBannerPath)) {
         unregisterFrames(customBannerFrames);
         customBannerFrames.clear();
         loadedBannerPath = var2;
         if (!var2.isEmpty()) {
            loadMedia(var2, customBannerFrames, false);
         }
      }

      long var3 = System.currentTimeMillis();
      if (!customAvatarFrames.isEmpty() && var3 - lastAvatarTime >= avatarDelay) {
         avatarFrameIndex = (avatarFrameIndex + 1) % customAvatarFrames.size();
         lastAvatarTime = var3;
      }

      if (!customBannerFrames.isEmpty() && var3 - lastBannerTime >= bannerDelay) {
         bannerFrameIndex = (bannerFrameIndex + 1) % customBannerFrames.size();
         lastBannerTime = var3;
      }
   }

   public static boolean hasCustomAvatar() {
      return !customAvatarFrames.isEmpty();
   }

   public static boolean hasCustomBanner() {
      return !customBannerFrames.isEmpty();
   }

   public static class_2960 getCurrentAvatarFrame() {
      return customAvatarFrames.isEmpty() ? null : customAvatarFrames.get(avatarFrameIndex % customAvatarFrames.size());
   }

   public static class_2960 getCurrentBannerFrame() {
      return customBannerFrames.isEmpty() ? null : customBannerFrames.get(bannerFrameIndex % customBannerFrames.size());
   }

   private static void loadMedia(String var0, List<class_2960> var1, boolean var2) {
      try {
         File var3 = Paths.get(var0).toFile();
         if (!var3.exists()) {
            return;
         }

         String var4 = var3.getName().toLowerCase();
         if (var4.endsWith(".gif")) {
            loadGif(var3, var1, var2);
         } else {
            loadImage(var3, var1, var2);
         }
      } catch (Exception var5) {
      }
   }

   private static void loadGif(File var0, List<class_2960> var1, boolean var2) {
      try {
         ImageInputStream var3 = ImageIO.createImageInputStream(var0);
         Iterator var4 = ImageIO.getImageReadersByFormatName("gif");
         if (!var4.hasNext()) {
            return;
         }

         ImageReader var5 = (ImageReader)var4.next();
         var5.setInput(var3);
         int var6 = var5.getNumImages(true);

         for (int var7 = 0; var7 < var6; var7++) {
            BufferedImage var8 = var5.read(var7);
            class_2960 var9 = registerBufferedImage(var8, var2 ? "avatar" : "banner");
            if (var9 != null) {
               var1.add(var9);
            }
         }

         var5.dispose();
      } catch (Exception var10) {
      }
   }

   private static void loadImage(File var0, List<class_2960> var1, boolean var2) {
      try {
         BufferedImage var3 = ImageIO.read(var0);
         if (var3 == null) {
            return;
         }

         class_2960 var4 = registerBufferedImage(var3, var2 ? "avatar" : "banner");
         if (var4 != null) {
            var1.add(var4);
         }
      } catch (Exception var5) {
      }
   }

   private static class_2960 registerBufferedImage(BufferedImage var0, String var1) {
      try {
         int var2 = var0.getWidth();
         int var3 = var0.getHeight();
         class_1011 var4 = new class_1011(class_1012.field_4997, var2, var3, false);

         for (int var5 = 0; var5 < var3; var5++) {
            for (int var6 = 0; var6 < var2; var6++) {
               int var7 = var0.getRGB(var6, var5);
               int var8 = var7 >> 24 & 0xFF;
               int var9 = var7 >> 16 & 0xFF;
               int var10 = var7 >> 8 & 0xFF;
               int var11 = var7 & 0xFF;
               var4.method_61941(var6, var5, var8 << 24 | var11 << 16 | var10 << 8 | var9);
            }
         }

         class_1043 var13 = new class_1043(() -> "rich_profile_" + var1, var4);
         class_2960 var14 = class_2960.method_60655("rich", "profile/" + var1 + "_" + textureCounter++);
         class_310.method_1551().method_1531().method_4616(var14, var13);
         return var14;
      } catch (Exception var12) {
         return null;
      }
   }

   private static void unregisterFrames(List<class_2960> var0) {
      for (class_2960 var2 : var0) {
         try {
            class_310.method_1551().method_1531().method_4615(var2);
         } catch (Exception var4) {
         }
      }
   }
}
