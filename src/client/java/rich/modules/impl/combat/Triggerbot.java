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
    // \u041f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e \u0412\u041a\u041b: \u0431\u044c\u0451\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u0438\u0434\u0435\u0430\u043b\u044c\u043d\u044b\u0439 \u043c\u043e\u043c\u0435\u043d\u0442 \u043f\u0430\u0434\u0435\u043d\u0438\u044f, \u0447\u0442\u043e\u0431\u044b \u0433\u0430\u0440\u0430\u043d\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u043e \u043f\u0440\u043e\u0445\u043e\u0434\u0438\u043b \u043a\u0440\u0438\u0442.
    public BooleanSetting perfectCrits = new BooleanSetting("PerfectCrits", "Wait for the ideal falling moment so every hit lands as a crit").setValue(true);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Send a real stop-sprint packet before an airborne hit so the server accepts the crit").setValue(true);
    // \u041c\u0438\u043d\u0438\u043c\u0430\u043b\u044c\u043d\u044b\u0439 \u0437\u0430\u0440\u044f\u0434 \u0430\u0442\u0430\u043a\u0438 \u0434\u043b\u044f \u0443\u0434\u0430\u0440\u0430 \u0432 \u043a\u0440\u0438\u0442-\u043e\u043a\u043d\u0435 (0.9 = \u0443\u0441\u043f\u0435\u0432\u0430\u0435\u0442 \u0432 \u043a\u043e\u0440\u043e\u0442\u043a\u0438\u0439 \u043f\u0440\u044b\u0436\u043e\u043a).
    public SliderSettings critCharge = new SliderSettings("CritCharge", "Min attack charge to fire a crit (lower = catches shorter jumps)").range(0.7F, 1.0F).setValue(0.9F);
    private int delay = 0;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.perfectCrits, this.sprintReset, this.critCharge);
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
        if (this.delay > 0) {
            --this.delay;
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

        // \u0418\u0434\u0435\u0430\u043b\u044c\u043d\u043e\u0435 \u043a\u0440\u0438\u0442-\u043e\u043a\u043d\u043e: \u0443\u0436\u0435 \u043f\u0430\u0434\u0430\u0435\u043c (fallDistance > 0) \u043f\u043e\u0441\u043b\u0435 \u0432\u0435\u0440\u0445\u043d\u0435\u0439 \u0442\u043e\u0447\u043a\u0438 \u043f\u0440\u044b\u0436\u043a\u0430,
        // \u043d\u0435 \u043d\u0430 \u0437\u0435\u043c\u043b\u0435, \u043d\u0435 \u0432 \u0432\u043e\u0434\u0435/\u043b\u0430\u0432\u0435, \u043d\u0435 \u043d\u0430 \u043b\u0435\u0441\u0442\u043d\u0438\u0446\u0435, \u043d\u0435 \u043d\u0430 \u0442\u0440\u0430\u043d\u0441\u043f\u043e\u0440\u0442\u0435. \u0420\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0438 \u043d\u0430 \u043d\u0438\u0437\u043a\u043e\u043c \u043f\u0440\u044b\u0436\u043a\u0435.
        boolean critWindow = !onGround
            && mc.player.fallDistance > 0.0F
            && mc.player.getVelocity().y < 0.0
            && !mc.player.isTouchingWater()
            && !mc.player.isInLava()
            && !mc.player.hasVehicle()
            && !mc.player.isClimbing();

        if (this.perfectCrits.isValue()) {
            // \u0436\u0434\u0451\u043c \u0438\u0434\u0435\u0430\u043b\u044c\u043d\u044b\u0439 \u043c\u043e\u043c\u0435\u043d\u0442 \u2014 \u043d\u0435 \u0431\u044c\u0451\u043c \u0440\u0430\u043d\u043e (\u043d\u0430 \u0432\u0437\u043b\u0451\u0442\u0435/\u043d\u0430 \u0437\u0435\u043c\u043b\u0435)
            if (!critWindow) {
                return;
            }
            if (cooldown < this.critCharge.getValue()) {
                return;
            }
        } else {
            float need = onGround ? 1.0f : 0.9f;
            if (cooldown < need) {
                return;
            }
        }

        // \u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043a\u0440\u0438\u0442-\u043e\u043a\u043d\u0435: \u0438\u043d\u0430\u0447\u0435 \u0441\u0435\u0440\u0432\u0435\u0440 \u0441\u0431\u0438\u0432\u0430\u0435\u0442 \u043a\u0440\u0438\u0442.
        boolean resetSprint = this.sprintReset.isValue() && critWindow && mc.player.isSprinting() && this.canResetSprint();
        if (resetSprint) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            mc.player.setSprinting(false);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (resetSprint) {
            mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            mc.player.setSprinting(true);
        }
        // \u0411\u0435\u0437 \u0438\u0441\u043a\u0443\u0441\u0441\u0442\u0432\u0435\u043d\u043d\u043e\u0439 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0438: \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0438\u0439 \u043a\u0440\u0438\u0442 \u043b\u043e\u0432\u0438\u043c \u0441\u0440\u0430\u0437\u0443 \u043d\u0430 \u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0435\u043c \u043f\u0440\u044b\u0436\u043a\u0435.
        this.delay = 0;
    }

    // \u041d\u0435 \u0441\u0431\u0440\u0430\u0441\u044b\u0432\u0430\u0435\u043c \u0441\u043f\u0440\u0438\u043d\u0442 \u0432 \u0432\u043e\u0434\u0435/\u043f\u043e\u043b\u0451\u0442\u0435 (\u0441\u043b\u043e\u043c\u0430\u0435\u0442 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0435).
    private boolean canResetSprint() {
        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
            return false;
        }
        return !mc.player.isGliding();
    }
}
