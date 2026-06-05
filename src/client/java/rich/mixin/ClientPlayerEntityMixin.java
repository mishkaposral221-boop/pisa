package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MovementType;
import net.minecraft.util.PlayerInput;
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
import rich.events.impl.MoveEvent;
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

   @Shadow protected abstract void autoJump(float dx, float dz);
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

      // SUPPRESS_FORWARD: Triggerbot sets this TRUE for exactly 1 tick before a crit
      // attack so the movement packet that leaves BEFORE the attack packet shows
      // the player not moving forward / not sprinting.
      // Flow:
      //   Tick N  (onTick HEAD): crit ready -> set SUPPRESS_FORWARD=true, queue target
      //   Tick N  (here)       : clear forward+sprint from PlayerInput, call setSprinting(false)
      //   Tick N  (sendMvt)    : movement packet sent with W=false, sprint=false
      //   Tick N+1 (onTick HEAD): fire the queued attack(), then SUPPRESS_FORWARD=false
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
            // Explicit packet: tell the server to stop sprinting NOW
            if (IMinecraft.mc.player.isSprinting()) {
               IMinecraft.mc.player.setSprinting(false);
            }
         }
      } catch (Throwable ignored) {}

      // SUPPRESS_SPRINT (legacy / other modules)
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

      // SUPPRESS_JUMP (crit gate)
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

   @Inject(method = "move", at = @At("HEAD"), cancellable = true)
   public void onMoveHook(MovementType type, Vec3d movement, CallbackInfo ci) {
      MoveEvent e = new MoveEvent(movement);
      EventManager.callEvent(e);
      double ox = getX(), oz = getZ();
      super.move(type, e.getMovement());
      autoJump((float)(getX() - ox), (float)(getZ() - oz));
      ci.cancel();
   }

   @ModifyExpressionValue(method = {"sendMovementPackets", "tick"},
                          at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
   private float hookSilentRotationYaw(float original) {
      if (IMinecraft.mc.player != null && AngleConnection.INSTANCE.getRotation() != null) {
         float yaw = AngleConnection.INSTANCE.getRotation().getYaw();
         float body = MoveUtil.calculateBodyYaw(
            yaw, prevBodyYaw, prevX, prevZ,
            IMinecraft.mc.player.getX(), IMinecraft.mc.player.getZ(),
            IMinecraft.mc.player.handSwingProgress
         );
         prevBodyYaw = body;
         prevX = IMinecraft.mc.player.getX();
         prevZ = IMinecraft.mc.player.getZ();
         IMinecraft.mc.player.setBodyYaw(body);
         return yaw;
      }
      return original;
   }

   @ModifyExpressionValue(method = {"sendMovementPackets", "tick"},
                          at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
   private float hookSilentRotationPitch(float original) {
      return AngleConnection.INSTANCE.getRotation() != null
         ? AngleConnection.INSTANCE.getRotation().getPitch()
         : original;
   }
}
