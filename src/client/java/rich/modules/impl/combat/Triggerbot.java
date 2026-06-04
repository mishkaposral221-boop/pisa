package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import rich.events.api.EventHandler;
import rich.events.impl.ClientTickStartEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    public BooleanSetting smartCrits = new BooleanSetting("SmartCrits", "Only attack when at low fall damage").setValue(true);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Server-side sprint reset right before the hit for full knockback").setValue(true);
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
        // \u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430 \u0440\u043e\u0432\u043d\u043e \u043a\u0430\u043a \u0432 KillAura (StrikeManager):
        // \u0442\u043e\u043b\u044c\u043a\u043e \u043a\u043b\u0438\u0435\u043d\u0442\u0441\u043a\u0438\u0439 \u0444\u043b\u0430\u0433, \u0431\u0435\u0437 \u0441\u044b\u0440\u044b\u0445 \u043f\u0430\u043a\u0435\u0442\u043e\u0432.
        // \u0412\u0430\u043d\u0438\u043b\u044c \u0441\u0430\u043c\u0430 \u0441\u0438\u043d\u0445\u0440\u043e\u043d\u0438\u0437\u0438\u0440\u0443\u0435\u0442 \u043e\u0434\u0438\u043d \u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0439 \u043f\u0430\u043a\u0435\u0442 \u0447\u0435\u0440\u0435\u0437 \u043f\u0430\u0439\u043f\u043b\u0430\u0439\u043d (AutoSprint \u0435\u0433\u043e \u0432\u0438\u0434\u0438\u0442).
        boolean resetSprint = this.sprintReset.isValue() && mc.player.isSprinting() && this.canResetSprint();
        if (resetSprint) {
            mc.player.setSprinting(false);
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (resetSprint) {
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
        if (!mc.options.jumpKey.isPressed() && onGround) {
            return true;
        }
        if (mc.player.isTouchingWater() || mc.player.hasVehicle()) {
            return true;
        }
        return !onGround && mc.player.fallDistance > 0.0;
    }
}
