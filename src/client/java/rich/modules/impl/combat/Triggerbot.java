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
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Triggerbot: auto-attacks targeted living entities.
 *
 * W-release mechanic (before EVERY hit):
 *   1. Attack decision made -> save target, suppress W.
 *      preAttackCountdown = random(1..2) ticks -- W stays released this long before hit.
 *   2. While preAttackCountdown > 0: keep W suppressed, tick down.
 *   3. preAttackCountdown == 0: doAttack(), then suppress W for postAttackCountdown
 *      = random(0..2) ticks -- W is not instantly re-pressed after hit.
 *
 * BUG FIX: the finally block previously overrode SUPPRESS_FORWARD=true set inside
 *   queueAttack() back to false on the same tick. Fixed by checking pendingTarget != null
 *   or postAttackCountdown > 0 in the finally expression.
 */
public class Triggerbot extends ModuleStructure {

    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    private static final boolean DEBUG_LOGS = Boolean.getBoolean("rich.debug.triggerbot");
    private static final long DEBUG_LOG_MIN_GAP_MS = 1000L;

    // How many ticks W is released BEFORE the hit fires (1..W_PRE_MAX)
    private static final int W_PRE_MAX  = 2;
    // How many ticks W stays released AFTER the hit (0..W_POST_MAX)
    private static final int W_POST_MAX = 2;

    private Entity  pendingTarget        = null;
    private int     preAttackCountdown   = 0;   // ticks to wait before firing
    private int     postAttackCountdown  = 0;   // ticks to keep W suppressed after hit
    private boolean pendingWasForward    = false;

    private String lastDiag = "";
    private long lastDiagLogMs = 0L;
    private int ticksOutOfWater = 10;
    private int ticksOnGround   = 0;

    private static final int   GROUND_COMBO_DELAY   = 2;
    private static final float GROUND_ATTACK_CHARGE = 0.93F;
    private static final float CRIT_CHARGE          = 0.84F;

    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff",
            "Min weapon charge when crit is impossible")
            .setValue(0.78F).range(0.3F, 1.0F);

    public SliderSettings jumpCharge = new SliderSettings("Jump charge",
            "Min charge before held-jump fires")
            .setValue(0.50F).range(0.3F, 1.0F);

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.noCritCharge, this.jumpCharge);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_FORWARD     = false;
        SUPPRESS_SPRINT      = false;
        SUPPRESS_JUMP        = false;
        pendingTarget        = null;
        preAttackCountdown   = 0;
        postAttackCountdown  = 0;
        ticksOutOfWater      = 0;
        ticksOnGround        = 0;
        lastDiag             = "";
        lastDiagLogMs        = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        // These are set to true inside the tick body when needed.
        // The finally block writes them to the volatile flags.
        // CRITICAL: pendingTarget != null and postAttackCountdown > 0 must also
        // propagate suppression -- see finally block.
        boolean wantSuppressForward = false;
        boolean wantSuppressJump    = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                ticksOnGround       = 0;
                pendingTarget       = null;
                postAttackCountdown = 0;
                return;
            }

            if (isInWater()) { ticksOutOfWater = 0; }
            else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else { ticksOnGround = 0; }

            // --- Post-attack W delay (W stays released for a bit after hitting) ---
            if (postAttackCountdown > 0) {
                postAttackCountdown--;
                wantSuppressForward = true;
                diag("POST_W", "post-attack W suppress, left=" + postAttackCountdown);
                // Don't return -- still process input; just keep W suppressed this tick.
                // Fall through to normal detection (charge won't be ready anyway).
            }

            // --- Pending pre-attack: W released, counting down before hit ---
            if (pendingTarget != null) {
                if (!isEntityValid(pendingTarget)) {
                    pendingTarget      = null;
                    preAttackCountdown = 0;
                    // postAttackCountdown stays, keeps W released
                    return;
                }
                if (preAttackCountdown > 0) {
                    preAttackCountdown--;
                    wantSuppressForward = true;
                    diag("W_WAIT", "pre-attack W suppress, left=" + preAttackCountdown);
                    return;
                }
                // countdown == 0: fire the hit
                Entity t         = pendingTarget;
                boolean wasForward = pendingWasForward;
                pendingTarget      = null;
                preAttackCountdown = 0;
                // Randomize how long W stays OFF after the hit (0..W_POST_MAX ticks)
                postAttackCountdown = ThreadLocalRandom.current().nextInt(0, W_POST_MAX + 1);
                doAttack(t, wasForward, postAttackCountdown == 0);
                diag("HIT_FIRE", "HIT fire fw=" + wasForward + " post=" + postAttackCountdown);
                if (postAttackCountdown > 0) {
                    wantSuppressForward = true;
                }
                return;
            }

            // --- Normal detection ---
            Entity target = mc.targetedEntity;
            if (!canHit(target)) return;

            boolean critPossible = critAchievable();
            float charge = charge();

            // --- Water hit ---
            if (isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    wantSuppressForward = queueAttack(target, "WATER");
                }
                return;
            }

            // --- No-crit path ---
            if (!critPossible) {
                float need = noCritCharge.getValue();
                if (charge >= need) {
                    wantSuppressForward = queueAttack(target, "NOCRIT");
                } else {
                    diag("NOCRIT_WAIT", "NOCRIT wait charge=" + fmt(charge));
                }
                return;
            }

            // --- Air crit path ---
            if (!mc.player.isOnGround()) {
                String blocker = critBlocker();
                if (blocker == null && charge >= CRIT_CHARGE) {
                    wantSuppressForward = queueAttack(target, "CRIT");
                } else {
                    diag("AIR_BLOCK", "block=" + blocker + " charge=" + fmt(charge));
                }
                return;
            }

            // --- Ground jump-crit gate ---
            if (this.isJumpHeld() || mc.player.getVelocity().y > 0.0) {
                if (this.isJumpHeld() && charge < jumpCharge.getValue()) {
                    wantSuppressJump = true;
                    diag("JUMP_GATE", "JUMP gate charge=" + fmt(charge));
                } else {
                    diag("GROUND_HOLD", "hold for crit charge=" + fmt(charge));
                }
                return;
            }

            // --- Ground combo ---
            if (charge >= GROUND_ATTACK_CHARGE && ticksOnGround >= GROUND_COMBO_DELAY) {
                wantSuppressForward = queueAttack(target, "COMBO");
            } else {
                diag("GROUND_WAIT", "wait charge=" + fmt(charge) + " ticks=" + ticksOnGround);
            }

        } finally {
            // FIX: pendingTarget != null means we just queued this tick (queue tick also
            // needs W suppressed, but queueAttack sets the flag BEFORE finally runs).
            // The return value of queueAttack() is propagated through wantSuppressForward.
            // postAttackCountdown was already decremented above, so if it was > 0 on entry
            // we already set wantSuppressForward = true there.
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    /**
     * Queues an attack with a random pre-attack W-release countdown (1..W_PRE_MAX ticks).
     * Returns true so the caller can set wantSuppressForward = true in the same tick.
     */
    private boolean queueAttack(Entity target, String reason) {
        pendingTarget      = target;
        pendingWasForward  = mc.options.forwardKey.isPressed();
        preAttackCountdown = ThreadLocalRandom.current().nextInt(1, W_PRE_MAX + 1); // 1..2
        diag("Q_" + reason, reason + " queued, pre=" + preAttackCountdown + " fw=" + pendingWasForward);
        return true; // caller sets wantSuppressForward
    }

    /**
     * @param restoreSprint true if W should be re-enabled immediately after hit.
     *                      False means postAttackCountdown will handle it.
     */
    private void doAttack(Entity target, boolean wasForward, boolean restoreSprint) {
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (restoreSprint && wasForward && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }
    }

    private boolean isEntityValid(Entity e) {
        return e instanceof LivingEntity
            && ((LivingEntity) e).isAlive()
            && mc.world != null
            && mc.world.getEntityById(e.getId()) != null;
    }

    private boolean canHit(Entity e) {
        return e instanceof LivingEntity
            && ((LivingEntity) e).isAlive()
            && !mc.player.isUsingItem()
            && isWeaponInHand();
    }

    private float charge() {
        return mc.player.getAttackCooldownProgress(0.0F);
    }

    private boolean isWeaponInHand() {
        Item item = mc.player.getMainHandStack().getItem();
        return item.getRegistryEntry().isIn(ItemTags.SWORDS)
            || item.getRegistryEntry().isIn(ItemTags.AXES)
            || item.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
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

    private String critBlocker() {
        if (!(mc.player.fallDistance > 0.0))                     return "fall<=0";
        if (ticksOutOfWater < 3)                                 return "justLeftWater";
        if (mc.player.isOnGround())                              return "onGround";
        if (mc.player.isClimbing())                              return "climbing";
        if (mc.player.isTouchingWater())                         return "water";
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION))  return "levitation";
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS))   return "blindness";
        if (mc.player.hasVehicle())                              return "vehicle";
        if (mc.player.getAbilities().flying)                     return "flying";
        return null;
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

    private void diag(String key, String msg) {
        if (key.equals(lastDiag)) return;
        lastDiag = key;
        if (!DEBUG_LOGS) return;
        long now = System.currentTimeMillis();
        if (now - this.lastDiagLogMs < DEBUG_LOG_MIN_GAP_MS) return;
        this.lastDiagLogMs = now;
        LOG.info("[Triggerbot] " + msg);
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
