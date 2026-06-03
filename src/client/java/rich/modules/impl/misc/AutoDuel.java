package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.timer.TimerUtil;

public class AutoDuel extends ModuleStructure {
   private final Pattern pattern = Pattern.compile("^\\w{3,16}$");
   private final SelectSetting mode = new SelectSetting("Режим", "Режим дуэли")
      .value("Шары", "Щит", "Шипы 3", "Незеритка", "Читерский рай", "Лук", "Классик", "Тотемы", "Нодебафф")
      .selected("Шары");
   private final SliderSettings slowTime = new SliderSettings("Скорость отправки", "Задержка между запросами").setValue(500.0F).range(300.0F, 1000.0F);
   private final BooleanSetting babki = new BooleanSetting("Играть на деньги", "Ставка монет").setValue(false);
   private final TextSetting money = new TextSetting("Монет", "Количество монет для ставки").setText("10000").visible(() -> this.babki.isValue());
   private double lastPosX;
   private double lastPosY;
   private double lastPosZ;
   private final List<String> sent = Lists.newArrayList();
   private final TimerUtil counter = TimerUtil.create();
   private final TimerUtil counter2 = TimerUtil.create();
   private final TimerUtil counterChoice = TimerUtil.create();
   private final TimerUtil counterTo = TimerUtil.create();

   public AutoDuel() {
      super("AutoDuel", "Auto Duel", ModuleCategory.UTILITIES);
      this.settings(this.mode, this.slowTime, this.babki, this.money);
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   @Override
   public void activate() {
      this.counter.resetCounter();
      this.counter2.resetCounter();
      this.counterChoice.resetCounter();
      this.counterTo.resetCounter();
      this.sent.clear();
      super.activate();
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onTick(TickEvent var1) {
      if (mc.player != null && mc.world != null) {
         this.handleDuelLogic();
         this.handleScreenInteraction();
      }
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginMutation)
   public void onPacket(PacketEvent var1) {
      if (var1.getType() == PacketEvent.Type.RECEIVE && var1.getPacket() instanceof GameMessageS2CPacket var2) {
         String var4 = var2.content().getString();
         if (var4.contains("начало") && var4.contains("через") && var4.contains("секунд!")
            || var4.contains("дуэли » во время поединка запрещено использовать команды")) {
            this.setState(false);
         }
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private void handleDuelLogic() {
      List<String> var1 = this.getOnlinePlayers();
      double var2 = Math.sqrt(
         Math.pow(this.lastPosX - mc.player.getX(), 2.0)
            + Math.pow(this.lastPosY - mc.player.getY(), 2.0)
            + Math.pow(this.lastPosZ - mc.player.getZ(), 2.0)
      );
      if (var2 > 500.0) {
         this.setState(false);
      } else {
         this.lastPosX = mc.player.getX();
         this.lastPosY = mc.player.getY();
         this.lastPosZ = mc.player.getZ();
         if (this.counter2.hasTimeElapsed(800L * var1.size())) {
            this.sent.clear();
            this.counter2.resetCounter();
         }

         for (String var5 : var1) {
            if (!this.sent.contains(var5) && !var5.equals(mc.player.getGameProfile().name()) && this.counter.hasTimeElapsed((long)this.slowTime.getValue())) {
               this.sendDuelRequest(var5);
               this.sent.add(var5);
               this.counter.resetCounter();
            }
         }
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private void sendDuelRequest(String var1) {
      if (this.babki.isValue()) {
         mc.player.networkHandler.sendChatCommand("duel " + var1 + " " + this.money.getText());
      } else {
         mc.player.networkHandler.sendChatCommand("duel " + var1);
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private void handleScreenInteraction() {
      if (mc.currentScreen != null && mc.player.currentScreenHandler instanceof ScreenHandler var1) {
         String var4 = mc.currentScreen.getTitle().getString();
         if (var4.contains("Выбор набора (1/1)")) {
            if (this.counterChoice.hasTimeElapsed(150L)) {
               int var3 = this.getKitSlot();
               if (var3 >= 0) {
                  mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, var3, 0, SlotActionType.QUICK_MOVE, mc.player);
               }

               this.counterChoice.resetCounter();
            }
         } else if (var4.contains("Настройка поединка") && this.counterTo.hasTimeElapsed(150L)) {
            mc.interactionManager.clickSlot(var1.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
            this.counterTo.resetCounter();
         }
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private int getKitSlot() {
      return switch (this.mode.getSelected()) {
         case "Щит" -> 0;
         case "Шипы 3" -> 1;
         case "Лук" -> 2;
         case "Тотемы" -> 3;
         case "Нодебафф" -> 4;
         case "Шары" -> 5;
         case "Классик" -> 6;
         case "Читерский рай" -> 7;
         case "Незеритка" -> 8;
         default -> -1;
      };
   }

   private List<String> getOnlinePlayers() {
      return mc.player
         .networkHandler
         .getPlayerList()
         .stream()
         .map(PlayerListEntry::getProfile)
         .<String>map(GameProfile::name)
         .filter(var1 -> this.pattern.matcher(var1).matches())
         .collect(Collectors.toList());
   }
}
