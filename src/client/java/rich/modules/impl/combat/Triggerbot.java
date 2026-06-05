package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.impl.movement.AutoSprint;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    // Read by ClientPlayerEntityMixin. While true, the sprint INPUT is forced false so vanilla keeps
    // us un-sprinted. We do NOT send any sprint packet ourselves - vanilla's own sendMovementPackets
    // emits the single, normal STOP_SPRINTING packet. (Sending our own caused a DUPLICATE packet that
    // the anti-cheat flagged as "badpackets".)
    public static volatile boolean SUPPRESS_SPRINT = false;

    private int ticksOutOfWater = 10;
    private int ticksOnGround = 0;
    // Consecutive ticks we have been cleanly non-sprinting (=> the server already got STOP_SPRINTING).
    private int cleanTicks = 0;
    // Throttle for ground-state console logging so we don't flood while waiting for charge.
    private int logThrottle = 0;

    private static final int GROUND_COMBO_DELAY = 3;
    private static final float GROUND_ATTACK_CHARGE = 1.0F;
    private static final float CRIT_CHARGE = 0.9F;
    // Only attack after being non-sprinting for at least this many ticks, guaranteeing the vanilla
    // STOP_SPRINTING packet reached the server BEFORE our attack packet -> server counts the crit.
    private static final int CLEAN_TICKS_REQUIRED = 1;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_SPRINT = false;
        this.ticksOutOfWater = 10;
        this.ticksOnGround = 0;
        this.cleanTicks = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppress = false;
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

            // Track how long we've been cleanly non-sprinting.
            if (!sprinting) {
                if (this.cleanTicks < 100) this.cleanTicks++;
            } else {
                this.cleanTicks = 0;
            }

            Entity target = mc.targetedEntity;
            boolean hittable = this.canHit(target);

            // On-screen debug so we can SEE why a crit does/doesn't fire.
            this.debug(target);

            if (mc.player.isUsingItem() || !this.isWeaponInHand()) {
                this.logState("no-weapon-or-using", target, false, false);
                return;
            }
            if (!hittable) {
                this.logState("no-target", target, false, false);
                return;
            }

            boolean critPossible = this.critAchievable();

            // Drop sprint the VANILLA way (no custom packet): clear the client sprint flag and keep the
            // sprint INPUT suppressed (SUPPRESS_SPRINT) so vanilla does not re-start it during
            // tickMovement; vanilla's own sendMovementPackets then sends ONE normal STOP_SPRINTING.
            if (critPossible) {
                wantSuppress = true;
                if (sprinting) {
                    mc.player.setSprinting(false);
                }
            }

            boolean serverClean = this.cleanTicks >= CLEAN_TICKS_REQUIRED && !mc.player.isSprinting();
            float charge = this.charge();

            // ---- IN WATER: crit impossible, normal hits. ----
            if (this.isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    this.logState("WATER-ATTACK", target, serverClean, true);
                    this.attack(target);
                } else {
                    this.logState("water-wait-charge", target, serverClean, false);
                }
                return;
            }

            // ---- AIRBORNE: land the crit, but only once sprint is confirmed off server-side. ----
            if (!mc.player.isOnGround()) {
                boolean ok = this.isPerfectCrit() && charge >= CRIT_CHARGE && serverClean;
                this.logState(ok ? "AIR-ATTACK" : "air-skip", target, serverClean, ok);
                if (ok) {
                    this.attack(target);
                }
                return;
            }

            // ---- ON GROUND ----
            // If a jump-crit is coming (jump held), keep sprint suppressed and wait for the air phase.
            if (critPossible && this.isJumpHeld()) {
                this.logState("ground-wait-jump", target, serverClean, false);
                return;
            }
            // Ground combo (non-crit). Only when sprint is confirmed off so we never emit a sprint-hit.
            boolean groundOk = charge >= GROUND_ATTACK_CHARGE && this.ticksOnGround >= GROUND_COMBO_DELAY && serverClean;
            this.logState(groundOk ? "GROUND-ATTACK" : "ground-wait", target, serverClean, groundOk);
            if (groundOk) {
                this.attack(target);
            }
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
        }
    }

    // Console logging (shows up in the game log/console the user pastes). Always logs attacks and any
    // airborne tick; throttles the repetitive ground/charge-wait spam so the log stays readable.
    private void logState(String why, Entity target, boolean serverClean, boolean attacking) {
        try {
            boolean airborne = mc.player != null && !mc.player.isOnGround();
            boolean verbose = attacking || airborne;
            if (!verbose) {
                if (this.logThrottle++ % 5 != 0) {
                    return;
                }
            }
            String s = String.format(
                "[TB] %s | air=%s fall=%.3f chg=%.2f clean=%d spr=%s srvSpr=%s jump=%s ground=%s perfectCrit=%s serverClean=%s tgt=%s",
                why,
                airborne,
                mc.player.fallDistance,
                this.charge(),
                this.cleanTicks,
                mc.player.isSprinting(),
                AutoSprint.isServerSprinting(),
                this.isJumpHeld(),
                mc.player.isOnGround(),
                this.isPerfectCrit(),
                serverClean,
                (target instanceof LivingEntity));
            System.out.println(s);
        } catch (Throwable ignored) {
        }
    }

    private void debug(Entity target) {
        try {
            String s = String.format(
                "TB tgt=%s air=%s fall=%.2f chg=%.2f clean=%d spr=%s jump=%s",
                (target != null && target instanceof LivingEntity),
                !mc.player.isOnGround(),
                mc.player.fallDistance,
                this.charge(),
                this.cleanTicks,
                mc.player.isSprinting(),
                this.isJumpHeld());
            mc.player.sendMessage(Text.literal(s), true);
        } catch (Throwable ignored) {
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

    // Mirror the SERVER crit rule (ServerPlayNetworkHandler): fallDistance>0, airborne, not climbing/
    // in water, no blindness/levitation, no vehicle, not flying. NOTE: the server does NOT check
    // velocity.y, so we must not require it either (that was skipping the first valid crit ticks).
    private boolean isPerfectCrit() {
        return mc.player.fallDistance > 0.0F
            && this.ticksOutOfWater >= 3
            && !mc.player.isOnGround()
            && !mc.player.isClimbing()
            && !mc.player.isTouchingWater()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
    }

    private boolean critAchievable() {
        return this.ticksOutOfWater >= 3
            && !mc.player.isTouchingWater()
            && !mc.player.isClimbing()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
    }
}
