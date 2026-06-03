package rich.modules.impl.combat;

import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
    public SliderSettings fov = new SliderSettings("FOV", "Field of view for aiming").setValue(45.0F).range(10.0F, 180.0F);
    public SliderSettings maxDistance = new SliderSettings("Distance", "Maximum distance to target").setValue(3.0F).range(1.0F, 6.0F);
    public SliderSettings smoothness = new SliderSettings("Smoothness", "Smoothing factor for aim").setValue(0.35F).range(0.05F, 1.0F);
    public SliderSettings throwStrength = new SliderSettings("Throw", "Throw strength for projectiles").setValue(0.5F).range(0.0F, 1.0F);
    public BooleanSetting onlyWeapon = new BooleanSetting("OnlyWeapon", "Only aim when holding weapon").setValue(true);

    public AimAssist() {
        super("AimAssist", "Assist aim towards targets", ModuleCategory.VISUALS);
        this.settings(this.fov, this.maxDistance, this.smoothness, this.throwStrength, this.onlyWeapon);
    }

    public static AimAssist getInstance() {
        return c.a(AimAssist.class);
    }

    public float fov() { return this.fov.getValue(); }
    public float maxDistance() { return this.maxDistance.getValue(); }
    public float smoothness() { return this.smoothness.getValue(); }
    public float throwStrength() { return this.throwStrength.getValue(); }
    public boolean onlyWeapon() { return this.onlyWeapon.isValue(); }

    public AimEngine getEngine() { return this.engine; }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        this.engine.onFrame(event.getPartialTicks());
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
