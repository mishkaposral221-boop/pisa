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
import rich.util.network.NetworkUtility;

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
        boolean resetSprint = this.sprintReset.isValue() && mc.player.isSprinting() && this.canResetSprint();
        // Send the stop-sprint packet straight to the server (bypassing the module pipeline)
        // so the hit is registered as a fresh sprint hit with full knockback.
        // We do NOT change client physics here - doing so is what slammed the player to the ground.
        if (resetSprint) {
            NetworkUtility.sendWithoutEvent(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        if (resetSprint) {
            // Resume sprint on the server right after the hit.
            NetworkUtility.sendWithoutEvent(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            // Restore the client sprint flag (vanilla attack() clears it) so movement speed
            // never dips and the client/server sprint state stays in sync.
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
