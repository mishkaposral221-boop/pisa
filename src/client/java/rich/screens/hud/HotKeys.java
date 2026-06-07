package rich.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.DrawContext;
import rich.Initialization;
import rich.client.draggables.AbstractHudElement;
import rich.modules.module.ModuleStructure;
import rich.theme.ClientTheme;
import rich.util.animations.Direction;
import rich.util.render.Render2D;
import rich.util.string.KeyHelper;

public class HotKeys extends AbstractHudElement {
   private List<ModuleStructure> keysList = new ArrayList<>();
   private long lastKeyChange = 0L;
   private String currentRandomKey = "NONE";
   private float animatedWidth = 80.0F;
   private float animatedHeight = 23.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final float TEXT_SCALE = 0.75F;

   public HotKeys() {
      super("HotKeys", 300, 40, 80, 23, true);
      this.stopAnimation();
   }

   @Override
   public boolean visible() {
      return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
   }

   @Override
   public void tick() {
      if (Initialization.getInstance() != null
         && Initialization.getInstance().getManager() != null
         && Initialization.getInstance().getManager().getModuleProvider() != null) {
         this.keysList = Initialization.getInstance()
            .getManager()
            .getModuleProvider()
            .getModuleStructures()
            .stream()
            .filter(var0 -> var0.isState() && var0.getKey() != -1)
            .toList();
         boolean var1 = !this.keysList.isEmpty();
         boolean var2 = this.isChat(this.mc.currentScreen);
         if (!var1 && !var2) {
            this.stopAnimation();
         } else {
            this.startAnimation();
         }

         if (!var1 && var2) {
            long var3 = System.currentTimeMillis();
            if (var3 - this.lastKeyChange >= 1000L) {
               List<String> var5 = List.of("A", "B", "C", "D", "E");
               this.currentRandomKey = var5.get(new Random().nextInt(var5.size()));
               this.lastKeyChange = var3;
            }
         }
      }
   }

   private float lerp(float var1, float var2, float var3) {
      float var4 = (float)(1.0 - Math.pow(0.001, var3 * 8.0F));
      return var1 + (var2 - var1) * var4;
   }

   private float textWidth(String var1) {
      return this.mc.textRenderer.getWidth(var1) * TEXT_SCALE;
   }

   private void drawHudText(DrawContext var1, String var2, float var3, float var4, int var5) {
      var1.getMatrices().pushMatrix();
      var1.getMatrices().translate(var3, var4);
      var1.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
      var1.drawText(this.mc.textRenderer, var2, 0, 0, var5, false);
      var1.getMatrices().popMatrix();
   }

   @Override
   public void drawDraggable(DrawContext var1, int var2) {
      if (var2 > 0) {
         float var3 = var2 / 255.0F;
         long var4 = System.currentTimeMillis();
         float var6 = (float)(var4 - this.lastUpdateTime) / 1000.0F;
         this.lastUpdateTime = var4;
         var6 = Math.min(var6, 0.1F);
         float var7 = this.getX();
         float var8 = this.getY();
         boolean var9 = !this.keysList.isEmpty();
         boolean var10 = !var9 && this.isChat(this.mc.currentScreen);
         byte var11 = 23;
         float var12 = 80.0F;
         if (var10) {
            var11 += 11;
            String var13 = "Example Module";
            String var14 = "[" + this.currentRandomKey + "]";
            float var15 = this.textWidth(var14);
            float var16 = this.textWidth(var13);
            var12 = Math.max(var16 + var15 + 50.0F, var12);
         } else {
            for (ModuleStructure var33 : this.keysList) {
               var11 += 11;
               String var35 = "[" + KeyHelper.getKeyName(var33.getKey()) + "]";
               float var37 = this.textWidth(var35);
               float var17 = this.textWidth(var33.getName());
               var12 = Math.max(var17 + var37 + 50.0F, var12);
            }
         }

         float var32 = var11 + 2;
         this.animatedWidth = this.lerp(this.animatedWidth, var12, var6);
         this.animatedHeight = this.lerp(this.animatedHeight, var32, var6);
         if (Math.abs(this.animatedWidth - var12) < 0.3F) {
            this.animatedWidth = var12;
         }

         if (Math.abs(this.animatedHeight - var32) < 0.3F) {
            this.animatedHeight = var32;
         }

         this.setWidth((int)Math.ceil(this.animatedWidth));
         this.setHeight((int)Math.ceil(this.animatedHeight));
         float var34 = this.animatedHeight;
         int var36 = (int)(255.0F * var3);
         if (var34 > 0.0F) {
            Render2D.gradientRect(var7, var8, this.getWidth(), var34, ClientTheme.bgGradient(var36), 5.0F);
            Render2D.outline(var7, var8, this.getWidth(), var34, 0.35F, ClientTheme.outline(var36), 5.0F);
         }

         long var38 = this.keysList.size();
         String var18 = String.valueOf(var38);
         float var19 = this.textWidth(var18);
         float var20 = this.textWidth("Active:");
         Render2D.gradientRect(var7 + this.getWidth() - var19 - var20 + 2.0F, var8 + 5.0F, 14.0F, 12.0F, ClientTheme.panelGradient(var36), 3.0F);
         this.drawHudText(var1, var18, var7 + this.getWidth() - var19 - var20 + 5.0F, var8 + 7.0F, new Color(165, 165, 165, var36).getRGB());
         this.drawHudText(var1, "Binds", var7 + 8.0F, var8 + 6.5F, new Color(255, 255, 255, var36).getRGB());
         byte var21 = 23;
         if (var10) {
            String var22 = "Example Module";
            String var23 = "[" + this.currentRandomKey + "]";
            float var24 = this.textWidth(var23);
            float var25 = var7 + this.getWidth() - var24 - 11.5F;
            Render2D.gradientRect(var25, var8 + var21 - 2.0F, var24 + 4.0F, 9.0F, ClientTheme.panelGradient(var36), 3.0F);
            Render2D.outline(var25, var8 + var21 - 2.0F, var24 + 4.0F, 9.0F, 0.05F, ClientTheme.outline(var36), 2.0F);
            Render2D.rect(var7 + 8.0F, var8 + var21 - 1.0F, 1.0F, 7.0F, ClientTheme.textSub((int)(128.0F * var3)), 1.0F);
            this.drawHudText(var1, var22, var7 + 13.0F, var8 + var21 - 1.5F, ClientTheme.text(var36));
            this.drawHudText(var1, var23, var25 + 2.0F, var8 + var21 - 1.0F, ClientTheme.textSub(var36));
         } else {
            for (ModuleStructure var40 : this.keysList) {
               String var41 = "[" + KeyHelper.getKeyName(var40.getKey()) + "]";
               float var42 = this.textWidth(var41);
               int var26 = ClientTheme.text(var36);
               int var27 = ClientTheme.textSub(var36);
               int var28 = ClientTheme.textSub((int)(128.0F * var3));
               float var29 = var7 + this.getWidth() - var42 - 11.5F;
               Render2D.gradientRect(var29, var8 + var21 - 2.0F, var42 + 4.0F, 9.0F, ClientTheme.panelGradient(var36), 3.0F);
               Render2D.outline(var29, var8 + var21 - 2.0F, var42 + 4.0F, 9.0F, 0.05F, ClientTheme.outline(var36), 2.0F);
               Render2D.rect(var7 + 8.0F, var8 + var21 - 1.0F, 1.0F, 7.0F, var28, 1.0F);
               this.drawHudText(var1, var40.getName(), var7 + 13.0F, var8 + var21 - 1.5F, var26);
               this.drawHudText(var1, var41, var29 + 2.0F, var8 + var21 - 1.0F, var27);
               var21 += 11;
            }
         }
      }
   }
}
