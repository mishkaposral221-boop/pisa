package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.c;

public class NoNausea extends ModuleStructure {
   public static NoNausea getInstance() {
      return c.a(NoNausea.class);
   }

   public NoNausea() {
      super("NoNausea", "No Nausea", ModuleCategory.UTILITIES);
   }
}
