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
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    // \u0412\u042b\u041a\u041b \u043f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e: \u0438\u043d\u0430\u0447\u0435 \u0431\u044c\u0451\u0442 \u0422\u041e\u041b\u042c\u041a\u041e \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 (\u0440\u0435\u0434\u043a\u043e \u0438 \u043c\u0435\u0434\u043b\u0435\u043d\u043d\u043e).
    public BooleanSetting onlyCrits = new BooleanSetting("OnlyCrits", "Attack ONLY while airborne so every hit crits (slower)").setValue(false);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Send a real stop-sprint packet before an airborne hit so the server allows the crit").setValue(true);
    private int delay = 0;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.onlyCrits, this.sprintReset);
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
        // \u0411\u044c\u0451\u043c \u043f\u0440\u0438 \u043f\u043e\u043b\u043d\u043e\u043c \u0437\u0430\u043c\u0430\u0445\u0435 (\u043d\u0430 \u0437\u0435\u043c\u043b\u0435 1.0, \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435 0.9 \u0434\u043b\u044f \u043a\u0440\u0438\u0442\u0430).
        float cooldown = mc.player.getAttackCooldownProgress(0.5f);
        float need = onGround ? 1.0f : 0.9f;
        if (cooldown < need) {
            return;
        }

        // \u041a\u0440\u0438\u0442-\u043e\u043a\u043d\u043e: \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435, \u043f\u0430\u0434\u0430\u0435\u043c, \u043d\u0435 \u0432 \u0432\u043e\u0434\u0435/\u043b\u0430\u0432\u0435, \u043d\u0435 \u043d\u0430 \u043b\u0435\u0441\u0442\u043d\u0438\u0446\u0435, \u043d\u0435 \u043d\u0430 \u0442\u0440\u0430\u043d\u0441\u043f\u043e\u0440\u0442\u0435.
        boolean critWindow = !onGround
            && mc.player.fallDistance > 0.0F
            && !mc.player.isTouchingWater()
            && !mc.player.isInLava()
            && !mc.player.hasVehicle()
            && !mc.player.isClimbing();

        // OnlyCrits: \u0431\u044c\u0451\u043c \u0442\u043e\u043b\u044c\u043a\u043e \u0432 \u043a\u0440\u0438\u0442-\u043e\u043a\u043d\u0435. \u041f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e \u0432\u044b\u043a\u043b => \u0431\u044c\u0451\u043c \u0431\u044b\u0441\u0442\u0440\u043e \u0432\u0435\u0437\u0434\u0435.
        if (this.onlyCrits.isValue() && !critWindow) {
            return;
        }

        // \u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u043d\u0443\u0436\u0435\u043d \u0422\u041e\u041b\u042c\u041a\u041e \u0432 \u043a\u0440\u0438\u0442-\u043e\u043a\u043d\u0435 (\u043d\u0430 \u0437\u0435\u043c\u043b\u0435 \u043a\u0440\u0438\u0442\u0430 \u043d\u0435\u0442 \u0432 \u043f\u0440\u0438\u043d\u0446\u0438\u043f\u0435).
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
        // \u0411\u0435\u0437 \u0438\u0441\u043a\u0443\u0441\u0441\u0442\u0432\u0435\u043d\u043d\u043e\u0439 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0438: \u0440\u0438\u0442\u043c \u043e\u0433\u0440\u0430\u043d\u0438\u0447\u0435\u043d \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u043e\u0439 \u0430\u0442\u0430\u043a\u0438.
        this.delay = 1;
    }

    // Never reset sprint in water or while gliding (elytra) - it breaks movement.
    private boolean canResetSprint() {
        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
            return false;
        }
        return !mc.player.isGliding();
    }
}
