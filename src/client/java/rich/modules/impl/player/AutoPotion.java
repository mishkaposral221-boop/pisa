package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.component.DataComponentTypes;
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
      if (mc.player == null) {
         return -1;
      }

      for (int var2 = 0; var2 < 9; var2++) {
         ItemStack var3 = mc.player.getInventory().getStack(var2);
         if (var3.isOf(Items.SPLASH_POTION)) {
            PotionContentsComponent var4 = (PotionContentsComponent)var3.getName(DataComponentTypes.POTION_CONTENTS);
            if (var4 != null) {
               for (StatusEffectInstance var6 : var4.getEffects()) {
                  if (var6.getEffectType() == var1.effect) {
                     return var2;
                  }
               }
            }
         }
      }

      return -1;
   }

   private boolean hasEffect(RegistryEntry<StatusEffect> var1) {
      return mc.player != null && mc.player.hasStatusEffect(var1);
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private boolean canBuff(AutoPotion.PotionType var1) {
      return this.hasEffect(var1.effect) ? false : var1.isEnabled(this) && this.findPotionSlot(var1) != -1;
   }

   @Native(type = Native.Type.VMProtectBeginMutation)
   private boolean canBuff() {
      return mc.player != null && mc.world != null
         ? (this.canBuff(AutoPotion.PotionType.STRENGTH) || this.canBuff(AutoPotion.PotionType.SPEED) || this.canBuff(AutoPotion.PotionType.FIRE_RESISTANCE))
            && mc.player.isOnGround()
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
      return mc.player != null && mc.world != null
         ? this.isActive() && this.canBuff() && mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() != Blocks.AIR
         : false;
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onRotationUpdate(RotationUpdateEvent var1) {
      if (mc.player != null && mc.world != null) {
         if (var1.getType() == 0 && (this.shouldThrow() || this.spoofed)) {
            this.performRotation();
         }
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private void performRotation() {
      Angle var1 = new Angle(mc.player.getYaw(), 90.0F);
      AngleConfig var2 = new AngleConfig(new LinearConstructor(), true, true);
      AngleConnection.INSTANCE.rotateTo(var1, 3, var2, TaskPriority.HIGH_IMPORTANCE_1, this);
      if (!this.spoofed) {
         this.spoofed = true;
         this.isActivePotion = true;
         this.rotationTicks = 0;
         this.selectedSlot = mc.player.getInventory().getSelectedSlot();
      }
   }

   @EventHandler
   @Native(type = Native.Type.VMProtectBeginUltra)
   public void onTick(TickEvent var1) {
      if (mc.player != null && mc.world != null) {
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
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.selectedSlot));
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
         mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.selectedSlot));
      }
   }

   @Native(type = Native.Type.VMProtectBeginUltra)
   private void throwPotion(AutoPotion.PotionType var1) {
      if (var1.isEnabled(this) && !this.hasEffect(var1.effect)) {
         if (mc.player != null && mc.player.networkHandler != null) {
            int var2 = this.findPotionSlot(var1);
            if (var2 != -1) {
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(var2));
               this.sendSequencedPacket(var1x -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, var1x, mc.player.getYaw(), 90.0F));
            }
         }
      }
   }

   private void sendSequencedPacket(SequencedPacketCreator var1) {
      if (mc.player != null && mc.player.networkHandler != null && mc.world != null) {
         try {
            ClientWorldAccessor var2 = (ClientWorldAccessor)mc.world;
            PendingUpdateManager var3 = var2.getPendingUpdateManager().incrementSequence();
            int var4 = var3.getSequence();
            mc.player.networkHandler.sendPacket(var1.predict(var4));
            var3.close();
         } catch (Exception var5) {
            mc.player.networkHandler.sendPacket(var1.predict(0));
         }
      }
   }

   private enum PotionType {
      STRENGTH(StatusEffects.STRENGTH, "Силу"),
      SPEED(StatusEffects.SPEED, "Скорость"),
      FIRE_RESISTANCE(StatusEffects.FIRE_RESISTANCE, "Огнестойкость");

      final RegistryEntry<StatusEffect> effect;
      final String settingName;

      PotionType(RegistryEntry<StatusEffect> var3, String var4) {
         this.effect = var3;
         this.settingName = var4;
      }

      public boolean isEnabled(AutoPotion var1) {
         return var1.potions.isSelected(this.settingName);
      }
   }
}
