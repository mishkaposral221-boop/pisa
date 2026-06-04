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
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    // Read by ClientPlayerEntityMixin: when true, the sprint INPUT is suppressed
    // for this tick so vanilla drops sprint and sends STOP_SPRINTING before a crit.
    public static volatile boolean SUPPRESS_SPRINT = false;

    // ---- The only user-facing control is the server preset. ----
    // The two modes share all the timing logic; the ONLY difference is how the crit is delivered:
    //   FunTime   - lenient anticheat: an instant attack on the falling tick already registers a
    //               critical hit, so we swing immediately (fastest).
    //   HolyWorld - strict anticheat: it rejects a crit performed while the player is sprinting and
    //               scores it as a plain hit. So we first drop sprint (suppress the sprint input so
    //               vanilla sends STOP_SPRINTING) and only attack on the NEXT tick. This is exactly
    //               why crits "didn't pass" on HolyWorld before.
    public static final String MODE_FUNTIME = "FunTime";
    public static final String MODE_HOLYWORLD = "HolyWorld";
    public SelectSetting mode = new SelectSetting("Mode", "Server preset - only the crit delivery differs").value(MODE_FUNTIME, MODE_HOLYWORLD);

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
    // momentarily released - which previously let an unwanted combo slip in "over time" in PvP.
    private int ticksOnGround = 0;
    private static final int GROUND_COMBO_DELAY = 3;

    // Fixed tuning that used to be sliders (the user asked to keep only the mode selector):
    //   - ground hits always swing at a full cooldown bar for max damage + knockback;
    //   - vanilla only counts a CRITICAL hit when the bar is > 0.9, so that is the crit threshold.
    private static final float GROUND_ATTACK_CHARGE = 1.0F;
    private static final float CRIT_CHARGE = 0.9F;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.mode);
    }

    // HolyWorld's anticheat needs the sprint dropped one tick before a crit; FunTime does not.
    private boolean useSprintReset() {
        return this.mode.isSelected(MODE_HOLYWORLD);
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

            // Deferred crit hit (HolyWorld mode only): STOP was sent last tick -> hit now.
            if (this.pendingCrit) {
                this.pendingCrit = false;
                Entity pt = mc.targetedEntity;
                if (this.canHit(pt) && this.charge() >= CRIT_CHARGE) {
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
                    // Rising toward the apex (or not a valid crit yet): HOLD and let the bar fill.
                    return;
                }
                if (charge < CRIT_CHARGE) {
                    // Descending but the bar is not crit-capable yet (e.g. Mining Fatigue): wait.
                    return;
                }
                if (this.useSprintReset() && mc.player.isSprinting() && this.canResetSprint()) {
                    // HolyWorld: drop sprint this tick, attack next tick so the crit is counted.
                    wantSuppress = true;
                    this.pendingCrit = true;
                    return;
                }
                this.attack(target);
                return;
            }

            // ---- ON GROUND ----
            // If a crit is coming (jump held while a crit is physically possible), HOLD the charged
            // hit so the combo never eats the crit.
            boolean critComing = this.critAchievable() && this.isJumpHeld();
            if (critComing) {
                // Only count ticks where we are genuinely STUCK on the ground (not rising).
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
            // grounded for a few ticks, so a momentary landing between crit jumps can never sneak
            // an unwanted combo in mid-fight.
            if (charge >= GROUND_ATTACK_CHARGE && this.ticksOnGround >= GROUND_COMBO_DELAY) {
                this.attack(target);
            }
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
        }
    }

    // The cooldown bar already accounts for the weapon's attack speed and any attribute modifiers
    // (Haste-style buffs AND Mining-Fatigue-style debuffs), so using it as THE timing signal makes
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

    // Whether a crit is even physically possible right now (so we should wait for it).
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
