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
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    // Read by ClientPlayerEntityMixin. While true, the sprint INPUT is forced false so vanilla keeps
    // us un-sprinted. We do NOT send any sprint packet ourselves - vanilla's own sendMovementPackets
    // emits the single, normal STOP_SPRINTING packet. (Sending our own caused a DUPLICATE packet that
    // the anti-cheat flagged as "badpackets".)
    public static volatile boolean SUPPRESS_SPRINT = false;

    // Диагностическое логирование (пишется в latest.log и консоль). Помогает понять почему
    // вместо крита иногда идёт комбо.
    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    // Ключ последней причины - чтобы не спамить одним и тем же состоянием каждый тик.
    private String lastDiag = "";

    private int ticksOutOfWater = 10;
    private int ticksOnGround = 0;
    // Consecutive ticks we have been cleanly non-sprinting (=> the server already got STOP_SPRINTING).
    private int cleanTicks = 0;

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
        this.lastDiag = "";
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

            if (mc.player.isUsingItem() || !this.isWeaponInHand()) {
                return;
            }
            if (!hittable) {
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

            // The server's REAL sprint state. While holding W, AutoSprint re-enables the client sprint
            // flag every tick and we clear it again -> cleanTicks keeps resetting to 0 even though the
            // server already received STOP_SPRINTING (srvSpr=false). In that case the old cleanTicks-only
            // gate wrongly blocked the crit. So when AutoSprint is active and confirms the server is NOT
            // sprinting, treat us as clean immediately; otherwise fall back to the cleanTicks heuristic.
            boolean autoSprintActive = AutoSprint.getInstance() != null && AutoSprint.getInstance().isState();
            boolean serverConfirmedClean = autoSprintActive && !AutoSprint.isServerSprinting();
            boolean serverClean = !mc.player.isSprinting()
                && (this.cleanTicks >= CLEAN_TICKS_REQUIRED || serverConfirmedClean);
            float charge = this.charge();

            // ---- IN WATER: crit impossible, normal hits. ----
            if (this.isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    this.attack(target);
                    LOG.info("[Triggerbot] HIT water charge=" + fmt(charge) + " " + this.state());
                }
                return;
            }

            // ---- CRIT IMPOSSIBLE (blindness / levitation / vehicle / flying / climbing / just-left-water):
            // there is NO crit to wait for, so do not freeze holding for one and do not gate on sprint
            // (a sprint-hit is perfectly legal here - it just deals knockback instead of a crit). Land a
            // normal full-charge hit whenever possible, in the air OR on the ground. THIS is what makes
            // the bot actually deal damage under a debuff sphere (Blindness + Mining Fatigue) instead of
            // standing there doing nothing. ----
            if (!critPossible) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    this.attack(target);
                    LOG.info("[Triggerbot] HIT no-crit-possible charge=" + fmt(charge)
                        + " blocker=" + this.critBlocker() + " " + this.state());
                } else {
                    this.diag("NOCRIT_WAIT", "no-crit-possible wait charge=" + fmt(charge)
                        + " (need " + GROUND_ATTACK_CHARGE + ") blocker=" + this.critBlocker() + " " + this.state());
                }
                return;
            }

            // ---- AIRBORNE: land the crit the INSTANT the window is valid (fallDistance>0 + charge),
            // once sprint is confirmed off server-side. No waiting for a 'cleaner' fall - if a player
            // knocks us, we still crit on the very first descending tick. ----
            if (!mc.player.isOnGround()) {
                String blocker = this.critBlocker();
                boolean perfectCrit = blocker == null;
                if (perfectCrit && charge >= CRIT_CHARGE && serverClean) {
                    this.attack(target);
                    LOG.info("[Triggerbot] CRIT air charge=" + fmt(charge) + " fall=" + fmt(mc.player.fallDistance)
                        + " velY=" + fmt(mc.player.getVelocity().y) + " " + this.state());
                } else {
                    // Почему крит НЕ ушёл - главная диагностика (точная причина).
                    String reason;
                    if (!perfectCrit) {
                        reason = "no-perfectCrit:" + blocker + " (fall=" + fmt(mc.player.fallDistance) + " ticksOOW=" + this.ticksOutOfWater + ")";
                    } else if (charge < CRIT_CHARGE) {
                        reason = "charge-too-low (" + fmt(charge) + " < " + CRIT_CHARGE + ")";
                    } else {
                        reason = "not-serverClean (sprint=" + mc.player.isSprinting() + " cleanTicks=" + this.cleanTicks + ")";
                    }
                    this.diag("AIR_BLOCK:" + reason, "AIR no-crit blocked: " + reason + " " + this.state());
                }
                return;
            }

            // ---- ON GROUND ----
            // HOLD for the crit when the player is jumping OR is being launched upward (e.g. knocked by
            // another player's hit -> velocity.y > 0). Firing a flat ground combo here would eat the
            // attack cooldown and ruin the incoming jump-crit. This is exactly the 'combo while I was in
            // a jump' bug: we now wait for the air phase and crit instead. NOTE: critPossible is false
            // when a crit is impossible anyway (e.g. blindness), so under a debuff sphere we DON'T hold -
            // we fall through and deal normal combo damage instead of standing there waiting forever.
            if (critPossible && (this.isJumpHeld() || mc.player.getVelocity().y > 0.0)) {
                this.diag("GROUND_HOLD", "GROUND hold-for-crit jump=" + this.isJumpHeld()
                    + " velY=" + fmt(mc.player.getVelocity().y) + " charge=" + fmt(charge) + " " + this.state());
                return;
            }
            // Ground combo (non-crit). Only when sprint is confirmed off so we never emit a sprint-hit.
            if (charge >= GROUND_ATTACK_CHARGE && this.ticksOnGround >= GROUND_COMBO_DELAY && serverClean) {
                this.attack(target);
                LOG.info("[Triggerbot] COMBO ground charge=" + fmt(charge) + " ticksGround=" + this.ticksOnGround
                    + " critPossible=" + critPossible + " " + this.state());
            } else if (charge < GROUND_ATTACK_CHARGE) {
                this.diag("GROUND_CHARGE", "GROUND wait charge=" + fmt(charge) + " (need " + GROUND_ATTACK_CHARGE + ") " + this.state());
            } else if (!serverClean) {
                this.diag("GROUND_NOTCLEAN", "GROUND wait not-serverClean sprint=" + mc.player.isSprinting()
                    + " cleanTicks=" + this.cleanTicks + " " + this.state());
            }
        } finally {
            SUPPRESS_SPRINT = wantSuppress;
        }
    }

    // Состояние эффектов/движения для лога. haste/miningFatigue = уровень (0 = нет).
    // NB: в ванилле Haste/Mining Fatigue НЕ влияют на скорость атаки - этот лог покажет это напрямую.
    private String state() {
        int haste = mc.player.hasStatusEffect(StatusEffects.HASTE) && mc.player.getStatusEffect(StatusEffects.HASTE) != null
            ? mc.player.getStatusEffect(StatusEffects.HASTE).getAmplifier() + 1 : 0;
        int fatigue = mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE) && mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE) != null
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
            return mc.player.input.playerInput.jump();
        } catch (Throwable t) {
            return false;
        }
    }

    // Returns the name of the FIRST condition that BLOCKS a crit, or null if a crit is valid.
    // Mirrors the SERVER crit rule (PlayerEntity.attack): fallDistance>0, airborne, not climbing/
    // in water, no levitation/BLINDNESS, no vehicle, not flying. (Server also requires !sprinting,
    // handled separately via serverClean.) Used both as the gate and for precise logging.
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

    // A crit is even ACHIEVABLE this tick (ignoring fallDistance/onGround which depend on jumping).
    // Includes BLINDNESS: while blinded the server forbids crits entirely, so we must NOT hold for a
    // crit that can never land - instead fall through to a normal ground combo.
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
