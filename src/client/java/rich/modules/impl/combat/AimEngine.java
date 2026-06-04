package rich.modules.impl.combat;

import java.util.Random;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
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
        MinecraftClient client = this.config.mc;
        Entity found;
        if (!this.config.isState() || client.player == null || client.world == null) {
            this.aimLockedTarget = null;
            this.aimReactionTicks = 0;
            this.aimPointTicks = 0;
            this.hasPrevTarget = false;
            this.firstFrame = true;
            return;
        }
        if (client.currentScreen != null) {
            return;
        }
        ClientPlayerEntity player = client.player;
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
        Vec3d look = player.getRotationVec(1.0f);
        Entity target = null;
        float aimFov = this.config.fov();
        if (this.aimLockedTarget != null) {
            for (Entity e : client.world.getEntities()) {
                if (!e.getUuid().equals(this.aimLockedTarget) || !(e instanceof LivingEntity)) continue;
                LivingEntity living = (LivingEntity)e;
                if (living.isAlive() && !living.isSpectator() && player.distanceTo((Entity)living) <= this.config.maxDistance()) {
                    target = living;
                    this.aimLostTicks = 0;
                    break;
                }
                if (++this.aimLostTicks < 25) break;
                this.aimLockedTarget = null;
                break;
            }
        }
        if (target == null && (found = this.config.findTarget(client)) != null) {
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
        if (!this.canSeeTarget(client, (PlayerEntity)player, target)) {
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
            float predFactor = MathHelper.clamp((float)(dist * 0.5f), (float)0.65f, (float)3.0f);
            targetX += velX * (double)predFactor;
            targetZ += velZ * (double)predFactor;
        }
        this.prevTargetX = target.getX();
        this.prevTargetZ = target.getZ();
        this.hasPrevTarget = true;
        double targetY = target.getY() + (double)target.getHeight() * this.aimOffsetY + this.aimRandom.nextGaussian() * 0.005;
        double dx = (targetX += this.aimOffsetX + this.aimRandom.nextGaussian() * 0.003) - eyePos.x;
        double dy = targetY - eyePos.y;
        double dz = (targetZ += this.aimOffsetZ + this.aimRandom.nextGaussian() * 0.003) - eyePos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float wantYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float wantPitch = (float)(-Math.toDegrees(Math.atan2(dy, hDist)));
        wantYaw = MathHelper.wrapDegrees((float)(wantYaw - player.getYaw()));
        wantPitch -= player.getPitch();
        float sens = (float)((Double)client.options.getMouseSensitivity().getValue()).doubleValue() * 0.6f + 0.2f;
        // Real Minecraft rotation quantum (GCD): one mouse-pixel = (sens*0.6+0.2)^3 * 8.0 * 0.15 = sens^3 * 1.2.
        // Snapping our pull to THIS grid keeps the total yaw/pitch delta a clean multiple of the player's
        // own sensitivity step, so server-side GCD/rotation checks can't tell the assist apart from the mouse.
        float step = sens * sens * sens * 1.2f;
        float deadzone = Math.max((float)Math.toDegrees(Math.atan2(halfW, dist)) * 0.18f, 0.30f);
        float totalAngle = (float)Math.sqrt(wantYaw * wantYaw + wantPitch * wantPitch);
        if (totalAngle > aimFov + 12.0f) {
            this.aimLockedTarget = null;
            this.aimReactionTicks = 0;
            this.aimPointTicks = 0;
            return;
        }
        if (this.aimRandom.nextFloat() < 0.08f) {
            return;
        }
        float applyYaw = 0.0f;
        float applyPitch = 0.0f;
        if (totalAngle > deadzone) {
            boolean movingToward = this.rawTickYawDelta * wantYaw + this.rawTickPitchDelta * wantPitch > 0.0f;
            float t = Math.min((totalAngle - deadzone) / 15.0f, 1.0f);
            float strength = movingToward ? 0.42f + t * 0.48f : 0.14f + t * 0.18f;
            float maxPull = movingToward ? 4.2f + t * 5.5f : 1.2f + t * 1.6f;
            float moveScale = movingToward ? MathHelper.clamp((float)(mouseDelta * 0.85f), (float)0.28f, (float)0.95f) : 0.35f;
            // Independent yaw/pitch jitter: correlated noise on both axes is itself a detectable signature.
            float jitterYaw = (this.aimRandom.nextFloat() - 0.5f) * 0.16f;
            float jitterPitch = (this.aimRandom.nextFloat() - 0.5f) * 0.10f;
            float undershoot = this.aimRandom.nextFloat() < 0.2f ? 0.7f + this.aimRandom.nextFloat() * 0.2f : 1.0f;
            float rawYawPull = MathHelper.clamp((float)(wantYaw * strength * moveScale * undershoot + jitterYaw), (float)(-maxPull), (float)maxPull);
            float rawPitchPull = MathHelper.clamp((float)(wantPitch * strength * moveScale * 0.55f * undershoot + jitterPitch), (float)(-maxPull * 0.55f), (float)(maxPull * 0.55f));
            applyYaw = Math.abs(rawYawPull) > step ? (float)Math.round(rawYawPull / step) * step : 0.0f;
            applyPitch = Math.abs(rawPitchPull) > step ? (float)Math.round(rawPitchPull / step) * step : 0.0f;
        } else if (totalAngle > deadzone * 0.4f) {
            float microStr = 0.14f;
            float rawYawPull = wantYaw * microStr;
            float rawPitchPull = wantPitch * microStr * 0.4f;
            applyYaw = Math.abs(rawYawPull) > step ? (float)Math.round(rawYawPull / step) * step : 0.0f;
            applyPitch = Math.abs(rawPitchPull) > step ? (float)Math.round(rawPitchPull / step) * step : 0.0f;
        }
        if (applyYaw != 0.0f || applyPitch != 0.0f) {
            float newYaw = player.getYaw() + applyYaw;
            float newPitch = MathHelper.clamp((float)(player.getPitch() + applyPitch), (float)-90.0f, (float)90.0f);
            player.setYaw(newYaw);
            player.setPitch(newPitch);
            this.lastPlayerYaw = newYaw;
            this.lastPlayerPitch = newPitch;
        }
    }

    private boolean canSeeTarget(MinecraftClient client, PlayerEntity player, Entity target) {
        Vec3d targetPos;
        Vec3d eyePos = player.getEyePos();
        BlockHitResult hit = client.world.raycast(new RaycastContext(eyePos, targetPos = new Vec3d(target.getX(), target.getY() + (double)target.getHeight() * 0.7, target.getZ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getPos().distanceTo(targetPos) < 1.0;
    }

    private void randomizeAimPoint(Entity target, float dist) {
        double half = Math.max(0.04, (double)target.getWidth() * 0.38);
        this.aimOffsetX = MathHelper.clamp((double)(this.aimRandom.nextGaussian() * half * 0.45), (double)(-half), (double)half);
        this.aimOffsetZ = MathHelper.clamp((double)(this.aimRandom.nextGaussian() * half * 0.45), (double)(-half), (double)half);
        double minY = dist > 3.0f ? 0.48 : 0.56;
        double maxY = dist > 3.0f ? 0.76 : 0.86;
        this.aimOffsetY = MathHelper.clamp((double)(minY + this.aimRandom.nextDouble() * (maxY - minY)), (double)minY, (double)maxY);
        this.aimPointTicks = 8 + this.aimRandom.nextInt(14);
    }
}
