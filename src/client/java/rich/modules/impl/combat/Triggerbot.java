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

import java.util.Locale;

/**
 * Triggerbot — auto-attacks crosshair entity.
 * Reconstructed faithfully from runtime-visuals-1.0.01.jar.
 *
 * Constants (not settings):
 *   GROUND_ATTACK_CHARGE = 0.93f
 *   CRIT_CHARGE          = 0.84f
 */
public class Triggerbot extends ModuleStructure {

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");

    // Mixin flags
    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    // Hardcoded thresholds
    private static final int   GROUND_COMBO_DELAY   = 2;
    private static final float GROUND_ATTACK_CHARGE = 0.93F;
    private static final float CRIT_CHARGE          = 0.84F;

    // Settings
    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff",
            "Min weapon charge when crit is impossible")
            .setValue(0.78F).range(0.30F, 1.0F);
    public SliderSettings jumpCharge = new SliderSettings("Jump charge",
            "Min charge before held-jump fires")
            .setValue(0.5F).range(0.3F, 1.0F);

    // State
    private Entity  pendingTarget      = null;
    private int     preAttackCountdown = 0;
    private boolean pendingWasForward  = false;
    private String  lastDiag           = "";

    private int ticksOutOfWater = 10;
    private int ticksOnGround   = 0;

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(noCritCharge, jumpCharge);
    }

    public static Triggerbot getInstance() {
        return (Triggerbot) c.a(Triggerbot.class);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_FORWARD = false;
        SUPPRESS_SPRINT  = false;
        SUPPRESS_JUMP    = false;
        pendingTarget    = null;
        ticksOnGround    = 0;
        ticksOutOfWater  = 0;
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

            if (isInWater()) ticksOutOfWater = 0;
            else if (ticksOutOfWater < 100) ticksOutOfWater++;

            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else ticksOnGround = 0;

            // Pending air-crit
            if (pendingTarget != null) {
                if (!isEntityValid(pendingTarget)) {
                    pendingTarget = null;
                    preAttackCountdown = 0;
                    return;
                }
                if (preAttackCountdown > 0) {
                    preAttackCountdown--;
                    wantSuppressForward = true;
                    return;
                }
                Entity t  = pendingTarget;
                boolean fw = pendingWasForward;
                pendingTarget = null;
                preAttackCountdown = 0;
                doAttack(t, fw);
                diag("CRIT_FIRE", "CRIT FIRE fw=" + fw);
                return;
            }

            Entity target = mc.targetedEntity;
            if (!canHit(target)) return;

            boolean critPossible = critAchievable();
            float   charge       = charge();

            // Water
            if (isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    doAttack(target, mc.options.forwardKey.isPressed());
                    diag("WATER", "HIT water");
                }
                return;
            }

            // Crit impossible
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

            // Air-crit
            if (!mc.player.isOnGround()) {
                String blocker = critBlocker();
                if (blocker == null && charge >= CRIT_CHARGE) {
                    pendingTarget       = target;
                    preAttackCountdown  = 0;
                    pendingWasForward   = mc.options.forwardKey.isPressed();
                    wantSuppressForward = true;
                    diag("CRIT_Q", "CRIT queue charge=" + fmt(charge));
                } else if (blocker != null) {
                    diag("AIR_BLOCK", "block=" + blocker + " charge=" + fmt(charge));
                }
                return;
            }

            // Jump-crit gate
            if (isJumpHeld() || mc.player.getVelocity().y > 0.0) {
                if (isJumpHeld() && charge < jumpCharge.getValue()) {
                    wantSuppressJump = true;
                    diag("JUMP_GATE", "JUMP gate charge=" + fmt(charge));
                } else {
                    diag("GROUND_HOLD", "hold for crit charge=" + fmt(charge));
                }
                return;
            }

            // Ground combo
            if (charge >= GROUND_ATTACK_CHARGE && ticksOnGround >= GROUND_COMBO_DELAY) {
                doAttack(target, mc.options.forwardKey.isPressed());
                diag("COMBO", "COMBO charge=" + fmt(charge));
            } else {
                diag("GROUND_WAIT", "wait charge=" + fmt(charge) + " ticks=" + ticksOnGround);
            }

        } finally {
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    private void doAttack(Entity target, boolean wasForward) {
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (wasForward && mc.options.forwardKey.isPressed())
            mc.player.setSprinting(true);
    }

    private boolean canHit(Entity e) {
        return e instanceof LivingEntity le
            && le.isAlive()
            && !mc.player.isUsingItem()
            && isWeaponInHand();
    }

    private boolean isEntityValid(Entity e) {
        return e instanceof LivingEntity le
            && le.isAlive()
            && mc.world != null
            && mc.world.getEntityById(e.getId()) != null;
    }

    private float charge() { return mc.player.getAttackCooldownProgress(0.0F); }

    private boolean isWeaponInHand() {
        Item i = mc.player.getMainHandStack().getItem();
        return i.getRegistryEntry().isIn(ItemTags.SWORDS)
            || i.getRegistryEntry().isIn(ItemTags.AXES)
            || i.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
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
        if (!(mc.player.fallDistance > 0.0F))                        return "fall<=0";
        if (ticksOutOfWater < 3)                                     return "justLeftWater";
        if (mc.player.isOnGround())                                  return "onGround";
        if (mc.player.isClimbing())                                  return "climbing";
        if (mc.player.isTouchingWater())                             return "water";
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION))     return "levitation";
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS))      return "blindness";
        if (mc.player.hasVehicle())                                  return "vehicle";
        if (mc.player.getAbilities().flying)                         return "flying";
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

    /** Logs only on state-label change to avoid spam. */
    private void diag(String label, String msg) {
        if (!label.equals(lastDiag)) {
            lastDiag = label;
            LOG.info("[Triggerbot] " + msg);
        }
    }

    private String fmt(double v) {
        return String.format(Locale.US, "%.2f", v);
    }
}
