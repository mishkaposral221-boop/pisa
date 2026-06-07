package rich.util.config.impl.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import rich.util.config.impl.consolelogger.Logger;
import rich.util.proxy.Proxy;

public class ProxyConfig {
   private static ProxyConfig instance;
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final Path configPath;
   private boolean proxyEnabled = false;
   private Proxy defaultProxy = new Proxy();
   private Proxy lastUsedProxy = new Proxy();

   private ProxyConfig() {
      Path var1 = Paths.get("RunTime Visuals", "configs", "proxy");
      this.configPath = var1.resolve("proxy.json");
   }

   public static ProxyConfig getInstance() {
      if (instance == null) {
         instance = new ProxyConfig();
      }

      return instance;
   }

   public void save() {
      try {
         JsonObject var1 = new JsonObject();
         var1.addProperty("proxyEnabled", this.proxyEnabled);
         JsonObject var2 = new JsonObject();
         var2.addProperty("ipPort", this.defaultProxy.ipPort);
         var2.addProperty("type", this.defaultProxy.type.name());
         var2.addProperty("username", this.defaultProxy.username);
         var2.addProperty("password", this.defaultProxy.password);
         var1.add("defaultProxy", var2);
         JsonObject var3 = new JsonObject();
         var3.addProperty("ipPort", this.lastUsedProxy.ipPort);
         var3.addProperty("type", this.lastUsedProxy.type.name());
         var3.addProperty("username", this.lastUsedProxy.username);
         var3.addProperty("password", this.lastUsedProxy.password);
         var1.add("lastUsedProxy", var3);
         Files.writeString(this.configPath, this.gson.toJson(var1), StandardCharsets.UTF_8);
      } catch (IOException var4) {
         Logger.error("ProxyConfig: Save failed! " + var4.getMessage());
      }
   }

   public void load() {
      try {
         if (!Files.exists(this.configPath)) {
            this.save();
            return;
         }

         String var1 = Files.readString(this.configPath, StandardCharsets.UTF_8);
         if (var1.isEmpty()) {
            return;
         }

         JsonObject var2 = JsonParser.parseString(var1).getAsJsonObject();
         if (var2.has("proxyEnabled")) {
            this.proxyEnabled = var2.get("proxyEnabled").getAsBoolean();
         }

         if (var2.has("defaultProxy")) {
            this.defaultProxy = this.parseProxy(var2.getAsJsonObject("defaultProxy"));
         }

         if (var2.has("lastUsedProxy")) {
            this.lastUsedProxy = this.parseProxy(var2.getAsJsonObject("lastUsedProxy"));
         }

         Logger.success("ProxyConfig: proxy.json loaded successfully!");
      } catch (Exception var3) {
         Logger.error("ProxyConfig: Load failed! " + var3.getMessage());
      }
   }

   private Proxy parseProxy(JsonObject var1) {
      Proxy var2 = new Proxy();
      if (var1.has("ipPort")) {
         var2.ipPort = var1.get("ipPort").getAsString();
      }

      if (var1.has("type")) {
         try {
            var2.type = Proxy.ProxyType.valueOf(var1.get("type").getAsString());
         } catch (IllegalArgumentException var4) {
         }
      }

      if (var1.has("username")) {
         var2.username = var1.get("username").getAsString();
      }

      if (var1.has("password")) {
         var2.password = var1.get("password").getAsString();
      }

      return var2;
   }

   public void setDefaultProxyAndSave(Proxy var1) {
      this.defaultProxy = var1;
      this.save();
   }

   public void setProxyEnabledAndSave(boolean var1) {
      this.proxyEnabled = var1;
      this.save();
   }

   public void setLastUsedProxyAndSave(Proxy var1) {
      this.lastUsedProxy = var1;
      this.save();
   }

   public boolean isProxyEnabled() {
      return this.proxyEnabled;
   }

   public void setProxyEnabled(boolean var1) {
      this.proxyEnabled = var1;
   }

   public Proxy getDefaultProxy() {
      return this.defaultProxy;
   }

   public void setDefaultProxy(Proxy var1) {
      this.defaultProxy = var1;
   }

   public Proxy getLastUsedProxy() {
      return this.lastUsedProxy;
   }

   public void setLastUsedProxy(Proxy var1) {
      this.lastUsedProxy = var1;
   }
}
