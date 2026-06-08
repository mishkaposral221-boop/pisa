package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.CloseScreenEvent;
import rich.events.impl.PlayerTravelEvent;
import rich.events.impl.PushEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.UsingItemEvent;
import rich.modules.impl.combat.Triggerbot;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.util.move.MoveUtil;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
   @Final @Shadow protected MinecraftClient client;
   @Shadow public Input input;
   private double prevX = 0.0;
   private double prevZ = 0.0;
   private float prevBodyYaw = 0.0F;

   // Максимальная дельта yaw, которую silent rotation может отправить серверу
   // за один тик. Защищает от мгновенных "телепортов" головы (>700 deg/sec),
   // на которые анти-чит реагирует откатом позиции. NaN = подмена не активна.
   private static final float MAX_SILENT_YAW_STEP = 35.0F;
   private float prevSentYaw = Float.NaN;

   @Shadow public abstract boolean isUsingItem();

   public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
      super(world, profile);
   }

   @Inject(method = "tick", at = @At("HEAD"))
   public void tick(CallbackInfo ci) {
      if (client.player != null && client.world != null) {
         EventManager.callEvent(new TickEvent());
      }
   }

   @Inject(method = "tickMovement",
           at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick()V", shift = Shift.AFTER))
   private void onInputTick(CallbackInfo ci) {
      if (IMinecraft.mc.player == null) return;

      try {
         Triggerbot tb = Triggerbot.getInstance();
         if (Triggerbot.SUPPRESS_FORWARD && tb != null && tb.isState()
               && input != null && input.playerInput != null) {
            PlayerInput pi = input.playerInput;
            if (pi.forward() || pi.sprint()) {
               input.playerInput = new PlayerInput(
                  false, pi.backward(), pi.left(), pi.right(),
                  pi.jump(), pi.sneak(), false
               );
            }
            if (IMinecraft.mc.player.isSprinting()) {
               IMinecraft.mc.player.setSprinting(false);
            }
         }
      } catch (Throwable ignored) {}

      try {
         Triggerbot tb = Triggerbot.getInstance();
         if (Triggerbot.SUPPRESS_SPRINT && tb != null && tb.isState()
               && input != null && input.playerInput != null
               && input.playerInput.sprint()) {
            PlayerInput pi = input.playerInput;
            input.playerInput = new PlayerInput(
               pi.forward(), pi.backward(), pi.left(), pi.right(),
               pi.jump(), pi.sneak(), false
            );
         }
      } catch (Throwable ignored) {}

      try {
         Triggerbot tb = Triggerbot.getInstance();
         if (Triggerbot.SUPPRESS_JUMP && tb != null && tb.isState()
               && input != null && input.playerInput != null
               && input.playerInput.jump()) {
            PlayerInput pi = input.playerInput;
            input.playerInput = new PlayerInput(
               pi.forward(), pi.backward(), pi.left(), pi.right(),
               false, pi.sneak(), pi.sprint()
            );
         }
      } catch (Throwable ignored) {}

      EventManager.callEvent(new PlayerTravelEvent(Vec3d.ZERO, false));
   }

   @Redirect(method = "applyMovementSpeedFactors",
             at = @At(value = "INVOKE",
                      target = "Lnet/minecraft/util/math/Vec2f;multiply(F)Lnet/minecraft/util/math/Vec2f;",
                      ordinal = 1))
   private Vec2f cancelItemSlowdown(Vec2f vec, float factor) {
      UsingItemEvent e = new UsingItemEvent((byte)1);
      EventManager.callEvent(e);
      return e.isCancelled() && isUsingItem() && !hasVehicle() ? vec.multiply(1.0F) : vec.multiply(factor);
   }

   @Inject(method = "closeHandledScreen", at = @At("HEAD"), cancellable = true)
   private void closeHandledScreenHook(CallbackInfo ci) {
      CloseScreenEvent e = new CloseScreenEvent(client.currentScreen);
      EventManager.callEvent(e);
      if (e.isCancelled()) ci.cancel();
   }

   @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
   public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
      PushEvent e = new PushEvent(PushEvent.Type.BLOCK);
      EventManager.callEvent(e);
      if (e.isCancelled()) ci.cancel();
   }

   // ВНИМАНИЕ: прежний onMoveHook с @Inject(method = "move", ...) удалён.
   // В 1.21.11 ClientPlayerEntity НЕ переопределяет move(MovementType, Vec3d),
   // поэтому Mixin не находил целевой метод (Scanned 0 targets) и инъекция падала.
   // Событие движения теперь фаерится из EntityMixin через устойчивый @ModifyVariable
   // на Entity.move, отфильтрованный по локальному игроку.

   @ModifyExpressionValue(method = {"sendMovementPackets", "tick"},
                          at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
   private float hookSilentRotationYaw(float original) {
      // Подмену включаем ТОЛЬКО при реально установленном угле (getCurrentAngle),
      // а не при null-фолбэке getRotation() на камеру -- иначе хук срабатывал
      // каждый тик и зря дёргал setBodyYaw даже без активного вращения.
      if (IMinecraft.mc.player != null && AngleConnection.INSTANCE.getCurrentAngle() != null) {
         float targetYaw = AngleConnection.INSTANCE.getRotation().getYaw();
         // Ограничиваем дельту поворота между тиками: серверу нельзя слать
         // мгновенный скачок головы, иначе ловим откат от анти-чита.
         float sentYaw = targetYaw;
         if (!Float.isNaN(prevSentYaw)) {
            float delta = MathHelper.wrapDegrees(targetYaw - prevSentYaw);
            if (delta > MAX_SILENT_YAW_STEP) delta = MAX_SILENT_YAW_STEP;
            else if (delta < -MAX_SILENT_YAW_STEP) delta = -MAX_SILENT_YAW_STEP;
            sentYaw = prevSentYaw + delta;
         }
         prevSentYaw = sentYaw;
         float body = MoveUtil.calculateBodyYaw(
            sentYaw, prevBodyYaw, prevX, prevZ,
            IMinecraft.mc.player.getX(), IMinecraft.mc.player.getZ(),
            IMinecraft.mc.player.handSwingProgress
         );
         prevBodyYaw = body;
         prevX = IMinecraft.mc.player.getX();
         prevZ = IMinecraft.mc.player.getZ();
         IMinecraft.mc.player.setBodyYaw(body);
         return sentYaw;
      }
      prevSentYaw = Float.NaN;
      return original;
   }

   @ModifyExpressionValue(method = {"sendMovementPackets", "tick"},
                          at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
   private float hookSilentRotationPitch(float original) {
      return AngleConnection.INSTANCE.getCurrentAngle() != null
         ? AngleConnection.INSTANCE.getRotation().getPitch()
         : original;
   }
}
