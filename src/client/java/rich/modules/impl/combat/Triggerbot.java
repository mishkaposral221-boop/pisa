package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

/**
 * Triggerbot: auto-attacks targeted living entities.
 *
 * Крит-механика (air crit): удар всегда в ОДИН тик; спринт снимается
 * и возвращается внутри тика в doAttack -> нет лишних START/STOP_SPRINTING
 * пакетов.
 *
 * АНТИ-ФЛАГ (humanize): раньше бот бил детерминированно -- ровно в
 * первый тик, когда заряд переходил фиксированный порог (0.84/0.93).
 * Идеальная периодичность -> античит за несколько минут набивал
 * violation level. Теперь на КАЖДЫЙ удар случайно разбрасываются:
 * пороги заряда, задержка наземного комбо и лишняя микро-пауза между
 * ударами (0..3 тика). Разбросом управляет слайдер "Humanize"
 * (0 = старое детерминированное поведение).
 *
 * wTap (W-tap crits): дополнительно мягко гасит горизонтальную скорость
 * в момент удара и подавляет лишний прыжок -- без лишних пакетов.
 *
 * Charge thresholds (базовые, дальше +jitter):
 *   GROUND_ATTACK_CHARGE  0.93
 *   CRIT_CHARGE           0.84
 *   GROUND_COMBO_DELAY    2
 */
public class Triggerbot extends ModuleStructure {

    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    private static final boolean DEBUG_LOGS = Boolean.getBoolean("rich.debug.triggerbot");
    private static final long DEBUG_LOG_MIN_GAP_MS = 1000L;

    private String lastDiag = "";
    private long lastDiagLogMs = 0L;
    private int ticksOutOfWater = 10;
    private int ticksOnGround   = 0;

    // --- Гуманизация таймингов (анти-флаг) ---
    // Пороги/задержки пере-роллятся после каждого удара, чтобы тайминг
    // атак не был периодичным. Инициализированы литералами (= базовые пороги).
    private final java.util.Random rng = new java.util.Random();
    private int   attackDelayTicks       = 0;
    private float critChargeTarget       = 0.84F;
    private float groundChargeTarget     = 0.93F;
    private float waterChargeTarget      = 0.93F;
    private float noCritChargeTarget     = 0.78F;
    private int   groundComboDelayTarget = 2;

    // --- Timing constants ---
    // Lower = hits sooner in the cooldown cycle. Don't go below ~0.80 or
    // the server will register 0 damage (weapon not ready).
    private static final int   GROUND_COMBO_DELAY   = 2;    // ticks on ground before ground combo
    private static final float GROUND_ATTACK_CHARGE = 0.93F; // ground attack threshold
    private static final float CRIT_CHARGE          = 0.84F; // air-crit threshold

    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff",
            "Min weapon charge when crit is impossible")
            .setValue(0.78F).range(0.0F, 1.0F);

    public SliderSettings jumpCharge = new SliderSettings("Jump charge",
            "Min charge before held-jump fires")
            .setValue(0.50F).range(0.0F, 1.0F);

    // Случайность таймингов атаки. 0 = жёсткий детерминизм (как раньше),
    // больше = сильнее разброс -> меньше флагов, но чуть медленнее бьёт.
    public SliderSettings randomness = new SliderSettings("Humanize",
            "Случайность таймингов атаки (анти-флаг)")
            .setValue(0.5F).range(0.0F, 1.0F);

    // Когда выключено (по умолчанию) -- триггербот НЕ трогает ввод движения.
    public BooleanSetting wTap = new BooleanSetting("W-tap crits",
            "Гасит скорость/прыжок ради крита. Без лишних пакетов")
            .setValue(false);

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.noCritCharge, this.jumpCharge, this.randomness, this.wTap);
    }

    @Override
    public void activate() {
        super.activate();
        rollTargets();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_FORWARD = false;
        SUPPRESS_SPRINT  = false;
        SUPPRESS_JUMP    = false;
        ticksOutOfWater  = 0;
        ticksOnGround    = 0;
        attackDelayTicks = 0;
        lastDiag = "";
        lastDiagLogMs = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppressForward = false;
        boolean wantSuppressJump    = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                ticksOnGround = 0;
                return;
            }

            // Отсчёт случайной микро-паузы между ударами (анти-флаг).
            if (attackDelayTicks > 0) attackDelayTicks--;

            if (isInWater()) { ticksOutOfWater = 0; }
            else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else { ticksOnGround = 0; }

            // --- Normal detection ---
            Entity target = mc.targetedEntity;
            if (!canHit(target)) return;

            boolean critPossible = critAchievable();
            float charge = charge();
            boolean fwd = mc.options.forwardKey.isPressed();

            if (isInWater()) {
                if (charge >= waterChargeTarget && fire(target, fwd, false)) {
                    diag("WATER", "HIT water");
                }
                return;
            }

            if (!critPossible) {
                if (charge >= noCritChargeTarget) {
                    if (fire(target, fwd, false)) diag("NOCRIT", "NOCRIT hit charge=" + fmt(charge));
                } else {
                    diag("NOCRIT_WAIT", "NOCRIT wait charge=" + fmt(charge));
                }
                return;
            }

            // --- Air crit path ---
            if (!mc.player.isOnGround()) {
                String blocker = critBlocker();
                if (blocker == null && charge >= critChargeTarget) {
                    // И обычный, и W-reset режим бьют в ЭТОТ ЖЕ тик. Спринт
                    // снимается/возвращается внутри тика в doAttack -> нет лишних
                    // START/STOP_SPRINTING пакетов. wTap доп. гасит гориз. скорость.
                    if (fire(target, fwd, wTap.isValue())) {
                        diag(wTap.isValue() ? "CRIT_WTAP" : "CRIT_DIRECT", "CRIT charge=" + fmt(charge));
                    }
                } else {
                    diag("AIR_BLOCK", "block=" + blocker + " charge=" + fmt(charge));
                }
                return;
            }

            // --- Ground jump-crit gate ---
            if (this.isJumpHeld() || mc.player.getVelocity().y > 0.0) {
                if (wTap.isValue() && this.isJumpHeld() && charge < jumpCharge.getValue()) {
                    wantSuppressJump = true;
                    diag("JUMP_GATE", "JUMP gate charge=" + fmt(charge));
                } else {
                    diag("GROUND_HOLD", "hold for crit charge=" + fmt(charge));
                }
                return;
            }

            // --- Ground combo ---
            if (charge >= groundChargeTarget && ticksOnGround >= groundComboDelayTarget) {
                if (fire(target, fwd, false)) diag("COMBO", "COMBO charge=" + fmt(charge));
            } else {
                diag("GROUND_WAIT", "wait charge=" + fmt(charge) + " ticks=" + ticksOnGround);
            }

        } finally {
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    // Единая точка атаки: уважает случайную микро-паузу и пере-роллит
    // пороги/задержки после успешного удара.
    private boolean fire(Entity target, boolean wasForward, boolean scrub) {
        if (attackDelayTicks > 0) return false;
        doAttack(target, wasForward, scrub);
        rollTargets();
        return true;
    }

    // Пере-ролл случайных порогов и задержек. Масштаб -- слайдер "Humanize".
    private void rollTargets() {
        float r = clamp(randomness.getValue(), 0.0F, 1.0F);
        critChargeTarget       = clamp(CRIT_CHARGE          + rng.nextFloat() * (0.12F * r), 0.80F, 0.99F);
        groundChargeTarget     = clamp(GROUND_ATTACK_CHARGE + rng.nextFloat() * (0.06F * r), 0.80F, 1.0F);
        waterChargeTarget      = clamp(GROUND_ATTACK_CHARGE + rng.nextFloat() * (0.06F * r), 0.80F, 1.0F);
        noCritChargeTarget     = clamp(noCritCharge.getValue() + rng.nextFloat() * (0.10F * r), 0.0F, 1.0F);
        groundComboDelayTarget = GROUND_COMBO_DELAY + Math.round(rng.nextFloat() * (2.0F * r));
        attackDelayTicks       = Math.round(rng.nextFloat() * (3.0F * r));
    }

    private void doAttack(Entity target, boolean wasForward) {
        doAttack(target, wasForward, false);
    }

    private void doAttack(Entity target, boolean wasForward, boolean scrubVelocity) {
        // Снятие/возврат спринта в пределах ОДНОГО тика -> sendMovementPackets
        // не видит изменения и лишний START/STOP_SPRINTING пакет не уходит.
        boolean wasSprinting = mc.player.isSprinting();
        if (wasSprinting) {
            mc.player.setSprinting(false);
        }
        // W-reset: мягко гасим горизонтальную скорость в этот же тик (без пакетов).
        if (scrubVelocity) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * 0.2, v.y, v.z * 0.2);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (wasSprinting && wasForward && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }
    }

    private boolean canHit(Entity e) {
        return e instanceof LivingEntity
            && ((LivingEntity) e).isAlive()
            && !mc.player.isUsingItem()
            && isWeaponInHand();
    }

    private float charge() {
        return mc.player.getAttackCooldownProgress(0.0F);
    }

    private boolean isWeaponInHand() {
        Item item = mc.player.getMainHandStack().getItem();
        return item.getRegistryEntry().isIn(ItemTags.SWORDS)
            || item.getRegistryEntry().isIn(ItemTags.AXES)
            || item.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
    }

    private boolean isInWater() {
        return mc.player.isTouchingWater()
            || mc.player.isSubmergedInWater()
            || mc.player.isSwimming();
    }

    private boolean isJumpHeld() {
        try { return mc.options.jumpKey.isPressed(); }
        catch (Throwable t) { return false; }
    }

    private String critBlocker() {
        if (!(mc.player.fallDistance > 0.0))                    return "fall<=0";
        if (ticksOutOfWater < 3)                                return "justLeftWater";
        if (mc.player.isOnGround())                             return "onGround";
        if (mc.player.isClimbing())                             return "climbing";
        if (mc.player.isTouchingWater())                        return "water";
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)) return "levitation";
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS))  return "blindness";
        if (mc.player.hasVehicle())                             return "vehicle";
        if (mc.player.getAbilities().flying)                    return "flying";
        return null;
    }

    private boolean critAchievable() {
        return ticksOutOfWater >= 3
            && !mc.player.isTouchingWater()
            && !mc.player.isClimbing()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
    }

    private void diag(String key, String msg) {
        if (key.equals(lastDiag)) {
            return;
        }
        lastDiag = key;
        if (!DEBUG_LOGS) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastDiagLogMs < DEBUG_LOG_MIN_GAP_MS) {
            return;
        }
        this.lastDiagLogMs = now;
        LOG.info("[Triggerbot] " + msg);
    }

    private static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
