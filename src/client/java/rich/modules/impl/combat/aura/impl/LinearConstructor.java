package rich.modules.impl.combat.aura.impl;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;

/**
 * LinearConstructor - fast rotation with hard per-tick cap.
 * Was 360 deg/tick (instant snap) = trivially detected.
 * Now capped at 40/30 deg/tick.
 * For anti-detect use HumanAngle (easing + jitter + random aim).
 */
public class LinearConstructor extends RotateConstructor {
    public static final LinearConstructor INSTANCE = new LinearConstructor();

    private static final float MAX_YAW   = 40.0F;
    private static final float MAX_PITCH = 30.0F;

    public LinearConstructor() {
        super("Linear");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, Vec3d vec, Entity entity) {
        Angle delta = MathAngle.calculateDelta(current, target);
        float dYaw   = delta.getYaw();
        float dPitch = delta.getPitch();
        float dist   = (float) Math.hypot(dYaw, dPitch);
        if (dist < 0.001F) return new Angle(current.getYaw(), current.getPitch());

        float maxY = Math.min(Math.abs(dYaw   / dist) * MAX_YAW,   MAX_YAW);
        float maxP = Math.min(Math.abs(dPitch / dist) * MAX_PITCH, MAX_PITCH);

        float newYaw   = current.getYaw()   + MathHelper.clamp(dYaw,   -maxY, maxY);
        float newPitch = current.getPitch() + MathHelper.clamp(dPitch, -maxP, maxP);

        return new Angle(newYaw, MathHelper.clamp(newPitch, -90.0F, 90.0F));
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.0, 0.0, 0.0);
    }
}
