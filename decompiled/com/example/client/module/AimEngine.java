/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_746
 */
package com.example.client.module;

import com.example.client.module.AimAssist;
import java.util.Random;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
public class AimEngine {
    private final AimAssist config;
    private final Random aimRandom = new Random();
    private float lastPlayerYaw = 0.0f;
    private float lastPlayerPitch = 0.0f;
    private float rawTickYawDelta = 0.0f;
    private float rawTickPitchDelta = 0.0f;
    private boolean firstFrame = true;
    private UUID aimLockedTarget = null;
    private int aimLostTicks = 0;
    private int aimPointTicks = 0;
    private double aimOffsetX = 0.0;
    private double aimOffsetY = 0.65;
    private double aimOffsetZ = 0.0;
    private double prevTargetX;
    private double prevTargetZ;
    private boolean hasPrevTarget = false;
    private int aimReactionTicks = 0;

    public AimEngine(AimAssist config) {
        this.config = config;
    }

    public void onFrame(class_310 client, float partialTick) {
        class_1297 found;
        if (!this.config.isEnabled() || client.field_1724 == null || client.field_1687 == null) {
            this.aimLockedTarget = null;
            this.aimReactionTicks = 0;
            this.aimPointTicks = 0;
            this.hasPrevTarget = false;
            this.firstFrame = true;
            return;
        }
        if (client.field_1755 != null) {
            return;
        }
        class_746 player = client.field_1724;
        if (this.firstFrame) {
            this.lastPlayerYaw = player.method_36454();
            this.lastPlayerPitch = player.method_36455();
            this.firstFrame = false;
            return;
        }
        this.rawTickYawDelta = class_3532.method_15393((float)(player.method_36454() - this.lastPlayerYaw));
        this.rawTickPitchDelta = player.method_36455() - this.lastPlayerPitch;
        this.lastPlayerYaw = player.method_36454();
        this.lastPlayerPitch = player.method_36455();
        float mouseDelta = Math.abs(this.rawTickYawDelta) + Math.abs(this.rawTickPitchDelta);
        if (mouseDelta < 0.005f) {
            return;
        }
        class_243 eyePos = player.method_33571();
        class_243 look = player.method_5828(1.0f);
        class_1297 target = null;
        float aimFov = this.config.fov();
        if (this.aimLockedTarget != null) {
            for (class_1297 e : client.field_1687.method_18112()) {
                if (!e.method_5667().equals(this.aimLockedTarget) || !(e instanceof class_1309)) continue;
                class_1309 living = (class_1309)e;
                if (living.method_5805() && !living.method_7325() && player.method_5739((class_1297)living) <= this.config.maxDistance()) {
                    target = living;
                    this.aimLostTicks = 0;
                    break;
                }
                if (++this.aimLostTicks < 25) break;
                this.aimLockedTarget = null;
                break;
            }
        }
        if (target == null && (found = this.config.findTarget(client)) != null) {
            target = found;
            this.aimLockedTarget = found.method_5667();
            this.aimLostTicks = 0;
            this.hasPrevTarget = false;
            this.aimReactionTicks = 1 + this.aimRandom.nextInt(3);
            this.aimPointTicks = 0;
        }
        if (target == null) {
            this.hasPrevTarget = false;
            this.aimReactionTicks = 0;
            this.aimPointTicks = 0;
            return;
        }
        if (!this.canSeeTarget(client, (class_1657)player, target)) {
            if (++this.aimLostTicks > 8) {
                this.aimLockedTarget = null;
                this.aimReactionTicks = 0;
                this.aimPointTicks = 0;
                this.hasPrevTarget = false;
            }
            return;
        }
        float dist = player.method_5739(target);
        float halfW = target.method_17681() * 0.5f;
        if (this.aimReactionTicks > 0) {
            --this.aimReactionTicks;
            return;
        }
        if (this.aimPointTicks-- <= 0) {
            this.randomizeAimPoint(target, dist);
        }
        double targetX = target.method_23317();
        double targetZ = target.method_23321();
        if (this.hasPrevTarget) {
            double velX = targetX - this.prevTargetX;
            double velZ = targetZ - this.prevTargetZ;
            float predFactor = class_3532.method_15363((float)(dist * 0.5f), (float)0.65f, (float)3.0f);
            targetX += velX * (double)predFactor;
            targetZ += velZ * (double)predFactor;
        }
        this.prevTargetX = target.method_23317();
        this.prevTargetZ = target.method_23321();
        this.hasPrevTarget = true;
        double targetY = target.method_23318() + (double)target.method_17682() * this.aimOffsetY + this.aimRandom.nextGaussian() * 0.005;
        double dx = (targetX += this.aimOffsetX + this.aimRandom.nextGaussian() * 0.003) - eyePos.field_1352;
        double dy = targetY - eyePos.field_1351;
        double dz = (targetZ += this.aimOffsetZ + this.aimRandom.nextGaussian() * 0.003) - eyePos.field_1350;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float wantYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float wantPitch = (float)(-Math.toDegrees(Math.atan2(dy, hDist)));
        wantYaw = class_3532.method_15393((float)(wantYaw - player.method_36454()));
        wantPitch -= player.method_36455();
        float sens = (float)((Double)client.field_1690.method_42495().method_41753()).doubleValue() * 0.6f + 0.2f;
        float step = sens * sens * sens * 0.8f;
        float deadzone = Math.max((float)Math.toDegrees(Math.atan2(halfW, dist)) * 0.22f, 0.35f);
        float totalAngle = (float)Math.sqrt(wantYaw * wantYaw + wantPitch * wantPitch);
        if (totalAngle > aimFov + 12.0f) {
            this.aimLockedTarget = null;
            this.aimReactionTicks = 0;
            this.aimPointTicks = 0;
            return;
        }
        if (this.aimRandom.nextFloat() < 0.08f) {
            return;
        }
        float applyYaw = 0.0f;
        float applyPitch = 0.0f;
        if (totalAngle > deadzone) {
            boolean movingToward = this.rawTickYawDelta * wantYaw + this.rawTickPitchDelta * wantPitch > 0.0f;
            float t = Math.min((totalAngle - deadzone) / 15.0f, 1.0f);
            float strength = movingToward ? 0.28f + t * 0.38f : 0.06f + t * 0.1f;
            float maxPull = movingToward ? 2.8f + t * 4.2f : 0.6f + t * 0.9f;
            float moveScale = movingToward ? class_3532.method_15363((float)(mouseDelta * 0.78f), (float)0.18f, (float)0.88f) : 0.2f;
            float jitter = (this.aimRandom.nextFloat() - 0.5f) * 0.16f;
            float undershoot = this.aimRandom.nextFloat() < 0.2f ? 0.7f + this.aimRandom.nextFloat() * 0.2f : 1.0f;
            float rawYawPull = class_3532.method_15363((float)(wantYaw * strength * moveScale * undershoot + jitter), (float)(-maxPull), (float)maxPull);
            float rawPitchPull = class_3532.method_15363((float)(wantPitch * strength * moveScale * 0.48f * undershoot + jitter * 0.3f), (float)(-maxPull * 0.48f), (float)(maxPull * 0.48f));
            applyYaw = Math.abs(rawYawPull) > step ? (float)Math.round(rawYawPull / step) * step : 0.0f;
            applyPitch = Math.abs(rawPitchPull) > step ? (float)Math.round(rawPitchPull / step) * step : 0.0f;
        } else if (totalAngle > deadzone * 0.4f) {
            float microStr = 0.09f;
            float rawYawPull = wantYaw * microStr;
            float rawPitchPull = wantPitch * microStr * 0.4f;
            applyYaw = Math.abs(rawYawPull) > step ? (float)Math.round(rawYawPull / step) * step : 0.0f;
            float f = applyPitch = Math.abs(rawPitchPull) > step ? (float)Math.round(rawPitchPull / step) * step : 0.0f;
        }
        if (applyYaw != 0.0f || applyPitch != 0.0f) {
            float newYaw = player.method_36454() + applyYaw;
            float newPitch = class_3532.method_15363((float)(player.method_36455() + applyPitch), (float)-90.0f, (float)90.0f);
            player.method_36456(newYaw);
            player.method_36457(newPitch);
            this.lastPlayerYaw = newYaw;
            this.lastPlayerPitch = newPitch;
        }
    }

    private boolean canSeeTarget(class_310 client, class_1657 player, class_1297 target) {
        class_243 targetPos;
        class_243 eyePos = player.method_33571();
        class_3965 hit = client.field_1687.method_17742(new class_3959(eyePos, targetPos = new class_243(target.method_23317(), target.method_23318() + (double)target.method_17682() * 0.7, target.method_23321()), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)player));
        return hit.method_17783() == class_239.class_240.field_1333 || hit.method_17784().method_1022(targetPos) < 1.0;
    }

    private void randomizeAimPoint(class_1297 target, float dist) {
        double half = Math.max(0.04, (double)target.method_17681() * 0.38);
        this.aimOffsetX = class_3532.method_15350((double)(this.aimRandom.nextGaussian() * half * 0.45), (double)(-half), (double)half);
        this.aimOffsetZ = class_3532.method_15350((double)(this.aimRandom.nextGaussian() * half * 0.45), (double)(-half), (double)half);
        double minY = dist > 3.0f ? 0.48 : 0.56;
        double maxY = dist > 3.0f ? 0.76 : 0.86;
        this.aimOffsetY = class_3532.method_15350((double)(minY + this.aimRandom.nextDouble() * (maxY - minY)), (double)minY, (double)maxY);
        this.aimPointTicks = 8 + this.aimRandom.nextInt(14);
    }
}

