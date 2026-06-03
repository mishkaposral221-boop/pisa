package rich.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import rich.client.draggables.AbstractHudElement;
import rich.theme.ClientTheme;
import rich.util.animations.Direction;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class Potions extends AbstractHudElement {
   private List<StatusEffectInstance> effectsList = new ArrayList<>();
   private Map<String, Float> effectAnimations = new LinkedHashMap<>();
   private Map<String, StatusEffectInstance> cachedEffects = new LinkedHashMap<>();
   private Set<String> activeEffectIds = new HashSet<>();
   private float animatedWidth = 80.0F;
   private float animatedHeight = 23.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private long lastEffectChange = 0L;
   private String currentRandomEffect = "speed";
   private static final List<String> RANDOM_EFFECTS = List.of(
      "speed",
      "slowness",
      "haste",
      "mining_fatigue",
      "strength",
      "jump_boost",
      "regeneration",
      "resistance",
      "fire_resistance",
      "water_breathing",
      "invisibility",
      "night_vision",
      "hunger",
      "weakness",
      "poison",
      "wither",
      "health_boost",
      "absorption"
   );
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float ICON_SIZE = 9.0F;
   private static final int BLINK_THRESHOLD_TICKS = 100;

   public Potions() {
      super("Potions", 300, 100, 80, 23, true);
      this.stopAnimation();
   }

   @Override
   public boolean visible() {
      return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
   }

   @Override
   public void tick() {
      if (this.mc.player == null) {
         this.effectsList = new ArrayList<>();
         this.activeEffectIds.clear();
         this.stopAnimation();
      } else {
         Collection var1 = this.mc.player.getStatusEffects();
         this.effectsList = new ArrayList<>(var1.stream().filter(StatusEffectInstance::shouldShowIcon).toList());
         this.activeEffectIds.clear();

         for (StatusEffectInstance var3 : this.effectsList) {
            String var4 = this.getEffectId(var3);
            this.activeEffectIds.add(var4);
            this.cachedEffects.put(var4, var3);
            if (!this.effectAnimations.containsKey(var4)) {
               this.effectAnimations.put(var4, 0.0F);
            }
         }

         boolean var6 = !this.activeEffectIds.isEmpty() || !this.effectAnimations.isEmpty();
         boolean var7 = this.isChat(this.mc.currentScreen);
         if (!var6 && !var7) {
            this.stopAnimation();
         } else {
            this.startAnimation();
         }

         if (this.effectsList.isEmpty() && var7) {
            long var8 = System.currentTimeMillis();
            if (var8 - this.lastEffectChange >= 1000L) {
               this.currentRandomEffect = RANDOM_EFFECTS.get(new Random().nextInt(RANDOM_EFFECTS.size()));
               this.lastEffectChange = var8;
            }
         }
      }
   }

   private String getEffectId(StatusEffectInstance var1) {
      return var1.getEffectType().getKey().map(var0 -> var0.getValue().toString()).orElse("unknown_" + var1.hashCode());
   }

   private float lerp(float var1, float var2, float var3) {
      float var4 = (float)(1.0 - Math.pow(0.001, var3 * 8.0F));
      return var1 + (var2 - var1) * var4;
   }

   private String formatDuration(int var1) {
      if (var1 == -1) {
         return "∞∞:∞∞";
      }

      int var2 = var1 / 20;
      int var3 = var2 / 60;
      int var4 = var2 % 60;
      return String.format("%02d:%02d", var3, var4);
   }

   private String getEffectName(StatusEffectInstance var1) {
      return ((StatusEffect)var1.getEffectType().value()).getName().getString();
   }

   private String getLevelText(int var1) {
      return var1 <= 0 ? "" : "LVL " + (var1 + 1);
   }

   private float getFullNameWidth(StatusEffectInstance var1) {
      String var2 = this.getEffectName(var1);
      int var3 = var1.getAmplifier();
      float var4 = Fonts.BOLD.getWidth(var2, 6.0F);
      if (var3 > 0) {
         String var5 = this.getLevelText(var3);
         float var6 = Fonts.REGULAR.getWidth(var5, 6.0F);
         return var4 + 3.0F + var6;
      } else {
         return var4;
      }
   }

   private Identifier getEffectTexture(RegistryEntry<StatusEffect> var1) {
      return var1.getKey()
         .<Identifier>map(RegistryKey::getValue)
         .map(var0 -> var0.withPrefixedPath("mob_effect/"))
         .orElse(Identifier.ofVanilla("mob_effect/speed"));
   }

   private Identifier getRandomEffectTexture() {
      return Identifier.ofVanilla("mob_effect/" + this.currentRandomEffect);
   }

   private int getBlinkAlpha(int var1, int var2) {
      if (var1 != -1 && var1 <= 100) {
         long var3 = System.currentTimeMillis();
         double var5 = 0.008;
         double var7 = Math.sin(var3 * var5);
         float var9 = (float)((var7 + 1.0) / 2.0);
         int var10 = Math.max(50, var2 - 150);
         return (int)(var10 + (var2 - var10) * (1.0F - var9));
      } else {
         return var2;
      }
   }

   @Override
   public void drawDraggable(DrawContext var1, int var2) {
      if (var2 > 0) {
         float var3 = var2 / 255.0F;
         long var4 = System.currentTimeMillis();
         float var6 = (float)(var4 - this.lastUpdateTime) / 1000.0F;
         this.lastUpdateTime = var4;
         var6 = Math.min(var6, 0.1F);
         ArrayList var7 = new ArrayList();

         for (Entry var9 : this.effectAnimations.entrySet()) {
            String var10 = (String)var9.getKey();
            float var11 = (Float)var9.getValue();
            float var12 = this.activeEffectIds.contains(var10) ? 1.0F : 0.0F;
            float var13 = this.lerp(var11, var12, var6);
            if (Math.abs(var13 - var12) < 0.01F) {
               var13 = var12;
            }

            if (var13 <= 0.01F && var12 == 0.0F) {
               var7.add(var10);
            } else {
               this.effectAnimations.put(var10, var13);
            }
         }

         for (String var50 : var7) {
            this.effectAnimations.remove(var50);
            this.cachedEffects.remove(var50);
         }

         float var49 = this.getX();
         float var51 = this.getY();
         boolean var52 = !this.effectAnimations.isEmpty();
         boolean var53 = !var52 && this.isChat(this.mc.currentScreen);
         int var54 = 23;
         float var55 = 80.0F;
         String var14 = "00:00";
         if (var53) {
            var54 += 11;
            String var15 = "Example Effect";
            String var16 = "LVL";
            float var17 = Fonts.BOLD.getWidth(var14, 6.0F);
            float var18 = Fonts.BOLD.getWidth(var15, 6.0F);
            float var19 = Fonts.REGULAR.getWidth(var16, 6.0F);
            var55 = Math.max(var18 + 3.0F + var19 + var17 + 60.0F, var55);
         } else if (var52) {
            for (Entry var58 : this.effectAnimations.entrySet()) {
               String var60 = (String)var58.getKey();
               float var62 = (Float)var58.getValue();
               if (!(var62 <= 0.0F)) {
                  StatusEffectInstance var64 = this.cachedEffects.get(var60);
                  if (var64 != null) {
                     var54 += (int)(var62 * 11.0F);
                     String var20 = this.formatDuration(var64.getDuration()) + "";
                     float var21 = Fonts.BOLD.getWidth(var20, 6.0F);
                     float var22 = this.getFullNameWidth(var64);
                     var55 = Math.max(var22 + var21 + 60.0F, var55);
                  }
               }
            }
         }

         float var57 = var54 + 2;
         this.animatedWidth = this.lerp(this.animatedWidth, var55, var6);
         this.animatedHeight = this.lerp(this.animatedHeight, var57, var6);
         if (Math.abs(this.animatedWidth - var55) < 0.3F) {
            this.animatedWidth = var55;
         }

         if (Math.abs(this.animatedHeight - var57) < 0.3F) {
            this.animatedHeight = var57;
         }

         this.setWidth((int)Math.ceil(this.animatedWidth));
         this.setHeight((int)Math.ceil(this.animatedHeight));
         float var59 = this.animatedHeight;
         int var61 = (int)(255.0F * var3);
         if (var59 > 0.0F) {
            Render2D.gradientRect(var49, var51, this.getWidth(), var59, ClientTheme.bgGradient(var61), 5.0F);
            Render2D.outline(var49, var51, this.getWidth(), var59, 0.35F, ClientTheme.outline(var61), 5.0F);
         }

         Scissor.enable(var49, var51, this.getWidth(), var59, 2.0F);
         int var63 = this.activeEffectIds.isEmpty() ? 1 : this.activeEffectIds.size();
         String var65 = String.valueOf(var63);
         float var66 = Fonts.BOLD.getWidth(var65, 6.0F);
         float var67 = Fonts.BOLD.getWidth("Potions", 6.0F);
         Render2D.gradientRect(var49 + this.getWidth() - var66 - var67 + 3.0F, var51 + 5.0F, 14.0F, 12.0F, ClientTheme.panelGradient(var61), 3.0F);
         Fonts.HUD_ICONS.draw("f", var49 + this.getWidth() - var66 - var67 + 5.0F, var51 + 6.0F, 10.0F, new Color(165, 165, 165, var61).getRGB());
         Fonts.BOLD.draw("Potions", var49 + 8.0F, var51 + 6.5F, 6.0F, new Color(255, 255, 255, var61).getRGB());
         int var68 = 23;
         if (var53) {
            String var23 = "Example Effect";
            String var24 = "LVL";
            String var25 = "00:00";
            float var26 = Fonts.BOLD.getWidth(var25, 6.0F);
            float var27 = var49 + this.getWidth() - var26 - 11.5F;
            Render2D.gradientRect(var27, var51 + var68 - 2.0F, var26 + 4.0F, 9.0F, ClientTheme.panelGradient(var61), 3.0F);
            Render2D.outline(var27, var51 + var68 - 2.0F, var26 + 4.0F, 9.0F, 0.05F, ClientTheme.outline(var61), 2.0F);
            Identifier var28 = this.getRandomEffectTexture();
            float var29 = 0.5F;
            float var30 = var49 + 8.0F;
            float var31 = var51 + var68 - 2.5F;
            var1.getMatrices().pushMatrix();
            var1.getMatrices().translate(var30, var31);
            var1.getMatrices().scale(var29, var29);
            var1.drawGuiTexture(RenderPipelines.GUI_TEXTURED, var28, 0, 0, 18, 18);
            var1.getMatrices().popMatrix();
            float var32 = var49 + 20.0F;
            Fonts.BOLD.draw(var23, var32, var51 + var68 - 1.5F, 6.0F, new Color(255, 255, 255, var61).getRGB());
            float var33 = Fonts.BOLD.getWidth(var23, 6.0F);
            Fonts.TEST.draw(var24, var32 + var33 + 2.0F, var51 + var68 - 0.5F, 5.0F, new Color(155, 155, 155, var61).getRGB());
            Fonts.BOLD.draw(var25, var27 + 2.0F, var51 + var68 - 1.0F, 6.0F, new Color(165, 165, 165, var61).getRGB());
         } else if (var52) {
            for (Entry var70 : this.effectAnimations.entrySet()) {
               String var71 = (String)var70.getKey();
               float var72 = (Float)var70.getValue();
               if (!(var72 <= 0.0F)) {
                  StatusEffectInstance var73 = this.cachedEffects.get(var71);
                  if (var73 != null) {
                     String var74 = this.getEffectName(var73);
                     int var75 = var73.getAmplifier();
                     String var76 = this.getLevelText(var75);
                     String var77 = this.formatDuration(var73.getDuration()) + "";
                     int var78 = var73.getDuration();
                     float var79 = Fonts.BOLD.getWidth(var77, 6.0F);
                     int var34 = (int)(255.0F * var72 * var3);
                     int var35 = this.getBlinkAlpha(var78, var34);
                     int var36 = new Color(255, 255, 255, var35).getRGB();
                     int var37 = new Color(155, 155, 155, var35).getRGB();
                     int var38 = new Color(165, 165, 165, var35).getRGB();
                     float var39 = var49 + this.getWidth() - var79 - 11.5F;
                     Render2D.gradientRect(var39, var51 + var68 - 2.0F, var79 + 4.0F, 9.0F, ClientTheme.panelGradient(var35), 3.0F);
                     Render2D.outline(var39, var51 + var68 - 2.0F, var79 + 4.0F, 9.0F, 0.05F, ClientTheme.outline(var35), 2.0F);
                     Identifier var40 = this.getEffectTexture(var73.getEffectType());
                     float var41 = 0.5F;
                     float var42 = var49 + 8.0F;
                     float var43 = var51 + var68 - 2.5F;
                     var1.getMatrices().pushMatrix();
                     var1.getMatrices().translate(var42, var43);
                     var1.getMatrices().scale(var41, var41);
                     int var44 = new Color(255, 255, 255, var35).getRGB();
                     var1.drawGuiTexture(RenderPipelines.GUI_TEXTURED, var40, 0, 0, 18, 18, var44);
                     var1.getMatrices().popMatrix();
                     float var45 = var49 + 20.0F;
                     Fonts.BOLD.draw(var74, var45, var51 + var68 - 1.5F, 6.0F, var36);
                     if (var75 > 0) {
                        float var46 = Fonts.BOLD.getWidth(var74, 6.0F);
                        Fonts.TEST.draw(var76, var45 + var46 + 2.0F, var51 + var68 - 0.5F, 5.0F, var37);
                     }

                     Fonts.BOLD.draw(var77, var39 + 2.0F, var51 + var68 - 1.0F, 6.0F, var38);
                     var68 += (int)(var72 * 11.0F);
                  }
               }
            }
         }

         Scissor.disable();
      }
   }
}
