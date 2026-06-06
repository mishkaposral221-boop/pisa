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

/**
 * Core aim-assist engine. Reconstructed from runtime-visuals-1.0.01.jar.
 */
public class AimEngine {

    private final AimAssist config;
    private final Random aimRandom = new Random();

    // --- Rotation state ---
    private float   lastPlayerYaw;
    private float   lastPlayerPitch;
    private float   rawTickYawDelta;
    private float   rawTickPitchDelta;
    private boolean firstFrame = true;

    // --- Target lock ---
    private UUID    aimLockedTarget  = null;
    private int     aimLostTicks     = 0;
    private int     aimReactionTicks = 0;

    // --- Aim-point randomization ---
    private double  aimOffsetX   = 0.0;
    private double  aimOffsetY   = 0.65;
    private double  aimOffsetZ   = 0.0;
    private int     aimPointTicks = 0;

    // --- Prediction ---
    private double  prevTargetX;
    private double  prevTargetZ;
    private boolean hasPrevTarget = false;

    // --- GCD remainder accumulators ---
    private float   yawRemainder   = 0.0F;
    private float   pitchRemainder = 0.0F;

    // --- Per-tick angle budget ---
    private static final float BASE_TICK_YAW   = 13.0F;
    private static final float BASE_TICK_PITCH =  7.5F;
    private long    lastAimTick  = -1L;
    private float   tickAimYaw   = 0.0F;
    private float   tickAimPitch = 0.0F;
    private float   tickYawCap   = 15.0F;
    private float   tickPitchCap =  9.0F;

    public AimEngine(AimAssist config) {
        this.config = config;
    }

    public void onFrame(float partialTick) {
        MinecraftClient mc = config.mc;
        if (!config.isState() || mc.player == null || mc.world == null) {
            resetState();
            return;
        }
        if (mc.currentScreen != null) return;

        ClientPlayerEntity player = mc.player;

        if (firstFrame) {
            lastPlayerYaw   = player.getYaw();
            lastPlayerPitch = player.getPitch();
            firstFrame = false;
            return;
        }

        rawTickYawDelta   = MathHelper.wrapDegrees(player.getYaw()   - lastPlayerYaw);
        rawTickPitchDelta = player.getPitch() - lastPlayerPitch;
        lastPlayerYaw     = player.getYaw();
        lastPlayerPitch   = player.getPitch();

        float mouseDelta = Math.abs(rawTickYawDelta) + Math.abs(rawTickPitchDelta);
        if (mouseDelta < 0.01F) {
            yawRemainder   = 0.0F;
            pitchRemainder = 0.0F;
            return;
        }

        Entity target = resolveTarget(mc, player);
        if (target == null) {
            yawRemainder   = 0.0F;
            pitchRemainder = 0.0F;
            return;
        }

        if (aimReactionTicks > 0) { --aimReactionTicks; return; }

        if (!canSeeTarget(mc, player, target)) {
            if (++aimLostTicks > 8) {
                aimLockedTarget  = null;
                aimReactionTicks = 0;
                hasPrevTarget    = false;
            }
            return;
        }
        aimLostTicks = 0;

        if (--aimPointTicks <= 0)
            randomizeAimPoint(target, player.distanceTo(target));

        applyRotation(mc, player, target);
    }

    private void applyRotation(MinecraftClient mc, ClientPlayerEntity player, Entity target) {
        Vec3d eye = player.getEyePos();

        double tx = target.getX(), tz = target.getZ();
        if (hasPrevTarget) {
            float dist       = player.distanceTo(target);
            float predFactor = MathHelper.clamp(dist * 0.4F, 0.5F, 2.0F);
            tx += (tx - prevTargetX) * predFactor;
            tz += (tz - prevTargetZ) * predFactor;
        }
        prevTargetX   = target.getX();
        prevTargetZ   = target.getZ();
        hasPrevTarget = true;

        double ty = target.getY() + target.getHeight() * aimOffsetY
                    + aimRandom.nextGaussian() * 0.005;
        double dx = tx + aimOffsetX + aimRandom.nextGaussian() * 0.003 - eye.x;
        double dy = ty - eye.y;
        double dz = tz + aimOffsetZ + aimRandom.nextGaussian() * 0.003 - eye.z;

        double hDist    = Math.sqrt(dx * dx + dz * dz);
        float wantYaw   = (float)  Math.toDegrees(Math.atan2(-dx, dz));
        float wantPitch = (float)(-Math.toDegrees(Math.atan2(dy, hDist)));

        float yawD      = MathHelper.wrapDegrees(wantYaw   - player.getYaw());
        float pitchD    = wantPitch - player.getPitch();
        float totalAngle = (float) Math.sqrt(yawD * yawD + pitchD * pitchD);
        if (totalAngle < 0.001F) return;

        if (totalAngle > config.fov() + 12.0F) { aimLockedTarget = null; return; }

        float halfW   = target.getWidth() * 0.5F;
        float dist    = player.distanceTo(target);
        float deadzone = Math.max((float) Math.toDegrees(Math.atan2(halfW, dist)) * 0.15F, 0.25F);
        if (totalAngle < deadzone) { yawRemainder = 0.0F; pitchRemainder = 0.0F; return; }

        if (aimRandom.nextFloat() < 0.04F) return;

        // GCD (mouse quantum)
        float sens = (float)((Double) mc.options.getMouseSensitivity().getValue()).doubleValue()
                     * 0.6F + 0.2F;
        float step = sens * sens * sens * 1.2F;

        boolean movingToward = rawTickYawDelta * yawD + rawTickPitchDelta * pitchD > 0;
        float maxPull  = 18.0F + config.smoothness() * 14.0F;
        float yawCap   = (Math.abs(yawD)   / totalAngle) * maxPull;
        float pitchCap = (Math.abs(pitchD) / totalAngle) * maxPull * 0.65F;

        float t        = Math.min((totalAngle - 0.25F) / 18.0F, 1.0F);
        float strength = movingToward ? 0.5F + t * 0.35F : 0.05F;
        float jitterYaw   = (aimRandom.nextFloat() - 0.5F) * 0.16F;
        float jitterPitch = (aimRandom.nextFloat() - 0.5F) * 0.10F;

        float r = aimRandom.nextFloat();
        float overshoot = r < 0.14F ? 1.04F + aimRandom.nextFloat() * 0.12F
                        : r < 0.32F ? 0.72F + aimRandom.nextFloat() * 0.18F
                        : 0.95F;

        float rawYawPull   = MathHelper.clamp(yawD   * strength * overshoot + jitterYaw,   -yawCap,   yawCap);
        float rawPitchPull = MathHelper.clamp(pitchD * strength * overshoot * 0.6F + jitterPitch, -pitchCap, pitchCap);
        if (!movingToward) { rawYawPull *= 0.12F; rawPitchPull *= 0.12F; }

        long curTick = mc.world.getTime();
        if (curTick != lastAimTick) {
            lastAimTick  = curTick;
            tickAimYaw   = 0.0F;
            tickAimPitch = 0.0F;
            tickYawCap   = BASE_TICK_YAW   + aimRandom.nextFloat() * 5.0F;
            tickPitchCap = BASE_TICK_PITCH + aimRandom.nextFloat() * 3.0F;
        }
        rawYawPull   = MathHelper.clamp(rawYawPull,   -(tickYawCap   - Math.abs(tickAimYaw)),   tickYawCap   - Math.abs(tickAimYaw));
        rawPitchPull = MathHelper.clamp(rawPitchPull, -(tickPitchCap - Math.abs(tickAimPitch)), tickPitchCap - Math.abs(tickAimPitch));

        float desiredYaw   = rawYawPull   + yawRemainder;
        float desiredPitch = rawPitchPull + pitchRemainder;
        float applyYaw     = Math.round(desiredYaw   / step) * step;
        float applyPitch   = Math.round(desiredPitch / step) * step;
        yawRemainder   = MathHelper.clamp(desiredYaw   - applyYaw,   -step * 3.5F, step * 3.5F);
        pitchRemainder = MathHelper.clamp(desiredPitch - applyPitch, -step * 3.5F, step * 3.5F);

        tickAimYaw   += applyYaw;
        tickAimPitch += applyPitch;

        if (applyYaw != 0.0F || applyPitch != 0.0F) {
            float newYaw   = player.getYaw()   + applyYaw;
            float newPitch = MathHelper.clamp(player.getPitch() + applyPitch, -90.0F, 90.0F);
            player.setYaw(newYaw);
            player.setPitch(newPitch);
            lastPlayerYaw   = newYaw;
            lastPlayerPitch = newPitch;
        }
    }

    private Entity resolveTarget(MinecraftClient mc, ClientPlayerEntity player) {
        if (aimLockedTarget != null) {
            for (Entity e : mc.world.getEntities()) {
                if (!e.getUuid().equals(aimLockedTarget)) continue;
                if (!(e instanceof LivingEntity living)) break;
                if (living.isAlive() && !living.isSpectator()
                        && player.distanceTo(e) <= config.maxDistance()) {
                    aimLostTicks = 0;
                    return e;
                }
                if (++aimLostTicks >= 25) aimLockedTarget = null;
                break;
            }
        }
        Entity found = config.findTarget(mc);
        if (found != null) {
            aimLockedTarget  = found.getUuid();
            aimLostTicks     = 0;
            hasPrevTarget    = false;
            aimReactionTicks = 1 + aimRandom.nextInt(2);
            aimPointTicks    = 0;
        }
        return found;
    }

    private boolean canSeeTarget(MinecraftClient mc, PlayerEntity player, Entity target) {
        Vec3d eye       = player.getEyePos();
        double targetY  = target.getY() + target.getHeight() * 0.7;
        Vec3d targetPos = new Vec3d(target.getX(), targetY, target.getZ());
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
            eye, targetPos,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            player));
        return hit.getType() == HitResult.Type.MISS
            || hit.getPos().distanceTo(targetPos) < 1.0;
    }

    private void randomizeAimPoint(Entity target, float dist) {
        double half = Math.max(0.04, target.getWidth() * 0.38);
        aimOffsetX = MathHelper.clamp(aimRandom.nextGaussian() * half * 0.45, -half, half);
        aimOffsetZ = MathHelper.clamp(aimRandom.nextGaussian() * half * 0.45, -half, half);
        double minY = dist > 3.0F ? 0.48 : 0.56;
        double maxY = dist > 3.0F ? 0.76 : 0.86;
        aimOffsetY = minY + aimRandom.nextDouble() * (maxY - minY);
        aimPointTicks = 8 + aimRandom.nextInt(14);
    }

    private void resetState() {
        aimLockedTarget  = null;
        aimReactionTicks = 0;
        aimPointTicks    = 0;
        hasPrevTarget    = false;
        firstFrame       = true;
        yawRemainder     = 0.0F;
        pitchRemainder   = 0.0F;
        tickAimYaw       = 0.0F;
        tickAimPitch     = 0.0F;
        lastAimTick      = -1L;
    }
}
