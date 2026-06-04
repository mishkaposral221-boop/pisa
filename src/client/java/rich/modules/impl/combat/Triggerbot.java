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
import rich.util.player.PlayerSimulation;

public class Triggerbot extends ModuleStructure {
    // \u041d\u0430 \u0437\u0435\u043c\u043b\u0435 = \u043a\u043e\u043c\u0431\u043e; \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 = \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u043e \u043f\u0440\u0435\u0434\u0441\u043a\u0430\u0437\u0430\u043d\u043d\u043e\u043c\u0443 \u0442\u0430\u0439\u043c\u0438\u043d\u0433\u0443 \u043a\u0440\u0438\u0442\u0430.
    public BooleanSetting combo = new BooleanSetting("Combo", "Combo on the ground; in the air only crit-timed hits").setValue(true);
    // \u0412\u041d\u0418\u041c\u0410\u041d\u0418\u0415: \u0441\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u0432\u044b\u0437\u044b\u0432\u0430\u0435\u0442 \u0440\u0443\u0431\u0431\u0435\u0440\u0431\u044d\u043d\u0434/\u043b\u0430\u0433\u0438 \u043d\u0430 \u0441\u0442\u0440\u043e\u0433\u043e\u043c \u0430\u043d\u0442\u0438\u0447\u0438\u0442\u0435. \u041f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e \u0432\u044b\u043a\u043b.
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Drop sprint on crit hit (WARNING: causes rubber-band on strict anticheat)").setValue(false);
    // \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0437\u0430\u0440\u044f\u0434.
    public SliderSettings attackCharge = new SliderSettings("AttackCharge", "Min attack charge before a hit").range(0.7F, 1.0F).setValue(0.95F);
    // \u041d\u0430 \u0441\u043a\u043e\u043b\u044c\u043a\u043e \u0442\u0438\u043a\u043e\u0432 \u0432\u043f\u0435\u0440\u0451\u0434 \u0441\u0438\u043c\u0443\u043b\u0438\u0440\u0443\u0435\u043c \u043f\u0430\u0434\u0435\u043d\u0438\u0435 \u0434\u043b\u044f \u043f\u043e\u0438\u0441\u043a\u0430 \u043e\u043a\u043d\u0430 \u043a\u0440\u0438\u0442\u0430.
    public SliderSettings lookahead = new SliderSettings("CritLookahead", "How many ticks to simulate ahead to time the crit").range(1.0F, 12.0F).setValue(6.0F);

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.combo, this.sprintReset, this.attackCharge, this.lookahead);
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

        // \u041e\u0442\u043a\u0430\u0442 \u0430\u0442\u0430\u043a\u0438.
        if (mc.player.getAttackCooldownProgress(0.0F) < this.attackCharge.getValue()) {
            return;
        }

        if (mc.player.isOnGround()) {
            // \u041d\u0430 \u0437\u0435\u043c\u043b\u0435 \u2014 \u043a\u043e\u043c\u0431\u043e.
            if (!this.combo.isValue()) {
                return;
            }
            this.attack(target, false);
            return;
        }

        // \u0412 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 \u2014 \u0430\u0434\u0430\u043f\u0442\u0438\u0432\u043d\u044b\u0439 \u0442\u0430\u0439\u043c\u0438\u043d\u0433 \u043a\u0440\u0438\u0442\u0430 (\u0441\u0438\u043c\u0443\u043b\u044f\u0446\u0438\u044f \u043f\u0430\u0434\u0435\u043d\u0438\u044f).
        int max = Math.round(this.lookahead.getValue());
        int firstCritTick = -1;
        for (int t = 0; t <= max; t++) {
            if (this.willBeCritInTicks(t)) {
                firstCritTick = t;
                break;
            }
        }
        if (firstCritTick < 0) {
            return;
        }
        if (firstCritTick > 0) {
            // \u041a\u0440\u0438\u0442 \u0431\u0443\u0434\u0435\u0442 \u0441\u043a\u043e\u0440\u043e \u2014 \u0436\u0434\u0451\u043c \u043d\u0443\u0436\u043d\u044b\u0439 \u0442\u0438\u043a.
            return;
        }
        // \u043a\u0440\u0438\u0442 \u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d \u0441\u0435\u0439\u0447\u0430\u0441.
        this.attack(target, true);
    }

    private void attack(Entity target, boolean crit) {
        // \u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u0422\u041e\u041b\u042c\u041a\u041e \u0435\u0441\u043b\u0438 \u044f\u0432\u043d\u043e \u0432\u043a\u043b\u044e\u0447\u0451\u043d (\u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e \u0432\u044b\u043a\u043b \u2014 \u043d\u0438\u043a\u0430\u043a\u0438\u0445 \u043f\u043e\u0431\u043e\u0447\u043a\u0438 \u043d\u0430 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0435).
        boolean reset = crit && this.sprintReset.isValue() && mc.player.isSprinting() && this.canResetSprint();
        if (reset) {
            mc.player.setSprinting(false);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (reset) {
            mc.player.setSprinting(true);
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

    // \u041f\u0440\u0435\u0434\u0441\u043a\u0430\u0437\u0430\u043d\u0438\u0435 \u043a\u0440\u0438\u0442\u0430 \u0447\u0435\u0440\u0435\u0437 \u0441\u0438\u043c\u0443\u043b\u044f\u0446\u0438\u044e \u0444\u0438\u0437\u0438\u043a\u0438.
    private boolean willBeCritInTicks(int ticks) {
        if (ticks == 0) {
            return this.isPerfectCrit();
        }
        PlayerSimulation sim = PlayerSimulation.simulateLocalPlayer(ticks);
        return sim.fallDistance > 0.0F
            && !sim.onGround
            && sim.velocity.y <= 0.0
            && !sim.isClimbing()
            && !sim.player.isTouchingWater()
            && !sim.hasStatusEffect(StatusEffects.BLINDNESS)
            && !sim.player.hasVehicle()
            && !sim.player.getAbilities().flying;
    }

    private boolean canResetSprint() {
        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
            return false;
        }
        return !mc.player.isGliding();
    }
}
