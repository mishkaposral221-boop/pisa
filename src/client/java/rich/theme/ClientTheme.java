package rich.theme;

import java.awt.Color;
import java.util.function.Consumer;
import rich.modules.impl.render.Arrows;
import rich.modules.impl.render.BlockOverlay;
import rich.modules.impl.render.ChinaHat;
import rich.modules.impl.render.CustomFog;
import rich.modules.impl.render.CustomSky;
import rich.modules.impl.render.Esp;
import rich.modules.impl.render.GhostTrail;
import rich.modules.impl.render.GroundPulse;
import rich.modules.impl.render.HitEffect;
import rich.modules.impl.render.JumpCircle;
import rich.modules.impl.render.Particles;
import rich.modules.impl.render.TargetESP;
import rich.modules.impl.render.WorldParticles;
import rich.modules.module.ModuleStructure;
import rich.util.c;

public class ClientTheme {
   private static ClientTheme.Theme current = ClientTheme.Theme.DARK;

   public static ClientTheme.Theme get() {
      return current;
   }

   public static void set(ClientTheme.Theme var0) {
      current = var0;
      applyToModules();
   }

   public static void setQuiet(ClientTheme.Theme var0) {
      current = var0;
   }

   public static int accent(int var0) {
      Color var1 = current.accent;
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var0).getRGB();
   }

   public static Color accentColor() {
      return current.accent;
   }

   public static void applyToModules() {
      int var0 = current.accent.getRGB();
      int var1 = brighten(var0, 1.35F);
      getTickProgress(HitEffect.class, var1x -> var1x.colorSetting.setColor(var0));
      getTickProgress(Arrows.class, var1x -> var1x.arrowColor.setColor(var0));
      getTickProgress(Particles.class, var1x -> var1x.color.setColor(var0));
      getTickProgress(WorldParticles.class, var1x -> var1x.cubeColor.setColor(var0));
      getTickProgress(Esp.class, var1x -> var1x.boxColor.setColor(var0));
      getTickProgress(JumpCircle.class, var2 -> var2.applyThemeColors(var0, var1));
      getTickProgress(TargetESP.class, var2 -> var2.applyThemeColors(var0, var1));
      getTickProgress(ChinaHat.class, var2 -> {
         var2.color1.setColor(var0);
         var2.color2.setColor(var1);
         var2.color3.setColor(brighten(var0, 1.5F));
         var2.color4.setColor(brighten(var0, 1.75F));
      });
      getTickProgress(GroundPulse.class, var1x -> var1x.colorSetting.setColor(var0));
      getTickProgress(GhostTrail.class, var1x -> var1x.color.setColor(var0));
      getTickProgress(CustomSky.class, var1x -> var1x.color.setColor(var0));
      getTickProgress(CustomFog.class, var1x -> var1x.color.setColor(var0));
      getTickProgress(BlockOverlay.class, var1x -> var1x.applyThemeColor(var0));
   }

   private static <T extends ModuleStructure> void getTickProgress(Class<T> var0, Consumer<T> var1) {
      try {
         ModuleStructure var2 = c.a(var0);
         if (var2 != null) {
            var1.accept(var0.cast(var2));
         }
      } catch (Exception var3) {
      }
   }

   private static int brighten(int var0, float var1) {
      int var2 = Math.min(255, (int)((var0 >> 16 & 0xFF) * var1));
      int var3 = Math.min(255, (int)((var0 >> 8 & 0xFF) * var1));
      int var4 = Math.min(255, (int)((var0 & 0xFF) * var1));
      return var0 & 0xFF000000 | var2 << 16 | var3 << 8 | var4;
   }

   public static int bg(int var0) {
      Color var1 = current.bgPrimary;
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var0).getRGB();
   }

   public static int bgSecondary(int var0) {
      Color var1 = current.bgSecondary;
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var0).getRGB();
   }

   public static int panel(int var0) {
      Color var1 = current.panelBg;
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var0).getRGB();
   }

   public static int outline(int var0) {
      Color var1 = current.panelOutline;
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var0).getRGB();
   }

   public static int text(int var0) {
      Color var1 = current.textPrimary;
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var0).getRGB();
   }

   public static int textSub(int var0) {
      Color var1 = current.textSecondary;
      return new Color(var1.getRed(), var1.getGreen(), var1.getBlue(), var0).getRGB();
   }

   public static int[] bgGradient(int var0) {
      int var1 = bg(var0);
      int var2 = bgSecondary(var0);
      return new int[]{var1, var1, var2, var2};
   }

   public static int[] panelGradient(int var0) {
      int var1 = panel(var0);
      int var2 = bg(var0);
      return new int[]{var1, var1, var2, var2};
   }

   public enum Theme {
      DARK(
         "Dark",
         new Color(32, 32, 34),
         new Color(24, 24, 26),
         new Color(60, 60, 65),
         new Color(110, 110, 115),
         new Color(255, 255, 255),
         new Color(185, 185, 185),
         new Color(137, 97, 72)
      ),
      MIDNIGHT(
         "Midnight",
         new Color(18, 18, 32),
         new Color(13, 13, 24),
         new Color(42, 42, 72),
         new Color(80, 80, 130),
         new Color(210, 215, 255),
         new Color(140, 150, 220),
         new Color(100, 120, 220)
      ),
      ROSE(
         "Rose",
         new Color(38, 24, 30),
         new Color(28, 16, 22),
         new Color(85, 45, 58),
         new Color(145, 70, 95),
         new Color(255, 200, 215),
         new Color(225, 130, 160),
         new Color(220, 80, 120)
      ),
      FOREST(
         "Forest",
         new Color(20, 32, 20),
         new Color(14, 22, 14),
         new Color(40, 70, 40),
         new Color(65, 120, 65),
         new Color(185, 240, 185),
         new Color(110, 190, 110),
         new Color(70, 180, 70)
      ),
      OCEAN(
         "Ocean",
         new Color(14, 26, 40),
         new Color(10, 18, 30),
         new Color(28, 65, 105),
         new Color(45, 100, 160),
         new Color(160, 215, 255),
         new Color(90, 165, 235),
         new Color(50, 140, 220)
      ),
      AMBER(
         "Amber",
         new Color(34, 26, 12),
         new Color(24, 18, 8),
         new Color(80, 58, 16),
         new Color(145, 105, 30),
         new Color(255, 220, 120),
         new Color(225, 175, 80),
         new Color(220, 160, 40)
      ),
      VIOLET(
         "Violet",
         new Color(26, 16, 40),
         new Color(18, 10, 28),
         new Color(65, 35, 100),
         new Color(110, 60, 165),
         new Color(225, 185, 255),
         new Color(175, 115, 240),
         new Color(160, 80, 240)
      ),
      MONO(
         "Mono",
         new Color(28, 28, 28),
         new Color(20, 20, 20),
         new Color(58, 58, 58),
         new Color(100, 100, 100),
         new Color(240, 240, 240),
         new Color(165, 165, 165),
         new Color(180, 180, 180)
      ),
      CHERRY(
         "Cherry",
         new Color(40, 10, 18),
         new Color(28, 6, 12),
         new Color(100, 20, 40),
         new Color(180, 40, 70),
         new Color(255, 180, 195),
         new Color(240, 100, 130),
         new Color(255, 50, 90)
      ),
      NEON(
         "Neon",
         new Color(8, 8, 16),
         new Color(4, 4, 10),
         new Color(20, 20, 45),
         new Color(0, 200, 255),
         new Color(200, 255, 255),
         new Color(0, 220, 255),
         new Color(0, 255, 200)
      ),
      SUNSET(
         "Sunset",
         new Color(40, 18, 10),
         new Color(28, 10, 6),
         new Color(100, 45, 20),
         new Color(200, 90, 40),
         new Color(255, 210, 160),
         new Color(255, 150, 80),
         new Color(255, 100, 30)
      ),
      ICE(
         "Ice",
         new Color(16, 24, 36),
         new Color(10, 16, 26),
         new Color(35, 55, 80),
         new Color(100, 160, 220),
         new Color(200, 230, 255),
         new Color(140, 190, 240),
         new Color(180, 220, 255)
      ),
      GOLD(
         "Gold",
         new Color(30, 22, 6),
         new Color(20, 14, 4),
         new Color(75, 55, 10),
         new Color(200, 160, 30),
         new Color(255, 235, 150),
         new Color(240, 200, 80),
         new Color(255, 215, 0)
      ),
      TOXIC(
         "Toxic",
         new Color(10, 24, 8),
         new Color(6, 16, 4),
         new Color(20, 60, 15),
         new Color(60, 180, 40),
         new Color(180, 255, 160),
         new Color(100, 230, 70),
         new Color(80, 255, 30)
      ),
      BLOOD(
         "Blood",
         new Color(24, 4, 4),
         new Color(16, 2, 2),
         new Color(70, 8, 8),
         new Color(160, 20, 20),
         new Color(255, 160, 160),
         new Color(220, 80, 80),
         new Color(200, 20, 20)
      );

      public final String name;
      public final Color bgPrimary;
      public final Color bgSecondary;
      public final Color panelBg;
      public final Color panelOutline;
      public final Color textPrimary;
      public final Color textSecondary;
      public final Color accent;

      Theme(String var3, Color var4, Color var5, Color var6, Color var7, Color var8, Color var9, Color var10) {
         this.name = var3;
         this.bgPrimary = var4;
         this.bgSecondary = var5;
         this.panelBg = var6;
         this.panelOutline = var7;
         this.textPrimary = var8;
         this.textSecondary = var9;
         this.accent = var10;
      }
   }
}
