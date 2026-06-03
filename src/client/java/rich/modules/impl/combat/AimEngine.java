package rich.modules.impl.combat;

import java.util.Random;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class AimEngine {
    private final AimAssist config;
    private final Random aimRandom = new Random();
    private float lastPlayerYaw = 0.0f;
    private float lastPlayerPitch = 0.0f;
    private float rawTickYawDelta = 0.0f;
    private float rawTickPitchDelta = 0.0f;
    private boolean firstFrame = true;
    private UUID aimLockedTarget = null;
    private int aimLostTicks = 0;
    private int aimPointTicks = 0;
    private double aimOffsetX = 0.0;
    private double aimOffsetY = 0.65;
    private double aimOffsetZ = 0.0;
    private double prevTargetX;
    private double prevTargetZ;
    private boolean hasPrevTarget = false;
    private int aimReactionTicks = 0;

    public AimEngine(AimAssist config) {
        this.config = config;
    }

    public void onFrame(float partialTick) {
        if (!this.config.isState() || this.config.mc.player == null || this.config.mc.world == null) {
            this.aimLockedTarget = null;
            this.aimReactionTicks = 0;
            this.aimPointTicks = 0;
            this.hasPrevTarget = false;
            this.firstFrame = true;
            return;
        }
        if (this.config.mc.currentScreen != null) {
            return;
        }
        PlayerEntity player = this.config.mc.player;
        if (this.firstFrame) {
            this.lastPlayerYaw = player.getYaw();
            this.lastPlayerPitch = player.getPitch();
            this.firstFrame = false;
            return;
        }
        this.rawTickYawDelta = MathHelper.wrapDegrees((float)(player.getYaw() - this.lastPlayerYaw));
        this.rawTickPitchDelta = player.getPitch() - this.lastPlayerPitch;
        this.lastPlayerYaw = player.getYaw();
        this.lastPlayerPitch = player.getPitch();
        float mouseDelta = Math.abs(this.rawTickYawDelta) + Math.abs(this.rawTickPitchDelta);
        if (mouseDelta < 0.005f) {
            return;
        }
        Vec3d eyePos = player.getEyePos();
        Entity target = null;
        if (this.aimLockedTarget != null) {
            for (Entity e : this.config.mc.world.getEntities()) {
                if (!e.getUuid().equals(this.aimLockedTarget) || !(e instanceof LivingEntity)) continue;
                LivingEntity living = (LivingEntity)e;
                if (living.isAlive() && !living.isSpectator() && player.distanceTo(living) <= this.config.maxDistance()) {
                    target = living;
                    this.aimLostTicks = 0;
                    break;
                }
                if (++this.aimLostTicks < 25) break;
                this.aimLockedTarget = null;
                break;
            }
        }
        Entity found;
        if (target == null && (found = this.config.findTarget()) != null) {
            target = found;
            this.aimLockedTarget = found.getUuid();
            this.aimLostTicks = 0;
            this.hasPrevTarget = false;
            this.aimReactionTicks = 1 + this.aimRandom.nextInt(3);
            this.aimPointTicks = 0;
        }
        if (target == null) {
            this.hasPrevTarget = false;
            this.aimReactionTicks = 0;
            this.aimPointTicks = 0;
            return;
        }
        if (!this.canSeeTarget(player, target)) {
            if (++this.aimLostTicks > 8) {
                this.aimLockedTarget = null;
                this.aimReactionTicks = 0;
                this.aimPointTicks = 0;
                this.hasPrevTarget = false;
            }
            return;
        }
        float dist = player.distanceTo(target);
        float halfW = target.getWidth() * 0.5f;
        if (this.aimReactionTicks > 0) {
            --this.aimReactionTicks;
            return;
        }
        if (this.aimPointTicks-- <= 0) {
            this.randomizeAimPoint(target, dist);
        }
        double targetX = target.getX();
        double targetZ = target.getZ();
        if (this.hasPrevTarget) {
            double velX = targetX - this.prevTargetX;
            double velZ = targetZ - this.prevTargetZ;
            float predFactor = MathHelper.clamp((float)(dist * 0.5f), 0.65f, 3.0f);
            targetX += velX * predFactor;
            targetZ += velZ * predFactor;
        }
        this.prevTargetX = target.getX();
        this.prevTargetZ = target.getZ();
        this.hasPrevTarget = true;
        double targetY = target.getY() + target.getHeight() * this.aimOffsetY + this.aimRandom.nextGaussian() * 0.005;
        double dx = (targetX + this.aimOffsetX + this.aimRandom.nextGaussian() * 0.003) - eyePos.x;
        double dy = targetY - eyePos.y;
        double dz = (targetZ + this.aimOffsetZ + this.aimRandom.nextGaussian() * 0.003) - eyePos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float wantYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float wantPitch = (float)(-Math.toDegrees(Math.atan2(dy, hDist)));
        wantYaw = MathHelper.wrapDegrees(wantYaw - player.getYaw());
        wantPitch -= player.getPitch();
        float sens = (float)this.config.mc.options.getMouseSensitivity().getValue().doubleValue() * 0.6f + 0.2f;
        float step = sens * sens * sens * 0.8f;
        float deadzone = Math.max((float)Math.toDegrees(Math.atan2(halfW, dist)) * 0.22f, 0.35f);
        float totalAngle = (float)Math.sqrt(wantYaw * wantYaw + wantPitch * wantPitch);
        float aimFov = this.config.fov();
        if (totalAngle > aimFov + 12.0f) {
            this.aimLockedTarget = null;
            this.aimReactionTicks = 0;
            this.aimPointTicks = 0;
            return;
        }
        if (this.aimRandom.nextFloat() < 0.08f) {
            return;
        }
        float smoothing = this.config.smoothness();
        float targetYaw = wantYaw;
        float targetPitch = wantPitch;
        if (Math.abs(wantYaw) < deadzone) {
            targetYaw = 0.0f;
        }
        if (Math.abs(wantPitch) < deadzone) {
            targetPitch = 0.0f;
        }
        float newYaw = player.getYaw() + targetYaw * smoothing;
        float newPitch = player.getPitch() + targetPitch * smoothing;
        player.setYaw(newYaw);
        player.setPitch(MathHelper.clamp(newPitch, -90.0f, 90.0f));
    }

    private boolean canSeeTarget(PlayerEntity player, Entity target) {
        Vec3d from = player.getEyePos();
        Vec3d to = target.getEyePos();
        RaycastContext context = new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player);
        BlockHitResult result = this.config.mc.world.raycast(context);
        return result == null || result.getType() == HitResult.Type.MISS || result.getBlockPos().equals(target.getBlockPos());
    }

    private void randomizeAimPoint(Entity target, float dist) {
        float boxSize = target.getWidth() * 0.5f;
        this.aimOffsetX = (this.aimRandom.nextDouble() - 0.5) * boxSize * 0.6;
        this.aimOffsetY = 0.3 + this.aimRandom.nextDouble() * 0.7;
        this.aimOffsetZ = (this.aimRandom.nextDouble() - 0.5) * boxSize * 0.6;
        this.aimPointTicks = 10 + this.aimRandom.nextInt(10);
    }
}

