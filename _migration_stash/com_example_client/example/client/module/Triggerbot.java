package com.example.client.module;

import com.example.client.ModMenuInitializer;
import com.example.client.menu.data.ModModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.network.ClientPlayerEntity;

public class Triggerbot {
    private ModModule moduleRef;
    private static Triggerbot INSTANCE;
    private int delay = 0;

    public Triggerbot() {
        INSTANCE = this;
    }

    public static Triggerbot getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public boolean smartCrits() {
        if (this.moduleRef == null || this.moduleRef.settings.length < 1) {
            return true;
        }
        return this.moduleRef.settings[0].boolValue;
    }

    public void tick(MinecraftClient client) {
        boolean isWeapon;
        if (!this.isEnabled() || client.player == null || client.world == null) {
            return;
        }
        if (client.currentScreen != null) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player.isUsingItem()) {
            return;
        }
        if (this.delay > 0) {
            --this.delay;
            return;
        }
        Item mainItem = player.getMainHandStack().getItem();
        boolean bl = isWeapon = mainItem.getRegistryEntry().isIn(ItemTags.SWORDS) || mainItem.getRegistryEntry().isIn(ItemTags.AXES) || mainItem.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE);
        if (!isWeapon) {
            return;
        }
        Entity target = client.targetedEntity;
        if (target == null || !(target instanceof LivingEntity)) {
            return;
        }
        if (!this.autoCrit(client)) {
            return;
        }
        KeyBinding.onKeyPressed((InputUtil.Key)client.options.attackKey.getDefaultKey());
        this.delay = 10;
        ModMenuInitializer.TARGET_HUD.setTarget((LivingEntity)target);
    }

    private boolean autoCrit(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        boolean onGround = player.isOnGround();
        float cooldown = player.getAttackCooldownProgress(0.5f);
        float f = onGround ? 1.0f : 0.9f;
        if (cooldown < f) {
            return false;
        }
        if (!this.smartCrits()) {
            return true;
        }
        if (!client.options.jumpKey.isPressed() && onGround) {
            return true;
        }
        if (player.isTouchingWater() || player.hasVehicle()) {
            return true;
        }
        return !onGround && player.fallDistance > 0.0;
    }
}

