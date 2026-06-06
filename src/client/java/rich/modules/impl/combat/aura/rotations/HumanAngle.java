package rich.modules.impl.combat.aura.rotations;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import rich.Initialization;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.modules.impl.combat.aura.target.Vector;

import java.util.Random;

/**
 * HumanAngle - anti-detect aimbot rotation.
 *
 * Key anti-detect changes vs previous version:
 *
 * 1. MAX_YAW_TICK reduced 38 -> 28 deg/tick.
 *    38 is too fast for automatic patterns — ACs flag it even with noise.
 *    28 is high-DPI achievable and much safer.
 *
 * 2. When canHit=true, NO longer use speed=1.0 (instant snap).
 *    An instant snap to exact target on every attack tick is the #1 detection signal.
 *    Now uses speed=0.90 with jitter kept ON.

 * 3. Aim point: always a random point on upper body, never exact center.
 *    Sigma increased slightly so the variance is human-like.
 *
 * 4. Removed RaycastAngle (was causing potential NPE on some entity states).
 *    Tracking detection now uses simple dist threshold.
 */
public class HumanAngle extends RotateConstructor {

    // Hard cap per tick. 28/18 is fast but within human high-DPI range.
    // DO NOT increase above 32/22 - that's where ACs start flagging auto-aim.
    private static final float MAX_YAW_TICK   = 28.0F;
    private static final float MAX_PITCH_TICK = 18.0F;

    // Jitter state (exponential moving average of gaussian noise)
    private float jYaw   = 0.0F;
    private float jPitch = 0.0F;

    // Micro-overshoot state
    private float osYaw   = 0.0F;
    private float osPitch = 0.0F;

    private final Random rng = new Random();

    public HumanAngle() {
        super("Human");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, Vec3d vec, Entity entity) {
        // Safe manager access
        StrikeManager sm = null;
        try {
            sm = Initialization.getInstance().getManager().getAttackPerpetrator();
        } catch (Exception ignored) {}
        boolean canHit = entity != null && sm != null && sm.canAttack(null, 0);

        // ── 1. Select aim point ──────────────────────────────────────────────
        // Never aim at exact geometric center — always add small random offset.
        // This prevents the "always perfect center" detection pattern.
        if (entity != null) {
            float yF = entity.isOnGround() ? 0.92F : 1.35F;
            Vec3d base = Vector.hitbox(entity, 1.0F, yF, 1.0F, 2.0F);
            // Gaussian offset ~4% of hitbox width — visible variance, human-like
            double ox = rng.nextGaussian() * 0.06;
            double oy = rng.nextGaussian() * 0.04;
            target = MathAngle.calculateAngle(new Vec3d(base.x + ox, base.y + oy, base.z + ox));
        }

        // ── 2. Delta & distance ──────────────────────────────────────────────
        Angle delta = MathAngle.calculateDelta(current, target);
        float dYaw   = delta.getYaw();
        float dPitch = delta.getPitch();
        float dist   = (float) Math.hypot(dYaw, dPitch);
        if (dist < 0.005F) return current;

        // ── 3. Speed factor ──────────────────────────────────────────────────
        //
        // dist=180 -> base=0.92 -> move capped at 28°/tick -> 7 ticks to target
        // dist=60  -> base=0.92 -> move=55° capped at 28°/tick
        // dist=25  -> base=0.45 -> move=11.3°/tick (slowing down)
        // dist=8   -> base=0.15 -> move=1.2°/tick (precision approach)
        //
        // canHit: use 0.90 (NOT 1.0!) — instant snap is the top detection signal.
        //         We still hit on time because canHit window lasts multiple ticks.
        //
        float speed;
        if (canHit) {
            // Slightly randomized fast speed, but NOT instant snap
            speed = 0.88F + rng.nextFloat() * 0.08F; // 0.88 - 0.96
        } else {
            // Distance easing: slow down as we approach target
            speed = MathHelper.clamp(dist / 55.0F, 0.12F, 0.90F);
            // Random tick-to-tick variation (humans are inconsistent)
            speed *= (0.84F + rng.nextFloat() * 0.28F);
            speed  = MathHelper.clamp(speed, 0.10F, 0.93F);
        }

        // ── 4. Raw movement ──────────────────────────────────────────────────
        float moveYaw   = dYaw   * speed;
        float movePitch = dPitch * speed;

        // ── 5. Hard cap (core anti-detect measure) ───────────────────────────
        moveYaw   = MathHelper.clamp(moveYaw,   -MAX_YAW_TICK,   MAX_YAW_TICK);
        movePitch = MathHelper.clamp(movePitch, -MAX_PITCH_TICK, MAX_PITCH_TICK);

        // ── 6. Micro-jitter ──────────────────────────────────────────────────
        // ALWAYS add jitter — even when canHit=true.
        // Reason: zero jitter during attack = detectable signature.
        // Jitter is smaller when close (dist < 5) to avoid overshooting the hitbox.
        float jitterScale = (dist > 5.0F) ? 1.0F : Math.max(0.2F, dist / 5.0F);
        if (dist > 0.5F) {
            float sigma = canHit ? 0.10F : 0.22F; // less jitter during attack
            float tjY = (float)(rng.nextGaussian() * sigma);
            float tjP = (float)(rng.nextGaussian() * sigma * 0.6F);
            // Low-pass smooth so noise is correlated across ticks (real mouse behaviour)
            jYaw   += (tjY * jitterScale - jYaw)   * 0.40F;
            jPitch += (tjP * jitterScale - jPitch) * 0.40F;
        } else {
            jYaw   *= 0.50F;
            jPitch *= 0.50F;
        }

        // ── 7. Micro-overshoot (only when not attacking) ─────────────────────
        // 7% chance per tick when 1.5 < dist < 10 to add a tiny overshoot.
        // Simulates the natural flick-past-target-then-correct pattern.
        if (!canHit && dist > 1.5F && dist < 10.0F) {
            if (rng.nextFloat() < 0.07F) {
                osYaw   = (rng.nextFloat() - 0.5F) * 2.5F;
                osPitch = (rng.nextFloat() - 0.5F) * 1.2F;
            }
        }
        osYaw   *= 0.70F;
        osPitch *= 0.70F;

        // ── 8. Final angle ───────────────────────────────────────────────────
        float finalYaw   = current.getYaw()   + moveYaw   + jYaw   + (canHit ? 0.0F : osYaw);
        float finalPitch = current.getPitch() + movePitch + jPitch + (canHit ? 0.0F : osPitch);

        return new Angle(finalYaw, MathHelper.clamp(finalPitch, -90.0F, 90.0F));
    }

    @Override
    public Vec3d randomValue() {
        return Vec3d.ZERO;
    }
}
