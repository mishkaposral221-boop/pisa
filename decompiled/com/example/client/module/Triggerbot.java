/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1792
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_3489
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_746
 */
package com.example.client.module;

import com.example.client.ModMenuInitializer;
import com.example.client.menu.data.ModModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1792;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_3675;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
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

    public void tick(class_310 client) {
        boolean isWeapon;
        if (!this.isEnabled() || client.field_1724 == null || client.field_1687 == null) {
            return;
        }
        if (client.field_1755 != null) {
            return;
        }
        class_746 player = client.field_1724;
        if (player.method_6115()) {
            return;
        }
        if (this.delay > 0) {
            --this.delay;
            return;
        }
        class_1792 mainItem = player.method_6047().method_7909();
        boolean bl = isWeapon = mainItem.method_40131().method_40220(class_3489.field_42611) || mainItem.method_40131().method_40220(class_3489.field_42612) || mainItem.method_40131().method_40220(class_3489.field_63258);
        if (!isWeapon) {
            return;
        }
        class_1297 target = client.field_1692;
        if (target == null || !(target instanceof class_1309)) {
            return;
        }
        if (!this.autoCrit(client)) {
            return;
        }
        class_304.method_1420((class_3675.class_306)client.field_1690.field_1886.method_1429());
        this.delay = 10;
        ModMenuInitializer.TARGET_HUD.setTarget((class_1309)target);
    }

    private boolean autoCrit(class_310 client) {
        class_746 player = client.field_1724;
        boolean onGround = player.method_24828();
        float cooldown = player.method_7261(0.5f);
        float f = onGround ? 1.0f : 0.9f;
        if (cooldown < f) {
            return false;
        }
        if (!this.smartCrits()) {
            return true;
        }
        if (!client.field_1690.field_1903.method_1434() && onGround) {
            return true;
        }
        if (player.method_5799() || player.method_5765()) {
            return true;
        }
        return !onGround && player.field_6017 > 0.0;
    }
}

