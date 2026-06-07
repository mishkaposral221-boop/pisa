package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.c;

/**
 * Chams - renders enemy players through walls with their REAL skin/armor/items texture.
 * No colour tint: the player looks exactly as normal but is visible through walls
 * (NO_DEPTH_TEST pipeline in ClientPipelines#CHAMS_ENTITY).
 */
public class Chams extends ModuleStructure {

    /** Set true by LivingEntityRendererMixin while rendering a Chams target player. */
    public static volatile boolean RICH$EQUIPMENT_TARGET = false;

    /** Show armor through walls. */
    public BooleanSetting showArmor = new BooleanSetting("ShowArmor", "Show armor through walls")
            .setValue(true);

    /** Show held items through walls. */
    public BooleanSetting showItems = new BooleanSetting("ShowItems", "Show held items through walls")
            .setValue(true);

    public static Chams getInstance() {
        return c.a(Chams.class);
    }

    public Chams() {
        super("Chams", "Render entity models through walls", ModuleCategory.VISUALS);
        this.settings(this.showArmor, this.showItems);
    }
}
