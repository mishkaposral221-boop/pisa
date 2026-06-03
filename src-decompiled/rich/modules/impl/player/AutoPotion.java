package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2246;
import net.minecraft.class_2868;
import net.minecraft.class_2886;
import net.minecraft.class_6880;
import net.minecraft.class_7202;
import net.minecraft.class_7204;
import net.minecraft.class_9334;
import rich.events.api.EventHandler;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.mixin.ClientWorldAccessor;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.util.math.TaskPriority;
import rich.util.timer.StopWatch;

public class AutoPotion extends ModuleStructure {
   private final BooleanSetting autoOff = new BooleanSetting("Авто отключение", "Автоматически выключать модуль после использования").setValue(false);
   private final MultiSelectSetting potions = new MultiSelectSetting("Бросать", "Выберите зелья для автоброса")
      .value("Силу", "Скорость", "Огнестойкость")
      .selected("Силу", "Скорость");
   private final StopWatch timer = new StopWatch();
   private boolean spoofed = false;
   private boolean isActivePotion = false;
   private int rotationTicks = 0;
   private int selectedSlot = -1;
   private final float THROW_PITCH = 90.0F;
   private final int ROTATION_WAIT_TICKS = 2;

   public AutoPotion() {
      super("AutoPotion", ModuleCategory.UTILITIES);
      this.settings(this.potions, this.autoOff);
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   @Override
   public void deactivate() {
      this.isActivePotion = false;
      this.spoofed = false;
      this.rotationTicks = 0;
      this.selectedSlot = -1;
      AngleConnection.INSTANCE.startReturning();
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private int findPotionSlot(AutoPotion.PotionType var1) {
      if (mc.field_1724 == null) {
         return -1;
      }

      for (int var2 = 0; var2 < 9; var2++) {
         class_1799 var3 = mc.field_1724.method_31548().method_5438(var2);
         if (var3.method_31574(class_1802.field_8436)) {
            class_1844 var4 = (class_1844)var3.method_58694(class_9334.field_49651);
            if (var4 != null) {
               for (class_1293 var6 : var4.method_57397()) {
                  if (var6.method_5579() == var1.effect) {
                     return var2;
                  }
               }
            }
         }
      }

      return -1;
   }

   private boolean hasEffect(class_6880<class_1291> var1) {
      return mc.field_1724 != null && mc.field_1724.method_6059(var1);
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private boolean canBuff(AutoPotion.PotionType var1) {
      return this.hasEffect(var1.effect) ? false : var1.isEnabled(this) && this.findPotionSlot(var1) != -1;
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private boolean canBuff() {
      return mc.field_1724 != null && mc.field_1687 != null
         ? (this.canBuff(AutoPotion.PotionType.STRENGTH) || this.canBuff(AutoPotion.PotionType.SPEED) || this.canBuff(AutoPotion.PotionType.FIRE_RESISTANCE))
            && mc.field_1724.method_24828()
            && this.timer.finished(500.0)
         : false;
   }

   private boolean isActive() {
      return this.isActivePotion
         || this.canBuff(AutoPotion.PotionType.STRENGTH)
         || this.canBuff(AutoPotion.PotionType.SPEED)
         || this.canBuff(AutoPotion.PotionType.FIRE_RESISTANCE);
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private boolean shouldThrow() {
      return mc.field_1724 != null && mc.field_1687 != null
         ? this.isActive() && this.canBuff() && mc.field_1687.method_8320(mc.field_1724.method_24515().method_10074()).method_26204() != class_2246.field_10124
         : false;
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onRotationUpdate(RotationUpdateEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (var1.getType() == 0 && (this.shouldThrow() || this.spoofed)) {
            this.performRotation();
         }
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private void performRotation() {
      Angle var1 = new Angle(mc.field_1724.method_36454(), 90.0F);
      AngleConfig var2 = new AngleConfig(new LinearConstructor(), true, true);
      AngleConnection.INSTANCE.rotateTo(var1, 3, var2, TaskPriority.HIGH_IMPORTANCE_1, this);
      if (!this.spoofed) {
         this.spoofed = true;
         this.isActivePotion = true;
         this.rotationTicks = 0;
         this.selectedSlot = mc.field_1724.method_31548().method_67532();
      }
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.isActivePotion && !this.shouldThrow() && !this.spoofed) {
            this.isActivePotion = false;
            if (this.autoOff.isValue()) {
               this.setState(false);
            }
         }

         if (this.spoofed) {
            this.processThrow();
         }
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private void processThrow() {
      this.rotationTicks++;
      Angle var1 = AngleConnection.INSTANCE.getRotation();
      boolean var2 = var1 != null && var1.getPitch() >= 80.0F;
      boolean var3 = this.rotationTicks >= 2;
      if (var2 && var3) {
         boolean var4 = false;
         if (this.canBuff(AutoPotion.PotionType.STRENGTH)) {
            this.throwPotion(AutoPotion.PotionType.STRENGTH);
            var4 = true;
         }

         if (this.canBuff(AutoPotion.PotionType.SPEED)) {
            this.throwPotion(AutoPotion.PotionType.SPEED);
            var4 = true;
         }

         if (this.canBuff(AutoPotion.PotionType.FIRE_RESISTANCE)) {
            this.throwPotion(AutoPotion.PotionType.FIRE_RESISTANCE);
            var4 = true;
         }

         if (this.selectedSlot != -1) {
            mc.field_1724.field_3944.method_52787(new class_2868(this.selectedSlot));
         }

         this.timer.reset();
         this.spoofed = false;
         this.rotationTicks = 0;
         this.isActivePotion = false;
         if (this.autoOff.isValue() || !var4) {
            this.setState(false);
         }
      }

      if (this.rotationTicks > 10) {
         this.resetThrowState();
      }
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private void resetThrowState() {
      this.spoofed = false;
      this.rotationTicks = 0;
      this.isActivePotion = false;
      if (this.selectedSlot != -1) {
         mc.field_1724.field_3944.method_52787(new class_2868(this.selectedSlot));
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private void throwPotion(AutoPotion.PotionType var1) {
      if (var1.isEnabled(this) && !this.hasEffect(var1.effect)) {
         if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
            int var2 = this.findPotionSlot(var1);
            if (var2 != -1) {
               mc.field_1724.field_3944.method_52787(new class_2868(var2));
               this.sendSequencedPacket(var1x -> new class_2886(class_1268.field_5808, var1x, mc.field_1724.method_36454(), 90.0F));
            }
         }
      }
   }

   private void sendSequencedPacket(class_7204 var1) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null && mc.field_1687 != null) {
         try {
            ClientWorldAccessor var2 = (ClientWorldAccessor)mc.field_1687;
            class_7202 var3 = var2.getPendingUpdateManager().method_41937();
            int var4 = var3.method_41942();
            mc.field_1724.field_3944.method_52787(var1.predict(var4));
            var3.close();
         } catch (Exception var5) {
            mc.field_1724.field_3944.method_52787(var1.predict(0));
         }
      }
   }

   private enum PotionType {
      STRENGTH(class_1294.field_5910, "Силу"),
      SPEED(class_1294.field_5904, "Скорость"),
      FIRE_RESISTANCE(class_1294.field_5918, "Огнестойкость");

      final class_6880<class_1291> effect;
      final String settingName;

      PotionType(class_6880<class_1291> var3, String var4) {
         this.effect = var3;
         this.settingName = var4;
      }

      public boolean isEnabled(AutoPotion var1) {
         return var1.potions.isSelected(this.settingName);
      }
   }
}
