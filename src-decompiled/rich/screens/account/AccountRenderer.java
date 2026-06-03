package rich.screens.account;

import java.util.List;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import rich.util.a;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class AccountRenderer {
   private static final float BLUR_RADIUS = 15.0F;
   private static final float OUTLINE_THICKNESS = 1.0F;

   public void renderLeftPanelTop(float var1, float var2, float var3, float var4, float var5, String var6, boolean var7, float var8, float var9, long var10) {
      int var12 = (int)(var5 * 120.0F);
      int var13 = (int)(var5 * 150.0F);
      int var14 = (int)(var5 * 100.0F);
      int var15 = (int)(var5 * 80.0F);
      int var16 = (int)(var5 * 255.0F);
      int var17 = (int)(var5 * 155.0F);
      int var18 = this.withAlpha(855828, var12);
      int var19 = this.withAlpha(1053208, var12);
      int var20 = this.withAlpha(526604, var12);
      int var21 = this.withAlpha(855828, var12);
      int var22 = this.withAlpha(1316639, var13);
      int var23 = this.withAlpha(1579812, var13);
      int var24 = this.withAlpha(1053466, var13);
      int var25 = this.withAlpha(1316639, var13);
      int var26 = this.withAlpha(2435638, var14);
      int var27 = this.withAlpha(395280, var15);
      int[] var28 = new int[]{var18, var19, var21, var20};
      Render2D.gradientRect(var1, var2, var3, var4, var28, 6.0F);
      int[] var29 = new int[]{var22, var23, var25, var24};
      Render2D.gradientRect(var1, var2, var3, 22.0F, var29, 6.0F, 6.0F, 0.0F, 0.0F);
      Render2D.outline(var1, var2, var3, var4, 1.0F, var26, 6.0F);
      Fonts.BOLD.drawCentered("Account Panel", var1 + var3 / 2.0F - 15.0F, var2 + 7.0F, 8.0F, this.withAlpha(16777215, var16));
      Fonts.REGULARNEW.draw("Nickname", var1 + 5.0F, var2 + 28.0F, 5.5F, this.withAlpha(16777215, var17));
      float var30 = var1 + 5.0F;
      float var31 = var2 + 38.0F;
      float var32 = 14.0F;
      float var33 = 14.0F;
      float var34 = 3.0F;
      float var35 = var3 - 10.0F - var33 - var34;
      this.renderNicknameField(var30, var31, var35, var32, var5, var6, var7, var10);
      float var36 = var30 + var35 + var34;
      boolean var37 = this.isMouseOver(var8, var9, var36, var31, var33, var33);
      this.renderAddButton(var36, var31, var33, var5, var37, var16);
      float var38 = var3 - 10.0F;
      float var39 = 16.0F;
      float var40 = var1 + 5.0F;
      float var41 = var31 + var32 + 6.0F;
      boolean var42 = this.isMouseOver(var8, var9, var40, var41, var38, var39);
      this.renderRandomButton(var40, var41, var38, var39, var5, var42, var16);
      float var43 = var1 + 5.0F;
      float var44 = var41 + var39 + 5.0F;
      boolean var45 = this.isMouseOver(var8, var9, var43, var44, var38, var39);
      this.renderClearAllButton(var43, var44, var38, var39, var5, var45, var16);
   }

   private void renderNicknameField(float var1, float var2, float var3, float var4, float var5, String var6, boolean var7, long var8) {
      int var10 = (int)(var5 * 255.0F);
      int var11 = (int)(var5 * 155.0F);
      int var12 = (int)(var5 * 180.0F);
      int var13 = var7 ? (int)(var5 * 180.0F) : (int)(var5 * 80.0F);
      int var14 = this.withAlpha(658448, var12);
      int var15 = this.withAlpha(526862, var12);
      int[] var16 = new int[]{var14, var14, var15, var15};
      Render2D.gradientRect(var1, var2, var3, var4, var16, 3.0F);
      int var17 = var7 ? this.withAlpha(3820122, var13) : this.withAlpha(2435638, var13);
      Render2D.outline(var1, var2, var3, var4, 0.5F, var17, 3.0F);
      String var18 = var6.isEmpty() && !var7 ? "Enter nick..." : var6;
      int var19 = var6.isEmpty() && !var7 ? this.withAlpha(6318200, var11) : this.withAlpha(13685980, var10);
      Fonts.TEST.draw(var18, var1 + 4.0F, var2 + 4.5F, 5.5F, var19);
      if (var7 && var8 / 500L % 2L == 0L) {
         float var20 = var1 + 4.0F + Fonts.TEST.getWidth(var6, 5.5F);
         Render2D.rect(var20, var2 + 3.0F, 0.5F, var4 - 6.0F, this.withAlpha(13685980, var10), 0.0F);
      }
   }

   private void renderAddButton(float var1, float var2, float var3, float var4, boolean var5, int var6) {
      int var7 = var5 ? (int)(var4 * 180.0F) : (int)(var4 * 140.0F);
      int var8 = this.withAlpha(1316639, var7);
      int var9 = this.withAlpha(1579812, var7);
      int var10 = this.withAlpha(1053466, var7);
      int var11 = this.withAlpha(1316639, var7);
      int[] var12 = new int[]{var8, var9, var11, var10};
      Render2D.gradientRect(var1, var2, var3, var3, var12, 3.0F);
      Render2D.outline(var1, var2, var3, var3, 0.5F, this.withAlpha(2435638, (int)(var4 * 100.0F)), 3.0F);
      float var13 = var1 + var3 / 2.0F;
      float var14 = var2 + var3 / 2.0F;
      float var15 = 5.0F;
      float var16 = 1.2F;
      Render2D.rect(var13 - var15 / 2.0F, var14 - var16 / 2.0F, var15, var16, this.withAlpha(16777215, var6), 0.5F);
      Render2D.rect(var13 - var16 / 2.0F, var14 - var15 / 2.0F, var16, var15, this.withAlpha(16777215, var6), 0.5F);
   }

   private void renderRandomButton(float var1, float var2, float var3, float var4, float var5, boolean var6, int var7) {
      int var8 = var6 ? (int)(var5 * 200.0F) : (int)(var5 * 140.0F);
      int var9 = var6 ? this.withAlpha(1711912, var8) : this.withAlpha(1316639, var8);
      int var10 = var6 ? this.withAlpha(1975085, var8) : this.withAlpha(1579812, var8);
      int var11 = var6 ? this.withAlpha(1316895, var8) : this.withAlpha(1053466, var8);
      int var12 = var6 ? this.withAlpha(1711912, var8) : this.withAlpha(1316639, var8);
      int[] var13 = new int[]{var9, var10, var12, var11};
      Render2D.gradientRect(var1, var2, var3, var4, var13, 3.0F);
      int var14 = var6 ? this.withAlpha(3820122, (int)(var5 * 150.0F)) : this.withAlpha(2435638, (int)(var5 * 100.0F));
      Render2D.outline(var1, var2, var3, var4, 0.5F, var14, 3.0F);
      int var15 = var6 ? this.withAlpha(16777215, var7) : this.withAlpha(13687012, var7);
      Fonts.DEFAULT.draw("Random", var1 + 6.0F, var2 + 5.0F, 5.5F, var15);
      Fonts.ICONS.draw("R", var1 + 75.0F, var2 + 3.5F, 10.0F, var15);
   }

   private void renderClearAllButton(float var1, float var2, float var3, float var4, float var5, boolean var6, int var7) {
      int var8 = var6 ? (int)(var5 * 200.0F) : (int)(var5 * 140.0F);
      int var9 = var6 ? this.withAlpha(2759194, var8) : this.withAlpha(1709078, var8);
      int var10 = var6 ? this.withAlpha(3022366, var8) : this.withAlpha(1971736, var8);
      int var11 = var6 ? this.withAlpha(2364436, var8) : this.withAlpha(1445906, var8);
      int var12 = var6 ? this.withAlpha(2759194, var8) : this.withAlpha(1709078, var8);
      int[] var13 = new int[]{var9, var10, var12, var11};
      Render2D.gradientRect(var1, var2, var3, var4, var13, 3.0F);
      int var14 = var6 ? this.withAlpha(5913146, (int)(var5 * 150.0F)) : this.withAlpha(3484202, (int)(var5 * 100.0F));
      Render2D.outline(var1, var2, var3, var4, 0.5F, var14, 3.0F);
      int var15 = var6 ? this.withAlpha(16744576, var7) : this.withAlpha(13672608, var7);
      Fonts.DEFAULT.draw("Clear All", var1 + 6.0F, var2 + 5.0F, 5.5F, var15);
      Fonts.GUI_ICONS.draw("O", var1 + 77.0F, var2 + 2.5F, 11.0F, var15);
   }

   public void renderLeftPanelBottom(float var1, float var2, float var3, float var4, float var5, String var6, String var7, class_2960 var8) {
      int var9 = (int)(var5 * 120.0F);
      int var10 = (int)(var5 * 150.0F);
      int var11 = (int)(var5 * 100.0F);
      int var12 = (int)(var5 * 80.0F);
      int var13 = (int)(var5 * 255.0F);
      int var14 = (int)(var5 * 155.0F);
      int var15 = this.withAlpha(855828, var9);
      int var16 = this.withAlpha(1053208, var9);
      int var17 = this.withAlpha(526604, var9);
      int var18 = this.withAlpha(855828, var9);
      int var19 = this.withAlpha(1316639, var10);
      int var20 = this.withAlpha(1579812, var10);
      int var21 = this.withAlpha(1053466, var10);
      int var22 = this.withAlpha(1316639, var10);
      int var23 = this.withAlpha(2435638, var11);
      int var24 = this.withAlpha(395280, var12);
      Render2D.blur(var1, var2, var3, var4, 15.0F, 6.0F, var24);
      int[] var25 = new int[]{var15, var16, var18, var17};
      Render2D.gradientRect(var1, var2, var3, var4, var25, 6.0F);
      int[] var26 = new int[]{var19, var20, var22, var21};
      Render2D.gradientRect(var1, var2, var3, 22.0F, var26, 6.0F, 6.0F, 0.0F, 0.0F);
      Render2D.outline(var1, var2, var3, var4, 1.0F, var23, 6.0F);
      Fonts.BOLD.drawCentered("Active Session", var1 + var3 / 2.0F - 15.0F, var2 + 6.0F, 8.0F, this.withAlpha(16777215, var13));
      if (!var6.isEmpty()) {
         float var27 = var1 + 8.0F;
         float var28 = var2 + 28.0F;
         float var29 = 24.0F;
         class_2960 var30 = SkinManager.getSkin(var6);
         int var31 = this.withAlpha(16777215, var13);
         this.drawPlayerFace(var30, var27, var28, var29, var31);
         float var32 = var27 + var29 + 6.0F;
         float var33 = var28 + 4.0F;
         float var34 = var33 + 10.0F;
         Fonts.TEST.draw(var6, var32, var33, 6.0F, this.withAlpha(16777215, var13));
         Fonts.TEST.draw(var7, var32, var34, 4.5F, this.withAlpha(8423568, var13));
      } else {
         Fonts.REGULARNEW.drawCentered("No account selected", var1 + 50.0F, var2 + 36.0F, 5.0F, this.withAlpha(6318200, var14));
      }
   }

   public void renderRightPanel(
      float var1, float var2, float var3, float var4, float var5, List<AccountEntry> var6, float var7, float var8, float var9, float var10, int var11
   ) {
      int var12 = (int)(var5 * 120.0F);
      int var13 = (int)(var5 * 150.0F);
      int var14 = (int)(var5 * 100.0F);
      int var15 = (int)(var5 * 80.0F);
      int var16 = (int)(var5 * 255.0F);
      int var17 = (int)(var5 * 155.0F);
      int var18 = this.withAlpha(855828, var12);
      int var19 = this.withAlpha(1053208, var12);
      int var20 = this.withAlpha(526604, var12);
      int var21 = this.withAlpha(855828, var12);
      int var22 = this.withAlpha(1316639, var13);
      int var23 = this.withAlpha(1579812, var13);
      int var24 = this.withAlpha(1053466, var13);
      int var25 = this.withAlpha(1316639, var13);
      int var26 = this.withAlpha(2435638, var14);
      int var27 = this.withAlpha(395280, var15);
      Render2D.blur(var1, var2, var3, var4, 15.0F, 6.0F, var27);
      int[] var28 = new int[]{var18, var19, var21, var20};
      Render2D.gradientRect(var1, var2, var3, var4, var28, 6.0F);
      int[] var29 = new int[]{var22, var23, var25, var24};
      Render2D.gradientRect(var1, var2, var3, 22.0F, var29, 6.0F, 6.0F, 0.0F, 0.0F);
      Render2D.outline(var1, var2, var3, var4, 1.0F, var26, 6.0F);
      Fonts.BOLD.draw("Accounts List", var1 + 8.0F, var2 + 7.0F, 8.0F, this.withAlpha(16777215, var16));
      Render2D.blur(var1, var2, var3, var4, 0.0F, 0.0F, a.b(0, 0, 0, 1));
      float var30 = var1 + 5.0F;
      float var31 = var2 + 28.0F;
      float var32 = var3 - 10.0F;
      float var33 = var4 - 31.0F;
      float var34 = (var32 - 5.0F) / 2.0F;
      float var35 = 40.0F;
      float var36 = 5.0F;
      float var37 = var11 / var10;
      Scissor.enable(var30 * var10, var31 * var10, var32 * var10, var33 * var10, var37);

      for (int var38 = 0; var38 < var6.size(); var38++) {
         AccountEntry var39 = (AccountEntry)var6.get(var38);
         int var40 = var38 % 2;
         int var41 = var38 / 2;
         float var42 = var30 + var40 * (var34 + var36);
         float var43 = var31 + var41 * (var35 + var36) - var7;
         if (!(var43 + var35 < var31 - 10.0F) && !(var43 > var31 + var33 + 10.0F)) {
            this.renderAccountCard(var42, var43, var34, var35, var39, var5, var8, var9, var31, var33);
         }
      }

      Scissor.disable();
      if (var6.isEmpty()) {
         Fonts.REGULARNEW.drawCentered("No accounts added", var1 + var3 / 2.0F, var2 + var4 / 2.0F + 2.0F, 6.0F, this.withAlpha(6318200, var17));
      }
   }

   private void renderAccountCard(
      float var1, float var2, float var3, float var4, AccountEntry var5, float var6, float var7, float var8, float var9, float var10
   ) {
      int var11 = (int)(var6 * 255.0F);
      boolean var12 = this.isMouseOver(var7, var8, var1, var2, var3, var4) && var8 >= var9 && var8 <= var9 + var10;
      int var13 = var12 ? (int)(var6 * 160.0F) : (int)(var6 * 120.0F);
      int var14 = this.withAlpha(1185052, var13);
      int var15 = this.withAlpha(1448482, var13);
      int var16 = this.withAlpha(921622, var13);
      int var17 = this.withAlpha(1185052, var13);
      int[] var18 = new int[]{var14, var15, var17, var16};
      Render2D.gradientRect(var1, var2, var3, var4, var18, 4.0F);
      Render2D.blur(var1, var2, 1.0F, 1.0F, 0.0F, 0.0F, a.b(0, 0, 0, 0));
      int var19 = this.withAlpha(2435638, (int)(var6 * 80.0F));
      Render2D.outline(var1, var2, var3, var4, 0.5F, var19, 4.0F);
      float var20 = var1 + 7.0F;
      float var21 = var2 + 7.0F;
      float var22 = 25.0F;
      class_2960 var23 = SkinManager.getSkin(var5.getName());
      this.drawPlayerFace(var23, var20, var21, var22, this.withAlpha(16777215, var11));
      float var24 = var20 + var22 + 5.0F;
      float var25 = var21 + 2.0F;
      float var26 = var25 + 9.0F;
      String var27 = var5.getName();
      float var28 = var3 - var22 - 45.0F;
      if (Fonts.TEST.getWidth(var27, 7.0F) > var28) {
         while (Fonts.TEST.getWidth(var27 + "...", 7.0F) > var28 && var27.length() > 3) {
            var27 = var27.substring(0, var27.length() - 1);
         }

         var27 = var27 + "...";
      }

      Fonts.TEST.draw(var27, var24, var25, 7.0F, this.withAlpha(16777215, var11));
      Fonts.TEST.draw(var5.getDate(), var24, var26, 6.0F, this.withAlpha(7370888, var11));
      float var29 = 12.0F;
      float var30 = var2 + var4 - var29 - 5.0F;
      float var31 = var1 + var3 - var29 * 2.0F - 8.0F;
      float var32 = var1 + var3 - var29 - 5.0F;
      boolean var33 = this.isMouseOver(var7, var8, var31, var30, var29, var29) && var8 >= var9 && var8 <= var9 + var10;
      boolean var34 = this.isMouseOver(var7, var8, var32, var30, var29, var29) && var8 >= var9 && var8 <= var9 + var10;
      int var35 = var33 ? (int)(var6 * 220.0F) : (int)(var6 * 160.0F);
      int var36;
      int var37;
      if (var5.isPinned()) {
         var36 = this.withAlpha(4864528, var35);
         var37 = this.withAlpha(13934615, (int)(var6 * 180.0F));
      } else {
         var36 = this.withAlpha(1711396, var35);
         var37 = this.withAlpha(3488326, (int)(var6 * 100.0F));
      }

      int[] var38 = new int[]{var36, var36, var36, var36};
      Render2D.gradientRect(var31, var30, var29, var29, var38, 3.0F);
      Render2D.outline(var31, var30, var29, var29, 0.5F, var37, 3.0F);
      Render2D.blur(var1, var2, 1.0F, 1.0F, 0.0F, 0.0F, a.b(0, 0, 0, 0));
      int var39 = var5.isPinned() ? this.withAlpha(16766720, var11) : this.withAlpha(12634324, var11);
      Fonts.MAINMENUSCREEN.drawCentered("c", var31 + var29 / 2.0F, var30 + 1.5F, 9.0F, var39);
      int var40 = var34 ? (int)(var6 * 200.0F) : (int)(var6 * 140.0F);
      int var41 = var34 ? this.withAlpha(5909034, var40) : this.withAlpha(1711396, var40);
      int[] var42 = new int[]{var41, var41, var41, var41};
      Render2D.gradientRect(var32, var30, var29, var29, var42, 3.0F);
      Render2D.outline(var32, var30, var29, var29, 0.5F, this.withAlpha(3488326, (int)(var6 * 100.0F)), 3.0F);
      Render2D.blur(var1, var2, 1.0F, 1.0F, 0.0F, 0.0F, a.b(0, 0, 0, 0));
      int var43 = var34 ? this.withAlpha(16744576, var11) : this.withAlpha(12634324, var11);
      Fonts.GUI_ICONS.drawCentered("O", var32 + var29 / 2.0F, var30 + 0.5F, 11.0F, var43);
   }

   public void drawPlayerFace(class_2960 var1, float var2, float var3, float var4, int var5) {
      float var6 = 0.125F;
      float var7 = 0.125F;
      float var8 = 0.25F;
      float var9 = 0.25F;
      Render2D.texture(var1, var2, var3, var4, var4, var6, var7, var8, var9, var5, 0.0F, 3.0F);
      float var10 = 1.12F;
      float var11 = var4 * var10;
      float var12 = (var11 - var4) / 2.0F;
      float var13 = 0.625F;
      float var14 = 0.125F;
      float var15 = 0.75F;
      float var16 = 0.25F;
      Render2D.texture(var1, var2 - var12, var3 - var12, var11, var11, var13, var14, var15, var16, var5, 0.0F, 3.0F);
   }

   public boolean isMouseOver(float var1, float var2, float var3, float var4, float var5, float var6) {
      return var1 >= var3 && var1 <= var3 + var5 && var2 >= var4 && var2 <= var4 + var6;
   }

   public int withAlpha(int var1, int var2) {
      return var1 & 16777215 | class_3532.method_15340(var2, 0, 255) << 24;
   }
}
