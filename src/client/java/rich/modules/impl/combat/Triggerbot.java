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
    // for this tick so vanilla drops sprint and sends STOP_SPRINTING before the
    // crit attack. Only set while airborne for a crit, and only if SprintReset is on.
    public static volatile boolean SUPPRESS_SPRINT = false;

    public BooleanSetting combo = new BooleanSetting("Combo", "Hit on the ground as soon as the bar is charged").setValue(true);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Drop sprint one tick before an airborne crit (off = hit instantly, better for short drops)").setValue(false);
    // Ground combo: full bar = max damage + knockback. This is what stops weak/early hits.
    public SliderSettings attackCharge = new SliderSettings("AttackCharge", "Cooldown-bar fraction for GROUND hits (1.0 = full bar = max dmg)").range(0.7F, 1.0F).setValue(1.0F);
    // Airborne crit: a bit lower so the SHORT falling window when you step off a block
    // is actually usable. A crit at ~0.85 charge still out-damages a full non-crit, and
    // missing the crit entirely (because the bar wasn't 100% full for 2 ticks) is worse.
    public SliderSettings critCharge = new SliderSettings("CritCharge", "Cooldown-bar fraction for AIRBORNE crits (lower = catches short block drops)").range(0.6F, 1.0F).setValue(0.85F);

    // Deferred crit: tick N suppress sprint (STOP goes out), tick N+1 we attack.
    private boolean pendingCrit = false;

    // Ticks since last water contact; the server only accepts a crit once it agrees
    // we are out of the water, so wait a few ticks after climbing out of a pool.
    private int ticksOutOfWater = 10;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.combo, this.sprintReset, this.attackCharge, this.critCharge);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_SPRINT = false;
        this.pendingCrit = false;
        this.ticksOutOfWater = 10;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppress = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                this.pendingCrit = false;
                return;
            }

            // Track time since last water contact (used to gate crits after a swim).
            if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
                this.ticksOutOfWater = 0;
            } else if (this.ticksOutOfWater < 100) {
                this.ticksOutOfWater++;
            }

            // Deferred crit hit: sprint was dropped last tick (STOP already sent) -> hit now.
            // Only reached when SprintReset is enabled.
            if (this.pendingCrit) {
                this.pendingCrit = false;
                Entity pt = mc.targetedEntity;
                if (!mc.player.isUsingItem()
                    && this.isWeaponInHand()
                    && pt instanceof LivingEntity
                    && ((LivingEntity) pt).isAlive()
                    && mc.player.getAttackCooldownProgress(0.0F) >= this.critCharge.getValue()) {
                    mc.interactionManager.attackEntity(mc.player, pt);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
                return;
            }

            if (mc.player.isUsingItem() || !this.isWeaponInHand()) {
                return;
            }

            Entity target = mc.targetedEntity;
            if (!(target instanceof LivingEntity) || !((LivingEntity) target).isAlive()) {
                return;
            }

            if (!mc.player.isOnGround()) {
                // AIRBORNE: only fire when this is a real, server-valid crit (falling, fallDistance > 0,
                // out of water, no levitation, etc). Uses its own, lower charge gate so the short window
                // when stepping off a block is actually usable.
                if (!this.isPerfectCrit()) {
                    return;
                }
                if (mc.player.getAttackCooldownProgress(0.0F) < this.critCharge.getValue()) {
                    return;
                }

                if (this.sprintReset.isValue() && mc.player.isSprinting() && this.canResetSprint()) {
                    // Sprint held: suppress sprint input THIS tick (vanilla drops sprint + sends STOP),
                    // then hit on the NEXT tick so STOP_SPRINTING reaches the server before the attack.
                    wantSuppress = true;
                    this.pendingCrit = true;
                    return;
                }

                // Hit immediately - the crit lands in the same falling window (best for short drops).
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }

            // ON GROUND: straight combo at full charge. Never blocked by held jump anymore,
            // so combos actually register. Full bar keeps hits at max damage/knockback.
            if (!this.combo.isValue()) {
                return;
            }
            if (mc.player.getAttackCooldownProgress(0.0F) < this.attackCharge.getValue()) {
                return;
            }
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
        }
    }

    private boolean isWeaponInHand() {
        Item item = mc.player.getMainHandStack().getItem();
        return item.getRegistryEntry().isIn(ItemTags.SWORDS)
            || item.getRegistryEntry().isIn(ItemTags.AXES)
            || item.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
    }

    // A clean vanilla crit the server (and anti-cheat) will actually count.
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

    private boolean canResetSprint() {
        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
            return false;
        }
        return !mc.player.isGliding();
    }
}
