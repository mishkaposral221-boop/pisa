package rich.modules.impl.combat;

import java.util.Random;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;

/**
 * KillAuraSpooky -- SPAngle profile (SpookyTime bypass).
 *
 * Silent rotation (from 1st person camera is not affected):
 *   AngleConnection.INSTANCE.setRotation(angle)
 *   -> ClientPlayerEntityMixin @ModifyExpressionValue on sendMovementPackets
 *   -> packet yaw/pitch = aim yaw/pitch
 *   -> player.yaw is NEVER changed -> camera stays on real mouse yaw
 *
 * On disable: AngleConnection reset + bodyYaw/headYaw snapped back.
 */
public class KillAuraSpooky extends ModuleStructure {

    public final SliderSettings range      = new SliderSettings("Range",     "Attack range")      .setValue(3.1F).range(1.0F, 6.0F);
    public final SliderSettings fovSetting = new SliderSettings("FOV",       "Max FOV to target") .setValue(180.0F).range(10.0F, 180.0F);
    public final BooleanSetting onlySword  = new BooleanSetting("OnlySword", "Only sword/axe")    .setValue(true);
    public final BooleanSetting silentRot  = new BooleanSetting("Silent",    "Silent rotation")   .setValue(true);

    private final Random rng = new Random();

    private float   curYaw;
    private float   curPitch;
    private boolean hasRotation = false;

    private Entity  lockedTarget      = null;
    private Entity  pendingAttack     = null;
    private long    preAttackDeadline = 0L;
    private boolean pendingWasForward = false;

    private int     ticksOutOfWater = 10;
    private int     ticksOnGround   = 0;
    private boolean wasFalling      = false;
    private int     fallGroundTicks  = 0;

    private static final int   W_PRE_MIN       = 1;
    private static final int   W_PRE_MAX       = 15;
    private static final int   FALL_GATE_TICKS = 40;
    private static final float CRIT_CHARGE     = 0.84F;

    public KillAuraSpooky() {
        super("KillAuraSpooky", "SPAngle kill-aura (SpookyTime bypass)", ModuleCategory.UTILITIES);
    }

    // -------------------------------------------------------------------------
    // Enable / Disable
    // -------------------------------------------------------------------------

    @Override
    public void activate() {
        if (mc.player != null) {
            curYaw   = mc.player.getYaw();
            curPitch = mc.player.getPitch();
        }
        hasRotation   = false;
        lockedTarget  = null;
        pendingAttack = null;
    }

    @Override
    public void deactivate() {
        // Clear silent rotation so the next sendMovementPackets uses real yaw
        AngleConnection.INSTANCE.setRotation(null);
        hasRotation   = false;
        lockedTarget  = null;
        pendingAttack = null;
        // Snap body/head yaw back so the model doesn't stay rotated
        if (mc.player != null) {
            mc.player.setBodyYaw(mc.player.getYaw());
            mc.player.headYaw = mc.player.getYaw();
        }
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null || !this.isState()) return;

        // Water timer
        if (mc.player.isTouchingWater()) ticksOutOfWater = 0;
        else if (ticksOutOfWater < 20) ticksOutOfWater++;

        // Ground / fall timer
        boolean onGround = mc.player.isOnGround();
        if (onGround) {
            if (wasFalling) { fallGroundTicks = 0; wasFalling = false; }
            if (ticksOnGround < 20) ticksOnGround++;
        } else {
            ticksOnGround = 0;
            if (mc.player.getVelocity().y < -0.1) wasFalling = true;
        }
        if (wasFalling && onGround) { if (fallGroundTicks < FALL_GATE_TICKS) fallGroundTicks++; }

        // Pending attack
        if (pendingAttack != null) {
            if (System.currentTimeMillis() > preAttackDeadline || pendingWasForward) {
                doAttack(pendingAttack);
                pendingAttack = null;
            }
        }

        Entity target = findTarget();
        if (target == null) {
            resetRotation();
            lockedTarget = null;
            return;
        }
        lockedTarget = target;

        // Compute needed angles
        Angle needed = calcAngle(mc.player, target);
        Angle next   = spAngleRotate(new Angle(curYaw, curPitch), needed);

        curYaw   = next.getYaw();
        curPitch = next.getPitch();
        hasRotation = true;

        if (silentRot.isValue()) {
            AngleConnection.INSTANCE.setRotation(next);
        } else {
            mc.player.setYaw(curYaw);
            mc.player.setPitch(curPitch);
        }

        // Attack when on target
        boolean reached = Math.abs(MathHelper.wrapDegrees(needed.getYaw() - curYaw)) < 3.0F
                       && Math.abs(needed.getPitch() - curPitch) < 3.0F;
        if (reached && canAttack(target)) {
            int preDelay = W_PRE_MIN + rng.nextInt(W_PRE_MAX - W_PRE_MIN + 1);
            pendingAttack     = target;
            preAttackDeadline = System.currentTimeMillis() + preDelay;
            pendingWasForward = isForwardPressed();
        }
    }

    // -------------------------------------------------------------------------
    // SPAngle rotation
    // -------------------------------------------------------------------------

    private Angle spAngleRotate(Angle current, Angle target) {
        float dy = MathHelper.wrapDegrees(target.getYaw()   - current.getYaw());
        float dp = MathHelper.wrapDegrees(target.getPitch() - current.getPitch());

        float yawLimit   = (float) Math.min(Math.abs(dy), 74.0F + (float)(rng.nextDouble() * 1.03));
        float pitchLimit = (float) Math.min(Math.abs(dp), 32.33F);

        float totalYaw   = Math.abs(dy);
        float totalPitch = Math.abs(dp);
        boolean reached  = totalYaw <= yawLimit && totalPitch <= pitchLimit;

        float maxStepYaw, maxStepPitch;
        if (reached) {
            maxStepYaw   = 65.0F + rng.nextFloat() * 35.0F;
            maxStepPitch = 65.0F + rng.nextFloat() * 35.0F;
        } else {
            maxStepYaw   = 7.7F  + rng.nextFloat() * 4.4F;
            maxStepPitch = 7.7F  + rng.nextFloat() * 4.4F;
        }

        float stepYaw;
        if (totalYaw < 1e-4F) {
            stepYaw = 0;
        } else {
            float scaleY = Math.min(totalYaw, maxStepYaw) / totalYaw;
            if (!reached) scaleY = ease(scaleY);
            stepYaw = Math.copySign(Math.min(yawLimit, totalYaw * scaleY), dy);
        }

        float stepPitch;
        if (totalPitch < 1e-4F) {
            stepPitch = 0;
        } else {
            float scaleP = Math.min(totalPitch, maxStepPitch) / totalPitch;
            if (!reached) scaleP = ease(scaleP);
            stepPitch = Math.copySign(Math.min(pitchLimit, totalPitch * scaleP), dp);
        }

        float newYaw   = current.getYaw()   + stepYaw;
        float newPitch = current.getPitch() + stepPitch;
        newPitch = MathHelper.clamp(newPitch, -90.0F, 90.0F);

        // Idle sway when on target
        if (reached) {
            long ms = System.currentTimeMillis();
            double t = (ms % 12000L) / 1200.0;
            float sway = (float)(Math.sin(t * 3.0 * 2.0 * Math.PI) * 1.15 * rng.nextGaussian() * 0.15);
            newYaw += sway;
        }

        return new Angle(newYaw, newPitch).adjustSensitivity();
    }

    private float ease(float t) {
        return t * (0.5F + 0.5F * t);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void resetRotation() {
        hasRotation = false;
        if (silentRot.isValue()) {
            AngleConnection.INSTANCE.setRotation(null);
        }
    }

    private Entity findTarget() {
        if (mc.player == null || mc.world == null) return null;
        float maxRange = range.getValue();
        float fov      = fovSetting.getValue();
        Entity best    = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (le.isDead() || le.getHealth() <= 0) continue;
            if (e == mc.player) continue;
            if (e instanceof PlayerEntity pe && pe.isCreative()) continue;
            if (le.hasStatusEffect(StatusEffects.INVISIBILITY)) continue;

            double dist = mc.player.distanceTo(e);
            if (dist > maxRange) continue;

            Angle needed = calcAngle(mc.player, e);
            float dYaw = Math.abs(MathHelper.wrapDegrees(needed.getYaw() - mc.player.getYaw()));
            if (dYaw > fov / 2f) continue;

            if (!hasLineOfSight(e)) continue;

            if (dist < bestDist) {
                bestDist = dist;
                best     = e;
            }
        }
        return best;
    }

    private boolean hasLineOfSight(Entity target) {
        if (mc.world == null) return false;
        Vec3d eyes  = mc.player.getCameraPosVec(1.0F);
        Vec3d tPos  = target.getCameraPosVec(1.0F);
        var result  = mc.world.raycast(new RaycastContext(
                eyes, tPos, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        return result.getType() == HitResult.Type.MISS
            || (result instanceof BlockHitResult bhr
                && bhr.getBlockPos().equals(target.getBlockPos()));
    }

    private boolean canAttack(Entity target) {
        if (mc.player == null) return false;
        if (ticksOutOfWater < 2) return false;
        float cooldown = mc.player.getAttackCooldownProgress(0.5F);
        if (cooldown < CRIT_CHARGE) return false;
        if (onlySword.isValue()) {
            boolean isSword = mc.player.getMainHandStack().isIn(ItemTags.SWORDS);
            boolean isAxe   = mc.player.getMainHandStack().isIn(ItemTags.AXES);
            if (!isSword && !isAxe) return false;
        }
        return true;
    }

    private void doAttack(Entity target) {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isForwardPressed() {
        return mc.options.forwardKey.isPressed();
    }

    private static Angle calcAngle(ClientPlayerEntity player, Entity target) {
        Vec3d eyes = player.getCameraPosVec(1.0F);
        Vec3d tPos = target.getCameraPosVec(1.0F);
        double dx = tPos.x - eyes.x;
        double dy = tPos.y - eyes.y;
        double dz = tPos.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return new Angle(yaw, pitch);
    }
}
