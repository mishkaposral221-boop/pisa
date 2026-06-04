package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
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
    // Combo: \u0431\u044c\u0451\u0442 \u043d\u0430 \u043f\u043e\u043b\u043d\u043e\u043c \u0437\u0430\u043c\u0430\u0445\u0435 \u0432\u0441\u0451 \u0432\u0440\u0435\u043c\u044f (\u0438 \u043d\u0430 \u0437\u0435\u043c\u043b\u0435), \u0430 \u0432 \u0444\u0430\u0437\u0435 \u043f\u0430\u0434\u0435\u043d\u0438\u044f \u0442\u043e\u0442 \u0436\u0435 \u0443\u0434\u0430\u0440 \u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u0441\u044f \u043a\u0440\u0438\u0442\u043e\u043c.
    public BooleanSetting combo = new BooleanSetting("Combo", "Keep attacking at full charge even on the ground; crits land automatically while falling").setValue(true);
    // SprintReset: \u043e\u0434\u0438\u043d STOP_SPRINTING \u043f\u0435\u0440\u0435\u0434 \u0443\u0434\u0430\u0440\u043e\u043c \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435, \u0447\u0442\u043e\u0431\u044b \u0441\u0435\u0440\u0432\u0435\u0440 \u0437\u0430\u0441\u0447\u0438\u0442\u0430\u043b \u043a\u0440\u0438\u0442. \u0421\u043f\u0440\u0438\u043d\u0442 \u0432\u043e\u0437\u043e\u0431\u043d\u043e\u0432\u0438\u0442\u0441\u044f \u0441\u0430\u043c \u043d\u0430 \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0435\u043c \u0442\u0438\u043a\u0435.
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Send one stop-sprint packet before an airborne hit so the server accepts the crit").setValue(true);
    // \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0437\u0430\u0440\u044f\u0434 \u0434\u043b\u044f \u0443\u0434\u0430\u0440\u0430: 1.0 = \u043c\u0430\u043a\u0441 \u0443\u0440\u043e\u043d, \u043d\u0438\u0436\u0435 = \u0431\u044b\u0441\u0442\u0440\u0435\u0435 \u043a\u043e\u043c\u0431\u043e / \u043b\u043e\u0432\u0438\u0442 \u043a\u043e\u0440\u043e\u0442\u043a\u0438\u0439 \u043f\u0440\u044b\u0436\u043e\u043a.
    public SliderSettings attackCharge = new SliderSettings("AttackCharge", "Min attack charge before a hit (1.0 = max damage, lower = faster combo)").range(0.7F, 1.0F).setValue(0.92F);
    private boolean resprintNext = false;

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
        // \u041e\u0442\u043b\u043e\u0436\u0435\u043d\u043d\u043e\u0435 \u0432\u043e\u0437\u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u043f\u043e\u0441\u043b\u0435 \u043a\u0440\u0438\u0442\u0430 (\u043d\u0430 \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u0442\u0438\u043a, \u0447\u0442\u043e\u0431\u044b STOP \u0438 START \u043d\u0435 \u0441\u043b\u0438\u043b\u0438\u0441\u044c).
        if (this.resprintNext) {
            this.resprintNext = false;
            if (mc.player.input != null && mc.player.input.sprinting && this.canResetSprint() && !mc.player.isSprinting()) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                mc.player.setSprinting(true);
            }
        }
        if (mc.currentScreen != null) {
            return;
        }
        if (mc.player.isUsingItem()) {
            return;
        }
        Item mainItem = mc.player.getMainHandStack().getItem();
        boolean isWeapon = mainItem.getRegistryEntry().isIn(ItemTags.SWORDS) || 
                          mainItem.getRegistryEntry().isIn(ItemTags.AXES) || 
                          mainItem.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
        if (!isWeapon) {
            return;
        }
        Entity target = mc.targetedEntity;
        if (target == null || !(target instanceof LivingEntity)) {
            return;
        }

        boolean onGround = mc.player.isOnGround();
        float cooldown = mc.player.getAttackCooldownProgress(0.5f);

        // \u041a\u0440\u0438\u0442-\u043e\u043a\u043d\u043e: \u0443\u0436\u0435 \u043f\u0430\u0434\u0430\u0435\u043c \u043f\u043e\u0441\u043b\u0435 \u0432\u0435\u0440\u0445\u043d\u0435\u0439 \u0442\u043e\u0447\u043a\u0438 \u043f\u0440\u044b\u0436\u043a\u0430, \u043d\u0435 \u043d\u0430 \u0437\u0435\u043c\u043b\u0435/\u0432\u043e\u0434\u0435/\u043b\u0435\u0441\u0442\u043d\u0438\u0446\u0435/\u0442\u0440\u0430\u043d\u0441\u043f\u043e\u0440\u0442\u0435.
        boolean critWindow = !onGround
            && mc.player.fallDistance > 0.0F
            && mc.player.getVelocity().y < 0.0
            && !mc.player.isTouchingWater()
            && !mc.player.isInLava()
            && !mc.player.hasVehicle()
            && !mc.player.isClimbing();

        // \u0416\u0434\u0451\u043c \u0437\u0430\u0440\u044f\u0434 (\u043a\u043e\u043c\u0431\u043e \u043d\u0430 \u043f\u043e\u043b\u043d\u043e\u0439 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438 \u0430\u0442\u0430\u043a\u0438).
        if (cooldown < this.attackCharge.getValue()) {
            return;
        }
        // \u0415\u0441\u043b\u0438 \u043a\u043e\u043c\u0431\u043e \u0432\u044b\u043a\u043b \u2014 \u0431\u044c\u0451\u043c \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043a\u0440\u0438\u0442-\u043e\u043a\u043d\u0435 (\u0447\u0438\u0441\u0442\u044b\u0435 \u043a\u0440\u0438\u0442\u044b).
        if (!this.combo.isValue() && !critWindow) {
            return;
        }

        // \u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u0435\u0440\u0435\u0434 \u043a\u0440\u0438\u0442-\u0443\u0434\u0430\u0440\u043e\u043c: \u043e\u0434\u0438\u043d STOP, \u0431\u0435\u0437 \u043f\u043e\u0432\u0442\u043e\u0440\u043d\u043e\u0433\u043e START \u0432 \u044d\u0442\u043e\u043c \u0436\u0435 \u0442\u0438\u043a\u0435.
        if (this.sprintReset.isValue() && critWindow && mc.player.isSprinting() && this.canResetSprint()) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            mc.player.setSprinting(false);
            this.resprintNext = true;
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // \u041d\u0435 \u0442\u0440\u043e\u0433\u0430\u0435\u043c \u0441\u043f\u0440\u0438\u043d\u0442 \u0432 \u0432\u043e\u0434\u0435/\u043f\u043e\u043b\u0451\u0442\u0435.
    private boolean canResetSprint() {
        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
            return false;
        }
        return !mc.player.isGliding();
    }
}
