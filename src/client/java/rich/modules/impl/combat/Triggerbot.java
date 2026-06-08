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
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

/**
 * Triggerbot: auto-attacks targeted living entities.
 *
 * W-release mechanic (air crit) -- ВКЛЮЧАЕТСЯ ТОЛЬКО при wTap=true:
 *   Tick N   : crit ready -> save target, suppress W (SUPPRESS_FORWARD=true), return
 *   Tick N   : onInputTick clears forward/sprint from playerInput
 *   Tick N   : sendMovementPackets sends pos with W=false, sprint=false
 *   Tick N+1 : pendingTarget set, countdown=0 -> attack(), then resume W
 *
 * По умолчанию (wTap=false) триггербот НЕ вмешивается в пакеты движения:
 * это убирает рассинхрон/руббербэндинг на сервере. Крит обеспечивается
 * одиночным снятием спринта прямо в момент удара (см. doAttack).
 *
 * Charge thresholds (lowered for faster hitting):
 *   GROUND_ATTACK_CHARGE  0.93  (was 1.0)  - hit ground targets at 93%
 *   CRIT_CHARGE           0.84  (was 0.9)  - hit air crits at 84%
 *   GROUND_COMBO_DELAY    2     (was 3)    - 1 tick less combo delay
 */
public class Triggerbot extends ModuleStructure {

    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    private static final boolean DEBUG_LOGS = Boolean.getBoolean("rich.debug.triggerbot");
    private static final long DEBUG_LOG_MIN_GAP_MS = 1000L;

    private Entity  pendingTarget      = null;
    private int     preAttackCountdown = 0;
    private boolean pendingWasForward  = false;

    private String lastDiag = "";
    private long lastDiagLogMs = 0L;
    private int ticksOutOfWater = 10;
    private int ticksOnGround   = 0;

    // --- Timing constants ---
    // Lower = hits sooner in the cooldown cycle. Don't go below ~0.80 or
    // the server will register 0 damage (weapon not ready).
    private static final int   GROUND_COMBO_DELAY   = 2;    // ticks on ground before ground combo
    private static final float GROUND_ATTACK_CHARGE = 0.93F; // ground attack threshold (was 1.0)
    private static final float CRIT_CHARGE          = 0.84F; // air-crit threshold     (was 0.9)

    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff",
            "Min weapon charge when crit is impossible")
            .setValue(0.78F).range(0.0F, 1.0F);

    public SliderSettings jumpCharge = new SliderSettings("Jump charge",
            "Min charge before held-jump fires")
            .setValue(0.50F).range(0.0F, 1.0F);

    // Когда выключено (по умолчанию) -- триггербот НЕ трогает ввод движения
    // (не гасит W / спринт / прыжок). Это убирает рассинхрон с сервером.
    // Когда включено -- возвращается агрессивная механика "W-release",
    // которая может вызывать откаты позиции (rubber-band) на сервере.
    public BooleanSetting wTap = new BooleanSetting("W-tap crits",
            "Гасит W/спринт перед ударом ради крита. Может вызывать рассинхрон на сервере")
            .setValue(false);

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.noCritCharge, this.jumpCharge, this.wTap);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_FORWARD = false;
        SUPPRESS_SPRINT  = false;
        SUPPRESS_JUMP    = false;
        pendingTarget    = null;
        preAttackCountdown = 0;
        ticksOutOfWater  = 0;
        ticksOnGround    = 0;
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
                pendingTarget = null;
                return;
            }

            if (isInWater()) { ticksOutOfWater = 0; }
            else if (ticksOutOfWater < 100) { ticksOutOfWater++; }

            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else { ticksOnGround = 0; }

            // --- Pending pre-attack (W release + 1 tick delay for crit) ---
            // Достижимо только в режиме wTap (см. ветку air-crit ниже).
            if (pendingTarget != null) {
                if (!isEntityValid(pendingTarget)) {
                    pendingTarget = null;
                    preAttackCountdown = 0;
                    return;
                }
                if (preAttackCountdown > 0) {
                    preAttackCountdown--;
                    wantSuppressForward = true;
                    return;
                }
                Entity t = pendingTarget;
                boolean wasForward = pendingWasForward;
                pendingTarget = null;
                preAttackCountdown = 0;
                doAttack(t, wasForward);
                diag("CRIT_FIRE", "CRIT FIRE fw=" + wasForward);
                return;
            }

            // --- Normal detection ---
            Entity target = mc.targetedEntity;
            if (!canHit(target)) return;

            boolean critPossible = critAchievable();
            float charge = charge();

            if (isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    doAttack(target, mc.options.forwardKey.isPressed());
                    diag("WATER", "HIT water");
                }
                return;
            }

            if (!critPossible) {
                float need = noCritCharge.getValue();
                if (charge >= need) {
                    doAttack(target, mc.options.forwardKey.isPressed());
                    diag("NOCRIT", "NOCRIT hit charge=" + fmt(charge));
                } else {
                    diag("NOCRIT_WAIT", "NOCRIT wait charge=" + fmt(charge));
                }
                return;
            }

            // --- Air crit path ---
            if (!mc.player.isOnGround()) {
                String blocker = critBlocker();
                if (blocker == null && charge >= CRIT_CHARGE) {
                    if (wTap.isValue()) {
                        // Старая механика: гасим W на 1 тик, потом бьём.
                        pendingTarget      = target;
                        preAttackCountdown = 0;
                        pendingWasForward  = mc.options.forwardKey.isPressed();
                        wantSuppressForward = true;
                        diag("CRIT_Q", "CRIT queue charge=" + fmt(charge));
                    } else {
                        // Анти-рассинхрон: не трогаем движение. Спринт снимается
                        // прямо в doAttack -- этого достаточно для крита.
                        doAttack(target, mc.options.forwardKey.isPressed());
                        diag("CRIT_DIRECT", "CRIT direct charge=" + fmt(charge));
                    }
                } else {
                    diag("AIR_BLOCK", "block=" + blocker + " charge=" + fmt(charge));
                }
                return;
            }

            // --- Ground jump-crit gate ---
            if (this.isJumpHeld() || mc.player.getVelocity().y > 0.0) {
                if (wTap.isValue() && this.isJumpHeld() && charge < jumpCharge.getValue()) {
                    // Подавление прыжка только в агрессивном режиме.
                    wantSuppressJump = true;
                    diag("JUMP_GATE", "JUMP gate charge=" + fmt(charge));
                } else {
                    diag("GROUND_HOLD", "hold for crit charge=" + fmt(charge));
                }
                return;
            }

            // --- Ground combo ---
            if (charge >= GROUND_ATTACK_CHARGE && ticksOnGround >= GROUND_COMBO_DELAY) {
                doAttack(target, mc.options.forwardKey.isPressed());
                diag("COMBO", "COMBO charge=" + fmt(charge));
            } else {
                diag("GROUND_WAIT", "wait charge=" + fmt(charge) + " ticks=" + ticksOnGround);
            }

        } finally {
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    private void doAttack(Entity target, boolean wasForward) {
        // Снимаем спринт только если реально спринтовали: крит требует
        // отсутствия спринта в момент удара, но дёргать состояние впустую
        // (лишние ENTITY_ACTION пакеты) не нужно.
        boolean wasSprinting = mc.player.isSprinting();
        if (wasSprinting) {
            mc.player.setSprinting(false);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (wasSprinting && wasForward && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }
    }

    private boolean isEntityValid(Entity e) {
        return e instanceof LivingEntity
            && ((LivingEntity) e).isAlive()
            && mc.world != null
            && mc.world.getEntityById(e.getId()) != null;
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

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}
