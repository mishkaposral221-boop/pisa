package rich.modules.impl.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1309;
import rich.events.api.EventHandler;
import rich.events.impl.AttackEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.a;
import rich.util.c;

public class HitEffect extends ModuleStructure {
   private static final long ENTITY_TINT_DURATION_MS = 350L;
   private final Map<Integer, Long> hitTintEntities = new HashMap<>();
   public ColorSetting colorSetting = new ColorSetting("Цвет", "Выберите цвет для эффекта").setColor(new Color(137, 97, 72, 255).getRGB());
   public SliderSettings alphaSetting = new SliderSettings("Прозрачность", "Прозрачность цвета сущности").range(0.0F, 255.0F).setValue(255.0F);

   public static HitEffect getInstance() {
      return c.a(HitEffect.class);
   }

   public HitEffect() {
      super("HitEffect", "Hit Effect", ModuleCategory.VISUALS);
      this.settings(this.colorSetting, this.alphaSetting);
   }

   @EventHandler
   public void onAttack(AttackEvent var1) {
      if (this.isState() && var1.getTarget() != null) {
         if (var1.getTarget() instanceof class_1309 var2) {
            this.hitTintEntities.put(var2.method_5628(), System.currentTimeMillis() + 350L);
         }
      }
   }

   public boolean shouldTintEntity(int var1) {
      if (!this.isState()) {
         return false;
      } else {
         Long var2 = this.hitTintEntities.get(var1);
         if (var2 == null) {
            return false;
         } else if (System.currentTimeMillis() > var2) {
            this.hitTintEntities.remove(var1);
            return false;
         } else {
            return true;
         }
      }
   }

   public int getEntityTintColor() {
      return a.h(this.colorSetting.getColor(), (int)this.alphaSetting.getValue());
   }
}
