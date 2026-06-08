package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

/**
 * Triggerbot: auto-attacks targeted living entities.
 *
 * LEGIT W-TAP (input-based, undetectable):
 *   Before a crit/hit while sprinting, the bot performs a real W-tap by
 *   rewriting the actual movement input (SUPPRESS_FORWARD -> handled in
 *   ClientPlayerEntityMixin). It does NOT touch velocity at all.
 *
 *   tick N   : release W. The mixin clears forward+sprint on playerInput and
 *              vanilla sends a normal STOP_SPRINTING movement packet. No hit.
 *   tick N+1 : the server already knows we stopped sprinting, so we attack
 *              (crit registers) and in the SAME tick W is re-pressed, so
 *              vanilla sends START_SPRINTING again.
 *
 *   This is exactly the packet pattern a human W-tap produces (~50 ms), so it
 *   is indistinguishable from a legit player tapping W. The previous version
 *   scrubbed horizontal velocity (x,z *0.2) every crit -- that unnatural
 *   motion was the real anticheat giveaway and has been removed entirely.
 *
 * HUMANIZE (anti-flag) kept:
 *   - random jitter on charge thresholds + micro-pauses between hits
 *   - reaction delay (~1..6 ticks) when a NEW target is acquired
 *   - "Humanize" slider (0 = deterministic / instant)
 *
 * Charge thresholds (base, +jitter):
 *   GROUND_ATTACK_CHARGE  0.93
 *   CRIT_CHARGE           0.84
 *   GROUND_COMBO_DELAY    2
 */
public class Triggerbot extends ModuleStructure {

    // Read by ClientPlayerEntityMixin to perform the real W-tap input rewrite.
    // SUPPRESS_FORWARD is driven from pendingTarget (exactly 1 tick of release).
    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    private static final boolean DEBUG_LOGS = Boolean.getBoolean("rich.debug.triggerbot");
    private static final long DEBUG_LOG_MIN_GAP_MS = 1000L;

    private String lastDiag = "";
    private long lastDiagLogMs = 0L;
    private int ticksOutOfWater = 10;
    private int ticksOnGround   = 0;

    // W-tap state: when set, W is released THIS tick and the queued target is
    // hit on the NEXT tick (then W is re-pressed automatically).
    private Entity pendingTarget = null;

    // --- Humanize timing (anti-flag) ---
    private final java.util.Random rng = new java.util.Random();
    private int   attackDelayTicks       = 0;
    private int   targetReactionTicks    = 0;   // reaction delay on a new target
    private int   lastTargetId           = -1;  // id of the previous target
    private float critChargeTarget       = 0.84F;
    private float groundChargeTarget     = 0.93F;
    private float waterChargeTarget      = 0.93F;
    private float noCritChargeTarget     = 0.78F;
    private int   groundComboDelayTarget = 2;

    // --- Timing constants ---
    private static final int   GROUND_COMBO_DELAY   = 2;
    private static final float GROUND_ATTACK_CHARGE = 0.93F;
    private static final float CRIT_CHARGE          = 0.84F;

    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff",
            "Min weapon charge when crit is impossible")
            .setValue(0.78F).range(0.0F, 1.0F);

    // Timing randomness + reaction delay. 0 = strict determinism, higher =
    // stronger humanization -> fewer flags but slower reaction.
    public SliderSettings randomness = new SliderSettings("Humanize",
            "Timing randomness and reaction delay (anti-flag)")
            .setValue(0.5F).range(0.0F, 1.0F);

    // Legit W-tap: release W -> hit -> press W (real input, no velocity hack).
    public BooleanSetting wTap = new BooleanSetting("W-tap",
            "Real W-tap before the hit: release W, attack, press W (legit input)")
            .setValue(true);

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.noCritCharge, this.randomness, this.wTap);
    }

    @Override
    public void activate() {
        super.activate();
        rollTargets();
        lastTargetId = -1;
        targetReactionTicks = 0;
        pendingTarget = null;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_FORWARD = false;
        SUPPRESS_SPRINT  = false;
        SUPPRESS_JUMP    = false;
        ticksOutOfWater  = 0;
        ticksOnGround    = 0;
        attackDelayTicks = 0;
        targetReactionTicks = 0;
        lastTargetId     = -1;
        pendingTarget    = null;
        lastDiag = "";
        lastDiagLogMs = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                ticksOnGround = 0;
                lastTargetId  = -1;
                pendingTarget = null;
                return;
            }

            if (attackDelayTicks > 0) attackDelayTicks--;

            if (isInWater()) { ticksOutOfWater = 0; }
            else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else { ticksOnGround = 0; }

            // --- W-tap phase 2: W was released last tick, now land the hit ---
            // pendingTarget is cleared here, so the finally block re-presses W
            // (SUPPRESS_FORWARD becomes false) in this same tick.
            if (pendingTarget != null) {
                Entity t = pendingTarget;
                pendingTarget = null;
                if (canHit(t) && t == mc.targetedEntity) {
                    doAttack(t);
                    rollTargets();
                    diag("WTAP_HIT", "wtap hit");
                }
                return;
            }

            // --- Normal detection ---
            Entity target = mc.targetedEntity;
            if (!canHit(target)) { lastTargetId = -1; return; }

            // --- Reaction delay on a NEW target (anti-flag for trigger+aim) ---
            int tid = target.getId();
            if (tid != lastTargetId) {
                lastTargetId = tid;
                float rr = clamp(randomness.getValue(), 0.0F, 1.0F);
                targetReactionTicks = (rr > 0.0F) ? (1 + Math.round(rng.nextFloat() * (5.0F * rr))) : 0;
            }
            if (targetReactionTicks > 0) {
                targetReactionTicks--;
                diag("REACT", "react=" + targetReactionTicks);
                return;
            }

            boolean critPossible = critAchievable();
            float charge = charge();

            if (isInWater()) {
                if (charge >= waterChargeTarget && fire(target)) {
                    diag("WATER", "HIT water");
                }
                return;
            }

            if (!critPossible) {
                if (charge >= noCritChargeTarget) {
                    if (fire(target)) diag("NOCRIT", "NOCRIT hit charge=" + fmt(charge));
                } else {
                    diag("NOCRIT_WAIT", "NOCRIT wait charge=" + fmt(charge));
                }
                return;
            }

            // --- Air crit path ---
            if (!mc.player.isOnGround()) {
                String blocker = critBlocker();
                if (blocker == null && charge >= critChargeTarget) {
                    if (fire(target)) diag("CRIT", "CRIT charge=" + fmt(charge));
                } else {
                    diag("AIR_BLOCK", "block=" + blocker + " charge=" + fmt(charge));
                }
                return;
            }

            // --- Ground hold: wait for the jump crit (no jump suppression) ---
            if (this.isJumpHeld() || mc.player.getVelocity().y > 0.0) {
                diag("GROUND_HOLD", "hold for crit charge=" + fmt(charge));
                return;
            }

            // --- Ground combo ---
            if (charge >= groundChargeTarget && ticksOnGround >= groundComboDelayTarget) {
                if (fire(target)) diag("COMBO", "COMBO charge=" + fmt(charge));
            } else {
                diag("GROUND_WAIT", "wait charge=" + fmt(charge) + " ticks=" + ticksOnGround);
            }
        } finally {
            // SUPPRESS_FORWARD is true for exactly the one tick where a W-tap is
            // queued (pendingTarget set). Next tick pendingTarget is cleared in
            // phase 2, so this becomes false and W is re-pressed -> genuine tap.
            SUPPRESS_FORWARD = (pendingTarget != null);
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = false;
        }
    }

    /**
     * Returns true if the target was actually attacked this tick.
     * When the legit W-tap applies (enabled + currently sprinting), this queues
     * the target instead: W is released this tick (via SUPPRESS_FORWARD in the
     * finally block) and the hit lands next tick. Returns false in that case.
     */
    private boolean fire(Entity target) {
        if (attackDelayTicks > 0) return false;
        if (wTap.isValue() && mc.player.isSprinting()) {
            pendingTarget = target; // release W this tick, hit next tick
            diag("WTAP_REL", "release W");
            return false;
        }
        doAttack(target);
        rollTargets();
        return true;
    }

    private void rollTargets() {
        float r = clamp(randomness.getValue(), 0.0F, 1.0F);
        critChargeTarget       = clamp(CRIT_CHARGE          + rng.nextFloat() * (0.12F * r), 0.80F, 0.99F);
        groundChargeTarget     = clamp(GROUND_ATTACK_CHARGE + rng.nextFloat() * (0.06F * r), 0.80F, 1.0F);
        waterChargeTarget      = clamp(GROUND_ATTACK_CHARGE + rng.nextFloat() * (0.06F * r), 0.80F, 1.0F);
        noCritChargeTarget     = clamp(noCritCharge.getValue() + rng.nextFloat() * (0.10F * r), 0.0F, 1.0F);
        groundComboDelayTarget = GROUND_COMBO_DELAY + Math.round(rng.nextFloat() * (2.0F * r));
        attackDelayTicks       = Math.round(rng.nextFloat() * (3.0F * r));
    }

    // Attack only: never touches sprint or velocity. Sprint is dropped purely
    // through the real W-tap (input rewrite) when needed, so crits register
    // server-side with no client-side motion tricks.
    private void doAttack(Entity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean canHit(Entity target) {
        if (!this.isState()) return false;
        if (!(target instanceof LivingEntity living)) return false;
        if (target == mc.player) return false;
        if (living.isDead() || living.getHealth() <= 0.0F) return false;
        if (!isWeaponInHand()) return false;
        return true;
    }

    private float charge() {
        return mc.player.getAttackCooldownProgress(0.0F);
    }

    private boolean isWeaponInHand() {
        Item item = mc.player.getMainHandStack().getItem();
        return mc.player.getMainHandStack().isIn(ItemTags.SWORDS)
                || mc.player.getMainHandStack().isIn(ItemTags.AXES)
                || item.toString().contains("mace");
    }

    private boolean isInWater() {
        return mc.player.isTouchingWater() || mc.player.isSubmergedInWater();
    }

    private boolean isJumpHeld() {
        return mc.options.jumpKey.isPressed();
    }

    private boolean critAchievable() {
        if (mc.player.isOnGround()) return true;
        return critBlocker() == null;
    }

    private String critBlocker() {
        if (mc.player.isClimbing()) return "climbing";
        if (isInWater()) return "water";
        if (mc.player.hasVehicle()) return "vehicle";
        if (mc.player.getVelocity().y > 0.0) return "rising";
        if (mc.player.isOnGround()) return "ground";
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) return "blindness";
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return "levitation";
        if (mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) return "slow_falling";
        return null;
    }

    private float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }

    private String fmt(float v) {
        return String.format("%.2f", v);
    }

    private void diag(String tag, String msg) {
        if (!DEBUG_LOGS) return;
        String d = tag + ":" + msg;
        long now = System.currentTimeMillis();
        if (d.equals(lastDiag) && now - lastDiagLogMs < DEBUG_LOG_MIN_GAP_MS) return;
        lastDiag = d;
        lastDiagLogMs = now;
        LOG.info("[Triggerbot] {}", d);
    }
}
