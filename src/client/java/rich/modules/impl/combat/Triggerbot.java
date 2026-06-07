package rich.modules.impl.combat;

import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.Setting;
import rich.modules.module.setting.implement.SliderSettings;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1656;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3489;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_746;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class Triggerbot extends ModuleStructure {

    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

    private static final Logger LOG = LoggerFactory.getLogger("Triggerbot");

    private static final float GROUND_ATTACK_CHARGE = 0.93f;
    private static final float CRIT_CHARGE          = 0.84f;
    private static final int   GROUND_COMBO_DELAY   = 2;

    private class_1297 pendingTarget    = null;
    private int        preAttackCountdown = 0;
    private boolean    pendingWasForward  = false;
    private String     lastDiag           = "";
    private int        ticksOutOfWater    = 10;
    private int        ticksOnGround      = 0;

    private final SliderSettings noCritCharge;
    private final SliderSettings jumpCharge;

    public Triggerbot() {
        super("Triggerbot", "Auto-attack targeted entities", ModuleCategory.VISUALS);
        this.noCritCharge = new SliderSettings("Charge under debuff", "Min weapon charge when crit is impossible")
                .setValue(0.78f)
                .range(0.30f, 1.0f);
        this.jumpCharge = new SliderSettings("Jump charge", "Min charge before held-jump fires")
                .setValue(0.5f)
                .range(0.30f, 1.0f);
        settings(new Setting[]{noCritCharge, jumpCharge});
    }

    public static Triggerbot getInstance() {
        return (Triggerbot) rich.util.c.a(Triggerbot.class);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        SUPPRESS_FORWARD = false;
        SUPPRESS_SPRINT  = false;
        SUPPRESS_JUMP    = false;
        pendingTarget      = null;
        preAttackCountdown = 0;
        ticksOutOfWater    = 0;
        ticksOnGround      = 0;
        lastDiag           = "";
    }

    public void onTick(TickEvent event) {
        boolean suppressForward = false;
        boolean suppressJump    = false;

        try {
            class_310 mc = Triggerbot.mc;
            if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1755 != null) {
                ticksOnGround  = 0;
                pendingTarget  = null;
                SUPPRESS_FORWARD = suppressForward;
                SUPPRESS_SPRINT  = false;
                SUPPRESS_JUMP    = suppressJump;
                return;
            }

            // water tracking
            if (isInWater()) {
                ticksOutOfWater = 0;
            } else if (ticksOutOfWater < 100) {
                ticksOutOfWater++;
            }

            // ground tracking
            if (mc.field_1724.method_24828()) {
                if (ticksOnGround < 100) ticksOnGround++;
            } else {
                ticksOnGround = 0;
            }

            // --- pending attack ---
            if (pendingTarget != null) {
                if (!isEntityValid(pendingTarget)) {
                    pendingTarget      = null;
                    preAttackCountdown = 0;
                    SUPPRESS_FORWARD = suppressForward;
                    SUPPRESS_SPRINT  = false;
                    SUPPRESS_JUMP    = suppressJump;
                    return;
                }
                if (preAttackCountdown > 0) {
                    preAttackCountdown--;
                    suppressForward = true;
                    SUPPRESS_FORWARD = suppressForward;
                    SUPPRESS_SPRINT  = false;
                    SUPPRESS_JUMP    = suppressJump;
                    return;
                }
                class_1297 t  = pendingTarget;
                boolean   fwd = pendingWasForward;
                pendingTarget      = null;
                preAttackCountdown = 0;
                doAttack(t, fwd);
                diag("CRIT_FIRE", fwd ? "fwd=true" : "fwd=false");
                SUPPRESS_FORWARD = suppressForward;
                SUPPRESS_SPRINT  = false;
                SUPPRESS_JUMP    = suppressJump;
                return;
            }

            // --- scan crosshair ---
            class_1297 target = mc.field_1692;
            if (!canHit(target)) {
                SUPPRESS_FORWARD = suppressForward;
                SUPPRESS_SPRINT  = false;
                SUPPRESS_JUMP    = suppressJump;
                return;
            }

            boolean critOk = critAchievable();
            float   chg    = charge();

            // Water path
            if (isInWater()) {
                if (chg >= GROUND_ATTACK_CHARGE) {
                    doAttack(target, mc.field_1690.field_1894.method_1434());
                    diag("WATER", "HIT water");
                }
                SUPPRESS_FORWARD = suppressForward;
                SUPPRESS_SPRINT  = false;
                SUPPRESS_JUMP    = suppressJump;
                return;
            }

            // No-crit path
            if (!critOk) {
                float minChg = noCritCharge.getValue();
                if (chg >= minChg) {
                    doAttack(target, mc.field_1690.field_1894.method_1434());
                    diag("NOCRIT", fmt((double) chg));
                } else {
                    diag("NOCRIT_WAIT", fmt((double) chg));
                }
                SUPPRESS_FORWARD = suppressForward;
                SUPPRESS_SPRINT  = false;
                SUPPRESS_JUMP    = suppressJump;
                return;
            }

            // Airborne
            if (!mc.field_1724.method_24828()) {
                if (!isJumpHeld() && mc.field_1724.method_18798().field_1351 <= 0.0) {
                    // falling
                } else {
                    if (isJumpHeld() && chg < jumpCharge.getValue()) {
                        suppressJump = true;
                        diag("JUMP_GATE", fmt((double) chg));
                    } else {
                        diag("GROUND_HOLD", fmt((double) chg));
                    }
                    SUPPRESS_FORWARD = suppressForward;
                    SUPPRESS_SPRINT  = false;
                    SUPPRESS_JUMP    = suppressJump;
                    return;
                }

                String blocker = critBlocker();
                if (blocker == null && chg >= CRIT_CHARGE) {
                    pendingTarget      = target;
                    preAttackCountdown = 0;
                    pendingWasForward  = mc.field_1690.field_1894.method_1434();
                    suppressForward    = true;
                    diag("CRIT_Q", fmt((double) chg));
                } else {
                    diag("AIR_BLOCK", blocker + " " + fmt((double) chg));
                }
                SUPPRESS_FORWARD = suppressForward;
                SUPPRESS_SPRINT  = false;
                SUPPRESS_JUMP    = suppressJump;
                return;
            }

            // On-ground combo
            if (chg >= GROUND_ATTACK_CHARGE && ticksOnGround >= GROUND_COMBO_DELAY) {
                doAttack(target, mc.field_1690.field_1894.method_1434());
                diag("COMBO", fmt((double) chg));
            } else {
                diag("GROUND_WAIT", fmt((double) chg) + " g=" + ticksOnGround);
            }

        } finally {
            SUPPRESS_FORWARD = suppressForward;
            SUPPRESS_SPRINT  = false;
            SUPPRESS_JUMP    = suppressJump;
        }
    }

    private void doAttack(class_1297 target, boolean wasForward) {
        mc.field_1724.method_5728(false);
        ((class_636) mc.field_1761).method_2918(mc.field_1724, target);
        mc.field_1724.method_6104(class_1268.field_5808);
        if (wasForward && mc.field_1690.field_1894.method_1434()) {
            mc.field_1724.method_5728(true);
        }
    }

    private boolean isEntityValid(class_1297 entity) {
        if (!(entity instanceof class_1309)) return false;
        class_1309 living = (class_1309) entity;
        if (!living.method_5805()) return false;
        if (mc.field_1687 == null) return false;
        return mc.field_1687.method_8469(entity.method_5628()) != null;
    }

    private boolean canHit(class_1297 entity) {
        if (!(entity instanceof class_1309)) return false;
        class_1309 living = (class_1309) entity;
        if (!living.method_5805()) return false;
        if (mc.field_1724.method_6115()) return false;
        return isWeaponInHand();
    }

    private float charge() {
        return mc.field_1724.method_7261(0f);
    }

    private boolean isWeaponInHand() {
        class_1799 inv   = mc.field_1724.method_6047();
        class_1792 stack = inv.method_7909();
        var tags = stack.method_40131();
        if (tags.method_40220(class_3489.field_42611)) return true;
        if (tags.method_40220(class_3489.field_42612)) return true;
        if (tags.method_40220(class_3489.field_63258)) return true;
        return false;
    }

    private boolean isInWater() {
        if (mc.field_1724.method_5799()) return true;
        if (mc.field_1724.method_5869()) return true;
        return mc.field_1724.method_5681();
    }

    private boolean isJumpHeld() {
        try {
            return mc.field_1690.field_1903.method_1434();
        } catch (Exception e) {
            return false;
        }
    }

    private String critBlocker() {
        if (mc.field_1724.field_6017 <= 0.0) return "fall<=0";
        if (ticksOutOfWater < 3)             return "justLeftWater";
        if (mc.field_1724.method_24828())    return "onGround";
        if (mc.field_1724.method_6101())     return "climbing";
        if (mc.field_1724.method_5799())     return "water";
        if (mc.field_1724.method_6059(class_1294.field_5902)) return "levitation";
        if (mc.field_1724.method_6059(class_1294.field_5919)) return "blindness";
        if (mc.field_1724.method_5765())     return "vehicle";
        if (mc.field_1724.method_31549().field_7479) return "flying";
        return null;
    }

    private boolean critAchievable() {
        if (ticksOutOfWater < 3)             return false;
        if (mc.field_1724.method_5799())     return false;
        if (mc.field_1724.method_6101())     return false;
        if (mc.field_1724.method_6059(class_1294.field_5902)) return false;
        if (mc.field_1724.method_6059(class_1294.field_5919)) return false;
        if (mc.field_1724.method_5765())     return false;
        if (mc.field_1724.method_31549().field_7479) return false;
        return true;
    }

    private void diag(String state, String msg) {
        if (!state.equals(lastDiag)) {
            lastDiag = state;
            LOG.info(msg);
        }
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%.2f", v);
    }
}
