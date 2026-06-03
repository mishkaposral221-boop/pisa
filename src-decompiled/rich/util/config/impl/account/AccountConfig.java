package rich.util.config.impl.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2960;
import rich.screens.account.AccountEntry;
import rich.util.config.impl.consolelogger.Logger;
import rich.util.session.SessionChanger;

public class AccountConfig {
   private static AccountConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;
   private final List<AccountEntry> accounts = new ArrayList<>();
   private String activeAccountName = "";
   private String activeAccountDate = "";
   private String activeAccountSkin = "";

   private AccountConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs");

      try {
         Files.createDirectories(var1);
      } catch (IOException var3) {
      }

      this.configPath = var1.resolve("accounts.json");
   }

   public static AccountConfig getInstance() {
      if (instance == null) {
         instance = new AccountConfig();
      }

      return instance;
   }

   public void save() {
      try {
         JsonObject var1 = new JsonObject();
         JsonArray var2 = new JsonArray();

         for (AccountEntry var4 : this.accounts) {
            JsonObject var5 = new JsonObject();
            var5.addProperty("name", var4.getName());
            var5.addProperty("date", var4.getDate());
            var5.addProperty("skin", var4.getSkin() != null ? var4.getSkin().toString() : "");
            var5.addProperty("pinned", var4.isPinned());
            var5.addProperty("originalIndex", var4.getOriginalIndex());
            var2.add(var5);
         }

         var1.add("accounts", var2);
         JsonObject var7 = new JsonObject();
         var7.addProperty("name", this.activeAccountName);
         var7.addProperty("date", this.activeAccountDate);
         var7.addProperty("skin", this.activeAccountSkin);
         var1.add("active", var7);
         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
         Logger.success("AccountConfig: accounts.json saved successfully!");
      } catch (IOException var6) {
         Logger.error("AccountConfig: Save failed! " + var6.getMessage());
      }
   }

   public void load() {
      try {
         if (!Files.exists(this.configPath)) {
            Logger.info("AccountConfig: No config file found, using defaults.");
            return;
         }

         String var1 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         if (var1 == null || var1.trim().isEmpty()) {
            Logger.error("AccountConfig: Config file is empty.");
            return;
         }

         JsonObject var2 = JsonParser.parseString(var1).getAsJsonObject();
         this.accounts.clear();
         if (var2.has("accounts")) {
            JsonArray var3 = var2.getAsJsonArray("accounts");

            for (int var4 = 0; var4 < var3.size(); var4++) {
               JsonObject var5 = var3.get(var4).getAsJsonObject();
               String var6 = var5.has("name") ? var5.get("name").getAsString() : "";
               String var7 = var5.has("date") ? var5.get("date").getAsString() : "";
               String var8 = var5.has("skin") ? var5.get("skin").getAsString() : "";
               boolean var9 = var5.has("pinned") && var5.get("pinned").getAsBoolean();
               int var10 = var5.has("originalIndex") ? var5.get("originalIndex").getAsInt() : var4;
               class_2960 var11 = null;
               if (!var8.isEmpty()) {
                  try {
                     var11 = class_2960.method_60654(var8);
                  } catch (Exception var13) {
                  }
               }

               AccountEntry var12 = new AccountEntry(var6, var7, var11, var9, var10);
               this.accounts.add(var12);
            }
         }

         if (var2.has("active")) {
            JsonObject var15 = var2.getAsJsonObject("active");
            this.activeAccountName = var15.has("name") ? var15.get("name").getAsString() : "";
            this.activeAccountDate = var15.has("date") ? var15.get("date").getAsString() : "";
            this.activeAccountSkin = var15.has("skin") ? var15.get("skin").getAsString() : "";
         }

         if (!this.activeAccountName.isEmpty()) {
            SessionChanger.changeUsername(this.activeAccountName);
         }

         Logger.success("AccountConfig: accounts.json loaded successfully!");
      } catch (Exception var14) {
         Logger.error("AccountConfig: Load failed! " + var14.getMessage());
      }
   }

   public List<AccountEntry> getAccounts() {
      return this.accounts;
   }

   public List<AccountEntry> getSortedAccounts() {
      ArrayList var1 = new ArrayList<>(this.accounts);
      var1.sort((var0, var1x) -> {
         if (var0.isPinned() && !var1x.isPinned()) {
            return -1;
         } else {
            return !var0.isPinned() && var1x.isPinned() ? 1 : Integer.compare(var0.getOriginalIndex(), var1x.getOriginalIndex());
         }
      });
      return var1;
   }

   public void addAccount(AccountEntry var1) {
      var1.setOriginalIndex(this.accounts.size());
      this.accounts.add(var1);
      this.save();
   }

   public void removeAccount(AccountEntry var1) {
      this.accounts.remove(var1);
      this.updateOriginalIndices();
      this.save();
   }

   public void removeAccountByIndex(int var1) {
      List var2 = this.getSortedAccounts();
      if (var1 >= 0 && var1 < var2.size()) {
         AccountEntry var3 = (AccountEntry)var2.get(var1);
         this.accounts.remove(var3);
         this.updateOriginalIndices();
         this.save();
      }
   }

   public void clearAllAccounts() {
      this.accounts.clear();
      this.activeAccountName = "";
      this.activeAccountDate = "";
      this.activeAccountSkin = "";
      this.save();
   }

   public AccountEntry getAccountBySortedIndex(int var1) {
      List var2 = this.getSortedAccounts();
      return var1 >= 0 && var1 < var2.size() ? (AccountEntry)var2.get(var1) : null;
   }

   private void updateOriginalIndices() {
      ArrayList var1 = new ArrayList();

      for (AccountEntry var3 : this.accounts) {
         if (!var3.isPinned()) {
            var1.add(var3);
         }
      }

      for (int var4 = 0; var4 < var1.size(); var4++) {
         ((AccountEntry)var1.get(var4)).setOriginalIndex(var4);
      }
   }

   public void togglePin(int var1) {
      List var2 = this.getSortedAccounts();
      if (var1 >= 0 && var1 < var2.size()) {
         AccountEntry var3 = (AccountEntry)var2.get(var1);
         var3.togglePinned();
         this.save();
      }
   }

   public String getActiveAccountName() {
      return this.activeAccountName;
   }

   public String getActiveAccountDate() {
      return this.activeAccountDate;
   }

   public class_2960 getActiveAccountSkin() {
      if (this.activeAccountSkin.isEmpty()) {
         return null;
      }

      try {
         return class_2960.method_60654(this.activeAccountSkin);
      } catch (Exception var2) {
         return null;
      }
   }

   public void setActiveAccount(String var1, String var2, class_2960 var3) {
      this.activeAccountName = var1;
      this.activeAccountDate = var2;
      this.activeAccountSkin = var3 != null ? var3.toString() : "";
      SessionChanger.changeUsername(var1);
      this.save();
   }
}
