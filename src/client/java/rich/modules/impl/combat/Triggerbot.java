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
    // us un-sprinted. We do NOT send any sprint packet ourselves - vanilla's own sendMovementPackets
    // emits the single, normal STOP_SPRINTING packet. (Sending our own caused a DUPLICATE packet that
    // the anti-cheat flagged as "badpackets".)
    public static volatile boolean SUPPRESS_SPRINT = false;

    // Read by ClientPlayerEntityMixin. While true, the jump INPUT is forced false for this tick so the
    // player's held jump does NOT fire yet. Used for jump-crit sync: we hold the jump back until the
    // weapon is charged enough that the upcoming descent lands a crit, so every jump crits (no misses).
    public static volatile boolean SUPPRESS_JUMP = false;

    // Диагностическое логирование (пишется в latest.log и консоль). Помогает понять почему
    // вместо крита иногда идёт комбо.
    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    // Ключ последней причины - чтобы не спамить одним и тем же состоянием каждый тик.
    private String lastDiag = "";

    // Min weapon charge required to hit when a crit is IMPOSSIBLE (under a debuff sphere: blindness +
    // slowed attack speed). Under the sphere the weapon does not refill to 1.00 between fast jumps, so
    // requiring full charge made the bot skip ~every 4th jump-hit. Lower this = hit EARLIER (stop
    // missing jumps) at the cost of weaker per-hit damage. Tunable in the module menu.
    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff", "Min weapon charge to hit when crit is impossible (sphere). Lower = hit earlier but weaker").setValue(0.80F).range(0.3F, 1.0F);

    // Min weapon charge before a HELD jump is allowed to fire. Free-jumping (holding space) spams jumps
    // faster than the weapon refills (12.5 ticks), so the crit drifts later every jump until a whole
    // descent passes below 0.9 charge -> a MISSED crit (the server requires >0.9 for a crit, so we
    // cannot just lower the crit threshold). Holding the jump back until charge >= this value makes
    // every jump descend with a crit-ready weapon -> a crit on EVERY jump, no misses. During the ~6t
    // ascent the weapon gains ~0.48 charge, so ~0.5 here yields a full-charge crit each jump. Lower =
    // jump sooner (less on-ground stutter, risk of a slightly later crit); higher = more reliable.
    public SliderSettings jumpCharge = new SliderSettings("Jump charge", "Min weapon charge before a held jump fires (perfect jump-crits). Lower = jump sooner").setValue(0.55F).range(0.3F, 1.0F);

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
            // hit as soon as charge reaches the configurable threshold, in the air OR on the ground. Under
            // a slowed-attack debuff sphere the weapon can't refill to 1.00 between fast jumps, so a full
            // charge requirement would skip ~every 4th jump-hit; noCritCharge lets us hit EARLIER. ----
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
                // JUMP-CRIT SYNC: while the player holds jump on the ground, do NOT let the jump fire
                // until the weapon is charged enough that the upcoming descent lands a crit. Free jumping
                // spams jumps faster than the weapon refills, so the crit drifts later every jump until a
                // whole descent passes below 0.9 charge -> a missed crit. Gating the held jump until
                // charge >= jumpCharge makes every jump descend crit-ready -> a crit on EVERY jump. We
                // only gate the player's OWN held jump (never force a jump they didn't ask for).
                if (mc.player.isOnGround() && this.isJumpHeld() && charge < this.jumpCharge.getValue()) {
                    wantSuppressJump = true;
                    this.diag("JUMP_GATE", "JUMP gated charge=" + fmt(charge)
                        + " (need " + fmt(this.jumpCharge.getValue()) + ") " + this.state());
                } else {
                    this.diag("GROUND_HOLD", "GROUND hold-for-crit jump=" + this.isJumpHeld()
                        + " velY=" + fmt(mc.player.getVelocity().y) + " charge=" + fmt(charge) + " " + this.state());
                }
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
            SUPPRESS_JUMP = wantSuppressJump;
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
            // Read the PHYSICAL jump key, NOT player.input.playerInput: while gating we rewrite
            // playerInput.jump() to false, so reading playerInput here would flip to false the next
            // tick and break the gate. The keybinding reflects the real key regardless of our rewrite.
            return mc.options.jumpKey.isPressed();
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
