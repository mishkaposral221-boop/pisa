package rich.modules.impl.combat;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
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
 * KillAuraSpooky — SPAngle rotation bypass + full Triggerbot attack pattern.
 *
 * SILENT MODE (default ON):
 *   AngleConnection.INSTANCE.setRotation(aimAngle)
 *   ClientPlayerEntityMixin does two things:
 *     1. preTravelSilentYawSwap  -> player.yaw = aimYaw before travel()
 *        => physics/movement use aimYaw => matches packet => Grim happy
 *     2. postTravelRestoreSilentYaw -> player.yaw = realYaw after travel()
 *        => camera stays on mouse => invisible from 1st person
 *     3. hookSilentRotationYaw/Pitch -> spoof packet yaw/pitch
 *
 * ATTACK PATTERN (mirrors Triggerbot exactly):
 *   W-release: suppress forward 1-15ms before every hit
 *   Water:     charge >= 0.93
 *   No-crit:   charge >= noCritCharge (0.78)
 *   Air crit:  fallDistance > 0, critBlocker == null, charge >= 0.84
 *              wasFalling cleared on successful crit
 *   Ground:    jump held + charge < jumpCharge -> suppress jump
 *              charge >= 0.93 + ticksOnGround >= 2 + !wasFalling -> COMBO
 *   wasFalling safety valve: COMBO_ALLOWED_AFTER_FALL_TICKS (40) ground ticks
 */
public class KillAuraSpooky extends ModuleStructure {

    // Checked by ClientPlayerEntityMixin.onInputTick to suppress W / jump
    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;

    // ── Settings ───────────────────────────────────────────────────────────────
    public final SliderSettings range         = new SliderSettings("Range",          "Attack range")                 .setValue(3.1F).range(1.0F, 6.0F);
    public final SliderSettings fovSetting    = new SliderSettings("FOV",            "Max FOV to target")            .setValue(180.0F).range(10.0F, 180.0F);
    public final BooleanSetting onlySword     = new BooleanSetting("OnlySword",      "Only sword/axe/melee")         .setValue(true);
    public final BooleanSetting silentRot     = new BooleanSetting("Silent",         "Silent rotation (no cam move)") .setValue(true);
    public final SliderSettings noCritCharge  = new SliderSettings("NoCrit charge",  "Min charge when no crit")      .setValue(0.78F).range(0.3F, 1.0F);
    public final SliderSettings jumpCharge    = new SliderSettings("Jump charge",    "Min charge before jump fires")  .setValue(0.50F).range(0.3F, 1.0F);

    // ── Rotation state ─────────────────────────────────────────────────────────
    private final Random rng = new Random();
    private float   curYaw;
    private float   curPitch;
    private boolean hasRotation = false;

    // ── Attack state (mirrors Triggerbot) ──────────────────────────────────────
    private Entity  lockedTarget           = null;
    private Entity  pendingAttack          = null;
    private long    preAttackDeadline      = 0L;
    private boolean pendingWasForward      = false;

    private int     ticksOutOfWater        = 10;
    private int     ticksOnGround          = 0;
    private boolean wasFalling             = false;
    private int     ticksOnGroundAfterFall = 0;

    private static final int   W_PRE_MIN                   = 1;
    private static final int   W_PRE_MAX                   = 15;
    private static final int   COMBO_ALLOWED_AFTER_FALL    = 40;
    private static final int   GROUND_COMBO_DELAY          = 2;
    private static final float GROUND_ATTACK_CHARGE        = 0.93F;
    private static final float CRIT_CHARGE                 = 0.84F;

    public KillAuraSpooky() {
        super("KillAuraSpooky", "SPAngle kill-aura (SpookyTime bypass)", ModuleCategory.UTILITIES);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Enable / Disable
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void activate() {
        if (mc.player != null) {
            curYaw   = mc.player.getYaw();
            curPitch = mc.player.getPitch();
        }
        hasRotation            = false;
        lockedTarget           = null;
        pendingAttack          = null;
        preAttackDeadline      = 0L;
        ticksOutOfWater        = 10;
        ticksOnGround          = 0;
        wasFalling             = false;
        ticksOnGroundAfterFall = 0;
        SUPPRESS_FORWARD       = false;
        SUPPRESS_JUMP          = false;
    }

    @Override
    public void deactivate() {
        AngleConnection.INSTANCE.setRotation(null);
        hasRotation            = false;
        lockedTarget           = null;
        pendingAttack          = null;
        preAttackDeadline      = 0L;
        wasFalling             = false;
        ticksOnGroundAfterFall = 0;
        SUPPRESS_FORWARD       = false;
        SUPPRESS_JUMP          = false;
        if (mc.player != null) {
            mc.player.setBodyYaw(mc.player.getYaw());
            mc.player.headYaw = mc.player.getYaw();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Tick
    // ═══════════════════════════════════════════════════════════════════════════

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppressForward = false;
        boolean wantSuppressJump    = false;
        try {
            if (mc.player == null || mc.world == null
                    || !this.isState() || mc.currentScreen != null) return;

            // ── Water timer ─────────────────────────────────────────────────
            if (isInWater()) { ticksOutOfWater = 0; }
            else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

            // ── Ground ticks ────────────────────────────────────────────────
            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else { ticksOnGround = 0; }

            // ── wasFalling mechanic (same as Triggerbot) ────────────────────
            if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0.0) {
                wasFalling             = true;
                ticksOnGroundAfterFall = 0;
            } else if (mc.player.isOnGround() && wasFalling) {
                ticksOnGroundAfterFall++;
                if (ticksOnGroundAfterFall >= COMBO_ALLOWED_AFTER_FALL) {
                    wasFalling             = false;
                    ticksOnGroundAfterFall = 0;
                }
            }

            // ── Find target ─────────────────────────────────────────────────
            Entity target = findTarget();

            // ── Rotation (always, even without target to reset) ─────────────
            if (target == null) {
                resetRotation();
                lockedTarget  = null;
                pendingAttack = null;
                return;
            }
            lockedTarget = target;

            Angle needed = calcAngle(mc.player, target);
            Angle next   = spAngleRotate(new Angle(curYaw, curPitch), needed);
            curYaw   = next.getYaw();
            curPitch = next.getPitch();
            hasRotation = true;

            if (silentRot.isValue()) {
                AngleConnection.INSTANCE.setRotation(next);
            } else {
                AngleConnection.INSTANCE.setRotation(null);
                mc.player.setYaw(curYaw);
                mc.player.setPitch(curPitch);
            }

            // ── On-target check ─────────────────────────────────────────────
            boolean onTarget = Math.abs(MathHelper.wrapDegrees(needed.getYaw()   - curYaw))   < 3.0F
                            && Math.abs(MathHelper.wrapDegrees(needed.getPitch() - curPitch)) < 3.0F;
            if (!onTarget) return; // still rotating, don't attack yet

            // ── Pending attack (W released, waiting deadline) ────────────────
            long now = System.currentTimeMillis();
            if (pendingAttack != null) {
                if (!isEntityValid(pendingAttack)) {
                    pendingAttack = null;
                    return;
                }
                if (now < preAttackDeadline) {
                    wantSuppressForward = true;
                    return;
                }
                Entity t           = pendingAttack;
                boolean wasForward = pendingWasForward;
                pendingAttack      = null;
                preAttackDeadline  = 0L;
                doAttack(t, wasForward);
                return;
            }

            if (!canHit(target)) return;

            boolean critPossible = critAchievable();
            float   charge       = charge();

            // ── Water hit ───────────────────────────────────────────────────
            if (isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    wantSuppressForward = queueAttack(target);
                }
                return;
            }

            // ── No-crit path ────────────────────────────────────────────────
            if (!critPossible) {
                if (charge >= noCritCharge.getValue()) {
                    wantSuppressForward = queueAttack(target);
                }
                return;
            }

            // ── Air crit path ───────────────────────────────────────────────
            if (!mc.player.isOnGround()) {
                String blocker = critBlocker();
                if (blocker == null && charge >= CRIT_CHARGE) {
                    wasFalling             = false;
                    ticksOnGroundAfterFall = 0;
                    wantSuppressForward    = queueAttack(target);
                }
                return;
            }

            // ── Ground: jump held -> gate jump until charge ready ───────────
            if (isJumpHeld() || mc.player.getVelocity().y > 0.0) {
                if (isJumpHeld() && charge < jumpCharge.getValue()) {
                    wantSuppressJump = true;
                }
                return;
            }

            // ── Ground combo ────────────────────────────────────────────────
            if (wasFalling) return;
            if (charge >= GROUND_ATTACK_CHARGE && ticksOnGround >= GROUND_COMBO_DELAY) {
                wantSuppressForward = queueAttack(target);
            }

        } finally {
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SPAngle rotation
    // ═══════════════════════════════════════════════════════════════════════════

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

        float stepYaw = 0;
        if (totalYaw >= 1e-4F) {
            float scale = Math.min(totalYaw, maxStepYaw) / totalYaw;
            if (!reached) scale = ease(scale);
            stepYaw = Math.copySign(Math.min(yawLimit, totalYaw * scale), dy);
        }

        float stepPitch = 0;
        if (totalPitch >= 1e-4F) {
            float scale = Math.min(totalPitch, maxStepPitch) / totalPitch;
            if (!reached) scale = ease(scale);
            stepPitch = Math.copySign(Math.min(pitchLimit, totalPitch * scale), dp);
        }

        float newYaw   = current.getYaw()   + stepYaw;
        float newPitch = MathHelper.clamp(current.getPitch() + stepPitch, -90.0F, 90.0F);

        if (reached) {
            long   ms   = System.currentTimeMillis();
            double t    = (ms % 12000L) / 1200.0;
            float  sway = (float)(Math.sin(t * 3.0 * 2.0 * Math.PI) * 1.15 * rng.nextGaussian() * 0.15);
            newYaw += sway;
        }

        return new Angle(newYaw, newPitch).adjustSensitivity();
    }

    private float ease(float t) { return t * (0.5F + 0.5F * t); }

    // ═══════════════════════════════════════════════════════════════════════════
    // Attack helpers (mirroring Triggerbot)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Queue W-release then hit (same as Triggerbot.queueAttack). */
    private boolean queueAttack(Entity target) {
        pendingAttack     = target;
        pendingWasForward = mc.options.forwardKey.isPressed();
        int preMs         = ThreadLocalRandom.current().nextInt(W_PRE_MIN, W_PRE_MAX + 1);
        preAttackDeadline = System.currentTimeMillis() + preMs;
        return true; // caller assigns SUPPRESS_FORWARD
    }

    private void doAttack(Entity target, boolean wasForward) {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (wasForward && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }
    }

    private boolean isEntityValid(Entity e) {
        return e instanceof LivingEntity le
            && le.isAlive()
            && mc.world != null
            && mc.world.getEntityById(e.getId()) != null;
    }

    private boolean canHit(Entity e) {
        return e instanceof LivingEntity le
            && le.isAlive()
            && mc.player != null
            && !mc.player.isUsingItem()
            && isWeaponInHand();
    }

    private float charge() {
        return mc.player.getAttackCooldownProgress(0.0F);
    }

    private boolean isWeaponInHand() {
        if (mc.player == null) return false;
        if (!onlySword.isValue()) return true;
        var stack = mc.player.getMainHandStack();
        return stack.isIn(ItemTags.SWORDS)
            || stack.isIn(ItemTags.AXES)
            || stack.isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
    }

    private boolean isInWater() {
        return mc.player.isTouchingWater()
            || mc.player.isSubmergedInWater()
            || mc.player.isSwimming();
    }

    private boolean isJumpHeld() {
        try { return mc.options.jumpKey.isPressed(); }
        catch (Throwable t) { return false; }
    }

    private boolean critAchievable() {
        return ticksOutOfWater >= 3
            && !mc.player.isTouchingWater()
            && !mc.player.isClimbing()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
    }

    /** Null = crit possible, non-null = reason blocked. */
    private String critBlocker() {
        if (!(mc.player.fallDistance > 0.0))                           return "fall<=0";
        if (ticksOutOfWater < 3)                                       return "justLeftWater";
        if (mc.player.isOnGround())                                    return "onGround";
        if (mc.player.isClimbing())                                    return "climbing";
        if (mc.player.isTouchingWater())                               return "water";
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION))       return "levitation";
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS))        return "blindness";
        if (mc.player.hasVehicle())                                    return "vehicle";
        if (mc.player.getAbilities().flying)                           return "flying";
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Target selection
    // ═══════════════════════════════════════════════════════════════════════════

    private void resetRotation() {
        hasRotation = false;
        AngleConnection.INSTANCE.setRotation(null);
        if (!silentRot.isValue() && mc.player != null) {
            // camera was rotating non-silently — nothing extra needed
        }
    }

    private Entity findTarget() {
        if (mc.player == null || mc.world == null) return null;
        float  maxRange = range.getValue();
        float  fov      = fovSetting.getValue();
        Entity best     = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (!le.isAlive() || le.getHealth() <= 0) continue;
            if (e == mc.player) continue;
            if (e instanceof PlayerEntity pe && pe.isCreative()) continue;
            if (le.hasStatusEffect(StatusEffects.INVISIBILITY)) continue;

            double dist = mc.player.distanceTo(e);
            if (dist > maxRange) continue;

            Angle  needed = calcAngle(mc.player, e);
            float  dYaw   = Math.abs(MathHelper.wrapDegrees(needed.getYaw() - mc.player.getYaw()));
            if (dYaw > fov / 2f) continue;

            if (!hasLineOfSight(e)) continue;

            if (dist < bestDist) { bestDist = dist; best = e; }
        }
        return best;
    }

    private boolean hasLineOfSight(Entity target) {
        if (mc.world == null) return false;
        Vec3d eyes   = mc.player.getCameraPosVec(1.0F);
        Vec3d tPos   = target.getCameraPosVec(1.0F);
        var   result = mc.world.raycast(new RaycastContext(
                eyes, tPos, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, mc.player));
        return result.getType() == HitResult.Type.MISS
            || (result instanceof BlockHitResult bhr
                && bhr.getBlockPos().equals(target.getBlockPos()));
    }

    private static Angle calcAngle(ClientPlayerEntity player, Entity target) {
        Vec3d  eyes = player.getCameraPosVec(1.0F);
        Vec3d  tPos = target.getCameraPosVec(1.0F);
        double dx   = tPos.x - eyes.x;
        double dy   = tPos.y - eyes.y;
        double dz   = tPos.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float  yaw  = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float  pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return new Angle(yaw, pitch);
    }
}
