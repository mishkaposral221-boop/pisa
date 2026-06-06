package rich.modules.impl.combat;

import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class AimAssist extends ModuleStructure {

    private final AimEngine engine = new AimEngine(this);
    private final Random random = new Random();

    public SliderSettings fov = new SliderSettings("FOV", "Field of view for aiming")
            .setValue(45.0F).range(10.0F, 180.0F);
    public SliderSettings maxDistance = new SliderSettings("Distance", "Maximum distance to target")
            .setValue(3.0F).range(1.0F, 6.0F);
    public SliderSettings smoothness = new SliderSettings("Smoothness", "Smoothing factor for aim")
            .setValue(0.35F).range(0.05F, 1.0F);
    public SliderSettings throwStrength = new SliderSettings("Throw", "Throw strength for projectiles")
            .setValue(0.5F).range(0.1F, 1.0F);
    public BooleanSetting onlyWeapon = new BooleanSetting("OnlyWeapon", "Only aim when holding weapon")
            .setValue(true);

    public AimAssist() {
        super("AimAssist", "Assist aim towards targets", ModuleCategory.VISUALS);
        this.settings(fov, maxDistance, smoothness, throwStrength, onlyWeapon);
    }

    public static AimAssist getInstance() {
        return (AimAssist) c.a(AimAssist.class);
    }

    public float fov()          { return fov.getValue(); }
    public float maxDistance()  { return maxDistance.getValue(); }
    public float smoothness()   { return smoothness.getValue(); }
    public boolean onlyWeapon() { return onlyWeapon.isValue(); }
    public AimEngine getEngine() { return engine; }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        engine.onFrame(event.getPartialTicks());
    }

    /** Finds best target within FOV. Priority: smallest angle, distance as tiebreaker. */
    public Entity findTarget(MinecraftClient client) {
        if (client.player == null || client.world == null) return null;
        ClientPlayerEntity player = client.player;
        if (onlyWeapon() && !isHoldingWeapon(player)) return null;

        Entity best = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof PlayerEntity targetPlayer)) continue;
            if (!targetPlayer.isAlive() || targetPlayer.isSpectator()) continue;

            float dist = player.distanceTo(entity);
            if (dist > maxDistance() || dist < 0.5F) continue;

            double[] diff = getAngleDiff(player, entity.getEyePos());
            double angleDist = Math.sqrt(diff[0] * diff[0] + diff[1] * diff[1]);
            if (angleDist > fov()) continue;

            double score = angleDist + dist * 0.1;
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }
        return best;
    }

    public double[] getAngleDiff(PlayerEntity player, Vec3d target) {
        return getAngleDiffWithYawPitch(player, target, player.getYaw(), player.getPitch());
    }

    public double[] getAngleDiffWithYawPitch(PlayerEntity player, Vec3d target, float yaw, float pitch) {
        Vec3d eye      = player.getEyePos();
        Vec3d toTarget = target.subtract(eye);
        double horizDist  = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        double targetYaw   = Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));
        double targetPitch = Math.toDegrees(-Math.atan2(toTarget.y, horizDist));
        double yawDiff   = MathHelper.wrapDegrees(targetYaw   - yaw);
        double pitchDiff = MathHelper.wrapDegrees(targetPitch - pitch);
        return new double[]{ yawDiff, pitchDiff };
    }

    /** Returns point on target's bounding box closest to the player's crosshair. */
    public Vec3d getNearestPoint(Entity target, PlayerEntity player) {
        Box   box      = target.getBoundingBox();
        Vec3d eye      = player.getEyePos();
        float yawRad   = player.getYaw()   * ((float) Math.PI / 180.0F);
        float pitchRad = player.getPitch() * ((float) Math.PI / 180.0F);
        double lookX   = -Math.sin(yawRad) * Math.cos(pitchRad);
        double lookY   = -Math.sin(pitchRad);
        double lookZ   =  Math.cos(yawRad) * Math.cos(pitchRad);
        double dist    = eye.distanceTo(box.getCenter());
        Vec3d ct = eye.add(new Vec3d(lookX, lookY, lookZ).multiply(dist));
        return new Vec3d(
            MathHelper.clamp(ct.x, box.minX, box.maxX),
            MathHelper.clamp(ct.y, box.minY, box.maxY),
            MathHelper.clamp(ct.z, box.minZ, box.maxZ)
        );
    }

    private boolean isHoldingWeapon(PlayerEntity player) {
        Item i = player.getMainHandStack().getItem();
        return i.getRegistryEntry().isIn(ItemTags.SWORDS)
            || i.getRegistryEntry().isIn(ItemTags.AXES)
            || i.getRegistryEntry().isIn(ItemTags.PICKAXES)
            || i.getRegistryEntry().isIn(ItemTags.MELEE_WEAPON_ENCHANTABLE)
            || i instanceof RangedWeaponItem;
    }
}
