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
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;

/**
 * KillAuraSpooky — non-packet mode.
 *
 * Rotation modes:
 *   Silent=true  → NO rotation sent at all. Camera doesn't move.
 *                  Works on Grim because Grim open-source has NO killaura
 *                  aim checks (confirmed: reddit r/admincraft, Grim issues).
 *   Silent=false → Camera rotates visibly to target (sets player.yaw directly).
 *
 * Attack pattern mirrors Triggerbot:
 *   W-release, charge gates, crit/combo/water logic.
 */
public class KillAuraSpooky extends ModuleStructure {

    // Checked by ClientPlayerEntityMixin.onInputTick to suppress W / jump
    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;

    // ── Settings ───────────────────────────────────────────────────────────────
    public final SliderSettings range         = new SliderSettings("Range",          "Attack range")                 .setValue(3.1F).range(1.0F, 6.0F);
    public final SliderSettings fovSetting    = new SliderSettings("FOV",            "Max FOV to target")            .setValue(180.0F).range(10.0F, 180.0F);
    public final BooleanSetting onlySword     = new BooleanSetting("OnlySword",      "Only sword/axe/melee")         .setValue(true);
    public final BooleanSetting silentRot     = new BooleanSetting("Silent",         "No rotation sent (non-packet)")
                                                    .setValue(true);
    public final SliderSettings noCritCharge  = new SliderSettings("NoCrit charge",  "Min charge when no crit")      .setValue(0.78F).range(0.3F, 1.0F);
    public final SliderSettings jumpCharge    = new SliderSettings("Jump charge",    "Min charge before jump fires")  .setValue(0.50F).range(0.3F, 1.0F);

    // ── Smooth camera state (non-silent mode only) ─────────────────────────────
    private final Random rng = new Random();
    private float   curYaw;
    private float   curPitch;

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
        super("KillAuraSpooky", "Non-packet silent kill-aura", ModuleCategory.UTILITIES);
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
        lockedTarget           = null;
        pendingAttack          = null;
        preAttackDeadline      = 0L;
        wasFalling             = false;
        ticksOnGroundAfterFall = 0;
        SUPPRESS_FORWARD       = false;
        SUPPRESS_JUMP          = false;
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

            // ── wasFalling mechanic ──────────────────────────────────────────
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

            if (target == null) {
                lockedTarget  = null;
                pendingAttack = null;
                return;
            }
            lockedTarget = target;

            // ── Rotation ─────────────────────────────────────────────────────
            if (!silentRot.isValue()) {
                // Non-silent: smoothly rotate camera to target
                rotateCamera(target);
                // Wait until camera is roughly on target
                boolean onTarget = isCameraOnTarget(target);
                if (!onTarget) return;
            }
            // Silent=true: no rotation at all, attack regardless of aim
            // Grim open-source has NO killaura aim checks, so this is fine.

            // ── Pending attack ───────────────────────────────────────────────
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

            // ── Ground: jump held -> gate until charge ready ─────────────────
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
    // Camera rotation (non-silent only)
    // ═══════════════════════════════════════════════════════════════════════════

    private void rotateCamera(Entity target) {
        if (mc.player == null) return;
        float[] needed = calcAngleRaw(mc.player, target);
        float   dy     = MathHelper.wrapDegrees(needed[0] - curYaw);
        float   dp     = MathHelper.wrapDegrees(needed[1] - curPitch);

        float stepYaw   = MathHelper.clamp(dy,   -74.0F, 74.0F);
        float stepPitch = MathHelper.clamp(dp,   -32.0F, 32.0F);

        curYaw   += stepYaw;
        curPitch  = MathHelper.clamp(curPitch + stepPitch, -90.0F, 90.0F);

        mc.player.setYaw(curYaw);
        mc.player.setPitch(curPitch);
    }

    private boolean isCameraOnTarget(Entity target) {
        if (mc.player == null) return false;
        float[] needed = calcAngleRaw(mc.player, target);
        return Math.abs(MathHelper.wrapDegrees(needed[0] - curYaw))   < 3.0F
            && Math.abs(MathHelper.wrapDegrees(needed[1] - curPitch)) < 3.0F;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Attack helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private boolean queueAttack(Entity target) {
        pendingAttack     = target;
        pendingWasForward = mc.options.forwardKey.isPressed();
        int preMs         = ThreadLocalRandom.current().nextInt(W_PRE_MIN, W_PRE_MAX + 1);
        preAttackDeadline = System.currentTimeMillis() + preMs;
        return true;
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

            // FOV filter uses real player yaw (camera)
            float[] needed = calcAngleRaw(mc.player, e);
            float   dYaw   = Math.abs(MathHelper.wrapDegrees(needed[0] - mc.player.getYaw()));
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

    private static float[] calcAngleRaw(ClientPlayerEntity player, Entity target) {
        Vec3d  eyes = player.getCameraPosVec(1.0F);
        Vec3d  tPos = target.getCameraPosVec(1.0F);
        double dx   = tPos.x - eyes.x;
        double dy   = tPos.y - eyes.y;
        double dz   = tPos.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float  yaw  = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float  pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        return new float[]{ yaw, pitch };
    }
}
