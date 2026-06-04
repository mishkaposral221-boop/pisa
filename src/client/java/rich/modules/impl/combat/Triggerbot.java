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
    // Airborne crit: a bit lower so the SHORT falling window off a block is usable.
    public SliderSettings critCharge = new SliderSettings("CritCharge", "Cooldown-bar fraction for AIRBORNE crits (lower = catches short drops)").range(0.6F, 1.0F).setValue(0.85F);

    // Deferred crit: tick N suppress sprint (STOP goes out), tick N+1 attack.
    private boolean pendingCrit = false;
    // Ticks since last water contact (server only counts crits once out of water).
    private int ticksOutOfWater = 10;
    // How long we have been holding a charged hit on the ground waiting for a crit.
    private int groundHoldTicks = 0;
    // Safety: if we hold for a crit but can't get airborne (e.g. ceiling), fall back to a
    // ground hit after this many ticks so the bot never freezes.
    private static final int GROUND_HOLD_LIMIT = 8;

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
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppress = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                this.pendingCrit = false;
                this.groundHoldTicks = 0;
                return;
            }

            if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
                this.ticksOutOfWater = 0;
            } else if (this.ticksOutOfWater < 100) {
                this.ticksOutOfWater++;
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

            // ---- AIRBORNE: strike the crit as soon as the bar is ready. ----
            if (!mc.player.isOnGround()) {
                this.groundHoldTicks = 0;
                if (!this.isPerfectCrit()) {
                    // Rising toward the apex (or not a valid crit yet): HOLD and let the bar
                    // fill. Holding never loses charge - the bar caps at 1.0 and only resets
                    // when we actually attack, so the descent crit lands at full power.
                    return;
                }
                if (charge < this.critCharge.getValue()) {
                    return; // descending but bar not ready this tick -> wait
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
            // If a crit is coming (jump held / about to leave the ground) and CritPriority is
            // on, HOLD the charged hit so the combo never eats the crit. The bar keeps filling
            // while we wait, so nothing is lost.
            boolean critComing = this.critPriority.isValue() && this.critAchievable() && this.isJumpHeld();
            if (critComing) {
                this.groundHoldTicks++;
                if (this.groundHoldTicks <= GROUND_HOLD_LIMIT) {
                    return; // hold for the upcoming crit
                }
                // Stuck on the ground (e.g. ceiling) -> stop starving, allow a ground hit.
            } else {
                this.groundHoldTicks = 0;
            }

            // Ground combo at full charge for max damage + knockback.
            if (this.combo.isValue() && charge >= this.attackCharge.getValue()) {
                this.attack(target);
            }
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
        }
    }

    // The cooldown bar already accounts for the weapon's attack speed and any attribute
    // modifiers (Haste-style / attack-speed buffs), so using it as THE timing signal makes
    // the bot adapt to every situation automatically.
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
