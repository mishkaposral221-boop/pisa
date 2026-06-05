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

/**
 * Triggerbot: auto-attacks targeted living entities.
 *
 * W-release mechanic (air crit):
 *   Tick N   : crit ready -> save target, suppress W (SUPPRESS_FORWARD=true), return
 *   Tick N   : onInputTick clears forward/sprint from playerInput
 *   Tick N   : sendMovementPackets sends pos with W=false, sprint=false
 *   Tick N+1 : pendingTarget set, countdown=0 -> attack(), then resume W
 */
public class Triggerbot extends ModuleStructure {

    /** Read by ClientPlayerEntityMixin.onInputTick to clear the W key for 1 tick before a crit. */
    public static volatile boolean SUPPRESS_FORWARD = false;
    /** Read by ClientPlayerEntityMixin.onInputTick to clear the jump key when gating crits. */
    public static volatile boolean SUPPRESS_JUMP    = false;
    /** Unused now but kept for any external readers. */
    public static volatile boolean SUPPRESS_SPRINT  = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");

    // Pre-attack W-release state
    private Entity  pendingTarget    = null; // target saved for next-tick attack
    private int     preAttackCountdown = 0;  // ticks remaining before we fire
    private boolean pendingWasForward  = false; // was W held when we queued the attack?

    private String lastDiag = "";
    private int ticksOutOfWater = 10;
    private int ticksOnGround   = 0;

    private static final int   GROUND_COMBO_DELAY  = 3;
    private static final float GROUND_ATTACK_CHARGE = 1.0F;
    private static final float CRIT_CHARGE          = 0.9F;

    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff",
            "Min weapon charge when crit is impossible")
            .setValue(0.80F).range(0.3F, 1.0F);

    public SliderSettings jumpCharge = new SliderSettings("Jump charge",
            "Min charge before held-jump fires")
            .setValue(0.55F).range(0.3F, 1.0F);

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

            // Counters
            if (isInWater()) { ticksOutOfWater = 0; }
            else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else { ticksOnGround = 0; }

            // --- Handle pending pre-attack state ---
            if (pendingTarget != null) {
                if (!isEntityValid(pendingTarget)) {
                    // Target gone, abort
                    pendingTarget = null;
                    preAttackCountdown = 0;
                    return;
                }
                if (preAttackCountdown > 0) {
                    // Still waiting, keep W suppressed
                    preAttackCountdown--;
                    wantSuppressForward = true;
                    return;
                }
                // Countdown reached 0 -> fire!
                Entity t = pendingTarget;
                boolean wasForward = pendingWasForward;
                pendingTarget = null;
                preAttackCountdown = 0;
                doAttack(t, wasForward);
                LOG.info("[Triggerbot] CRIT FIRE (was_fw=" + wasForward + ")");
                return;
            }

            // --- Normal detection ---
            Entity target = mc.targetedEntity;
            if (!canHit(target)) return;

            boolean critPossible = critAchievable();
            float charge = charge();

            // In water - no crit possible
            if (isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    doAttack(target, mc.options.forwardKey.isPressed());
                    LOG.info("[Triggerbot] HIT water");
                }
                return;
            }

            // Crit impossible (debuff / condition)
            if (!critPossible) {
                float need = noCritCharge.getValue();
                if (charge >= need) {
                    doAttack(target, mc.options.forwardKey.isPressed());
                    diag("NOCRIT", "NOCRIT hit charge=" + fmt(charge));
                } else {
                    diag("NOCRIT_WAIT", "NOCRIT wait charge=" + fmt(charge));
                }
                return;
            }

            // --- AIR CRIT path (with W pre-release) ---
            if (!mc.player.isOnGround()) {
                String blocker = critBlocker();
                if (blocker == null && charge >= CRIT_CHARGE) {
                    // Queue the attack: release W this tick, attack next tick
                    pendingTarget     = target;
                    preAttackCountdown = 0; // attack on NEXT tick (1 tick = ~50ms)
                    pendingWasForward = mc.options.forwardKey.isPressed();
                    wantSuppressForward = true; // clear W+sprint from input this tick
                    diag("CRIT_QUEUE", "CRIT queued fall=" + fmt(mc.player.fallDistance)
                        + " charge=" + fmt(charge) + " fw=" + pendingWasForward);
                } else {
                    diag("AIR_BLOCK", "AIR block=" + blocker + " charge=" + fmt(charge));
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

            // --- Ground combo attack ---
            if (charge >= GROUND_ATTACK_CHARGE && ticksOnGround >= GROUND_COMBO_DELAY) {
                doAttack(target, mc.options.forwardKey.isPressed());
                diag("COMBO", "COMBO ground charge=" + fmt(charge));
            } else {
                diag("GROUND_WAIT", "ground wait charge=" + fmt(charge) + " ticks=" + ticksOnGround);
            }

        } finally {
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    /**
     * Send the actual attack.
     * Order of packets to server:
     *   STOP_SPRINTING -> attack -> START_SPRINTING (if applicable)
     */
    private void doAttack(Entity target, boolean wasForward) {
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
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
        if (!(mc.player.fallDistance > 0.0))                          return "fall<=0";
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
        if (!key.equals(lastDiag)) {
            lastDiag = key;
            LOG.info("[Triggerbot] " + msg);
        }
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
