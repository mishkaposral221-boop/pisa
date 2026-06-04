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
    // for this tick so vanilla itself drops sprint and sends STOP_SPRINTING
    // before the crit attack. Only set while airborne for a crit.
    public static volatile boolean SUPPRESS_SPRINT = false;

    public BooleanSetting combo = new BooleanSetting("Combo", "Combo on the ground; in the air hit on the crit tick").setValue(true);
    public BooleanSetting critPriority = new BooleanSetting("CritPriority", "While jump is held, don't waste the hit on a ground combo").setValue(true);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Drop sprint via input one tick so the crit lands, then resume").setValue(true);
    // Timing is driven ONLY by the attack-cooldown bar. This threshold is the
    // fraction of the bar that must be recharged before a hit (1.0 = full bar).
    // The bar already accounts for haste/Speed/fatigue, so timing self-adjusts.
    public SliderSettings attackCharge = new SliderSettings("AttackCharge", "Cooldown-bar fraction required before a hit (1.0 = full bar = max dmg)").range(0.7F, 1.0F).setValue(0.95F);

    // Deferred crit: tick N suppress sprint (STOP goes out), tick N+1 we attack.
    private boolean pendingCrit = false;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.combo, this.critPriority, this.sprintReset, this.attackCharge);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_SPRINT = false;
        this.pendingCrit = false;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppress = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                this.pendingCrit = false;
                return;
            }

            // Deferred crit hit: sprint was dropped last tick (STOP already sent) -> hit now.
            if (this.pendingCrit) {
                this.pendingCrit = false;
                Entity pt = mc.targetedEntity;
                if (!mc.player.isUsingItem()
                    && this.isWeaponInHand()
                    && pt instanceof LivingEntity
                    && ((LivingEntity) pt).isAlive()
                    && this.isCooldownReady()) {
                    mc.interactionManager.attackEntity(mc.player, pt);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
                return;
            }

            if (mc.player.isUsingItem()) {
                return;
            }
            if (!this.isWeaponInHand()) {
                return;
            }

            Entity target = mc.targetedEntity;
            if (!(target instanceof LivingEntity) || !((LivingEntity) target).isAlive()) {
                return;
            }

            // Single timing gate: the attack-cooldown bar. Adapts to haste/Speed/fatigue.
            if (!this.isCooldownReady()) {
                return;
            }

            if (mc.player.isOnGround()) {
                // Ground combo: never touch sprint -> no ground-drag.
                if (!this.combo.isValue()) {
                    return;
                }
                if (this.critPriority.isValue() && this.isJumpHeld()) {
                    return;
                }
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }

            // Airborne: only when really in a crit state (falling + fallDistance).
            if (!this.isPerfectCrit()) {
                return;
            }

            if (this.sprintReset.isValue() && mc.player.isSprinting() && this.canResetSprint()) {
                // Sprint held: suppress sprint input THIS tick (vanilla drops sprint + sends STOP),
                // then hit on the NEXT tick so STOP_SPRINTING reaches the server before the attack.
                wantSuppress = true;
                this.pendingCrit = true;
                return;
            }

            // Not sprinting (or reset off) -> hit immediately, the crit lands anyway.
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
        }
    }

    private boolean isCooldownReady() {
        return mc.player.getAttackCooldownProgress(0.0F) >= this.attackCharge.getValue();
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

    private boolean isPerfectCrit() {
        return mc.player.fallDistance > 0.0
            && !mc.player.isOnGround()
            && !mc.player.isClimbing()
            && !mc.player.isTouchingWater()
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
