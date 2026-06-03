package rich;

import net.minecraft.class_1041;
import net.minecraft.class_289;
import net.minecraft.class_310;
import net.minecraft.class_9779;
import rich.util.render.draw.DrawEngine;
import rich.util.render.draw.DrawEngineImpl;

public interface IMinecraft {
   class_310 mc = class_310.method_1551();
   class_1041 window = class_310.method_1551().method_22683();
   class_289 tessellator = class_289.method_1348();
   class_9779 tickCounter = mc.method_61966();
   DrawEngine drawEngine = new DrawEngineImpl();
}
