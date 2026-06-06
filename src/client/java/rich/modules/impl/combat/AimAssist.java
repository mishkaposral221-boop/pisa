package rich.modules.impl.combat;

import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.Setting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_3532;
import net.minecraft.class_1799;
import net.minecraft.class_1792;
import net.minecraft.class_1811;
import net.minecraft.class_638;
import net.minecraft.class_746;

import java.util.Random;

public class AimAssist extends ModuleStructure {

    private final AimEngine engine;
    private final Random random;

    private final SliderSettings fov;
    private final SliderSettings maxDistance;
    private final SliderSettings smoothness;
    private final SliderSettings throwStrength;
    private final BooleanSetting onlyWeapon;

    public AimAssist() {
        super("AimAssist", "Assist aim towards targets", ModuleCategory.VISUALS);
        this.engine = new AimEngine(this);
        this.random = new Random();
        this.fov = new SliderSettings("FOV", "Field of view for aiming")
                .setValue(45.0f)
                .range(10.0f, 180.0f);
        this.maxDistance = new SliderSettings("Distance", "Maximum distance to target")
                .setValue(3.0f)
                .range(1.0f, 6.0f);
        this.smoothness = new SliderSettings("Smoothness", "Smoothing factor for aim")
                .setValue(0.35f)
                .range(0.05f, 1.0f);
        this.throwStrength = new SliderSettings("Throw", "Throw strength for projectiles")
                .setValue(0.5f)
                .range(0.0f, 1.0f);
        this.onlyWeapon = new BooleanSetting("OnlyWeapon", "Only aim when holding weapon")
                .setValue(true);
        settings(new Setting[]{fov, maxDistance, smoothness, throwStrength, onlyWeapon});
    }

    public static AimAssist getInstance() {
        return (AimAssist) rich.util.c.a(AimAssist.class);
    }

    public float fov() {
        return fov.getValue();
    }

    public float maxDistance() {
        return maxDistance.getValue();
    }

    public float smoothness() {
        return smoothness.getValue();
    }

    public float throwStrength() {
        return throwStrength.getValue();
    }

    public boolean onlyWeapon() {
        return onlyWeapon.isValue();
    }

    public AimEngine getEngine() {
        return engine;
    }

    public void onWorldRender(WorldRenderEvent event) {
        engine.onFrame(event.getPartialTicks());
    }

    public class_1297 findTarget(class_310 mc) {
        if (mc.field_1724 == null || mc.field_1687 == null) return null;
        class_746 player = mc.field_1724;
        if (onlyWeapon() && !isHoldingWeapon(player)) return null;

        class_1297 best = null;
        double bestScore = Double.MAX_VALUE;

        for (Object obj : (Iterable<?>) mc.field_1687.method_18112()) {
            class_1297 entity = (class_1297) obj;
            if (entity == player) continue;
            if (!(entity instanceof class_1657)) continue;
            class_1657 living = (class_1657) entity;
            if (!living.method_5805()) continue;
            if (living.method_7325()) continue;

            double dist = (double) player.method_5739(entity);
            if (dist > (double) maxDistance()) continue;
            if (dist < 0.5d) continue;

            double[] angleDiff = getAngleDiff(player, entity.method_33571());
            double fovDist = Math.sqrt(angleDiff[0] * angleDiff[0] + angleDiff[1] * angleDiff[1]);
            if (fovDist > (double) fov()) continue;

            double score = fovDist + dist * 0.1d;
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }
        return best;
    }

    public class_243 getNearestPoint(class_1297 entity, class_1657 player) {
        class_238 bb = entity.method_5829();
        class_243 eye = player.method_33571();
        float yawRad = player.method_36454() * 0.017453292f;
        float pitchRad = player.method_36455() * 0.017453292f;
        double dirX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dirY = -Math.sin(pitchRad);
        double dirZ = Math.cos(yawRad) * Math.cos(pitchRad);
        double t = eye.method_1022(bb.method_1005());
        class_243 point = eye.method_1021(t).method_1019(new class_243(dirX, dirY, dirZ));
        double cx = class_3532.method_15350(point.field_1352, bb.field_1323, bb.field_1320);
        double cy = class_3532.method_15350(point.field_1351, bb.field_1322, bb.field_1325);
        double cz = class_3532.method_15350(point.field_1350, bb.field_1321, bb.field_1324);
        return new class_243(cx, cy, cz);
    }

    public double[] getAngleDiff(class_1657 player, class_243 target) {
        return getAngleDiffWithYawPitch(player, target, player.method_36454(), player.method_36455());
    }

    public double[] getAngleDiffWithYawPitch(class_1657 player, class_243 target, float yaw, float pitch) {
        class_243 eye = player.method_33571();
        class_243 delta = target.method_1020(eye);
        double dx = delta.field_1352;
        double dy = delta.field_1351;
        double dz = delta.field_1350;
        double horizDist = Math.sqrt(dx * dx + dz * dz);
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double targetPitch = -Math.toDegrees(Math.atan2(dy, horizDist));
        double yawDiff = class_3532.method_15338(targetYaw - (double) yaw);
        double pitchDiff = class_3532.method_15338(targetPitch - (double) pitch);
        return new double[]{yawDiff, pitchDiff};
    }

    public boolean isHoldingWeapon(class_1657 player) {
        class_1799 inv = player.method_6047();
        class_1792 stack = inv.method_7909();
        var tags = stack.method_40131();
        if (tags.method_40220(class_3489.field_42611)) return true;
        if (tags.method_40220(class_3489.field_42612)) return true;
        if (tags.method_40220(class_3489.field_42614)) return true;
        if (tags.method_40220(class_3489.field_63258)) return true;
        if (stack instanceof class_1811) return true;
        return false;
    }
}
