/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1811
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3489
 *  net.minecraft.class_3532
 *  net.minecraft.class_746
 */
package com.example.client.module;

import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import com.example.client.module.AimEngine;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1811;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
public class AimAssist {
    private final AimEngine engine = new AimEngine(this);
    private final Random random = new Random();
    private ModModule moduleRef;
    private static AimAssist INSTANCE;

    public float fov() {
        return this.getSetting(0).getFloat();
    }

    public float maxDistance() {
        return this.getSetting(1).getFloat();
    }

    public float smoothness() {
        return this.getSetting(2).getFloat();
    }

    public float throwStrength() {
        return this.getSetting(3).getFloat();
    }

    public boolean onlyWeapon() {
        return this.getSetting(4).getBool();
    }

    public AimAssist() {
        INSTANCE = this;
    }

    public static AimAssist getInstance() {
        return INSTANCE;
    }

    public void setModuleRef(ModModule ref) {
        this.moduleRef = ref;
    }

    public boolean isEnabled() {
        return this.moduleRef != null && this.moduleRef.enabled;
    }

    public AimEngine getEngine() {
        return this.engine;
    }

    private Setting getSetting(int index) {
        if (this.moduleRef == null || index >= this.moduleRef.settings.length) {
            Setting[] defaults = new Setting[]{new Setting("FOV", 10.0, 180.0, 45.0), new Setting("Distance", 1.0, 6.0, 3.0), new Setting("Smoothness", 0.05, 1.0, 0.35), new Setting("Throw", 0.0, 1.0, 0.5), new Setting("OnlyWeapon", true)};
            return defaults[index];
        }
        return this.moduleRef.settings[index];
    }

    public class_1297 findTarget(class_310 client) {
        if (client.field_1724 == null || client.field_1687 == null) {
            return null;
        }
        class_746 player = client.field_1724;
        if (this.onlyWeapon() && !this.isHoldingWeapon((class_1657)player)) {
            return null;
        }
        class_1297 best = null;
        double bestScore = Double.MAX_VALUE;
        for (class_1297 entity : client.field_1687.method_18112()) {
            double score;
            double[] diff;
            double angleDist;
            double dist;
            class_1309 living;
            if (entity == player || !(entity instanceof class_1309) || !(living = (class_1309)entity).method_5805() || (dist = (double)player.method_5739(entity)) > (double)this.maxDistance() || dist < 0.5 || (angleDist = Math.sqrt((diff = this.getAngleDiff((class_1657)player, entity.method_33571()))[0] * diff[0] + diff[1] * diff[1])) > (double)this.fov() || !((score = dist + angleDist * 0.1) < bestScore)) continue;
            bestScore = score;
            best = entity;
        }
        return best;
    }

    public class_243 getNearestPoint(class_1297 entity, class_1657 player) {
        class_238 box = entity.method_5829();
        class_243 eye = player.method_33571();
        float yawRad = player.method_36454() * ((float)Math.PI / 180);
        float pitchRad = player.method_36455() * ((float)Math.PI / 180);
        double lookX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double lookY = -Math.sin(pitchRad);
        double lookZ = Math.cos(yawRad) * Math.cos(pitchRad);
        double dist = eye.method_1022(box.method_1005());
        class_243 crosshairTarget = eye.method_1019(new class_243(lookX, lookY, lookZ).method_1021(dist));
        double px = class_3532.method_15350((double)crosshairTarget.field_1352, (double)box.field_1323, (double)box.field_1320);
        double py = class_3532.method_15350((double)crosshairTarget.field_1351, (double)box.field_1322, (double)box.field_1325);
        double pz = class_3532.method_15350((double)crosshairTarget.field_1350, (double)box.field_1321, (double)box.field_1324);
        return new class_243(px, py, pz);
    }

    public double[] getAngleDiff(class_1657 player, class_243 target) {
        return this.getAngleDiffWithYawPitch(player, target, player.method_36454(), player.method_36455());
    }

    public double[] getAngleDiffWithYawPitch(class_1657 player, class_243 target, float yaw, float pitch) {
        class_243 eye = player.method_33571();
        class_243 toTarget = target.method_1020(eye);
        double dx = toTarget.field_1352;
        double dy = toTarget.field_1351;
        double dz = toTarget.field_1350;
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double targetPitch = Math.toDegrees(-Math.atan2(dy, horizDist));
        double yawDiff = class_3532.method_15338((double)(targetYaw - (double)yaw));
        double pitchDiff = class_3532.method_15338((double)(targetPitch - (double)pitch));
        return new double[]{yawDiff, pitchDiff};
    }

    private boolean isHoldingWeapon(class_1657 player) {
        class_1799 held = player.method_6047();
        class_1792 item = held.method_7909();
        return item.method_40131().method_40220(class_3489.field_42611) || item.method_40131().method_40220(class_3489.field_42612) || item.method_40131().method_40220(class_3489.field_42614) || item.method_40131().method_40220(class_3489.field_63258) || item instanceof class_1811;
    }
}

