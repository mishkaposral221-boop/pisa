package rich.modules.impl.combat;

import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.Setting;
import rich.modules.module.setting.implement.SliderSettings;

public class Triggerbot extends ModuleStructure {

    public static volatile boolean SUPPRESS_FORWARD = false;
    public static volatile boolean SUPPRESS_JUMP    = false;
    public static volatile boolean SUPPRESS_SPRINT  = false;

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
    }

    public void onTick(TickEvent event) {
        SUPPRESS_FORWARD = false;
        SUPPRESS_SPRINT  = false;
        SUPPRESS_JUMP    = false;
    }
}
