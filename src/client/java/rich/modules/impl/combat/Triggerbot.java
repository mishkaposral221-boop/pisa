package rich.modules.impl.combat;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import rich.events.api.EventHandler;
import rich.events.impl.ClientTickStartEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.c;

public class Triggerbot extends ModuleStructure {
    public BooleanSetting smartCrits = new BooleanSetting("SmartCrits", "Only attack when at low fall damage").setValue(true);
    public BooleanSetting sprintReset = new BooleanSetting("SprintReset", "Briefly stop sprinting right before the hit, then resume if forward is still held (more knockback)").setValue(true);
    private int delay = 0;
    private boolean resumeSprint = false;

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
        // Возобновляем бег на следующий тик после сброса, если игрок всё ещё удерживает движение вперёд
        if (this.resumeSprint) {
            this.resumeSprint = false;
            if (mc.options.forwardKey.isPressed() && !mc.player.isSneaking()) {
                mc.player.setSprinting(true);
            }
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
        // Сброс бега прямо перед ударом для максимального отбрасывания
        if (this.sprintReset.isValue() && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
            this.resumeSprint = true;
        }
        KeyBinding.onKeyPressed((InputUtil.Key)mc.options.attackKey.getDefaultKey());
        this.delay = 10;
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
