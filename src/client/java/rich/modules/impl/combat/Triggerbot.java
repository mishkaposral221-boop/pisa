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
    public BooleanSetting smartCrits = new BooleanSetting("SmartCrits", "Only attack when falling so the hit can crit").setValue(true);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Send a real stop-sprint packet right before the hit so the server allows the crit").setValue(true);
    private int delay = 0;

    public static Triggerbot getInstance() {
        return c.a(Triggerbot.class);
    }

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.settings(this.smartCrits, this.sprintReset);
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
        if (!this.autoCrit()) {
            return;
        }
        // \u041a\u0420\u0418\u0422\u042b: \u0432\u0430\u043d\u0438\u043b\u044c\u043d\u044b\u0439 \u0441\u0435\u0440\u0432\u0435\u0440 \u0437\u0430\u0441\u0447\u0438\u0442\u044b\u0432\u0430\u0435\u0442 \u043a\u0440\u0438\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u0435\u0441\u043b\u0438 \u0438\u0433\u0440\u043e\u043a \u041d\u0415 \u0441\u043f\u0440\u0438\u043d\u0442\u0443\u0435\u0442.
        // \u0420\u0430\u043d\u044c\u0448\u0435 \u043c\u044b \u0441\u0442\u0430\u0432\u0438\u043b\u0438 setSprinting(false) \u0438 \u0441\u0440\u0430\u0437\u0443 setSprinting(true) \u0432 \u043e\u0434\u043d\u043e\u043c \u0442\u0438\u043a\u0435 \u2014
        // \u0438\u0442\u043e\u0433\u043e\u0432\u043e\u0435 \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u0435 \u043d\u0435 \u043c\u0435\u043d\u044f\u043b\u043e\u0441\u044c, \u043f\u043e\u044d\u0442\u043e\u043c\u0443 \u043f\u0430\u043a\u0435\u0442 STOP_SPRINTING \u043d\u0438\u043a\u043e\u0433\u0434\u0430 \u043d\u0435 \u0443\u0445\u043e\u0434\u0438\u043b \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440.
        // \u0422\u0435\u043f\u0435\u0440\u044c \u0448\u043b\u0451\u043c \u043d\u0430\u0441\u0442\u043e\u044f\u0449\u0438\u0439 \u043f\u0430\u043a\u0435\u0442 STOP \u041f\u0415\u0420\u0415\u0414 \u0430\u0442\u0430\u043a\u043e\u0439 \u0438 START \u043f\u043e\u0441\u043b\u0435 \u043d\u0435\u0451.
        boolean resetSprint = this.sprintReset.isValue() && mc.player.isSprinting() && this.canResetSprint();
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
        this.delay = 10;
    }

    // Never reset sprint in water or while gliding (elytra) - it breaks movement.
    private boolean canResetSprint() {
        if (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isSwimming()) {
            return false;
        }
        return !mc.player.isGliding();
    }

    private boolean autoCrit() {
        boolean onGround = mc.player.isOnGround();
        float cooldown = mc.player.getAttackCooldownProgress(0.5f);
        float f = onGround ? 1.0f : 0.9f;
        if (cooldown < f) {
            return false;
        }
        if (!this.smartCrits.isValue()) {
            return true;
        }
        // \u041a\u0440\u0438\u0442 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u0435\u043d \u0432 \u0432\u043e\u0434\u0435 / \u043d\u0430 \u0442\u0440\u0430\u043d\u0441\u043f\u043e\u0440\u0442\u0435 / \u043d\u0430 \u043b\u0435\u0441\u0442\u043d\u0438\u0446\u0435 \u2014 \u0431\u044c\u0451\u043c \u043e\u0431\u044b\u0447\u043d\u044b\u043c \u0443\u0434\u0430\u0440\u043e\u043c.
        if (mc.player.isTouchingWater() || mc.player.isInLava() || mc.player.hasVehicle() || mc.player.isClimbing()) {
            return true;
        }
        // \u041a\u0440\u0438\u0442 \u0437\u0430\u0441\u0447\u0438\u0442\u044b\u0432\u0430\u0435\u0442\u0441\u044f \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u0440\u0438 \u041f\u0410\u0414\u0415\u041d\u0418\u0418: \u043d\u0435 \u043d\u0430 \u0437\u0435\u043c\u043b\u0435, fallDistance > 0 \u0438 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u043d\u0438\u0437.
        // \u042d\u0442\u043e \u043e\u0442\u0441\u0435\u043a\u0430\u0435\u0442 \u0444\u0430\u0437\u0443 \u0432\u0437\u043b\u0451\u0442\u0430 \u043f\u0440\u0438 \u0437\u0430\u0436\u0430\u0442\u043e\u043c \u043f\u0440\u044b\u0436\u043a\u0435, \u043a\u043e\u0433\u0434\u0430 \u043a\u0440\u0438\u0442 \u043d\u0435 \u043f\u0440\u043e\u0445\u043e\u0434\u0438\u0442.
        return !onGround && mc.player.fallDistance > 0.0F && mc.player.getVelocity().y < 0.0;
    }
}
