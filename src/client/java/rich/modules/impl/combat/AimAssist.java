package rich.modules.impl.combat;

import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.Setting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;

public class AimAssist extends ModuleStructure {

    private final AimEngine engine;

    private final SliderSettings fov;
    private final SliderSettings maxDistance;
    private final SliderSettings smoothness;
    private final SliderSettings throwStrength;
    private final BooleanSetting onlyWeapon;

    public AimAssist() {
        super("AimAssist", "Assist aim towards targets", ModuleCategory.VISUALS);
        this.engine = new AimEngine(this);
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
}
