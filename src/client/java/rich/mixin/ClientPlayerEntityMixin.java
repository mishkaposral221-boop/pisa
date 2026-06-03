package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MovementType;
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
import rich.modules.impl.combat.aura.AngleConnection;
import rich.util.move.MoveUtil;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
   @Final
   @Shadow
   protected MinecraftClient client;
   @Shadow
   public Input input;
   private double prevX = 0.0;
   private double prevZ = 0.0;
   private float prevBodyYaw = 0.0F;

   @Shadow
   protected abstract void autoJump(float var1, float var2);

   @Shadow
   public abstract boolean isUsingItem();

   public ClientPlayerEntityMixin(ClientWorld var1, GameProfile var2) {
      super(var1, var2);
   }

   @Inject(method = "tick", at = @At("HEAD"))
   public void tick(CallbackInfo var1) {
      if (this.client.player != null && this.client.world != null) {
         EventManager.callEvent(new TickEvent());
      }
   }

   @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/Input;tick()V", shift = Shift.AFTER))
   private void onInputTick(CallbackInfo var1) {
      if (IMinecraft.mc.player != null) {
         PlayerTravelEvent var2 = new PlayerTravelEvent(Vec3d.ZERO, false);
         EventManager.callEvent(var2);
      }
   }

   @Redirect(method = "applyMovementSpeedFactors", at = @At(value = "INVOKE", target = "Lnet/minecraft/Vec2f;multiply(F)Lnet/minecraft/Vec2f;", ordinal = 1))
   private Vec2f cancelItemSlowdown(Vec2f var1, float var2) {
      UsingItemEvent var3 = new UsingItemEvent((byte)1);
      EventManager.callEvent(var3);
      return var3.isCancelled() && this.isUsingItem() && !this.hasVehicle() ? var1.multiply(1.0F) : var1.multiply(var2);
   }

   @Inject(method = "closeHandledScreen", at = @At("HEAD"), cancellable = true)
   private void closeHandledScreenHook(CallbackInfo var1) {
      CloseScreenEvent var2 = new CloseScreenEvent(this.client.currentScreen);
      EventManager.callEvent(var2);
      if (var2.isCancelled()) {
         var1.cancel();
      }
   }

   @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
   public void pushOutOfBlocks(double var1, double var3, CallbackInfo var5) {
      PushEvent var6 = new PushEvent(PushEvent.Type.BLOCK);
      EventManager.callEvent(var6);
      if (var6.isCancelled()) {
         var5.cancel();
      }
   }

   @Inject(
      method = "move",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/AbstractClientPlayerEntity;move(Lnet/minecraft/MovementType;Lnet/minecraft/Vec3d;)V"),
      cancellable = true
   )
   public void onMoveHook(MovementType var1, Vec3d var2, CallbackInfo var3) {
      MoveEvent var4 = new MoveEvent(var2);
      EventManager.callEvent(var4);
      double var5 = this.getX();
      double var7 = this.getZ();
      super.move(var1, var4.getMovement());
      this.autoJump((float)(this.getX() - var5), (float)(this.getZ() - var7));
      var3.cancel();
   }

   @ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/ClientPlayerEntity;getYaw()F"))
   private float hookSilentRotationYaw(float var1) {
      if (IMinecraft.mc.player != null && AngleConnection.INSTANCE.getRotation() != null) {
         float var2 = AngleConnection.INSTANCE.getRotation().getYaw();
         float var3 = MoveUtil.calculateBodyYaw(
            var2,
            this.prevBodyYaw,
            this.prevX,
            this.prevZ,
            IMinecraft.mc.player.getX(),
            IMinecraft.mc.player.getZ(),
            IMinecraft.mc.player.handSwingProgress
         );
         this.prevBodyYaw = var3;
         this.prevX = IMinecraft.mc.player.getX();
         this.prevZ = IMinecraft.mc.player.getZ();
         IMinecraft.mc.player.setBodyYaw(var3);
         return var2;
      } else {
         return var1;
      }
   }

   @ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/ClientPlayerEntity;getPitch()F"))
   private float hookSilentRotationPitch(float var1) {
      return AngleConnection.INSTANCE.getRotation() != null ? AngleConnection.INSTANCE.getRotation().getPitch() : var1;
   }
}
