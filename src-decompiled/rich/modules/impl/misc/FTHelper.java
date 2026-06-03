package rich.modules.impl.misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_408;
import org.lwjgl.glfw.GLFW;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.render.font.Fonts;

public class FTHelper extends ModuleStructure {
   private final BindSetting disorientationButton = new BindSetting("Кнопка дизориентации", "Кнопка для использования дизориентации");
   private final BindSetting trapButton = new BindSetting("Кнопка трапки", "Кнопка для использования трапки");
   private final BindSetting revealingDustButton = new BindSetting("Кнопка явной пыли", "Кнопка для использования явной пыли");
   private final BindSetting godAuraButton = new BindSetting("Кнопка божьей ауры", "Кнопка для использования божьей ауры");
   private final BindSetting freezeSnowballButton = new BindSetting("Кнопка снежка заморозки", "Кнопка для использования снежка заморозки");
   private final BindSetting layerButton = new BindSetting("Кнопка пласта", "Кнопка для использования пласта");
   private final BindSetting fireTornadoButton = new BindSetting("Кнопка огненного смерча", "Кнопка для использования огненного смерча");
   private final BooleanSetting showTrapTimer = new BooleanSetting("Показывать время трапки", "Таймер 15 сек после использования трапки").setValue(true);
   private final Map<BindSetting, Boolean> wasPressedMap = new HashMap<>();
   private long trapActivatedAt = -1L;
   private static final long TRAP_DURATION_MS = 15000L;
   private boolean rightWasPressed = false;

   public FTHelper() {
      super("FTHelper", "Помощник для сервера FunTime", ModuleCategory.UTILITIES);
      this.settings(this.showTrapTimer);
      BindSetting[] var1 = new BindSetting[]{
         this.disorientationButton,
         this.trapButton,
         this.revealingDustButton,
         this.godAuraButton,
         this.freezeSnowballButton,
         this.layerButton,
         this.fireTornadoButton
      };

      for (BindSetting var5 : var1) {
         this.settings(var5);
         this.wasPressedMap.put(var5, false);
      }
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null && !(mc.field_1755 instanceof class_408)) {
         this.handleBind(this.disorientationButton, "Дезориентация");
         this.handleBind(this.trapButton, "Трапка");
         this.handleBind(this.revealingDustButton, "Явная пыль");
         this.handleBind(this.godAuraButton, "Божья аура");
         this.handleBind(this.freezeSnowballButton, "Снежок заморозки");
         this.handleBind(this.layerButton, "Пласт");
         this.handleBind(this.fireTornadoButton, "Огненный смерч");
         if (this.showTrapTimer.isValue()) {
            boolean var2 = mc.field_1724.method_6047().method_7909() == class_1802.field_22021
               || mc.field_1724.method_6079().method_7909() == class_1802.field_22021;
            boolean var3 = GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), 1) == 1;
            if (var2 && var3 && !this.rightWasPressed) {
               this.trapActivatedAt = System.currentTimeMillis();
            }

            this.rightWasPressed = var2 && var3;
         }
      }
   }

   private void handleBind(BindSetting var1, String var2) {
      if (var1.getKey() != -1 && (var2 == null || !var2.isEmpty())) {
         boolean var3 = var1.getKey() >= 0 && var1.getKey() <= 7;
         long var4 = mc.method_22683().method_4490();
         boolean var6;
         if (var3) {
            var6 = GLFW.glfwGetMouseButton(var4, var1.getKey()) == 1;
         } else {
            var6 = GLFW.glfwGetKey(var4, var1.getKey()) == 1;
         }

         if (var6 && !this.wasPressedMap.getOrDefault(var1, false) && var2 != null) {
            this.useItemFromHotbar(var2);
            if ("Трапка".equals(var2) && this.showTrapTimer.isValue()) {
               this.trapActivatedAt = System.currentTimeMillis();
            }
         }

         this.wasPressedMap.put(var1, var6);
      }
   }

   @EventHandler
   public void onDraw(DrawEvent var1) {
      if (this.showTrapTimer.isValue() && this.trapActivatedAt >= 0L) {
         if (mc.field_1724 != null) {
            long var2 = System.currentTimeMillis() - this.trapActivatedAt;
            if (var2 >= 15000L) {
               this.trapActivatedAt = -1L;
            } else {
               float var4 = (float)(15000L - var2) / 1000.0F;
               String var5 = "До окончания трапки осталось:";
               String var6 = String.format("%.1f секунд", var4);
               int var7 = var1.getDrawContext().method_51421();
               int var8 = var1.getDrawContext().method_51443();
               float var9 = var7 / 2.0F;
               float var10 = var8 - 80.0F;
               float var11 = Fonts.BOLD.getWidth(var5, 6.0F);
               float var12 = Fonts.BOLD.getWidth(var6, 9.0F);
               Fonts.BOLD.draw(var5, var9 - var11 / 2.0F + 0.5F, var10 + 0.5F, 6.0F, new Color(0, 0, 0, 120).getRGB());
               Fonts.BOLD.draw(var5, var9 - var11 / 2.0F, var10, 6.0F, new Color(255, 200, 50, 220).getRGB());
               Fonts.BOLD.draw(var6, var9 - var12 / 2.0F + 0.5F, var10 + 11.5F, 9.0F, new Color(0, 0, 0, 120).getRGB());
               Fonts.BOLD.draw(var6, var9 - var12 / 2.0F, var10 + 11.0F, 9.0F, new Color(255, 255, 255, 255).getRGB());
            }
         }
      }
   }

   private void useItemFromHotbar(String var1) {
      if (mc.field_1724 != null) {
         int var2 = mc.field_1724.method_31548().method_67532();

         for (int var3 = 0; var3 < 9; var3++) {
            class_1799 var4 = mc.field_1724.method_31548().method_5438(var3);
            if (!var4.method_7960() && var4.method_7964().getString().contains(var1)) {
               mc.field_1724.method_31548().method_61496(var3);
               mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
               mc.execute(() -> mc.field_1724.method_31548().method_61496(var2));
               break;
            }
         }
      }
   }
}
