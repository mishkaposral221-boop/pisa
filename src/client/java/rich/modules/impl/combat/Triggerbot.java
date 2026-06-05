package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    // Still read by ClientPlayerEntityMixin: drops the sprint INPUT for this tick so vanilla does not
    // immediately re-start sprint while we are setting up / landing a crit (important when W is held).
    public static volatile boolean SUPPRESS_SPRINT = false;

    // Ticks since last water contact (server only counts crits once out of water).
    private int ticksOutOfWater = 10;
    // How long we have been waiting on the ground (jump held) for an upcoming crit.
    private int groundHoldTicks = 0;
    private static final int GROUND_HOLD_LIMIT = 8;

    // Consecutive ticks actually standing on the ground.
    private int ticksOnGround = 0;
    private static final int GROUND_COMBO_DELAY = 3;

    private static final float GROUND_ATTACK_CHARGE = 1.0F;
    private static final float CRIT_CHARGE = 0.9F;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        // No HolyWorld/FunTime split - the FunTime behaviour (sprint-reset crits + ground combo) is
        // used everywhere because it simply works better.
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
            // A crit is impossible in water, so just land normal hits at full charge.
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
                    wantSuppress = true;
                    if (this.isPerfectCrit() && charge >= CRIT_CHARGE) {
                        // Tell the SERVER we stopped sprinting THIS tick, BEFORE the attack packet.
                        // setSprinting(false) alone only flushes STOP_SPRINTING at end-of-tick (after
                        // the attack), so with W held the server still saw us sprinting and refused the
                        // crit. Sending the packet here fixes "holding W never crits".
                        this.stopSprintHard();
                        this.attack(target);
                    }
                }
                return;
            }

            // ---- ON GROUND ----
            boolean critComing = this.critAchievable() && this.isJumpHeld();
            if (critComing) {
                // Drop sprint now, while still grounded, so the first airborne tick is already clean.
                wantSuppress = true;
                this.stopSprintHard();
                if (mc.player.getVelocity().y <= 0.0) {
                    this.groundHoldTicks++;
                } else {
                    this.groundHoldTicks = 0;
                }
                if (this.groundHoldTicks <= GROUND_HOLD_LIMIT) {
                    return; // wait for the upcoming crit
                }
            } else {
                this.groundHoldTicks = 0;
            }

            // Ground combo (always on now - no mode split).
            if (charge >= GROUND_ATTACK_CHARGE && this.ticksOnGround >= GROUND_COMBO_DELAY) {
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

    // Stop sprinting on the client AND notify the server immediately, so a crit attack sent later in
    // this same tick is processed server-side as a non-sprinting (=> critical) hit.
    private void stopSprintHard() {
        if (mc.player == null || !this.canResetSprint()) {
            return;
        }
        if (mc.player.isSprinting()) {
            mc.player.setSprinting(false);
            try {
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                }
            } catch (Throwable ignored) {
            }
        }
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
