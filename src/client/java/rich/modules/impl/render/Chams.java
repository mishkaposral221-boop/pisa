package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class Chams extends ModuleStructure {
    // Set true by LivingEntityRendererMixin while rendering a Chams target player.
    // The body, armor and held item are all submitted within that same render() call,
    // so the equipment mixins read this flag to scope the no-depth (through-wall)
    // layer to Chams targets only (and never to the local player or non-Chams entities).
    public static volatile boolean RICH$EQUIPMENT_TARGET = false;

    public static Chams getInstance() {
        return c.a(Chams.class);
    }

    public Chams() {
        super("Chams", "Render entity models through walls", ModuleCategory.VISUALS);
    }
}
