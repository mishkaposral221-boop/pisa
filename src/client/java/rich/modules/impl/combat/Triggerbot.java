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
 * W-release mechanic (before and after EVERY hit) — millisecond precision:
 *
 *   1. Attack decision made -> save target, generate random pre-delay [W_PRE_MIN..W_PRE_MAX] ms.
 *      W is suppressed immediately. preAttackDeadline = now + random(1..50) ms.
 *   2. Each tick: if now < preAttackDeadline -> keep W suppressed, return.
 *   3. Deadline reached: doAttack(), generate post-delay [0..W_POST_MAX] ms.
 *      W stays suppressed until postAttackDeadline.
 *   4. After postAttackDeadline: W is restored.
 *
 * Timings:
 *   Pre-attack  W release: 1 .. 50 ms (random)
 *   Post-attack W release: 1 .. 50 ms (random)
 */
public class Triggerbot extends ModuleStructure {

    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    private static final boolean DEBUG_LOGS = Boolean.getBoolean("rich.debug.triggerbot");
    private static final long DEBUG_LOG_MIN_GAP_MS = 1000L;

    // W-release timing in milliseconds
    private static final int W_PRE_MIN  = 1;   // min ms W released before hit
    private static final int W_PRE_MAX  = 50;  // max ms W released before hit
    private static final int W_POST_MIN = 1;   // min ms W released after hit
    private static final int W_POST_MAX = 50;  // max ms W released after hit

    private Entity  pendingTarget         = null;
    private long    preAttackDeadline     = 0L; // System.currentTimeMillis() target
    private long    postAttackDeadline    = 0L;
    private boolean pendingWasForward     = false;

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
        SUPPRESS_FORWARD   = false;
        SUPPRESS_SPRINT    = false;
        SUPPRESS_JUMP      = false;
        pendingTarget      = null;
        preAttackDeadline  = 0L;
        postAttackDeadline = 0L;
        ticksOutOfWater    = 0;
        ticksOnGround      = 0;
        lastDiag           = "";
        lastDiagLogMs      = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppressForward = false;
        boolean wantSuppressJump    = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                ticksOnGround      = 0;
                pendingTarget      = null;
                postAttackDeadline = 0L;
                return;
            }

            if (isInWater()) { ticksOutOfWater = 0; }
            else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else { ticksOnGround = 0; }

            long now = System.currentTimeMillis();

            // --- Post-attack W delay ---
            if (postAttackDeadline > 0L && now < postAttackDeadline) {
                wantSuppressForward = true;
                diag("POST_W", "post-attack W suppress, left=" + (postAttackDeadline - now) + "ms");
                // don't return — charge recheck is harmless, nothing will trigger
            }

            // --- Pre-attack pending: W is released, waiting for deadline ---
            if (pendingTarget != null) {
                if (!isEntityValid(pendingTarget)) {
                    pendingTarget     = null;
                    preAttackDeadline = 0L;
                    return;
                }
                if (now < preAttackDeadline) {
                    // Still waiting — keep W suppressed
                    wantSuppressForward = true;
                    diag("W_WAIT", "pre W suppress, left=" + (preAttackDeadline - now) + "ms");
                    return;
                }
                // Deadline reached — fire the hit
                Entity t          = pendingTarget;
                boolean wasForward = pendingWasForward;
                pendingTarget     = null;
                preAttackDeadline = 0L;
                // Random post-attack W delay
                int postMs        = ThreadLocalRandom.current().nextInt(W_POST_MIN, W_POST_MAX + 1);
                postAttackDeadline = now + postMs;
                wantSuppressForward = true; // keep W off this tick too
                doAttack(t, wasForward);
                diag("HIT_FIRE", "HIT fire fw=" + wasForward + " post=" + postMs + "ms");
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
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    /**
     * Queues an attack: generates a random pre-attack delay [W_PRE_MIN..W_PRE_MAX] ms.
     * Returns true so the caller sets wantSuppressForward = true on the queue tick.
     */
    private boolean queueAttack(Entity target, String reason) {
        pendingTarget     = target;
        pendingWasForward = mc.options.forwardKey.isPressed();
        int preMs         = ThreadLocalRandom.current().nextInt(W_PRE_MIN, W_PRE_MAX + 1);
        preAttackDeadline = System.currentTimeMillis() + preMs;
        diag("Q_" + reason, reason + " queued, pre=" + preMs + "ms fw=" + pendingWasForward);
        return true;
    }

    private void doAttack(Entity target, boolean wasForward) {
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        // Sprint will be restored after postAttackDeadline expires naturally
        // (SUPPRESS_FORWARD returns to false, player input flows through normally)
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
