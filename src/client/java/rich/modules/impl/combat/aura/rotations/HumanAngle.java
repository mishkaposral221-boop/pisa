package rich.modules.impl.combat.aura.rotations;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import rich.Initialization;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.modules.impl.combat.aura.target.RaycastAngle;
import rich.modules.impl.combat.aura.target.Vector;

import java.util.Random;

/**
 * HumanAngle - anti-detect aimbot rotation.
 *
 * What anti-cheats detect:
 *   1. Large angle change per tick (>20-40 deg/tick depending on AC sensitivity)
 *   2. Perfect linear acceleration (no noise)
 *   3. Always snapping to exact hitbox center
 *   4. GCD mismatch (handled by Angle.adjustSensitivity)
 *
 * What this does:
 *   - Hard cap: max 38 deg/tick yaw, 28 deg/tick pitch
 *     -> 180 deg target = ~5 ticks (250ms). Fast but not instant.
 *   - Distance-based easing:
 *     Far (>60 deg): approach at max speed
 *     Close (<15 deg): slow down precision approach
 *   - Gaussian micro-jitter (sigma ~0.15 deg) simulates mouse tremor
 *   - Random aim offset on hitbox (+-3% of hitbox size) so we don't
 *     always hit the exact same point
 *   - Near-target micro-overshoot then correction (like a real flick)
 */
public class HumanAngle extends RotateConstructor {

    // Hard cap on how many degrees we rotate per tick.
    // 38 deg/tick is fast but still within human-achievable range for high-DPI mice.
    private static final float MAX_YAW_TICK   = 38.0F;
    private static final float MAX_PITCH_TICK = 28.0F;

    // Jitter state (low-pass filtered gaussian noise)
    private float jYaw   = 0.0F;
    private float jPitch = 0.0F;

    // Overshoot state
    private float osYaw   = 0.0F;
    private float osPitch = 0.0F;

    // Whether we were previously on target (to detect new snaps)
    private boolean wasTracking = false;

    private final Random rng = new Random();

    public HumanAngle() {
        super("Human");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, Vec3d vec, Entity entity) {
        StrikeManager sm   = Initialization.getInstance().getManager().getAttackPerpetrator();
        boolean canHit     = entity != null && sm.canAttack(null, 0);
        boolean onTarget   = false;

        // ── 1. Select aim point ──────────────────────────────────────────────
        if (entity != null) {
            // Aim at upper body with tiny random offset per tick
            float yFactor = canHit
                ? (entity.isOnGround() ? 0.9F : 1.3F)
                : (entity.isOnGround() ? 1.0F : 1.4F);
            Vec3d base = Vector.hitbox(entity, 1.0F, yFactor, 1.0F, 2.0F);
            if (canHit) {
                // Small gaussian offset to avoid perfect-center detection
                double ox = rng.nextGaussian() * 0.04;
                double oy = rng.nextGaussian() * 0.025;
                base = new Vec3d(base.x + ox, base.y + oy, base.z + ox);
            }
            target = MathAngle.calculateAngle(base);

            // Check if we're currently tracking (already on target roughly)
            onTarget = RaycastAngle.rayTrace(
                AngleConnection.INSTANCE.getRotation().toVector(), 4.0, entity.getBoundingBox()
            );
        }

        // ── 2. Compute delta ─────────────────────────────────────────────────
        Angle delta = MathAngle.calculateDelta(current, target);
        float dYaw   = delta.getYaw();
        float dPitch = delta.getPitch();
        float dist   = (float) Math.hypot(dYaw, dPitch);
        if (dist < 0.005F) dist = 0.005F;

        // Detect if target just jumped to a new position (new target or teleport)
        boolean bigSnap = !wasTracking && dist > 80.0F;
        wasTracking = onTarget || dist < 5.0F;

        // ── 3. Speed factor (distance-based easing) ──────────────────────────
        //
        // speed = clamp(dist / 55, 0.12, 1.0) * jitter
        //
        // dist=180 -> speed=0.99 -> move=180*0.99=~178 -> capped at 38°/tick
        // dist=60  -> speed=0.99 -> move=~59.4 -> capped at 38°/tick
        // dist=25  -> speed=0.45 -> move=11.4°/tick  (slowing down)
        // dist=10  -> speed=0.18 -> move=1.8°/tick   (precision)
        // dist=3   -> speed=0.12 -> move=0.36°/tick  (micro-correction)
        //
        float speed;
        if (canHit) {
            // When attacking: go as fast as the cap allows (server needs us on target NOW)
            speed = 1.0F;
        } else if (bigSnap) {
            // New target: ramp up quickly to look like a mouse flick
            speed = 0.95F + rng.nextFloat() * 0.05F;
        } else {
            // Normal tracking: ease based on distance
            speed = MathHelper.clamp(dist / 55.0F, 0.12F, 0.92F);
            // Random variation (humans are not consistent)
            speed *= (0.88F + rng.nextFloat() * 0.24F);
            speed  = MathHelper.clamp(speed, 0.12F, 0.96F);
        }

        // ── 4. Raw movement this tick ────────────────────────────────────────
        float moveYaw   = dYaw   * speed;
        float movePitch = dPitch * speed;

        // ── 5. Hard cap per tick (the core anti-detect measure) ──────────────
        moveYaw   = MathHelper.clamp(moveYaw,   -MAX_YAW_TICK,   MAX_YAW_TICK);
        movePitch = MathHelper.clamp(movePitch, -MAX_PITCH_TICK, MAX_PITCH_TICK);

        // ── 6. Micro-jitter (gaussian mouse tremor) ──────────────────────────
        // Only add noise when not on the attack tick
        if (!canHit && dist > 1.5F) {
            float tjY = (float)(rng.nextGaussian() * 0.20);
            float tjP = (float)(rng.nextGaussian() * 0.12);
            // Low-pass: smooth the jitter (don't jump instantly to new noise value)
            jYaw   += (tjY - jYaw)   * 0.45F;
            jPitch += (tjP - jPitch) * 0.45F;
        } else {
            // Fade out jitter when near or attacking
            jYaw   *= 0.55F;
            jPitch *= 0.55F;
        }

        // ── 7. Micro-overshoot near target ───────────────────────────────────
        // Simulate the tiny flick-overshoot that real players make when close.
        // Probability 6% per tick when dist < 12 and not attacking.
        if (!canHit && dist > 1.0F && dist < 12.0F) {
            if (rng.nextFloat() < 0.06F) {
                osYaw   = (rng.nextFloat() - 0.5F) * 3.0F;
                osPitch = (rng.nextFloat() - 0.5F) * 1.5F;
            }
        }
        // Decay overshoot
        osYaw   *= 0.72F;
        osPitch *= 0.72F;

        // ── 8. Assemble final angle ──────────────────────────────────────────
        float finalYaw   = current.getYaw()   + moveYaw   + jYaw   + (canHit ? 0.0F : osYaw);
        float finalPitch = current.getPitch() + movePitch + jPitch + (canHit ? 0.0F : osPitch);

        return new Angle(finalYaw, MathHelper.clamp(finalPitch, -90.0F, 90.0F));
    }

    @Override
    public Vec3d randomValue() {
        return Vec3d.ZERO;
    }
}
