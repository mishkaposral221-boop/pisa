package rich.modules.impl.combat.aura;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.modules.impl.combat.aura.rotations.HumanAngle;

/**
 * AngleConfig - rotation plan factory.
 *
 * DEFAULT -> HumanAngle (anti-detect):
 *   max 38/28 deg/tick, distance easing, gaussian jitter, random hitbox offset
 *
 * FAST -> LinearConstructor (40/30 deg/tick, no jitter):
 *   For non-PvP uses where detection doesn't matter.
 */
public class AngleConfig {
    public static AngleConfig DEFAULT = new AngleConfig(new HumanAngle(), true, true);
    public static AngleConfig FAST    = new AngleConfig(new LinearConstructor(), true, true);

    public static boolean moveCorrection;
    public static boolean freeCorrection;

    private final RotateConstructor angleSmooth;

    public AngleConfig(boolean moveCorr, boolean freeCorr) {
        this(new HumanAngle(), moveCorr, freeCorr);
    }

    public AngleConfig(boolean moveCorr) {
        this(new HumanAngle(), moveCorr, true);
    }

    public AngleConfig(RotateConstructor smooth, boolean moveCorr, boolean freeCorr) {
        this.angleSmooth = smooth;
        moveCorrection   = moveCorr;
        freeCorrection   = freeCorr;
    }

    public AngleConstructor createRotationPlan(Angle angle, Vec3d vec, Entity entity, int ticks) {
        return new AngleConstructor(angle, vec, entity, angleSmooth, ticks, 1.0F, moveCorrection, freeCorrection);
    }

    public AngleConstructor createRotationPlan(Angle angle, Vec3d vec, Entity entity, boolean moveCorr, boolean freeCorr) {
        return new AngleConstructor(angle, vec, entity, angleSmooth, 1, 1.0F, moveCorr, freeCorr);
    }
}
