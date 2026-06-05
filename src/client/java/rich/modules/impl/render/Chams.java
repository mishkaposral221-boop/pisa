package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.c;

/**
 * Chams — renders enemy players through walls with their real skin/armor/items texture.
 *
 * <p>No color tint is applied — the player looks exactly like normal but is visible
 * through walls (thanks to the NO_DEPTH_TEST pipeline in {@link rich.util.render.clientpipeline.ClientPipelines#CHAMS_ENTITY}).</p>
 *
 * <p>RICH$EQUIPMENT_TARGET — flag set by LivingEntityRendererMixin to true during
 * render() of a target player. Armor and item mixins read this flag to scope
 * the through-wall layer only to enemy players.</p>
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
