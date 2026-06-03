package com.example.client.module;

import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import com.example.client.module.AimEngine;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;

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

    public Entity findTarget(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return null;
        }
        ClientPlayerEntity player = client.player;
        if (this.onlyWeapon() && !this.isHoldingWeapon((PlayerEntity)player)) {
            return null;
        }
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : client.world.getEntities()) {
            double score;
            double[] diff;
            double angleDist;
            double dist;
            LivingEntity living;
            if (entity == player || !(entity instanceof LivingEntity) || !(living = (LivingEntity)entity).isAlive() || (dist = (double)player.distanceTo(entity)) > (double)this.maxDistance() || dist < 0.5 || (angleDist = Math.sqrt((diff = this.getAngleDiff((PlayerEntity)player, entity.getEyePos()))[0] * diff[0] + diff[1] * diff[1])) > (double)this.fov() || !((score = dist + angleDist * 0.1) < bestScore)) continue;
            bestScore = score;
            best = entity;
        }
        return best;
    }

    public Vec3d getNearestPoint(Entity entity, PlayerEntity player) {
        Box box = entity.getBoundingBox();
        Vec3d eye = player.getEyePos();
        float yawRad = player.getYaw() * ((float)Math.PI / 180);
        float pitchRad = player.getPitch() * ((float)Math.PI / 180);
        double lookX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double lookY = -Math.sin(pitchRad);
        double lookZ = Math.cos(yawRad) * Math.cos(pitchRad);
        double dist = eye.distanceTo(box.getCenter());
        Vec3d crosshairTarget = eye.add(new Vec3d(lookX, lookY, lookZ).multiply(dist));
        double px = MathHelper.clamp((double)crosshairTarget.x, (double)box.minX, (double)box.maxX);
        double py = MathHelper.clamp((double)crosshairTarget.y, (double)box.minY, (double)box.maxY);
        double pz = MathHelper.clamp((double)crosshairTarget.z, (double)box.minZ, (double)box.maxZ);
        return new Vec3d(px, py, pz);
    }

    public double[] getAngleDiff(PlayerEntity player, Vec3d target) {
        return this.getAngleDiffWithYawPitch(player, target, player.getYaw(), player.getPitch());
    }

    public double[] getAngleDiffWithYawPitch(PlayerEntity player, Vec3d target, float yaw, float pitch) {
        Vec3d eye = player.getEyePos();
        Vec3d toTarget = target.subtract(eye);
        double dx = toTarget.x;
        double dy = toTarget.y;
        double dz = toTarget.z;
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double targetPitch = Math.toDegrees(-Math.atan2(dy, horizDist));
        double yawDiff = MathHelper.wrapDegrees((double)(targetYaw - (double)yaw));
        double pitchDiff = MathHelper.wrapDegrees((double)(targetPitch - (double)pitch));
        return new double[]{yawDiff, pitchDiff};
    }

    private boolean isHoldingWeapon(PlayerEntity player) {
        ItemStack held = player.getMainHandStack();
        Item item = held.getItem();
        return item.getRegistryEntry().isIn(ItemTags.SWORDS) || item.getRegistryEntry().isIn(ItemTags.AXES) || item.getRegistryEntry().isIn(ItemTags.PICKAXES) || item.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE) || item instanceof RangedWeaponItem;
    }
}

