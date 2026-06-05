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
    // Read by ClientPlayerEntityMixin: when true the sprint INPUT is suppressed this tick so vanilla
    // drops sprint and sends STOP_SPRINTING. Vanilla NEVER scores a crit while sprinting, so the bot
    // must already be non-sprinting (packet sent on an EARLIER tick) when the attack packet goes out.
    public static volatile boolean SUPPRESS_SPRINT = false;

    public static final String MODE_FUNTIME = "FunTime";
    public static final String MODE_HOLYWORLD = "HolyWorld";
    public SelectSetting mode = new SelectSetting("Mode", "Server preset").value(MODE_FUNTIME, MODE_HOLYWORLD);

    // Ticks since last water contact (server only counts crits once out of water).
    private int ticksOutOfWater = 10;
    // How long we have been STUCK on the ground (jump held but unable to leave) waiting for a crit.
    private int groundHoldTicks = 0;
    private static final int GROUND_HOLD_LIMIT = 8;

    // Consecutive ticks actually standing on the ground - a ground combo is only allowed once this
    // passes GROUND_COMBO_DELAY so a momentary landing between crit jumps can't sneak a combo in.
    private int ticksOnGround = 0;
    private static final int GROUND_COMBO_DELAY = 3;

    private static final float GROUND_ATTACK_CHARGE = 1.0F;
    private static final float CRIT_CHARGE = 0.9F;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.mode);
    }

    // Both presets must drop sprint before a crit - vanilla never crits while sprinting.
    private boolean useSprintReset() {
        return true;
    }

    // Only FunTime adds the aggressive auto ground combo; HolyWorld stays crit-only.
    private boolean allowGroundCombo() {
        return this.mode.isSelected(MODE_FUNTIME);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_SPRINT = false;
        this.ticksOutOfWater = 10;
        this.groundHoldTicks = 0;
        this.ticksOnGround = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppress = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                this.groundHoldTicks = 0;
                this.ticksOnGround = 0;
                return;
            }

            if (this.isInWater()) {
                this.ticksOutOfWater = 0;
            } else if (this.ticksOutOfWater < 100) {
                this.ticksOutOfWater++;
            }

            if (mc.player.isOnGround()) {
                if (this.ticksOnGround < 100) {
                    this.ticksOnGround++;
                }
            } else {
                this.ticksOnGround = 0;
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

            // ---- IN WATER ----
            // A crit is impossible in water, so the old airborne crit path (which required being out
            // of water) meant the bot NEVER swung while floating. Just land normal hits at full charge.
            if (this.isInWater()) {
                this.groundHoldTicks = 0;
                if (charge >= GROUND_ATTACK_CHARGE) {
                    this.attack(target);
                }
                return;
            }

            // ---- AIRBORNE ----
            if (!mc.player.isOnGround()) {
                this.groundHoldTicks = 0;
                if (this.critAchievable()) {
                    // Proactively drop sprint for the WHOLE airborne window. The STOP_SPRINTING packet
                    // flushes at the END of this tick, so by the time we actually swing (a LATER tick)
                    // the server already sees us as not sprinting and COUNTS the crit. This is exactly
                    // what a manual w-tap crit does, and is why crits "didn't register" before.
                    boolean droppedThisTick = false;
                    if (this.useSprintReset() && this.canResetSprint()) {
                        wantSuppress = true;
                        if (mc.player.isSprinting()) {
                            mc.player.setSprinting(false);
                            droppedThisTick = true;
                        }
                    }
                    // Never swing on the same tick we dropped sprint (the stop packet hasn't gone out
                    // yet). Once we are genuinely non-sprinting it is a clean, registered crit.
                    if (!droppedThisTick && this.isPerfectCrit() && charge >= CRIT_CHARGE && !mc.player.isSprinting()) {
                        this.attack(target);
                    }
                }
                return;
            }

            // ---- ON GROUND ----
            boolean critComing = this.critAchievable() && this.isJumpHeld();
            if (critComing) {
                if (mc.player.getVelocity().y <= 0.0) {
                    this.groundHoldTicks++;
                } else {
                    this.groundHoldTicks = 0;
                }
                if (this.groundHoldTicks <= GROUND_HOLD_LIMIT) {
                    return; // hold for the upcoming crit
                }
            } else {
                this.groundHoldTicks = 0;
            }

            if (this.allowGroundCombo() && charge >= GROUND_ATTACK_CHARGE && this.ticksOnGround >= GROUND_COMBO_DELAY) {
                this.attack(target);
            }
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
        }
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

    // Whether a crit is even physically possible right now (so we should wait for it / suppress sprint).
    private boolean critAchievable() {
        return this.ticksOutOfWater >= 3
            && !mc.player.isTouchingWater()
            && !mc.player.isClimbing()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
    }

    private boolean canResetSprint() {
        if (this.isInWater()) {
            return false;
        }
        return !mc.player.isGliding();
    }
}
