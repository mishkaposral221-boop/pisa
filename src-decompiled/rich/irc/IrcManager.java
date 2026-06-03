package rich.irc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import rich.modules.impl.misc.IrcModule;

public class IrcManager {
   private static final String API_URL = "http://5.189.159.34:8080";
   private static final IrcManager INSTANCE = new IrcManager();
   private final List<IrcManager.IrcMessage> messages = new ArrayList<>();
   private ScheduledExecutorService scheduler;
   private long lastMessageId = 0L;
   private volatile boolean running = false;

   public static IrcManager getInstance() {
      return INSTANCE;
   }

   public List<IrcManager.IrcMessage> getMessages() {
      return this.messages;
   }

   public void start() {
      if (!this.running) {
         this.running = true;
         PlayerPrefixManager.getInstance().loadPrefixes();
         new Thread(() -> {
            try {
               HttpURLConnection var1 = (HttpURLConnection)new URL("http://5.189.159.34:8080/irc/messages?since=0").openConnection();
               var1.setRequestMethod("GET");
               var1.setConnectTimeout(4000);
               var1.setReadTimeout(4000);
               if (var1.getResponseCode() == 200) {
                  StringBuilder var2 = new StringBuilder();

                  String var4;
                  try (BufferedReader var3 = new BufferedReader(new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8))) {
                     while ((var4 = var3.readLine()) != null) {
                        var2.append(var4);
                     }
                  }

                  String var15 = var2.toString();
                  long var16 = 0L;
                  int var6 = 0;

                  while (true) {
                     if ((var6 = var15.indexOf("\"id\"", var6)) != -1) {
                        int var7 = var15.indexOf(58, var6);
                        if (var7 != -1) {
                           int var8 = var7 + 1;

                           while (var8 < var15.length() && var15.charAt(var8) == ' ') {
                              var8++;
                           }

                           int var9 = var8;

                           while (var9 < var15.length() && Character.isDigit(var15.charAt(var9))) {
                              var9++;
                           }

                           if (var9 > var8) {
                              try {
                                 var16 = Math.max(var16, Long.parseLong(var15.substring(var8, var9)));
                              } catch (Exception var12) {
                              }
                           }

                           var6 = var9;
                           continue;
                        }
                     }

                     this.lastMessageId = var16;
                     break;
                  }
               }

               var1.disconnect();
            } catch (Exception var14) {
            }

            this.scheduler = Executors.newSingleThreadScheduledExecutor(var0 -> {
               Thread var1x = new Thread(var0, "irc-poll");
               var1x.setDaemon(true);
               return var1x;
            });
            this.scheduler.scheduleAtFixedRate(this::poll, 2L, 2L, TimeUnit.SECONDS);
            this.scheduler.scheduleAtFixedRate(() -> PlayerPrefixManager.getInstance().loadPrefixes(), 30L, 30L, TimeUnit.SECONDS);
         }, "irc-init").start();
      }
   }

   public void stop() {
      this.running = false;
      if (this.scheduler != null) {
         this.scheduler.shutdownNow();
      }
   }

   public boolean isRunning() {
      return this.running;
   }

   public void sendMessage(String var1) {
      class_310 var2 = class_310.method_1551();
      if (var2.field_1724 != null) {
         String var3 = var2.field_1724.method_5477().getString();
         String var4 = PlayerPrefixManager.getInstance().getPrefix(var3);
         new Thread(() -> {
            try {
               String var3x = var1.replace("\\", "\\\\").replace("\"", "\\\"");
               String var4x = var4.replace("\\", "\\\\").replace("\"", "\\\"");
               String var5 = "{\"username\":\"" + var3 + "\",\"prefix\":\"" + var4x + "\",\"text\":\"" + var3x + "\"}";
               HttpURLConnection var6 = (HttpURLConnection)new URL("http://5.189.159.34:8080/irc/send").openConnection();
               var6.setRequestMethod("POST");
               var6.setRequestProperty("Content-Type", "application/json");
               var6.setConnectTimeout(4000);
               var6.setReadTimeout(4000);
               var6.setDoOutput(true);

               try (OutputStream var7 = var6.getOutputStream()) {
                  var7.write(var5.getBytes(StandardCharsets.UTF_8));
               }

               var6.getResponseCode();
               var6.disconnect();
            } catch (Exception var12) {
            }
         }, "irc-send").start();
      }
   }

   private void poll() {
      try {
         HttpURLConnection var1 = (HttpURLConnection)new URL("http://5.189.159.34:8080/irc/messages?since=" + this.lastMessageId).openConnection();
         var1.setRequestMethod("GET");
         var1.setConnectTimeout(4000);
         var1.setReadTimeout(4000);
         if (var1.getResponseCode() != 200) {
            var1.disconnect();
            return;
         }

         StringBuilder var2 = new StringBuilder();

         String var4;
         try (BufferedReader var3 = new BufferedReader(new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8))) {
            while ((var4 = var3.readLine()) != null) {
               var2.append(var4);
            }
         }

         var1.disconnect();
         this.parseMessages(var2.toString());
      } catch (Exception var8) {
      }
   }

   private void parseMessages(String var1) {
      int var2 = 0;
      int var3 = var1.length();

      while (var2 < var3) {
         while (var2 < var3 && var1.charAt(var2) != '{') {
            var2++;
         }

         if (var2 >= var3) {
            break;
         }

         int var4 = 0;
         int var5 = var2;
         int var6 = -1;
         boolean var7 = false;

         while (var2 < var3) {
            char var8 = var1.charAt(var2);
            if (var7) {
               if (var8 == '\\') {
                  var2 += 2;
                  continue;
               }

               if (var8 == '"') {
                  var7 = false;
               }
            } else if (var8 == '"') {
               var7 = true;
            } else if (var8 == '{') {
               var4++;
            } else if (var8 == '}') {
               if (--var4 == 0) {
                  var6 = var2++;
                  break;
               }
            }

            var2++;
         }

         if (var6 == -1) {
            break;
         }

         String var20 = var1.substring(var5, var6 + 1);
         long var9 = this.parseLong(var20, "id");
         if (var9 > this.lastMessageId) {
            this.lastMessageId = var9;
            String var11 = this.parseStr(var20, "username");
            String var12 = this.parseStr(var20, "prefix");
            String var13 = this.parseStr(var20, "text");
            long var14 = this.parseLong(var20, "time");
            this.messages.add(new IrcManager.IrcMessage(var11, var12, var13, var14));
            if (this.messages.size() > 100) {
               this.messages.remove(0);
            }

            class_310 var16 = class_310.method_1551();
            if (var16.field_1724 != null && var16.field_1705 != null) {
               IrcModule var17 = IrcModule.getInstance();
               if (var17 != null && var17.isState()) {
                  class_5250 var18 = class_2561.method_43473();
                  var18.method_10852(class_2561.method_43470("§8[§bIRC§8] "));
                  if (!var12.isEmpty()) {
                     var18.method_10852(parseMinecraftText(var12));
                     var18.method_10852(class_2561.method_43470(" "));
                  }

                  var18.method_10852(class_2561.method_43470("§f" + var11 + "§7: §f" + var13));
                  class_5250 var19 = var18;
                  var16.execute(() -> var16.field_1705.method_1743().method_1812(var19));
               }
            }
         }
      }
   }

   public static class_5250 parseMinecraftText(String var0) {
      class_5250 var1 = class_2561.method_43473();
      int var2 = 0;
      class_124 var3 = null;
      StringBuilder var4 = new StringBuilder();

      while (var2 < var0.length()) {
         char var5 = var0.charAt(var2);
         if ((var5 == 167 || var5 == '&') && var2 + 1 < var0.length()) {
            if (var4.length() > 0) {
               class_5250 var6 = class_2561.method_43470(var4.toString());
               if (var3 != null) {
                  var6.method_27692(var3);
               }

               var1.method_10852(var6);
               var4.setLength(0);
            }

            char var8 = var0.charAt(var2 + 1);
            var3 = class_124.method_544(var8);
            var2 += 2;
         } else {
            var4.append(var5);
            var2++;
         }
      }

      if (var4.length() > 0) {
         class_5250 var7 = class_2561.method_43470(var4.toString());
         if (var3 != null) {
            var7.method_27692(var3);
         }

         var1.method_10852(var7);
      }

      return var1;
   }

   private String parseStr(String var1, String var2) {
      String var3 = "\"" + var2 + "\":\"";
      int var4 = var1.indexOf(var3);
      if (var4 == -1) {
         var3 = "\"" + var2 + "\": \"";
         var4 = var1.indexOf(var3);
      }

      if (var4 == -1) {
         return "";
      }

      var4 += var3.length();
      StringBuilder var5 = new StringBuilder();
      int var6 = var4;

      while (var6 < var1.length()) {
         char var7 = var1.charAt(var6);
         if (var7 == '\\' && var6 + 1 < var1.length()) {
            char var8 = var1.charAt(var6 + 1);
            if (var8 == '"') {
               var5.append('"');
               var6 += 2;
            } else if (var8 == '\\') {
               var5.append('\\');
               var6 += 2;
            } else if (var8 == 'n') {
               var5.append('\n');
               var6 += 2;
            } else if (var8 == 'r') {
               var5.append('\r');
               var6 += 2;
            } else if (var8 == 't') {
               var5.append('\t');
               var6 += 2;
            } else {
               if (var8 == 'u' && var6 + 5 <= var1.length()) {
                  try {
                     int var9 = Integer.parseInt(var1.substring(var6 + 2, var6 + 6), 16);
                     var5.append((char)var9);
                     var6 += 6;
                     continue;
                  } catch (NumberFormatException var10) {
                  }
               }

               var5.append(var8);
               var6 += 2;
            }
         } else {
            if (var7 == '"') {
               break;
            }

            var5.append(var7);
            var6++;
         }
      }

      return var5.toString();
   }

   private long parseLong(String var1, String var2) {
      String var3 = "\"" + var2 + "\":";
      int var4 = var1.indexOf(var3);
      if (var4 == -1) {
         return 0L;
      }

      var4 += var3.length();

      while (var4 < var1.length() && var1.charAt(var4) == ' ') {
         var4++;
      }

      int var5 = var4;

      while (var5 < var1.length() && Character.isDigit(var1.charAt(var5))) {
         var5++;
      }

      if (var5 == var4) {
         return 0L;
      }

      try {
         return Long.parseLong(var1.substring(var4, var5));
      } catch (Exception var7) {
         return 0L;
      }
   }

   public record IrcMessage() {
      private final String sender;
      private final String prefix;
      private final String text;
      private final long time;

      public IrcMessage(String var1, String var2, String var3, long var4) {
         this.sender = var1;
         this.prefix = var2;
         this.text = var3;
         this.time = var4;
      }
   }
}
