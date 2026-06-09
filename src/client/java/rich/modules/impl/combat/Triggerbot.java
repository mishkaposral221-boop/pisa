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
 *   1. Attack decision made -> save target, suppress W (SUPPRESS_FORWARD=true)
 *      preAttackCountdown = random(W_RELEASE_MIN..W_RELEASE_MAX) ticks
 *   2. While countdown > 0: keep W suppressed, decrement, return
 *   3. countdown == 0: doAttack(), restore W if it was held
 *
 * Randomized timings:
 *   W_RELEASE_MIN = 1 tick  (min ticks W is released before hit)
 *   W_RELEASE_MAX = 3 ticks (max ticks W is released before hit)
 *
 * Charge thresholds:
 *   GROUND_ATTACK_CHARGE  0.93
 *   CRIT_CHARGE           0.84
 *   GROUND_COMBO_DELAY    2
 */
public class Triggerbot extends ModuleStructure {

    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    private static final boolean DEBUG_LOGS = Boolean.getBoolean("rich.debug.triggerbot");
    private static final long DEBUG_LOG_MIN_GAP_MS = 1000L;

    // W-release timing before every hit (ticks)
    private static final int W_RELEASE_MIN = 1;
    private static final int W_RELEASE_MAX = 3;

    private Entity  pendingTarget      = null;
    private int     preAttackCountdown = 0;
    private boolean pendingWasForward  = false;

    private String lastDiag = "";
    private long lastDiagLogMs = 0L;
    private int ticksOutOfWater = 10;
    private int ticksOnGround   = 0;

    // --- Timing constants ---
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
        SUPPRESS_FORWARD = false;
        SUPPRESS_SPRINT  = false;
        SUPPRESS_JUMP    = false;
        pendingTarget    = null;
        preAttackCountdown = 0;
        ticksOutOfWater  = 0;
        ticksOnGround    = 0;
        lastDiag = "";
        lastDiagLogMs = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppressForward = false;
        boolean wantSuppressJump    = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                ticksOnGround = 0;
                pendingTarget = null;
                return;
            }

            if (isInWater()) { ticksOutOfWater = 0; }
            else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else { ticksOnGround = 0; }

            // --- Pending pre-attack: W released, counting down before hit ---
            if (pendingTarget != null) {
                if (!isEntityValid(pendingTarget)) {
                    pendingTarget = null;
                    preAttackCountdown = 0;
                    return;
                }
                if (preAttackCountdown > 0) {
                    preAttackCountdown--;
                    wantSuppressForward = true;
                    diag("W_WAIT", "W suppressed, countdown=" + preAttackCountdown);
                    return;
                }
                // countdown == 0: fire the hit
                Entity t = pendingTarget;
                boolean wasForward = pendingWasForward;
                pendingTarget = null;
                preAttackCountdown = 0;
                doAttack(t, wasForward);
                diag("HIT_FIRE", "HIT fire fw=" + wasForward);
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
                    queueAttack(target, "WATER");
                }
                return;
            }

            // --- No-crit path ---
            if (!critPossible) {
                float need = noCritCharge.getValue();
                if (charge >= need) {
                    queueAttack(target, "NOCRIT");
                } else {
                    diag("NOCRIT_WAIT", "NOCRIT wait charge=" + fmt(charge));
                }
                return;
            }

            // --- Air crit path ---
            if (!mc.player.isOnGround()) {
                String blocker = critBlocker();
                if (blocker == null && charge >= CRIT_CHARGE) {
                    queueAttack(target, "CRIT");
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
                queueAttack(target, "COMBO");
            } else {
                diag("GROUND_WAIT", "wait charge=" + fmt(charge) + " ticks=" + ticksOnGround);
            }

        } finally {
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    /**
     * Queues an attack: releases W for a random number of ticks, then hits.
     * The random countdown adds humanlike variance to every W-release.
     */
    private void queueAttack(Entity target, String reason) {
        pendingTarget     = target;
        pendingWasForward = mc.options.forwardKey.isPressed();
        // randomize how long W stays released before the hit (1..3 ticks)
        preAttackCountdown = ThreadLocalRandom.current().nextInt(W_RELEASE_MIN, W_RELEASE_MAX + 1);
        SUPPRESS_FORWARD  = true;
        diag("Q_" + reason, reason + " queued, W_ticks=" + preAttackCountdown + " fw=" + pendingWasForward);
    }

    private void doAttack(Entity target, boolean wasForward) {
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        // restore sprint only if W was actually held at the moment of queue
        if (wasForward && mc.options.forwardKey.isPressed()) {
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
        if (!(mc.player.fallDistance > 0.0))                    return "fall<=0";
        if (ticksOutOfWater < 3)                                return "justLeftWater";
        if (mc.player.isOnGround())                             return "onGround";
        if (mc.player.isClimbing())                             return "climbing";
        if (mc.player.isTouchingWater())                        return "water";
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return "levitation";
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS))  return "blindness";
        if (mc.player.hasVehicle())                             return "vehicle";
        if (mc.player.getAbilities().flying)                    return "flying";
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
