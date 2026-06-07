package rich.modules.impl.combat;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_638;
import net.minecraft.class_746;

import java.util.Random;
import java.util.UUID;

public class AimEngine {

    private final Random aimRandom;
    private float lastPlayerYaw = 0f;
    private float lastPlayerPitch = 0f;
    private float rawTickYawDelta = 0f;
    private float rawTickPitchDelta = 0f;
    private boolean firstFrame = true;
    private UUID aimLockedTarget = null;
    private int aimLostTicks = 0;
    private int aimPointTicks = 0;
    private double aimOffsetX = 0.0;
    private double aimOffsetY = 0.65;
    private double aimOffsetZ = 0.0;
    private boolean hasPrevTarget = false;
    private int aimReactionTicks = 0;
    private float yawRemainder = 0f;
    private float pitchRemainder = 0f;
    private long lastAimTick = -1L;
    private float tickAimYaw = 0f;
    private float tickAimPitch = 0f;
    private float tickYawCap = 15.0f;
    private float tickPitchCap = 9.0f;
    private double prevTargetX;
    private double prevTargetZ;

    private final AimAssist config;

    public AimEngine(AimAssist config) {
        this.aimRandom = new Random();
        this.config = config;
    }

    public void onFrame(float partialTicks) {
        config.isState();
        class_310 mc = AimAssist.mc;
        if (!config.isState() || mc.field_1724 == null || mc.field_1687 == null) {
            aimLockedTarget = null;
            aimReactionTicks = 0;
            aimPointTicks = 0;
            hasPrevTarget = false;
            firstFrame = true;
            yawRemainder = 0f;
            pitchRemainder = 0f;
            tickAimYaw = 0f;
            tickAimPitch = 0f;
            lastAimTick = -1L;
            return;
        }
        if (mc.field_1755 != null) return;

        class_746 player = mc.field_1724;

        if (firstFrame) {
            lastPlayerYaw = player.method_36454();
            lastPlayerPitch = player.method_36455();
            firstFrame = false;
            return;
        }

        rawTickYawDelta = class_3532.method_15393(player.method_36454() - lastPlayerYaw);
        rawTickPitchDelta = player.method_36455() - lastPlayerPitch;
        lastPlayerYaw = player.method_36454();
        lastPlayerPitch = player.method_36455();

        float totalDelta = Math.abs(rawTickYawDelta) + Math.abs(rawTickPitchDelta);
        if (totalDelta < 0.01f) {
            yawRemainder = 0f;
            pitchRemainder = 0f;
            return;
        }

        class_243 eyePos = player.method_33571();
        class_243 prevPos = player.method_5828(1.0f);
        class_1297 target = null;
        float fovSetting = config.fov();

        // Try to find locked target
        if (aimLockedTarget != null) {
            for (Object obj : (Iterable<?>) mc.field_1687.method_18112()) {
                class_1297 e = (class_1297) obj;
                if (!e.method_5667().equals(aimLockedTarget)) continue;
                if (!(e instanceof class_1309)) continue;
                class_1309 living = (class_1309) e;
                if (living.method_5805() && !living.method_7325()
                        && player.method_5739(e) <= config.maxDistance()) {
                    target = living;
                    aimLostTicks = 0;
                } else {
                    aimLostTicks++;
                    if (aimLostTicks >= 25) aimLockedTarget = null;
                }
                break;
            }
        }

        // Fallback: find new target
        if (target == null) {
            class_1297 found = config.findTarget(mc);
            if (found != null) {
                target = found;
                aimLockedTarget = found.method_5667();
                aimLostTicks = 0;
                hasPrevTarget = false;
                aimReactionTicks = 1 + aimRandom.nextInt(2);
                aimPointTicks = 0;
            }
        }

        if (target == null) {
            hasPrevTarget = false;
            aimReactionTicks = 0;
            aimPointTicks = 0;
            yawRemainder = 0f;
            pitchRemainder = 0f;
            return;
        }

        if (!canSeeTarget(mc, player, target)) {
            aimLostTicks++;
            if (aimLostTicks > 8) {
                aimLockedTarget = null;
                aimReactionTicks = 0;
                aimPointTicks = 0;
                hasPrevTarget = false;
            }
            return;
        }

        float dist = player.method_5739(target);
        float halfWidth = target.method_17681() * 0.5f;

        if (aimReactionTicks > 0) {
            aimReactionTicks--;
            return;
        }

        aimPointTicks--;
        if (aimPointTicks <= 0) {
            randomizeAimPoint(target, dist);
        }

        double targetX = target.method_23317();
        double targetZ = target.method_23321();

        if (hasPrevTarget) {
            double velX = targetX - prevTargetX;
            double velZ = targetZ - prevTargetZ;
            float predFactor = class_3532.method_15363(dist * 0.4f, 0.5f, 2.0f);
            targetX += velX * predFactor;
            targetZ += velZ * predFactor;
        }

        prevTargetX = target.method_23317();
        prevTargetZ = target.method_23321();
        hasPrevTarget = true;

        double aimY = target.method_23318() + (double) target.method_17682() * aimOffsetY
                + aimRandom.nextGaussian() * 0.005;
        targetX = targetX + aimOffsetX + aimRandom.nextGaussian() * 0.003;
        double dX = targetX - eyePos.field_1352;
        double dY = aimY - eyePos.field_1351;
        targetZ = targetZ + aimOffsetZ + aimRandom.nextGaussian() * 0.003;
        double dZ = targetZ - eyePos.field_1350;

        double horizDist = Math.sqrt(dX * dX + dZ * dZ);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dX, dZ));
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dY, horizDist));

        float diffYaw = class_3532.method_15393(targetYaw - player.method_36454());
        float diffPitch = targetPitch - player.method_36455();

        // GCD
        float sens = (float) ((double) mc.field_1690.method_42495().method_41753() * 0.6f + 0.2f);
        float gcd = sens * sens * sens * 1.2f;

        float aimAngle = Math.max(
                (float) Math.toDegrees(Math.atan2((double) halfWidth, (double) dist)) * 0.15f,
                0.25f);

        float angDist = (float) Math.sqrt((double) (diffYaw * diffYaw + diffPitch * diffPitch));

        // Too far outside FOV buffer — unlock
        if (angDist > fovSetting + 12.0f) {
            aimLockedTarget = null;
            aimReactionTicks = 0;
            aimPointTicks = 0;
            return;
        }

        // Frame skip
        if (aimRandom.nextFloat() < 0.04f) return;

        // Per-tick cap refresh
        long currentTick = mc.field_1687.method_75260();
        if (currentTick != lastAimTick) {
            lastAimTick = currentTick;
            tickAimYaw = 0f;
            tickAimPitch = 0f;
            tickYawCap = 13.0f + aimRandom.nextFloat() * 5.0f;
            tickPitchCap = 7.5f + aimRandom.nextFloat() * 3.0f;
        }

        float finalYaw = 0f;
        float finalPitch = 0f;

        boolean movingToward = (rawTickYawDelta * diffYaw + rawTickPitchDelta * diffPitch) > 0;

        if (angDist > aimAngle) {
            float t = Math.min((angDist - aimAngle) / 18.0f, 1.0f);

            float strength = movingToward ? 0.5f + t * 0.35f : 0.05f;
            float gcdRem   = movingToward ? 3.5f + t * 4.0f   : 0.6f;
            float speedMul = movingToward
                    ? class_3532.method_15363(totalDelta * 0.9f, 0.3f, 1.0f)
                    : 0.25f;

            float jitterYaw   = (aimRandom.nextFloat() - 0.5f) * 0.16f;
            float jitterPitch = (aimRandom.nextFloat() - 0.5f) * 0.10f;

            float r = aimRandom.nextFloat();
            float overshoot;
            if      (r < 0.14f) overshoot = 1.04f + aimRandom.nextFloat() * 0.12f;
            else if (r < 0.32f) overshoot = 0.72f + aimRandom.nextFloat() * 0.18f;
            else                overshoot = 0.95f;

            float rawYaw   = class_3532.method_15363(diffYaw   * strength * speedMul * overshoot + jitterYaw,   -gcdRem,  gcdRem);
            float rawPitch = class_3532.method_15363(diffPitch * strength * speedMul * 0.6f * overshoot + jitterPitch, -gcdRem * 0.6f, gcdRem * 0.6f);

            float capY = Math.max(0f, tickYawCap   - Math.abs(tickAimYaw));
            float capP = Math.max(0f, tickPitchCap - Math.abs(tickAimPitch));
            rawYaw   = class_3532.method_15363(rawYaw,   -capY, capY);
            rawPitch = class_3532.method_15363(rawPitch, -capP, capP);

            float accYaw   = rawYaw   + yawRemainder;
            float accPitch = rawPitch + pitchRemainder;

            finalYaw   = Math.round(accYaw   / gcd) * gcd;
            finalPitch = Math.round(accPitch / gcd) * gcd;

            yawRemainder   = class_3532.method_15363(accYaw   - finalYaw,   -gcd * 4.0f, gcd * 4.0f);
            pitchRemainder = class_3532.method_15363(accPitch - finalPitch, -gcd * 4.0f, gcd * 4.0f);

            tickAimYaw   += finalYaw;
            tickAimPitch += finalPitch;
        } else {
            yawRemainder   = 0f;
            pitchRemainder = 0f;
        }

        if (finalYaw != 0f || finalPitch != 0f) {
            float newYaw   = player.method_36454() + finalYaw;
            float newPitch = class_3532.method_15363(player.method_36455() + finalPitch, -90.0f, 90.0f);
            player.method_36456(newYaw);
            player.method_36457(newPitch);
            lastPlayerYaw   = newYaw;
            lastPlayerPitch = newPitch;
        }
    }

    private boolean canSeeTarget(class_310 mc, class_1657 player, class_1297 target) {
        class_243 eye = player.method_33571();
        class_243 targetPoint = new class_243(
                target.method_23317(),
                target.method_23318() + (double) target.method_17682() * 0.7,
                target.method_23321());
        var ctx = new class_3959(
                eye,
                targetPoint,
                class_3959.class_3960.field_17558,
                class_3959.class_242.field_1348,
                player);
        var result = mc.field_1687.method_17742(ctx);
        var type = result.method_17783();
        if (type == net.minecraft.class_239.class_240.field_1333) return true;
        return result.method_17784().method_1022(targetPoint) < 1.0;
    }

    private void randomizeAimPoint(class_1297 target, float dist) {
        double halfW = Math.max(0.04, (double) target.method_17681() * 0.38);
        aimOffsetX = class_3532.method_15350(aimRandom.nextGaussian() * halfW * 0.45, -halfW, halfW);
        aimOffsetZ = class_3532.method_15350(aimRandom.nextGaussian() * halfW * 0.45, -halfW, halfW);

        double yMin = dist > 3.0f ? 0.48 : 0.56;
        double yMax = dist > 3.0f ? 0.76 : 0.86;
        aimOffsetY = class_3532.method_15350(
                yMin + aimRandom.nextDouble() * (yMax - yMin), yMin, yMax);

        aimPointTicks = 8 + aimRandom.nextInt(14);
    }
}
