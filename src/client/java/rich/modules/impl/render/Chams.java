package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class Chams extends ModuleStructure {
    public static Chams getInstance() {
        return c.a(Chams.class);
    }

    public Chams() {
        super("Chams", "Render entity models through walls", ModuleCategory.VISUALS);
    }
}
