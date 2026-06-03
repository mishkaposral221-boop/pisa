package rich.modules.impl.render.particles;

import net.minecraft.class_1297;

public class TotemEmitter {
   private final class_1297 entity;
   private final int maxAge;
   private int age;

   public TotemEmitter(class_1297 var1, int var2) {
      this.entity = var1;
      this.maxAge = var2;
      this.age = 0;
   }

   public void tick() {
      this.age++;
   }

   public boolean isAlive() {
      return this.age < this.maxAge && this.entity != null && !this.entity.method_31481();
   }

   public float getProgress() {
      return (float)this.age / this.maxAge;
   }

   public class_1297 getEntity() {
      return this.entity;
   }

   public int getMaxAge() {
      return this.maxAge;
   }

   public int getAge() {
      return this.age;
   }
}
