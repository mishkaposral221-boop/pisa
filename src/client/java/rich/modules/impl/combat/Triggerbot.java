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
    // \u041d\u0430 \u0437\u0435\u043c\u043b\u0435 = \u043a\u043e\u043c\u0431\u043e; \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 = \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u043e \u0442\u0430\u0439\u043c\u0438\u043d\u0433\u0443 \u043a\u0440\u0438\u0442\u0430 (\u043d\u0435 \u043c\u0435\u0448\u0430\u044e\u0442 \u0434\u0440\u0443\u0433 \u0434\u0440\u0443\u0433\u0443).
    public BooleanSetting combo = new BooleanSetting("Combo", "Combo on the ground; in the air only crit-timed hits").setValue(true);
    // \u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u0447\u0435\u0440\u0435\u0437 mc.player (\u043a\u0430\u043a \u0432 \u0430\u0443\u0440\u0435) \u2014 \u0411\u0415\u0417 raw-\u043f\u0430\u043a\u0435\u0442\u043e\u0432, \u043f\u043e\u044d\u0442\u043e\u043c\u0443 \u043d\u0435\u0442 bad packet.
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Drop sprint via mc.player around an airborne hit so the crit lands (no raw packets)").setValue(true);
    // \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0437\u0430\u0440\u044f\u0434 (\u0430\u0443\u0440\u0430 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442 0.95).
    public SliderSettings attackCharge = new SliderSettings("AttackCharge", "Min attack charge before a hit (aura uses 0.95)").range(0.7F, 1.0F).setValue(0.95F);

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.combo, this.sprintReset, this.attackCharge);
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

        // \u041e\u0442\u043a\u0430\u0442 \u0430\u0442\u0430\u043a\u0438 (\u043a\u0430\u043a \u0432 \u0430\u0443\u0440\u0435).
        if (mc.player.getAttackCooldownProgress(0.0F) < this.attackCharge.getValue()) {
            return;
        }

        boolean onGround = mc.player.isOnGround();
        double velY = mc.player.getVelocity().y;
        boolean ascending = !onGround && velY > 0.0;
        boolean falling = !onGround && velY <= 0.0 && mc.player.fallDistance > 0.0F;

        if (onGround) {
            // \u041d\u0430 \u0437\u0435\u043c\u043b\u0435 \u2014 \u043a\u043e\u043c\u0431\u043e.
            if (!this.combo.isValue()) {
                return;
            }
        } else {
            // \u0412 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 \u2014 \u043d\u0438\u043a\u043e\u0433\u0434\u0430 \u043d\u0430 \u0432\u0437\u043b\u0451\u0442\u0435, \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043f\u0430\u0434\u0435\u043d\u0438\u0438 (\u043a\u0440\u0438\u0442). \u0420\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0438 \u043f\u0440\u0438 \u043f\u043b\u0430\u0432\u043d\u043e\u043c \u043f\u0430\u0434\u0435\u043d\u0438\u0438.
            if (ascending || !falling) {
                return;
            }
        }

        // \u0412\u0430\u043d\u0438\u043b\u044c\u043d\u043e\u0435 \u0443\u0441\u043b\u043e\u0432\u0438\u0435 \u043a\u0440\u0438\u0442\u0430 (\u0438\u0437 \u0430\u0443\u0440\u044b).
        boolean perfectCrit = mc.player.fallDistance > 0.0F
            && !onGround
            && !mc.player.isClimbing()
            && !mc.player.isTouchingWater()
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying;

        // \u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u0447\u0435\u0440\u0435\u0437 mc.player (\u043a\u0430\u043a \u0432 \u0430\u0443\u0440\u0435), \u0431\u0435\u0437 raw-\u043f\u0430\u043a\u0435\u0442\u043e\u0432: setSprinting(false) -> \u0443\u0434\u0430\u0440 -> setSprinting(true).
        boolean reset = this.sprintReset.isValue() && perfectCrit && mc.player.isSprinting() && this.canResetSprint();
        if (reset) {
            mc.player.setSprinting(false);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (reset) {
            mc.player.setSprinting(true);
        }
    }

    // \u041d\u0435 \u0442\u0440\u043e\u0433\u0430\u0435\u043c \u0441\u043f\u0440\u0438\u043d\u0442 \u0432 \u0432\u043e\u0434\u0435/\u043f\u043e\u043b\u0451\u0442\u0435.
    private boolean canResetSprint() {
        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
            return false;
        }
        return !mc.player.isGliding();
    }
}
