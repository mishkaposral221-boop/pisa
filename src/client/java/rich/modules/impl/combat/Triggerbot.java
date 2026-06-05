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
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class Triggerbot extends ModuleStructure {

    public static volatile boolean SUPPRESS_SPRINT = false;
    public static volatile boolean SUPPRESS_JUMP = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");
    private String lastDiag = "";

    public SliderSettings noCritCharge = new SliderSettings("Charge under debuff",
            "Min weapon charge to hit when crit is impossible")
            .setValue(0.80F).range(0.3F, 1.0F);

    public SliderSettings jumpCharge = new SliderSettings("Jump charge",
            "Min charge before held jump fires")
            .setValue(0.55F).range(0.3F, 1.0F);

    private int ticksOutOfWater = 10;
    private int ticksOnGround = 0;
    private int cleanTicks = 0;

    private static final int GROUND_COMBO_DELAY = 3;
    private static final float GROUND_ATTACK_CHARGE = 1.0F;
    private static final float CRIT_CHARGE = 0.9F;

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
        this.ticksOutOfWater = 0;
        this.ticksOnGround = 0;
        this.cleanTicks = 0;
        this.lastDiag = "";
    }

    @EventHandler
    public void onTick(TickEvent event) {
        boolean wantSuppressJump = false;
        try {
            if (mc.player == null || mc.world == null || mc.currentScreen != null) {
                this.ticksOnGround = 0;
                this.cleanTicks = 0;
                return;
            }

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

            boolean sprinting = mc.player.isSprinting();
            if (!sprinting) {
                if (this.cleanTicks < 100) this.cleanTicks++;
            } else {
                this.cleanTicks = 0;
            }

            Entity target = mc.targetedEntity;
            if (!this.canHit(target) || mc.player.isUsingItem() || !this.isWeaponInHand()) {
                return;
            }

            boolean critPossible = this.critAchievable();
            float charge = this.charge();

            if (this.isInWater()) {
                if (charge >= GROUND_ATTACK_CHARGE) {
                    this.attack(target);
                    LOG.info("[Triggerbot] HIT water charge=" + fmt(charge));
                }
                return;
            }

            if (!critPossible) {
                float need = this.noCritCharge.getValue();
                if (charge >= need) {
                    this.attack(target);
                    LOG.info("[Triggerbot] HIT no-crit charge=" + fmt(charge)
                        + " blocker=" + this.critBlocker());
                } else {
                    this.diag("NOCRIT_WAIT", "NOCRIT wait charge=" + fmt(charge)
                        + " need=" + fmt(need));
                }
                return;
            }

            if (!mc.player.isOnGround()) {
                String blocker = this.critBlocker();
                if (blocker == null && charge >= CRIT_CHARGE) {
                    this.attack(target);
                    LOG.info("[Triggerbot] CRIT air charge=" + fmt(charge)
                        + " fall=" + fmt(mc.player.fallDistance));
                } else {
                    this.diag("AIR_BLOCK", "AIR blocked=" + blocker
                        + " charge=" + fmt(charge));
                }
                return;
            }

            if (critPossible && (this.isJumpHeld() || mc.player.getVelocity().y > 0.0)) {
                if (this.isJumpHeld() && charge < this.jumpCharge.getValue()) {
                    wantSuppressJump = true;
                    this.diag("JUMP_GATE", "JUMP gated charge=" + fmt(charge));
                } else {
                    this.diag("GROUND_HOLD", "GROUND hold-for-crit charge=" + fmt(charge));
                }
                return;
            }

            if (charge >= GROUND_ATTACK_CHARGE && this.ticksOnGround >= GROUND_COMBO_DELAY) {
                this.attack(target);
                LOG.info("[Triggerbot] COMBO ground charge=" + fmt(charge)
                    + " ticks=" + this.ticksOnGround);
            } else if (charge < GROUND_ATTACK_CHARGE) {
                this.diag("GROUND_CHARGE", "GROUND wait charge=" + fmt(charge));
            } else {
                this.diag("GROUND_DELAY", "GROUND wait ticks=" + this.ticksOnGround);
            }
        } finally {
            SUPPRESS_SPRINT = false;
            SUPPRESS_JUMP = wantSuppressJump;
        }
    }

    private void attack(Entity target) {
        boolean wasSprinting = mc.player.isSprinting();
        if (wasSprinting) {
            mc.player.setSprinting(false);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (wasSprinting && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }
    }

    private boolean canHit(Entity e) {
        return !mc.player.isUsingItem() && this.isWeaponInHand()
            && e instanceof LivingEntity && ((LivingEntity) e).isAlive();
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
        return mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming();
    }

    private boolean isJumpHeld() {
        try {
            return mc.options.jumpKey.isPressed();
        } catch (Throwable t) {
            return false;
        }
    }

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

    private boolean critAchievable() {
        return this.ticksOutOfWater >= 3
            && !mc.player.isTouchingWater()
            && !mc.player.isClimbing()
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;
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
}
