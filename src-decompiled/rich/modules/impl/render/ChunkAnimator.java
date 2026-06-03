package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;

public class ChunkAnimator extends ModuleStructure {
   private static ChunkAnimator instance;
   private final SliderSettings speed = new SliderSettings("Скорость", "").range(1, 20).setValue(10.0F);

   public ChunkAnimator() {
      super("Chunk Animator", "Анимирует появляющиеся чанки", ModuleCategory.VISUALS);
      this.setState(false);
      instance = this;
      this.settings(this.speed);
   }

   public static ChunkAnimator getInstance() {
      return instance;
   }

   public float getSpeed() {
      return this.speed.getValue();
   }
}
