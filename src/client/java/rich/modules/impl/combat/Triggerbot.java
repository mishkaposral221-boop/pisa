package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import rich.events.api.EventHandler;
import rich.events.impl.ClientTickStartEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    // \u041d\u0430 \u0437\u0435\u043c\u043b\u0435 = \u043a\u043e\u043c\u0431\u043e; \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 = \u0431\u044c\u0451\u043c \u0432 \u043f\u0435\u0440\u0432\u044b\u0439 \u0436\u0435 \u0442\u0438\u043a \u043a\u0440\u0438\u0442\u0430.
    public BooleanSetting combo = new BooleanSetting("Combo", "Combo on the ground; in the air hit on the first crit tick").setValue(true);
    // \u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442 \u043a\u0440\u0438\u0442\u0430: \u043f\u043e\u043a\u0430 \u0437\u0430\u0436\u0430\u0442 \u043f\u0440\u044b\u0436\u043e\u043a \u2014 \u043d\u0435 \u043a\u043e\u043c\u0431\u0438\u043c \u043d\u0430 \u0437\u0435\u043c\u043b\u0435 (\u043d\u0435 \u0441\u0431\u0438\u0432\u0430\u0435\u043c \u043a\u0440\u0438\u0442).
    public BooleanSetting critPriority = new BooleanSetting("CritPriority", "While jump is held, don't waste the hit on a ground combo; save it for the crit").setValue(true);
    // \u0423\u043c\u043d\u044b\u0439 \u0441\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u043f\u043e \u0432\u0432\u043e\u0434\u0443.
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Release sprint for the crit, restore it only if the sprint key is still held").setValue(true);
    // \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0437\u0430\u0440\u044f\u0434.
    public SliderSettings attackCharge = new SliderSettings("AttackCharge", "Min attack charge before a hit").range(0.7F, 1.0F).setValue(0.95F);
    // \u0416\u0451\u0441\u0442\u043a\u0438\u0439 \u043f\u043e\u043b \u043c\u0435\u0436\u0434\u0443 \u0443\u0434\u0430\u0440\u0430\u043c\u0438 (\u043c\u0441) \u2014 \u0430\u043d\u0442\u0438-\u0441\u043f\u0430\u043c \u043f\u0430\u043a\u0435\u0442\u043e\u0432.
    public SliderSettings minDelay = new SliderSettings("MinDelayMs", "Hard floor between hits in ms (anti packet-spam)").range(0.0F, 1000.0F).setValue(300.0F);

    private long lastAttackMs = 0L;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.combo, this.critPriority, this.sprintReset, this.attackCharge, this.minDelay);
    }

    @EventHandler
    public void onTick(ClientTickStartEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mc.currentScreen != null) {
            return;
        }
        if (mc.player.isUsingItem()) {
            return;
        }
        Item mainItem = mc.player.getMainHandStack().getItem();
        boolean isWeapon = mainItem.getRegistryEntry().isIn(ItemTags.SWORDS)
            || mainItem.getRegistryEntry().isIn(ItemTags.AXES)
            || mainItem.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
        if (!isWeapon) {
            return;
        }
        Entity target = mc.targetedEntity;
        if (target == null || !(target instanceof LivingEntity)) {
            return;
        }

        // \u0416\u0451\u0441\u0442\u043a\u0438\u0439 \u0430\u043d\u0442\u0438-\u0441\u043f\u0430\u043c: \u043d\u0435 \u0447\u0430\u0449\u0435 \u043e\u0434\u043d\u043e\u0433\u043e \u0443\u0434\u0430\u0440\u0430 \u0432 minDelay \u043c\u0441.
        if (System.currentTimeMillis() - this.lastAttackMs < this.minDelay.getValue()) {
            return;
        }

        // \u041e\u0442\u043a\u0430\u0442 \u0430\u0442\u0430\u043a\u0438.
        if (mc.player.getAttackCooldownProgress(0.0F) < this.attackCharge.getValue()) {
            return;
        }

        if (mc.player.isOnGround()) {
            // \u041d\u0430 \u0437\u0435\u043c\u043b\u0435 \u2014 \u043a\u043e\u043c\u0431\u043e.
            if (!this.combo.isValue()) {
                return;
            }
            // \u041f\u0440\u0438\u043e\u0440\u0438\u0442\u0435\u0442 \u043a\u0440\u0438\u0442\u0430: \u0435\u0441\u043b\u0438 \u0438\u0433\u0440\u043e\u043a \u0434\u0435\u0440\u0436\u0438\u0442 \u043f\u0440\u044b\u0436\u043e\u043a \u2014 \u043d\u0435 \u0442\u0440\u0430\u0442\u0438\u043c \u0443\u0434\u0430\u0440 \u043d\u0430 \u043a\u043e\u043c\u0431\u043e, \u0436\u0434\u0451\u043c \u043a\u0440\u0438\u0442 \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435.
            if (this.critPriority.isValue() && this.isJumpHeld()) {
                return;
            }
            this.attack(target, false);
            return;
        }

        // \u0412 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 \u2014 \u0431\u044c\u0451\u043c \u0442\u043e\u043b\u044c\u043a\u043e \u043a\u043e\u0433\u0434\u0430 \u0440\u0435\u0430\u043b\u044c\u043d\u043e \u0432 \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0438 \u043a\u0440\u0438\u0442\u0430 (\u043f\u0430\u0434\u0430\u044e + fallDistance).
        if (!this.isPerfectCrit()) {
            return;
        }
        this.attack(target, true);
    }

    private void attack(Entity target, boolean crit) {
        // \u0423\u043c\u043d\u044b\u0439 \u0441\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u043f\u043e \u0432\u0432\u043e\u0434\u0443: \u0437\u0430\u043f\u043e\u043c\u0438\u043d\u0430\u0435\u043c, \u0434\u0435\u0440\u0436\u0438\u0442 \u043b\u0438 \u0438\u0433\u0440\u043e\u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0431\u0435\u0433\u0430 \u0414\u041e \u0443\u0434\u0430\u0440\u0430.
        boolean sprintHeld = this.isSprintHeld();
        boolean reset = crit && this.sprintReset.isValue() && mc.player.isSprinting() && this.canResetSprint();
        if (reset) {
            // \u041e\u0442\u0436\u0438\u043c\u0430\u0435\u043c \u0441\u043f\u0440\u0438\u043d\u0442 \u0440\u043e\u0432\u043d\u043e \u043d\u0430 \u0443\u0434\u0430\u0440 \u2014 \u0438\u043d\u0430\u0447\u0435 \u043a\u0440\u0438\u0442 \u043d\u0435 \u043f\u0440\u043e\u0439\u0434\u0451\u0442.
            mc.player.setSprinting(false);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        // \u0412\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u0435\u043c \u0441\u043f\u0440\u0438\u043d\u0442 \u0422\u041e\u041b\u042c\u041a\u041e \u0435\u0441\u043b\u0438 \u0438\u0433\u0440\u043e\u043a \u0432\u0441\u0451 \u0435\u0449\u0451 \u0434\u0435\u0440\u0436\u0438\u0442 \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u0431\u0435\u0433\u0430 (\u043d\u0435 \u043d\u0430\u0432\u044f\u0437\u044b\u0432\u0430\u0435\u043c \u043f\u0440\u043e\u0442\u0438\u0432 \u0432\u0432\u043e\u0434\u0430).
        if (reset && sprintHeld) {
            mc.player.setSprinting(true);
        }
        this.lastAttackMs = System.currentTimeMillis();
    }

    private boolean isJumpHeld() {
        try {
            return mc.player.input.playerInput.jump();
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean isSprintHeld() {
        try {
            return mc.player.input.playerInput.sprint();
        } catch (Throwable t) {
            return mc.player.isSprinting();
        }
    }

    private boolean isPerfectCrit() {
        return mc.player.fallDistance > 0.0F
            && !mc.player.isOnGround()
            && !mc.player.isClimbing()
            && !mc.player.isTouchingWater()
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
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
