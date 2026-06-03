package rich.screens.account;

import net.minecraft.class_2960;

public class AccountEntry {
   private String name;
   private String date;
   private boolean pinned;
   private int originalIndex;

   public AccountEntry(String var1, String var2) {
      this.name = var1;
      this.date = var2;
      this.pinned = false;
      this.originalIndex = -1;
   }

   public AccountEntry(String var1, String var2, class_2960 var3) {
      this.name = var1;
      this.date = var2;
      this.pinned = false;
      this.originalIndex = -1;
   }

   public AccountEntry(String var1, String var2, class_2960 var3, boolean var4) {
      this.name = var1;
      this.date = var2;
      this.pinned = var4;
      this.originalIndex = -1;
   }

   public AccountEntry(String var1, String var2, class_2960 var3, boolean var4, int var5) {
      this.name = var1;
      this.date = var2;
      this.pinned = var4;
      this.originalIndex = var5;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String var1) {
      this.name = var1;
   }

   public String getDate() {
      return this.date;
   }

   public void setDate(String var1) {
      this.date = var1;
   }

   public class_2960 getSkin() {
      return SkinManager.getSkin(this.name);
   }

   public void setSkin(class_2960 var1) {
   }

   public boolean isPinned() {
      return this.pinned;
   }

   public void setPinned(boolean var1) {
      this.pinned = var1;
   }

   public void togglePinned() {
      this.pinned = !this.pinned;
   }

   public int getOriginalIndex() {
      return this.originalIndex;
   }

   public void setOriginalIndex(int var1) {
      this.originalIndex = var1;
   }

   public void reloadSkin() {
      SkinManager.reloadSkin(this.name);
   }
}
