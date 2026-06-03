package rich.modules.impl.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.registry.tag.ItemTags;
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
    public SliderSettings fov = new SliderSettings("FOV", "Field of view for aiming").setValue(45.0F).range(10.0F, 180.0F);
    public SliderSettings maxDistance = new SliderSettings("Distance", "Maximum distance to target").setValue(3.0F).range(1.0F, 6.0F);
    public SliderSettings smoothness = new SliderSettings("Smoothness", "Smoothing factor for aim").setValue(0.35F).range(0.05F, 1.0F);
    public SliderSettings throwStrength = new SliderSettings("Throw", "Throw strength for projectiles").setValue(0.5F).range(0.0F, 1.0F);
    public BooleanSetting onlyWeapon = new BooleanSetting("OnlyWeapon", "Only aim when holding weapon").setValue(false);

    public static AimAssist getInstance() {
        return c.a(AimAssist.class);
    }

    public AimAssist() {
        super("AimAssist", "Assist aim towards targets", ModuleCategory.VISUALS);
        this.settings(this.fov, this.maxDistance, this.smoothness, this.throwStrength, this.onlyWeapon);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.player != null && mc.world != null) {
            this.engine.onFrame(event.getPartialTicks());
        }
    }

    public float fov() {
        return this.fov.getValue();
    }

    public float maxDistance() {
        return this.maxDistance.getValue();
    }

    public float smoothness() {
        return this.smoothness.getValue();
    }

    public float throwStrength() {
        return this.throwStrength.getValue();
    }

    public boolean onlyWeapon() {
        return this.onlyWeapon.isValue();
    }

    public AimEngine getEngine() {
        return this.engine;
    }

    public Entity findTarget() {
        if (mc.player == null || mc.world == null) {
            return null;
        }
        PlayerEntity player = mc.player;
        if (this.onlyWeapon() && !this.isHoldingWeapon(player)) {
            return null;
        }
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == player || !(entity instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity)entity;
            if (!living.isAlive()) continue;
            double dist = player.distanceTo(entity);
            if (dist > this.maxDistance() || dist < 0.5) continue;
            double[] diff = this.getAngleDiff(player, entity.getEyePos());
            double angleDist = Math.sqrt(diff[0] * diff[0] + diff[1] * diff[1]);
            if (angleDist > this.fov()) continue;
            double score = dist + angleDist * 0.1;
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }
        return best;
    }

    public Vec3d getNearestPoint(Entity entity, PlayerEntity player) {
        Vec3d box = entity.getBoundingBox().getCenter();
        Vec3d eye = player.getEyePos();
        float yawRad = player.getYaw() * ((float)Math.PI / 180);
        float pitchRad = player.getPitch() * ((float)Math.PI / 180);
        Vec3d forward = new Vec3d(
            -(float)Math.sin(yawRad) * (float)Math.cos(pitchRad),
            -(float)Math.sin(pitchRad),
            (float)Math.cos(yawRad) * (float)Math.cos(pitchRad)
        );
        Vec3d toBox = box.subtract(eye);
        double dot = toBox.dotProduct(forward);
        if (dot <= 0.0) {
            return box;
        }
        Vec3d pointOnRay = eye.add(forward.multiply(dot));
        double dist = pointOnRay.distanceTo(box);
        return pointOnRay.subtract(toBox.normalize().multiply(Math.sqrt(Math.max(0.0, dist * dist - 0.3))));
    }

    public boolean isHoldingWeapon(PlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof RangedWeaponItem || stack.isIn(ItemTags.AXES) || stack.isIn(ItemTags.SWORDS);
    }

    private double[] getAngleDiff(PlayerEntity player, Vec3d target) {
        Vec3d eye = player.getEyePos();
        Vec3d toTarget = target.subtract(eye);
        double distance = toTarget.length();
        if (distance == 0.0) {
            return new double[]{0.0, 0.0};
        }
        double pitch = Math.asin(toTarget.y / distance) * 180.0 / Math.PI;
        double yaw = Math.atan2(toTarget.z, toTarget.x) * 180.0 / Math.PI - 90.0;
        double currentYaw = player.getYaw();
        double currentPitch = player.getPitch();
        double yawDiff = yaw - currentYaw;
        double pitchDiff = pitch - currentPitch;
        yawDiff = ((yawDiff + 180.0) % 360.0) - 180.0;
        return new double[]{yawDiff, pitchDiff};
    }
}
