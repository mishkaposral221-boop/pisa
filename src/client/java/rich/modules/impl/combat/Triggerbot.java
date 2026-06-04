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
    // Combo \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0422\u041e\u041b\u042c\u041a\u041e \u043d\u0430 \u0437\u0435\u043c\u043b\u0435. \u0412 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 \u2014 \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u043e \u0442\u0430\u0439\u043c\u0438\u043d\u0433\u0443 \u043a\u0440\u0438\u0442\u0430, \u0447\u0442\u043e\u0431\u044b \u043a\u043e\u043c\u0431\u043e \u043d\u0435 \u0441\u044a\u0435\u0434\u0430\u043b\u043e \u043e\u0442\u043a\u0430\u0442 \u0434\u043e \u043a\u0440\u0438\u0442\u0430.
    public BooleanSetting combo = new BooleanSetting("Combo", "Combo on the ground; in the air only crit-timed hits (no interference)").setValue(true);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Stop-sprint for one tick around an airborne hit so the server accepts the crit").setValue(true);
    // \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0437\u0430\u0440\u044f\u0434 \u0434\u043b\u044f \u0443\u0434\u0430\u0440\u0430.
    public SliderSettings attackCharge = new SliderSettings("AttackCharge", "Min attack charge before a hit (1.0 = max damage, lower = faster combo)").range(0.7F, 1.0F).setValue(0.92F);

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

        // \u0416\u0434\u0451\u043c \u0437\u0430\u0440\u044f\u0434 \u0432 \u043b\u044e\u0431\u043e\u043c \u0441\u043b\u0443\u0447\u0430\u0435.
        if (cooldown < this.attackCharge.getValue()) {
            return;
        }

        // \u041a\u0440\u0438\u0442-\u043e\u043a\u043d\u043e = \u0432\u0430\u043d\u0438\u043b\u044c\u043d\u043e\u0435 \u0443\u0441\u043b\u043e\u0432\u0438\u0435 \u043a\u0440\u0438\u0442\u0430: \u043f\u0430\u0434\u0430\u0435\u043c (fallDistance > 0), \u043d\u0435 \u043d\u0430 \u0437\u0435\u043c\u043b\u0435/\u0432\u043e\u0434\u0435/\u043b\u0430\u0432\u0435/\u043b\u0435\u0441\u0442\u043d\u0438\u0446\u0435/\u0442\u0440\u0430\u043d\u0441\u043f\u043e\u0440\u0442\u0435.
        // \u0411\u0435\u0437 \u0436\u0451\u0441\u0442\u043a\u043e\u0433\u043e \u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u0438\u044f \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438 \u2014 \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0438 \u043f\u0440\u0438 \u043f\u043b\u0430\u0432\u043d\u043e\u043c \u043f\u0430\u0434\u0435\u043d\u0438\u0438.
        boolean critWindow = !onGround
            && mc.player.fallDistance > 0.0F
            && !mc.player.isTouchingWater()
            && !mc.player.isInLava()
            && !mc.player.hasVehicle()
            && !mc.player.isClimbing();

        if (!onGround) {
            // \u0412 \u0432\u043e\u0437\u0434\u0443\u0445\u0435: \u041d\u0415 \u043a\u043e\u043c\u0431\u0438\u043c \u043d\u0430 \u0432\u0437\u043b\u0451\u0442\u0435 \u2014 \u0436\u0434\u0451\u043c \u043c\u043e\u043c\u0435\u043d\u0442 \u043f\u0430\u0434\u0435\u043d\u0438\u044f (\u043a\u0440\u0438\u0442). \u0422\u0430\u043a \u043e\u0442\u043a\u0430\u0442 \u043d\u0435 \u0442\u0440\u0430\u0442\u0438\u0442\u0441\u044f \u0437\u0440\u044f.
            if (!critWindow) {
                return;
            }
        } else {
            // \u041d\u0430 \u0437\u0435\u043c\u043b\u0435: \u043a\u043e\u043c\u0431\u043e (\u0435\u0441\u043b\u0438 \u0432\u043a\u043b).
            if (!this.combo.isValue()) {
                return;
            }
        }

        // \u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u043e\u0434 \u043a\u0440\u0438\u0442: STOP -> \u0443\u0434\u0430\u0440 -> START \u0432 \u043e\u0434\u043d\u043e\u043c \u0442\u0438\u043a\u0435 (\u0441\u0435\u0440\u0432\u0435\u0440 \u043e\u0431\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u0435\u0442 \u043f\u043e \u043f\u043e\u0440\u044f\u0434\u043a\u0443).
        boolean doReset = this.sprintReset.isValue() && critWindow && mc.player.isSprinting() && this.canResetSprint();
        if (doReset) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            mc.player.setSprinting(false);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (doReset) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
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
