package rich.mixin;

import net.minecraft.class_846.class_851;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import rich.util.interfaces.IBuiltChunkAnimator;

@Mixin(class_851.class)
public class MixinBuiltChunk implements IBuiltChunkAnimator {
   @Unique
   private float animation = 100.0F;

   @Override
   public float getAnimation() {
      return this.animation;
   }

   @Override
   public void setAnimation(float var1) {
      this.animation = var1;
   }
}
