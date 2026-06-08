package rich.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.BoundingBoxControlEvent;
import rich.events.impl.MoveEvent;
import rich.events.impl.PlayerVelocityStrafeEvent;
import rich.modules.impl.combat.aura.AngleConnection;

@Mixin(Entity.class)
public abstract class EntityMixin implements IMinecraft {
   @Shadow
   private Box boundingBox;
   @Shadow
   public float yaw;
   @Unique
   private boolean client$local;
   @Unique
   private final MinecraftClient client = MinecraftClient.getInstance();

   @Inject(method = "<init>", at = @At("TAIL"))
   private void onInit(EntityType<?> var1, World var2, CallbackInfo var3) {
      this.client$local = (Object)this instanceof ClientPlayerEntity;
   }

   @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
   public final void getBoundingBox(CallbackInfoReturnable<Box> var1) {
      BoundingBoxControlEvent var2 = new BoundingBoxControlEvent(this.boundingBox, (Entity)(Object)this);
      EventManager.callEvent(var2);
      var1.setReturnValue(var2.getBox());
   }

   // Хук движения локального игрока.
   //
   // Раньше это делалось через @Inject в ClientPlayerEntity#move, но в 1.21.11
   // ClientPlayerEntity больше не переопределяет move(...), поэтому таргет не находился.
   // Entity#move(MovementType, Vec3d) существует ВСЕГДА и Sodium его не трогает,
   // поэтому @ModifyVariable по аргументу-вектору движения — устойчивая точка.
   // Фильтруемся по локальному игроку, чтобы не затрагивать остальные сущности.
   @ModifyVariable(method = "move", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private Vec3d rich$onClientPlayerMove(Vec3d movement) {
      if ((Object)this == mc.player) {
         MoveEvent event = new MoveEvent(movement);
         EventManager.callEvent(event);
         return event.getMovement();
      }
      return movement;
   }

   @Redirect(
      method = "updateVelocity",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;")
   )
   public Vec3d hookVelocity(Vec3d var1, float var2, float var3) {
      if ((Object)this == mc.player) {
         PlayerVelocityStrafeEvent var4 = new PlayerVelocityStrafeEvent(var1, var2, var3, Entity.movementInputToVelocity(var1, var2, var3));
         EventManager.callEvent(var4);
         return var4.getVelocity();
      } else {
         return Entity.movementInputToVelocity(var1, var2, var3);
      }
   }

   @ModifyVariable(method = "getRotationVector(FF)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private float modifyPitch(float var1) {
      return (Object)this instanceof ClientPlayerEntity && AngleConnection.INSTANCE.getCurrentAngle() != null ? AngleConnection.INSTANCE.getCurrentAngle().getPitch() : var1;
   }
}
