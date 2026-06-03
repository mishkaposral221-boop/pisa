package rich.util.render.gif;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import rich.util.render.Render2D;

public class GifRender {
   private static final List<Identifier> avatarFrames = new ArrayList<>();
   private static final List<Identifier> backgroundFrames = new ArrayList<>();
   private static long lastAvatarTime = 0L;
   private static long lastBackgroundTime = 0L;
   private static int avatarFrameIndex = 0;
   private static int backgroundFrameIndex = 0;
   private static final long AVATAR_DELAY = 33L;
   private static final long BACKGROUND_DELAY = 50L;
   private static boolean initialized = false;

   public static void init() {
      if (!initialized) {
         avatarFrames.clear();
         backgroundFrames.clear();

         for (int var0 = 1; var0 <= 100; var0++) {
            String var1 = String.format("image%03d", var0);
            Identifier var2 = Identifier.of("rich", "images/gifs/avatar/" + var1 + ".png");
            avatarFrames.add(var2);
         }

         for (int var3 = 0; var3 <= 16; var3++) {
            String var4 = String.format("frame_%02d_delay-0.05s", var3);
            Identifier var5 = Identifier.of("rich", "images/gifs/back/" + var4 + ".png");
            backgroundFrames.add(var5);
         }

         lastAvatarTime = System.currentTimeMillis();
         lastBackgroundTime = System.currentTimeMillis();
         initialized = true;
      }
   }

   public static void tick() {
      if (initialized) {
         long var0 = System.currentTimeMillis();
         if (!avatarFrames.isEmpty() && var0 - lastAvatarTime >= 33L) {
            avatarFrameIndex = (avatarFrameIndex + 1) % avatarFrames.size();
            lastAvatarTime = var0;
         }

         if (!backgroundFrames.isEmpty() && var0 - lastBackgroundTime >= 50L) {
            backgroundFrameIndex = (backgroundFrameIndex + 1) % backgroundFrames.size();
            lastBackgroundTime = var0;
         }
      }
   }

   public static void drawAvatar(float var0, float var1, float var2, float var3, int var4) {
      if (!initialized) {
         init();
      }

      if (!avatarFrames.isEmpty()) {
         Render2D.texture(avatarFrames.getName(avatarFrameIndex), var0, var1, var2, var3, 1.0F, 15.0F, var4);
      }
   }

   public static void drawAvatar(float var0, float var1, float var2, float var3, float var4, int var5) {
      if (!initialized) {
         init();
      }

      if (!avatarFrames.isEmpty()) {
         Render2D.texture(avatarFrames.getName(avatarFrameIndex), var0, var1, var2, var3, 1.0F, var4, var5);
      }
   }

   public static void drawBackground(float var0, float var1, float var2, float var3, int var4) {
      if (!initialized) {
         init();
      }

      if (!backgroundFrames.isEmpty()) {
         Render2D.texture(backgroundFrames.getName(backgroundFrameIndex), var0, var1, var2, var3, var4);
      }
   }

   public static void drawBackground(float var0, float var1, float var2, float var3, float var4, int var5) {
      if (!initialized) {
         init();
      }

      if (!backgroundFrames.isEmpty()) {
         Render2D.texture(backgroundFrames.getName(backgroundFrameIndex), var0, var1, var2, var3, 1.0F, var4, var5);
      }
   }

   public static void resetAvatar() {
      avatarFrameIndex = 0;
      lastAvatarTime = System.currentTimeMillis();
   }

   public static void resetBackground() {
      backgroundFrameIndex = 0;
      lastBackgroundTime = System.currentTimeMillis();
   }

   public static void reset() {
      resetAvatar();
      resetBackground();
   }
}
