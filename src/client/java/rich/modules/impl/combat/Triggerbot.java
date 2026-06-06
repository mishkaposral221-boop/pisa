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

/**
 * Triggerbot — автоатака при наведении прицела на LivingEntity.
 *
 * Антидетект:
 *  - Gaussian-задержка перед атакой (имитация реакции)
 *  - Случайный пропуск атаки (~5%)
 *  - W-release для air-критов
 *  - Настройка заряда оружия
 */
public class Triggerbot extends ModuleStructure {

    // Флаги для миксина (KeyboardInputMixin / ClientPlayerInteractionManagerMixin)
    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    // ─── Настройки ─────────────────────────────────────────────────────────────
    public SliderSettings groundCharge = new SliderSettings("Ground charge",
            "Мин. заряд для удара на земле")
            .setValue(0.93F).range(0.5F, 1.0F);

    public SliderSettings critCharge = new SliderSettings("Air crit charge",
            "Мин. заряд для воздушного крита")
            .setValue(0.84F).range(0.5F, 1.0F);

    public SliderSettings noCritCharge = new SliderSettings("No-crit charge",
            "Мин. заряд при невозможности крита")
            .setValue(0.78F).range(0.3F, 1.0F);

    public SliderSettings jumpCharge = new SliderSettings("Jump charge",
            "Мин. заряд перед прыжком")
            .setValue(0.50F).range(0.3F, 1.0F);

    /** Задержка реакции: min..max мс перед каждой атакой (Gaussian внутри диапазона) */
    public SliderSettings reactionMin = new SliderSettings("Reaction min ms",
            "Мин. задержка реакции (мс)")
            .setValue(40F).range(0F, 200F);
    public SliderSettings reactionMax = new SliderSettings("Reaction max ms",
            "Макс. задержка реакции (мс)")
            .setValue(110F).range(0F, 300F);

    /** Случайный пропуск атак — снижает CPS до human-like */
    public SliderSettings missChance = new SliderSettings("Miss chance %",
            "Шанс пропуска удара (антидетект)")
            .setValue(5F).range(0F, 30F);

    public BooleanSetting airCrit = new BooleanSetting("Air crit",
            "W-release для воздушных критов").setValue(true);

    // ─── Состояние ─────────────────────────────────────────────────────────────
    private Entity  pendingTarget      = null;
    private int     preAttackCountdown = 0;
    private boolean pendingWasForward  = false;

    /** Тик, с которого началась задержка реакции (-1 = нет ожидания) */
    private long    reactionStartTick  = -1;
    /** Длительность текущей задержки реакции в тиках */
    private int     reactionDurationTicks = 0;
    /** Цель, на которую мы «реагируем» */
    private Entity  reactionTarget     = null;

    private int  ticksOutOfWater = 10;
    private int  ticksOnGround   = 0;
    private static final int GROUND_COMBO_DELAY = 2;

    private final java.util.Random rng = new java.util.Random();

    public Triggerbot() {
        super("Triggerbot", "Авто-удар по цели в прицеле", ModuleCategory.COMBAT);
        this.settings(groundCharge, critCharge, noCritCharge, jumpCharge,
                      reactionMin, reactionMax, missChance, airCrit);
    }

    public static Triggerbot getInstance() { return c.a(Triggerbot.class); }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_FORWARD = false;
        SUPPRESS_SPRINT  = false;
        SUPPRESS_JUMP    = false;
        pendingTarget    = null;
        reactionTarget   = null;
        reactionStartTick = -1;
        ticksOnGround = 0; ticksOutOfWater = 0;
    }

    // ─── Основной тик ─────────────────────────────────────────────────────────
    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppressForward = false;
        boolean wantSuppressJump    = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                ticksOnGround = 0; pendingTarget = null; reactionTarget = null;
                reactionStartTick = -1; return;
            }

            // Счётчики среды
            if (isInWater()) ticksOutOfWater = 0;
            else if (ticksOutOfWater < 100) ticksOutOfWater++;
            if (mc.player.isOnGround()) { if (ticksOnGround < 100) ticksOnGround++; }
            else ticksOnGround = 0;

            long curTick = mc.world.getTime();

            // ── Pending air-crit (W-release + 1 тик) ──
            if (pendingTarget != null) {
                if (!isEntityValid(pendingTarget)) {
                    pendingTarget = null; preAttackCountdown = 0; return;
                }
                if (preAttackCountdown > 0) {
                    preAttackCountdown--; wantSuppressForward = true; return;
                }
                Entity t = pendingTarget; boolean fw = pendingWasForward;
                pendingTarget = null; preAttackCountdown = 0;
                doAttack(t, fw);
                return;
            }

            // ── Определяем цель ──
            Entity target = mc.targetedEntity;
            if (!canHit(target)) {
                reactionTarget = null; reactionStartTick = -1; return;
            }

            // ── Задержка реакции ──
            // При смене цели (или первом обнаружении) — рандомная пауза
            if (target != reactionTarget) {
                reactionTarget = target;
                reactionStartTick = curTick;
                // Gaussian между reactionMin и reactionMax (мс → тики ~20мс/тик)
                float minMs = reactionMin.getValue();
                float maxMs = reactionMax.getValue();
                float meanMs  = (minMs + maxMs) * 0.5f;
                float sigmaMs = (maxMs - minMs) * 0.25f;
                float delayMs = (float) Math.max(minMs,
                        Math.min(maxMs, meanMs + rng.nextGaussian() * sigmaMs));
                reactionDurationTicks = Math.max(0, Math.round(delayMs / 50f));
            }
            if (reactionStartTick >= 0 && (curTick - reactionStartTick) < reactionDurationTicks)
                return;

            // ── Случайный пропуск (miss chance) ──
            if (rng.nextFloat() * 100f < missChance.getValue()) return;

            boolean critPossible = critAchievable();
            float   charge       = charge();

            // ── Вода ──
            if (isInWater()) {
                if (charge >= groundCharge.getValue()) doAttack(target, mc.options.forwardKey.isPressed());
                return;
            }

            // ── Крит невозможен ──
            if (!critPossible) {
                if (charge >= noCritCharge.getValue()) doAttack(target, mc.options.forwardKey.isPressed());
                return;
            }

            // ── Air-crit (в воздухе) ──
            if (!mc.player.isOnGround()) {
                if (airCrit.isValue() && critBlocker() == null && charge >= critCharge.getValue()) {
                    pendingTarget     = target;
                    preAttackCountdown = 0;
                    pendingWasForward = mc.options.forwardKey.isPressed();
                    wantSuppressForward = true;
                } // иначе ждём следующий тик
                return;
            }

            // ── Земля: jump-crit gate ──
            if (isJumpHeld() || mc.player.getVelocity().y > 0.0) {
                if (isJumpHeld() && charge < jumpCharge.getValue())
                    wantSuppressJump = true;
                return;
            }

            // ── Ground combo ──
            if (charge >= groundCharge.getValue() && ticksOnGround >= GROUND_COMBO_DELAY)
                doAttack(target, mc.options.forwardKey.isPressed());

        } finally {
            SUPPRESS_FORWARD = wantSuppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = wantSuppressJump;
        }
    }

    // ─── Атака ────────────────────────────────────────────────────────────────
    private void doAttack(Entity target, boolean wasForward) {
        mc.player.setSprinting(false);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        // Возобновляем спринт только если W всё ещё зажат
        if (wasForward && mc.options.forwardKey.isPressed())
            mc.player.setSprinting(true);
        // Сбрасываем задержку реакции (следующая атака — новая пауза)
        reactionStartTick = -1;
        reactionTarget    = null;
    }

    // ─── Хелперы ──────────────────────────────────────────────────────────────
    private boolean canHit(Entity e) {
        return e instanceof LivingEntity le
            && le.isAlive()
            && !mc.player.isUsingItem()
            && isWeaponInHand();
    }

    private boolean isEntityValid(Entity e) {
        return e instanceof LivingEntity le
            && le.isAlive()
            && mc.world != null
            && mc.world.getEntityById(e.getId()) != null;
    }

    private float charge() { return mc.player.getAttackCooldownProgress(0.0F); }

    private boolean isWeaponInHand() {
        Item i = mc.player.getMainHandStack().getItem();
        return i.getRegistryEntry().isIn(ItemTags.SWORDS)
            || i.getRegistryEntry().isIn(ItemTags.AXES)
            || i.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
    }

    private boolean isInWater() {
        return mc.player.isTouchingWater()
            || mc.player.isSubmergedInWater()
            || mc.player.isSwimming();
    }

    private boolean isJumpHeld() {
        try { return mc.options.jumpKey.isPressed(); } catch (Throwable t) { return false; }
    }

    private String critBlocker() {
        if (!(mc.player.fallDistance > 0.0))                      return "fall<=0";
        if (ticksOutOfWater < 3)                                  return "justLeftWater";
        if (mc.player.isOnGround())                               return "onGround";
        if (mc.player.isClimbing())                               return "climbing";
        if (mc.player.isTouchingWater())                          return "water";
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION))  return "levitation";
        if (mc.player.hasStatusEffect(StatusEffects.BLINDNESS))   return "blindness";
        if (mc.player.hasVehicle())                               return "vehicle";
        if (mc.player.getAbilities().flying)                      return "flying";
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
}
