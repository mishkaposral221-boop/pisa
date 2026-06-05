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
import rich.modules.impl.movement.AutoSprint;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    // Read by ClientPlayerEntityMixin. While true, the sprint INPUT is forced false so vanilla keeps
    // us un-sprinted for this tick only. Set to true ONLY on the exact tick we attack (~1 tick = ~50ms),
    // so sprint is suppressed for just that tick and immediately resumes on the next tick.
    public static volatile boolean SUPPRESS_SPRINT = false;

    // Read by ClientPlayerEntityMixin. While true, the jump INPUT is forced false for this tick.
    public static volatile boolean SUPPRESS_JUMP = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    private String lastDiag = "";

    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff",
            "Min weapon charge to hit when crit is impossible (sphere). Lower = hit earlier but weaker")
            .setValue(0.80F).range(0.3F, 1.0F);

    public SliderSettings jumpCharge = new SliderSettings("Jump charge",
            "Min weapon charge before a held jump fires (perfect jump-crits). Lower = jump sooner")
            .setValue(0.55F).range(0.3F, 1.0F);

    private int ticksOutOfWater = 10;
    private int ticksOnGround = 0;
    private int cleanTicks = 0;

    private static final int GROUND_COMBO_DELAY = 3;
    private static final float GROUND_ATTACK_CHARGE = 1.0F;
    private static final float CRIT_CHARGE = 0.9F;

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
        SUPPRESS_SPRINT = false;
        SUPPRESS_JUMP = false;
        this.ticksOutOfWater = 10;
        this.ticksOnGround = 0;
        this.cleanTicks = 0;
        this.lastDiag = "";
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppress = false;
        boolean wantSuppressJump = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                this.ticksOnGround = 0;
                this.cleanTicks = 0;
                return;
            }

            boolean sprinting = mc.player.isSprinting();

            if (this.isInWater()) {
                this.ticksOutOfWater = 0;
            } else if (this.ticksOutOfWater < 100) {
                this.ticksOutOfWater++;
            }

            if (mc.player.isOnGround()) {
                if (this.ticksOnGround < 100) this.ticksOnGround++;
            } else {
                this.ticksOnGround = 0;
            }

            // Track how long we've been cleanly non-sprinting (for logging only).
            if (!sprinting) {
                if (this.cleanTicks < 100) this.cleanTicks++;
            } else {
                this.cleanTicks = 0;
            }

            Entity target = mc.targetedEntity;
            boolean hittable = this.canHit(target);

            if (mc.player.isUsingItem() || !this.isWeaponInHand()) {
                return;
            }
            if (!hittable) {
                return;
            }

            boolean critPossible = this.critAchievable();
            float charge = this.charge();

            // -----------------------------------------------------------------
            // SPRINT SUPPRESSION: 1 TICK ONLY, RIGHT BEFORE THE ATTACK
            //
            // Old behaviour: SUPPRESS_SPRINT = critPossible (true for the ENTIRE
            // jump, ~15 ticks / 750 ms). The player felt the sprint freeze for a
            // long time before every hit.
            //
            // New behaviour: SUPPRESS_SPRINT stays false until the exact tick we
            // decide to attack. Right before attack() we set wantSuppress=true and
            // mc.player.setSprinting(false). The movement packet vanilla sends
            // later in this same tick carries sprint=false, so the server registers
            // a no-sprint hit (crit). The very next tick wantSuppress returns to
            // false (via the finally block) and sprint resumes immediately if W
            // is still held. Total suppression time: ~50 ms (1 game tick).
            // -----------------------------------------------------------------

            // ---- IN WATER: crit impossible, normal hits. ----
            if (this.isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    this.attack(target);
                    LOG.info("[Triggerbot] HIT water charge=" + fmt(charge) + " " + this.state());
                }
                return;
            }

            // ---- CRIT IMPOSSIBLE ----
            if (!critPossible) {
                float need = this.noCritCharge.getValue();
                if (charge >= need) {
                    this.attack(target);
                    LOG.info("[Triggerbot] HIT no-crit-possible charge=" + fmt(charge)
                        + " need=" + fmt(need) + " blocker=" + this.critBlocker() + " " + this.state());
                } else {
                    this.diag("NOCRIT_WAIT", "no-crit-possible wait charge=" + fmt(charge)
                        + " (need " + fmt(need) + ") blocker=" + this.critBlocker() + " " + this.state());
                }
                return;
            }

            // ---- AIRBORNE: crit window ----
            // Clear sprint for exactly 1 tick, attack, sprint resumes next tick.
            if (!mc.player.isOnGround()) {
                String blocker = this.critBlocker();
                boolean perfectCrit = blocker == null;
                if (perfectCrit && charge >= CRIT_CHARGE) {
                    // Suppress sprint for THIS tick only: movement packet sent later
                    // in this tick carries sprint=false -> server registers a crit.
                    if (sprinting) {
                        wantSuppress = true;
                        mc.player.setSprinting(false);
                    }
                    this.attack(target);
                    LOG.info("[Triggerbot] CRIT air charge=" + fmt(charge)
                        + " fall=" + fmt(mc.player.fallDistance)
                        + " velY=" + fmt(mc.player.getVelocity().y) + " " + this.state());
                } else {
                    String reason;
                    if (!perfectCrit) {
                        reason = "no-perfectCrit:" + blocker
                            + " (fall=" + fmt(mc.player.fallDistance)
                            + " ticksOOW=" + this.ticksOutOfWater + ")";
                    } else {
                        reason = "charge-too-low (" + fmt(charge) + " < " + CRIT_CHARGE + ")";
                    }
                    this.diag("AIR_BLOCK:" + reason, "AIR no-crit blocked: " + reason + " " + this.state());
                }
                return;
            }

            // ---- ON GROUND ----
            // Hold for the crit when the player is jumping or being launched upward.
            if (critPossible && (this.isJumpHeld() || mc.player.getVelocity().y > 0.0)) {
                if (mc.player.isOnGround() && this.isJumpHeld() && charge < this.jumpCharge.getValue()) {
                    wantSuppressJump = true;
                    this.diag("JUMP_GATE", "JUMP gated charge=" + fmt(charge)
                        + " (need " + fmt(this.jumpCharge.getValue()) + ") " + this.state());
                } else {
                    this.diag("GROUND_HOLD", "GROUND hold-for-crit jump=" + this.isJumpHeld()
                        + " velY=" + fmt(mc.player.getVelocity().y)
                        + " charge=" + fmt(charge) + " " + this.state());
                }
                return;
            }

            // Ground combo: suppress sprint for 1 tick, attack, sprint resumes next tick.
            if (charge >= GROUND_ATTACK_CHARGE && this.ticksOnGround >= GROUND_COMBO_DELAY) {
                if (sprinting) {
                    wantSuppress = true;
                    mc.player.setSprinting(false);
                }
                this.attack(target);
                LOG.info("[Triggerbot] COMBO ground charge=" + fmt(charge)
                    + " ticksGround=" + this.ticksOnGround
                    + " critPossible=" + critPossible + " " + this.state());
            } else if (charge < GROUND_ATTACK_CHARGE) {
                this.diag("GROUND_CHARGE", "GROUND wait charge=" + fmt(charge)
                    + " (need " + GROUND_ATTACK_CHARGE + ") " + this.state());
            } else {
                this.diag("GROUND_DELAY", "GROUND wait ticksOnGround=" + this.ticksOnGround
                    + " (need " + GROUND_COMBO_DELAY + ") " + this.state());
            }
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
            SUPPRESS_JUMP = wantSuppressJump;
        }
    }

    private String state() {
        int haste = mc.player.hasStatusEffect(StatusEffects.HASTE)
            && mc.player.getStatusEffect(StatusEffects.HASTE) != null
            ? mc.player.getStatusEffect(StatusEffects.HASTE).getAmplifier() + 1 : 0;
        int fatigue = mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)
            && mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE) != null
            ? mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier() + 1 : 0;
        boolean blind = mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
        return "[onGround=" + mc.player.isOnGround()
            + " sprint=" + mc.player.isSprinting()
            + " cleanTicks=" + this.cleanTicks
            + " attackSpeed=" + fmt(mc.player.getAttackCooldownProgressPerTick())
            + " haste=" + haste
            + " miningFatigue=" + fatigue
            + " blind=" + blind + "]";
    }

    private void diag(String key, String full) {
        if (!key.equals(this.lastDiag)) {
            this.lastDiag = key;
            LOG.info("[Triggerbot] " + full);
        }
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }

    private float charge() {
        return mc.player.getAttackCooldownProgress(0.0F);
    }

    private void attack(Entity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean canHit(Entity e) {
        return !mc.player.isUsingItem() && this.isWeaponInHand()
            && e instanceof LivingEntity && ((LivingEntity) e).isAlive();
    }

    private boolean isWeaponInHand() {
        Item item = mc.player.getMainHandStack().getItem();
        return item.getRegistryEntry().isIn(ItemTags.SWORDS)
            || item.getRegistryEntry().isIn(ItemTags.AXES)
            || item.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
    }

    private boolean isInWater() {
        return mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming();
    }

    private boolean isJumpHeld() {
        try {
            return mc.options.jumpKey.isPressed();
        } catch (Throwable t) {
            return false;
        }
    }

    private String critBlocker() {
        if (!(mc.player.fallDistance > 0.0)) return "fall<=0";
        if (this.ticksOutOfWater < 3) return "justLeftWater";
        if (mc.player.isOnGround()) return "onGround";
        if (mc.player.isClimbing()) return "climbing";
        if (mc.player.isTouchingWater()) return "water";
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return "levitation";
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) return "blindness";
        if (mc.player.hasVehicle()) return "vehicle";
        if (mc.player.getAbilities().flying) return "flying";
        return null;
    }

    private boolean isPerfectCrit() {
        return this.critBlocker() == null;
    }

    private boolean critAchievable() {
        return this.ticksOutOfWater >= 3
            && !mc.player.isTouchingWater()
            && !mc.player.isClimbing()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
    }
}
