package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    // Read by ClientPlayerEntityMixin: when true, the sprint INPUT is suppressed
    // for this tick so vanilla drops sprint and sends STOP_SPRINTING before a crit.
    public static volatile boolean SUPPRESS_SPRINT = false;

    public BooleanSetting combo = new BooleanSetting("Combo", "Hit on the ground when no crit is achievable").setValue(true);
    public BooleanSetting critPriority = new BooleanSetting("CritPriority", "Always save the hit for a crit when one is coming, so the combo never eats a crit").setValue(true);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Drop sprint one tick before an airborne crit (off = hit instantly)").setValue(false);
    // Ground combo: full bar = max damage + knockback (stops weak/early hits).
    public SliderSettings attackCharge = new SliderSettings("AttackCharge", "Cooldown-bar fraction for GROUND hits (1.0 = full bar)").range(0.7F, 1.0F).setValue(1.0F);
    // Airborne crit. IMPORTANT: vanilla only registers a CRITICAL hit when the attack
    // cooldown bar is > 0.9 (otherwise it is just a normal hit). So this must stay >= 0.9
    // or the bot will swing without ever critting (this is exactly why crits failed under
    // Mining Fatigue: the slowed attack speed kept the bar below 0.9 during the fall).
    public SliderSettings critCharge = new SliderSettings("CritCharge", "Cooldown-bar fraction required for a CRIT (vanilla needs > 0.9)").range(0.9F, 1.0F).setValue(0.9F);

    // Deferred crit: tick N suppress sprint (STOP goes out), tick N+1 attack.
    private boolean pendingCrit = false;
    // Ticks since last water contact (server only counts crits once out of water).
    private int ticksOutOfWater = 10;
    // How long we have been STUCK on the ground (jump held but unable to leave) waiting for a crit.
    private int groundHoldTicks = 0;
    // Safety: if we are genuinely stuck on the ground (e.g. a 2-block ceiling) we fall back to a
    // ground hit after this many stuck ticks so the bot never freezes.
    private static final int GROUND_HOLD_LIMIT = 8;

    // How many consecutive ticks we have actually been standing on the ground. A ground combo is
    // only allowed once this passes GROUND_COMBO_DELAY. During bunny-hopping the player only
    // touches the ground for a tick or two between jumps, and on those ticks the jump key can be
    // momentarily released (critComing = false) - which previously let an unwanted combo slip in
    // "over time" in PvP. Requiring a few grounded ticks means only genuine ground fighting (the
    // player deliberately staying down) ever combos, while crit-bouncing keeps this near zero.
    private int ticksOnGround = 0;
    private static final int GROUND_COMBO_DELAY = 3;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.combo, this.critPriority, this.sprintReset, this.attackCharge, this.critCharge);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_SPRINT = false;
        this.pendingCrit = false;
        this.ticksOutOfWater = 10;
        this.groundHoldTicks = 0;
        this.ticksOnGround = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppress = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                this.pendingCrit = false;
                this.groundHoldTicks = 0;
                this.ticksOnGround = 0;
                return;
            }

            if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
                this.ticksOutOfWater = 0;
            } else if (this.ticksOutOfWater < 100) {
                this.ticksOutOfWater++;
            }

            // Track how long we have continuously stood on the ground.
            if (mc.player.isOnGround()) {
                if (this.ticksOnGround < 100) {
                    this.ticksOnGround++;
                }
            } else {
                this.ticksOnGround = 0;
            }

            // Deferred crit hit (only when SprintReset is on): STOP was sent last tick -> hit now.
            if (this.pendingCrit) {
                this.pendingCrit = false;
                Entity pt = mc.targetedEntity;
                if (this.canHit(pt) && this.charge() >= this.critCharge.getValue()) {
                    this.attack(pt);
                }
                return;
            }

            if (mc.player.isUsingItem() || !this.isWeaponInHand()) {
                this.groundHoldTicks = 0;
                return;
            }

            Entity target = mc.targetedEntity;
            if (!this.canHit(target)) {
                this.groundHoldTicks = 0;
                return;
            }

            float charge = this.charge();

            // ---- AIRBORNE: strike the crit as soon as the bar is actually crit-capable. ----
            if (!mc.player.isOnGround()) {
                this.groundHoldTicks = 0;
                if (!this.isPerfectCrit()) {
                    // Rising toward the apex (or not a valid crit yet): HOLD and let the bar
                    // fill. Holding never loses charge - the bar caps at 1.0 and only resets
                    // when we actually attack, so even under attack-speed debuffs the charge
                    // keeps building across bounces until a real crit is possible.
                    return;
                }
                if (charge < this.critCharge.getValue()) {
                    // Descending but the bar is not crit-capable yet (e.g. Mining Fatigue):
                    // do NOT swing - a sub-0.9 hit would be a wasted non-crit that resets the
                    // bar. Wait; the charge carries over to the next bounce until it crits.
                    return;
                }
                if (this.sprintReset.isValue() && mc.player.isSprinting() && this.canResetSprint()) {
                    wantSuppress = true;
                    this.pendingCrit = true;
                    return;
                }
                this.attack(target);
                return;
            }

            // ---- ON GROUND ----
            // If a crit is coming (jump held while a crit is physically possible) and
            // CritPriority is on, HOLD the charged hit so the combo never eats the crit.
            boolean critComing = this.critPriority.isValue() && this.critAchievable() && this.isJumpHeld();
            if (critComing) {
                // Only count ticks where we are genuinely STUCK on the ground (not rising).
                // A real jump has upward velocity on its launch tick and the airborne branch
                // resets this, so during normal bunny-hopping this stays ~0 and we never spam
                // a combo. It only climbs when the player physically cannot gain fall distance
                // (e.g. a 2-block ceiling), which is the only case a ground combo is correct.
                if (mc.player.getVelocity().y <= 0.0) {
                    this.groundHoldTicks++;
                } else {
                    this.groundHoldTicks = 0;
                }
                if (this.groundHoldTicks <= GROUND_HOLD_LIMIT) {
                    return; // hold for the upcoming crit
                }
                // Genuinely stuck -> stop starving and allow a ground hit.
            } else {
                this.groundHoldTicks = 0;
            }

            // Ground combo at full charge for max damage + knockback. Only after we have been
            // grounded for a few ticks, so a momentary landing between crit jumps (where the jump
            // key is briefly not held) can never sneak an unwanted combo in mid-fight.
            if (this.combo.isValue()
                && charge >= this.attackCharge.getValue()
                && this.ticksOnGround >= GROUND_COMBO_DELAY) {
                this.attack(target);
            }
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
        }
    }

    // The cooldown bar already accounts for the weapon's attack speed and any attribute
    // modifiers (Haste-style buffs AND Mining-Fatigue-style debuffs), so using it as THE
    // timing signal makes the bot adapt to every situation automatically.
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

    private boolean isJumpHeld() {
        try {
            return mc.player.input.playerInput.jump();
        } catch (Throwable t) {
            return false;
        }
    }

    // A clean vanilla crit the server will actually count.
    private boolean isPerfectCrit() {
        return mc.player.fallDistance > 0.0
            && mc.player.getVelocity().y < 0.0
            && this.ticksOutOfWater >= 3
            && !mc.player.isOnGround()
            && !mc.player.isClimbing()
            && !mc.player.isTouchingWater()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
    }

    // Whether a crit is even physically possible right now (so CritPriority should wait).
    private boolean critAchievable() {
        return this.ticksOutOfWater >= 3
            && !mc.player.isTouchingWater()
            && !mc.player.isClimbing()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
    }

    private boolean canResetSprint() {
        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
            return false;
        }
        return !mc.player.isGliding();
    }
}
