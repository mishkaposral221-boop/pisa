package rich.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.DrawContext;
import rich.Initialization;
import rich.client.draggables.AbstractHudElement;
import rich.modules.module.ModuleStructure;
import rich.util.animations.Direction;
import rich.util.string.KeyHelper;

public class HotKeys extends AbstractHudElement {
   private List<ModuleStructure> keysList = new ArrayList<>();
   private long lastKeyChange = 0L;
   private String currentRandomKey = "NONE";
   private float animatedWidth = 96.0F;
   private float animatedHeight = 24.0F;
   private long lastUpdateTime = System.currentTimeMillis();
   private static final float ANIMATION_SPEED = 8.0F;
   private static final int ROW_HEIGHT = 12;
   private static final int HEADER_HEIGHT = 24;

   public HotKeys() {
      super("HotKeys", 300, 40, 96, 24, true);
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

   private int textWidth(String var1) {
      return this.mc.textRenderer.getWidth(var1);
   }

   private int argb(int var1, int var2, int var3, int var4) {
      return new Color(var1, var2, var3, Math.max(0, Math.min(255, var4))).getRGB();
   }

   private void fill(DrawContext var1, float var2, float var3, float var4, float var5, int var6) {
      var1.fill((int)var2, (int)var3, (int)(var2 + var4), (int)(var3 + var5), var6);
   }

   private void drawText(DrawContext var1, String var2, float var3, float var4, int var5) {
      var1.drawText(this.mc.textRenderer, var2, (int)var3, (int)var4, var5, false);
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

         int var11 = HEADER_HEIGHT;
         float var12 = 96.0F;
         if (var10) {
            String var13 = "Example Module";
            String var14 = "[" + this.currentRandomKey + "]";
            var11 += ROW_HEIGHT;
            var12 = Math.max(this.textWidth(var13) + this.textWidth(var14) + 38.0F, var12);
         } else {
            for (ModuleStructure var15 : this.keysList) {
               String var16 = "[" + KeyHelper.getKeyName(var15.getKey()) + "]";
               var11 += ROW_HEIGHT;
               var12 = Math.max(this.textWidth(var15.getName()) + this.textWidth(var16) + 38.0F, var12);
            }
         }

         this.animatedWidth = this.lerp(this.animatedWidth, var12, var6);
         this.animatedHeight = this.lerp(this.animatedHeight, var11, var6);
         if (Math.abs(this.animatedWidth - var12) < 0.3F) {
            this.animatedWidth = var12;
         }

         if (Math.abs(this.animatedHeight - var11) < 0.3F) {
            this.animatedHeight = var11;
         }

         this.setWidth((int)Math.ceil(this.animatedWidth));
         this.setHeight((int)Math.ceil(this.animatedHeight));

         int var17 = (int)(230.0F * var3);
         int var18 = (int)(255.0F * var3);
         int var19 = (int)(170.0F * var3);
         int var20 = this.argb(12, 8, 12, var17);
         int var21 = this.argb(42, 19, 34, Math.min(255, var17 + 10));
         int var22 = this.argb(255, 255, 255, var18);
         int var23 = this.argb(165, 165, 165, var19);
         int var24 = this.argb(70, 35, 60, Math.min(210, var17));

         // Use only vanilla DrawContext for this HUD. This avoids the fixed-2x custom
         // Render2D/FontPipeline coordinate space that was making text disappear or show
         // only its tail on some GUI scales.
         this.fill(var1, var7, var8, this.getWidth(), this.getHeight(), var20);
         this.fill(var1, var7, var8, this.getWidth(), 1.0F, var24);
         this.fill(var1, var7, var8 + this.getHeight() - 1.0F, this.getWidth(), 1.0F, var24);
         this.fill(var1, var7, var8, 1.0F, this.getHeight(), var24);
         this.fill(var1, var7 + this.getWidth() - 1.0F, var8, 1.0F, this.getHeight(), var24);

         String var25 = String.valueOf(this.keysList.size());
         this.fill(var1, var7 + this.getWidth() - this.textWidth(var25) - 18.0F, var8 + 5.0F, this.textWidth(var25) + 10.0F, 12.0F, var21);
         this.drawText(var1, "Binds", var7 + 8.0F, var8 + 7.0F, var22);
         this.drawText(var1, var25, var7 + this.getWidth() - this.textWidth(var25) - 13.0F, var8 + 7.0F, var23);

         int var26 = HEADER_HEIGHT;
         if (var10) {
            String var27 = "Example Module";
            String var28 = "[" + this.currentRandomKey + "]";
            int var29 = this.textWidth(var28);
            float var30 = var7 + this.getWidth() - var29 - 10.0F;
            this.fill(var1, var7 + 8.0F, var8 + var26 + 1.0F, 1.0F, 7.0F, var23);
            this.drawText(var1, var27, var7 + 13.0F, var8 + var26, var22);
            this.drawText(var1, var28, var30, var8 + var26, var23);
         } else {
            for (ModuleStructure var31 : this.keysList) {
               String var32 = "[" + KeyHelper.getKeyName(var31.getKey()) + "]";
               int var33 = this.textWidth(var32);
               float var34 = var7 + this.getWidth() - var33 - 10.0F;
               this.fill(var1, var7 + 8.0F, var8 + var26 + 1.0F, 1.0F, 7.0F, var23);
               this.drawText(var1, var31.getName(), var7 + 13.0F, var8 + var26, var22);
               this.drawText(var1, var32, var34, var8 + var26, var23);
               var26 += ROW_HEIGHT;
            }
         }
      }
   }
}
