package rich.modules.impl.render.particles;

import net.minecraft.entity.Entity;

public class TotemEmitter {
   private final Entity entity;
   private final int maxAge;
   private int age;

   public TotemEmitter(Entity var1, int var2) {
      this.entity = var1;
      this.maxAge = var2;
      this.age = 0;
   }

   public void tick() {
      this.age++;
   }

   public boolean isAlive() {
      return this.age < this.maxAge && this.entity != null && !this.entity.isRemoved();
   }

   public float getProgress() {
      return (float)this.age / this.maxAge;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public int getMaxAge() {
      return this.maxAge;
   }

   public int getAge() {
      return this.age;
   }
}
