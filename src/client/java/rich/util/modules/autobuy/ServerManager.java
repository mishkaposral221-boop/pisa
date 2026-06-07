package rich.util.modules.autobuy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import rich.util.timer.TimerUtil;

public class ServerManager {
   private List<String> anarchyServers165 = new ArrayList<>();
   private List<String> anarchyServers214 = new ArrayList<>();
   private int currentServerIndex = 0;
   private String currentServer = "";
   private boolean inHub = false;
   private boolean waitingForServerLoad = false;
   private TimerUtil hubCheckTimer = TimerUtil.create();
   private TimerUtil serverSwitchCooldown = TimerUtil.create();

   public ServerManager() {
      this.initializeServers();
   }

   private void initializeServers() {
      this.anarchyServers165.addAll(List.of("/an102", "/an103", "/an104", "/an105", "/an106", "/an107"));

      for (int var1 = 203; var1 <= 221; var1++) {
         this.anarchyServers165.add("/an" + var1);
      }

      for (int var2 = 302; var2 <= 313; var2++) {
         this.anarchyServers165.add("/an" + var2);
      }

      this.anarchyServers165.addAll(List.of("/an502", "/an503", "/an504", "/an505", "/an506", "/an507", "/an602"));

      for (int var3 = 11; var3 <= 14; var3++) {
         this.anarchyServers214.add("/an" + var3);
      }

      for (int var4 = 21; var4 <= 27; var4++) {
         this.anarchyServers214.add("/an" + var4);
      }

      for (int var5 = 31; var5 <= 34; var5++) {
         this.anarchyServers214.add("/an" + var5);
      }

      for (int var6 = 51; var6 <= 53; var6++) {
         this.anarchyServers214.add("/an" + var6);
      }

      this.anarchyServers214.add("/an91");
   }

   public void resetTimers() {
      this.hubCheckTimer.resetCounter();
      this.serverSwitchCooldown.resetCounter();
   }

   public void reset() {
      this.currentServerIndex = 0;
      this.currentServer = "";
      this.inHub = false;
      this.waitingForServerLoad = false;
      this.resetTimers();
   }

   public void updateHubStatus(ClientWorld var1) {
      this.inHub = this.isInHubInternal(var1);
   }

   private boolean isInHubInternal(ClientWorld var1) {
      if (var1 == null) {
         return true;
      }

      Scoreboard var2 = var1.getScoreboard();
      ScoreboardObjective var3 = var2.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
      if (var3 == null) {
         return true;
      }

      String var4 = var3.getDisplayName().getString();
      return !var4.contains("Анархия-");
   }

   private int getCurrentAnarchyNumber(ClientWorld var1) {
      if (var1 == null) {
         return -1;
      }

      Scoreboard var2 = var1.getScoreboard();
      ScoreboardObjective var3 = var2.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
      if (var3 != null) {
         String var4 = var3.getDisplayName().getString();
         if (var4.contains("Анархия-")) {
            String[] var5 = var4.split("-");
            if (var5.length > 1) {
               try {
                  return Integer.parseInt(var5[1].trim());
               } catch (NumberFormatException var7) {
                  return -1;
               }
            }
         }
      }

      return -1;
   }

   private String getNextServer(List<String> var1, ClientWorld var2) {
      if (var1.isEmpty()) {
         return null;
      }

      int var3 = this.getCurrentAnarchyNumber(var2);
      if (var3 != -1) {
         String var4 = "/an" + var3;
         int var5 = var1.indexOf(var4);
         if (var5 != -1) {
            this.currentServerIndex = var5;
         }
      }

      this.currentServerIndex = (this.currentServerIndex + 1) % var1.size();
      return (String)var1.get(this.currentServerIndex);
   }

   public void switchToNextServer(ClientPlayerEntity var1, NetworkManager var2, String var3) {
      if (this.serverSwitchCooldown.hasTimeElapsed(3000L)) {
         List var4 = this.getAvailableServers(var3);
         if (var4 != null && !var4.isEmpty()) {
            ClientWorld var5 = (ClientWorld)var1.getEntityWorld();
            String var6 = this.getNextServer(var4, var5);
            if (var6 != null) {
               this.currentServer = var6;
               var1.networkHandler.sendChatCommand(var6.substring(1));
               var2.sendServerSwitch(var6);
               this.waitingForServerLoad = true;
               this.serverSwitchCooldown.resetCounter();
            }
         }
      }
   }

   public void joinAnarchyFromHub(ClientPlayerEntity var1, String var2) {
      List var3 = this.getAvailableServers(var2);
      if (var3 != null && !var3.isEmpty()) {
         String var4 = (String)var3.get(0);
         var1.networkHandler.sendChatCommand(var4.substring(1));
         this.waitingForServerLoad = true;
         this.hubCheckTimer.resetCounter();
      }
   }

   private List<String> getAvailableServers(String var1) {
      if (var1.equals("1.21.4")) {
         return new ArrayList<>(this.anarchyServers214);
      } else {
         return var1.equals("1.16.5") ? new ArrayList<>(this.anarchyServers165) : null;
      }
   }

   public boolean shouldJoinAnarchy(String var1) {
      boolean var2 = var1.equals("1.16.5") || var1.equals("1.21.4");
      return this.inHub && this.hubCheckTimer.hasTimeElapsed(3000L) && var2;
   }

   public boolean isInHub() {
      return this.inHub;
   }

   public boolean isWaitingForServerLoad() {
      return this.waitingForServerLoad;
   }

   public void setWaitingForServerLoad(boolean var1) {
      this.waitingForServerLoad = var1;
   }

   public String getCurrentServer() {
      return this.currentServer;
   }
}
