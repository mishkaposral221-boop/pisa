package rich.util.proxy;

import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.CheckboxWidget.Builder;
import org.apache.commons.lang3.StringUtils;
import rich.util.config.impl.proxy.ProxyConfig;

public class GuiProxy extends Screen {
   private boolean isSocks4 = false;
   private TextFieldWidget ipPort;
   private TextFieldWidget username;
   private TextFieldWidget password;
   private CheckboxWidget enabledCheck;
   private Screen parentScreen;
   private String msg = "";
   private int[] positionY;
   private int positionX;
   private static String text_proxy = Text.translatable("PROXY").getString();

   public GuiProxy(Screen var1) {
      super(Text.literal(text_proxy));
      this.parentScreen = var1;
   }

   private static boolean isValidIpPort(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         String[] var1 = var0.split(":");
         if (var1.length <= 1) {
            return false;
         }

         if (!StringUtils.isNumeric(var1[1])) {
            return false;
         }

         try {
            int var2 = Integer.parseInt(var1[1]);
            return var2 >= 0 && var2 <= 65535;
         } catch (NumberFormatException var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean checkProxy() {
      if (!isValidIpPort(this.ipPort.getText())) {
         this.ipPort.setFocused(true);
         return false;
      } else {
         return true;
      }
   }

   private void centerButtons(int var1, int var2, int var3) {
      this.positionX = this.width / 2 - var2 / 2;
      this.positionY = new int[var1];
      int var4 = (this.height + var1 * var3) / 2;
      int var5 = var4 - var1 * var3;

      for (int var6 = 0; var6 != var1; var6++) {
         this.positionY[var6] = var5 + var3 * var6;
      }
   }

   public boolean keyPressed(KeyInput var1) {
      if (var1.isEscape()) {
         MinecraftClient.getInstance().setScreen(this.parentScreen);
         return true;
      } else {
         super.keyPressed(var1);
         this.msg = "";
         return true;
      }
   }

   public void render(DrawContext var1, int var2, int var3, float var4) {
      super.render(var1, var2, var3, var4);
      if (this.enabledCheck.isChecked() && !isValidIpPort(this.ipPort.getText())) {
         this.enabledCheck.onPress(null);
      }

      var1.drawTextWithShadow(
         this.textRenderer,
         Text.translatable("Введите айпи адрес и порт. Пример ниже").getString(),
         this.width / 2 - 106,
         this.positionY[3] - 15,
         10526880
      );
      var1.drawTextWithShadow(this.textRenderer, Text.translatable("Айпи:Порт ▸").getString(), this.width / 2 - 140, this.positionY[3] + 15, 10526880);
      this.ipPort.render(var1, var2, var3, var4);
      var1.drawTextWithShadow(this.textRenderer, Text.translatable("Никнейм ▸").getString(), this.width / 2 - 131, this.positionY[4] + 15, 10526880);
      var1.drawTextWithShadow(this.textRenderer, Text.translatable("Пароль ▸").getString(), this.width / 2 - 126, this.positionY[5] + 15, 10526880);
      this.username.render(var1, var2, var3, var4);
      this.password.render(var1, var2, var3, var4);
      var1.drawCenteredTextWithShadow(this.textRenderer, this.msg, this.width / 2, this.positionY[6] + 5, 10526880);
   }

   public void init() {
      short var1 = 160;
      this.centerButtons(10, var1, 26);
      ProxyConfig var2 = ProxyConfig.getInstance();
      Proxy var3 = var2.getDefaultProxy();
      this.isSocks4 = var3.type == Proxy.ProxyType.SOCKS4;
      this.ipPort = new TextFieldWidget(this.textRenderer, this.positionX, this.positionY[3] + 10, var1, 20, Text.literal(""));
      this.ipPort.setText(var3.ipPort);
      this.ipPort.setMaxLength(1024);
      this.ipPort.setFocused(true);
      this.addSelectableChild(this.ipPort);
      this.username = new TextFieldWidget(this.textRenderer, this.positionX, this.positionY[4] + 10, var1, 20, Text.literal(""));
      this.username.setMaxLength(255);
      this.username.setText(var3.username);
      this.addSelectableChild(this.username);
      this.password = new TextFieldWidget(this.textRenderer, this.positionX, this.positionY[5] + 10, var1, 20, Text.literal(""));
      this.password.setMaxLength(255);
      this.password.setText(var3.password);
      this.addSelectableChild(this.password);
      int var4 = this.width / 2 - var1 / 2 * 3 / 2;
      ButtonWidget var5 = ButtonWidget.builder(Text.translatable("Применить"), var1x -> {
         ProxyConfig var2x = ProxyConfig.getInstance();
         if (this.enabledCheck.isChecked()) {
            if (this.checkProxy()) {
               Proxy var3x = new Proxy(this.isSocks4, this.ipPort.getText(), this.username.getText(), this.password.getText());
               var2x.setDefaultProxy(var3x);
               var2x.setProxyEnabled(true);
               var2x.save();
               MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
            }
         } else {
            Proxy var4x = new Proxy(this.isSocks4, this.ipPort.getText(), this.username.getText(), this.password.getText());
            var2x.setDefaultProxy(var4x);
            var2x.setProxyEnabled(false);
            var2x.save();
            MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
         }
      }).dimensions(var4 + (var1 / 2 - 62) * 2, this.positionY[7] - 10, var1 / 2 + 3, 20).build();
      this.addDrawableChild(var5);
      net.minecraft.client.gui.widget.CheckboxWidget.Builder var6 = CheckboxWidget.builder(Text.translatable("Включить прокси"), this.textRenderer);
      var6.pos(
         this.width / 2 - 34 - (13 + this.textRenderer.getWidth(Text.translatable("Включить прокси"))) / 2, this.positionY[7] + 15
      );
      if (var2.isProxyEnabled()) {
         var6.checked(true);
      }

      this.enabledCheck = var6.build();
      this.addDrawableChild(this.enabledCheck);
      ButtonWidget var7 = ButtonWidget.builder(Text.translatable("Отменить"), var1x -> MinecraftClient.getInstance().setScreen(this.parentScreen))
         .dimensions(var4 + (var1 / 2 - 16) * 2, this.positionY[7] - 10, var1 / 2 - 3, 20)
         .build();
      this.addDrawableChild(var7);
   }

   public void close() {
      this.msg = "";
      MinecraftClient.getInstance().setScreen(this.parentScreen);
   }
}
