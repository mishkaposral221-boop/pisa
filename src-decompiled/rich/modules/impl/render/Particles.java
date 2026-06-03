package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1667;
import net.minecraft.class_1682;
import net.minecraft.class_1685;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597.class_4598;
import rich.events.api.EventHandler;
import rich.events.impl.AttackEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.render.particles.Particle3D;
import rich.modules.impl.render.particles.TotemEmitter;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;

public class Particles extends ModuleStructure {
   private final List<Particle3D> particles = new ArrayList<>(128);
   private final List<TotemEmitter> totemEmitters = new ArrayList<>(4);
   public SelectSetting mode = new SelectSetting("Режим", "Тип партиклов")
      .value("Кубы", "Корона", "Куб", "Доллар", "Сердце", "Молния", "Линия", "Ромб", "Снежинка", "Звезда", "Звезда 2", "Треугольник", "Рандом")
      .selected("Звезда");
   public SelectSetting glowMode = new SelectSetting("Свечение", "Тип эффекта свечения").value("Bloom", "Bloom Sample", "Оба").selected("Bloom Sample");
   public MultiSelectSetting triggers = new MultiSelectSetting("Триггеры", "Когда спавнить партиклы")
      .value("Удар", "Тотем", "Ходьба", "Бросаемый предмет")
      .selected("Удар", "Тотем", "Ходьба", "Бросаемый предмет");
   public SliderSettings amount = new SliderSettings("Количество", "Кол-во партиклов при ударе").range(5, 25).setValue(20.0F);
   public SliderSettings walkAmount = new SliderSettings("Кол-во при ходьбе", "Кол-во партиклов в секунду при ходьбе")
      .range(5, 20)
      .setValue(15.0F)
      .visible(() -> this.triggers.isSelected("Ходьба"));
   public SliderSettings spread = new SliderSettings("Разброс", "Сила разброса частиц в стороны").range(0.5F, 3.0F).setValue(1.0F);
   public SliderSettings speed = new SliderSettings("Скорость", "Скорость движения частиц").range(0.1F, 3.0F).setValue(2.0F);
   public SliderSettings lifeTime = new SliderSettings("Время жизни", "Время жизни частиц в секундах").range(0.5F, 5.0F).setValue(2.0F);
   public SliderSettings size = new SliderSettings("Размер", "Размер частиц").range(0.1F, 1.0F).setValue(1.0F);
   public BooleanSetting randomColor = new BooleanSetting("Рандомный цвет", "Каждый партикл получает случайный цвет").setValue(false);
   public ColorSetting color = new ColorSetting("Цвет", "Цвет партиклов").value(-7773880).visible(() -> !this.randomColor.isValue());
   private static final float GLOW_SIZE = 7.5F;
   private static final int TOTEM_DURATION = 20;
   private static final float GRAVITY_STRENGTH = 0.04F;
   private static final int MAX_PARTICLES = 150;
   private static final int[] RANDOM_COLORS = new int[]{-65536, -33024, -256, -16711936, -16711681, -16776961, -7667457, -65281, -60269, -1, -16711809, -40121};
   private static final int[] TOTEM_COLORS = new int[]{-8586240, -10496, -13447886, -23296, -16711936, -5374161};
   private float walkParticleAccumulator = 0.0F;
   private Particle3D.ParticleMode cachedMode;
   private Particle3D.GlowMode cachedGlowMode;
   private float cachedSpread;
   private float cachedSpeed;
   private float cachedLifeTime;
   private float cachedSize;
   private int cachedColor;
   private boolean cachedRandomColor;

   public static Particles getInstance() {
      return c.a(Particles.class);
   }

   public Particles() {
      super("Particles", "Custom particles system", ModuleCategory.VISUALS);
      this.settings(
         this.mode, this.glowMode, this.triggers, this.amount, this.walkAmount, this.spread, this.speed, this.lifeTime, this.size, this.randomColor, this.color
      );
   }

   @Override
   public void deactivate() {
      this.particles.clear();
      this.totemEmitters.clear();
      this.walkParticleAccumulator = 0.0F;
   }

   private void refreshCache() {
      this.cachedMode = this.getParticleMode();
      this.cachedGlowMode = this.getGlowModeValue();
      this.cachedSpread = this.spread.getValue();
      this.cachedSpeed = this.speed.getValue();
      this.cachedLifeTime = this.lifeTime.getValue();
      this.cachedSize = this.size.getValue();
      this.cachedRandomColor = this.randomColor.isValue();
      this.cachedColor = this.color.getColor();
   }

   private int getParticleColor() {
      return this.cachedRandomColor ? RANDOM_COLORS[ThreadLocalRandom.current().nextInt(RANDOM_COLORS.length)] : this.cachedColor;
   }

   private float getGravity() {
      return 0.004F;
   }

   private Particle3D.ParticleMode getParticleMode() {
      return switch (this.mode.getSelected()) {
         case "Кубы" -> Particle3D.ParticleMode.CUBES;
         case "Корона" -> Particle3D.ParticleMode.CROWN;
         case "Куб" -> Particle3D.ParticleMode.CUBE_BLAST;
         case "Доллар" -> Particle3D.ParticleMode.DOLLAR;
         case "Сердце" -> Particle3D.ParticleMode.HEART;
         case "Молния" -> Particle3D.ParticleMode.LIGHTNING;
         case "Линия" -> Particle3D.ParticleMode.LINE;
         case "Ромб" -> Particle3D.ParticleMode.RHOMBUS;
         case "Снежинка" -> Particle3D.ParticleMode.SNOWFLAKE;
         case "Звезда" -> Particle3D.ParticleMode.STAR;
         case "Звезда 2" -> Particle3D.ParticleMode.STAR_ALT;
         case "Треугольник" -> Particle3D.ParticleMode.TRIANGLE;
         case "Рандом" -> Particle3D.ParticleMode.RANDOM;
         default -> Particle3D.ParticleMode.CUBES;
      };
   }

   private Particle3D.GlowMode getGlowModeValue() {
      return switch (this.glowMode.getSelected()) {
         case "Bloom" -> Particle3D.GlowMode.BLOOM;
         case "Bloom Sample" -> Particle3D.GlowMode.BLOOM_SAMPLE;
         default -> Particle3D.GlowMode.BOTH;
      };
   }

   private Particle3D spawnParticle(class_243 var1, class_243 var2, float var3, float var4) {
      return new Particle3D(var1, var2, this.getParticleColor(), var3, var4)
         .setGravity(this.getGravity())
         .setVelocityMultiplier(0.99F)
         .setMode(this.cachedMode)
         .setGlowMode(this.cachedGlowMode);
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.refreshCache();
         if (this.triggers.isSelected("Ходьба")) {
            this.handleWalkParticles();
         }

         if (this.triggers.isSelected("Бросаемый предмет")) {
            this.handleProjectileParticles();
         }

         Iterator var2 = this.totemEmitters.iterator();

         while (var2.hasNext()) {
            TotemEmitter var3 = (TotemEmitter)var2.next();
            var3.tick();
            if (var3.isAlive()) {
               this.spawnTotemParticlesBurst(var3.getEntity(), var3.getProgress());
            } else {
               var2.remove();
            }
         }

         this.particles.removeIf(var0 -> {
            var0.update();
            return var0.isDead();
         });
      }
   }

   private void handleWalkParticles() {
      double var1 = mc.field_1724.method_18798().method_1027();
      if (!(var1 <= 1.0E-4) && !mc.field_1724.method_5715()) {
         float var3 = this.walkAmount.getValue() / 20.0F;
         this.walkParticleAccumulator += var3;
         int var4 = (int)this.walkParticleAccumulator;
         this.walkParticleAccumulator -= var4;
         if (var4 > 0) {
            float var5 = mc.field_1724.method_36454();
            double var6 = Math.toRadians(var5 + 90.0F);
            double var8 = Math.cos(var6) * 0.5;
            double var10 = Math.sin(var6) * 0.5;
            float var12 = this.cachedSpread * 0.05F;
            float var13 = this.cachedSpeed;
            ThreadLocalRandom var14 = ThreadLocalRandom.current();

            for (int var15 = 0; var15 < var4 && this.particles.size() < 150; var15++) {
               double var16 = mc.field_1724.method_23317() - var8 + (var14.nextDouble() - 0.5) * 0.3;
               double var18 = mc.field_1724.method_23318() + 0.3 + var14.nextDouble() * (mc.field_1724.method_17682() - 0.3);
               double var20 = mc.field_1724.method_23321() - var10 + (var14.nextDouble() - 0.5) * 0.3;
               class_243 var22 = new class_243(
                  (var14.nextDouble() - 0.5) * var12 * var13, (var14.nextDouble() - 0.5) * var12 * 0.5 * var13, (var14.nextDouble() - 0.5) * var12 * var13
               );
               this.particles.add(this.spawnParticle(new class_243(var16, var18, var20), var22, this.cachedSize * 0.6F, this.cachedLifeTime * 0.5F));
            }
         }
      } else {
         this.walkParticleAccumulator = 0.0F;
      }
   }

   private void handleProjectileParticles() {
      if (this.particles.size() < 150) {
         float var1 = this.cachedSpread * 0.03F;
         float var2 = this.cachedSpeed;
         ThreadLocalRandom var3 = ThreadLocalRandom.current();

         for (class_1297 var5 : mc.field_1687.method_18112()) {
            if (var5 instanceof class_1682 || var5 instanceof class_1667 || var5 instanceof class_1685) {
               if (this.particles.size() >= 150) {
                  break;
               }

               class_243 var6 = var5.method_18798();
               boolean var7 = var6.method_1027() > 0.01
                  || Math.abs(var5.method_23317() - var5.field_6014) > 0.01
                  || Math.abs(var5.method_23321() - var5.field_5969) > 0.01;
               if (var7) {
                  class_243 var8 = new class_243(
                     var5.method_23317() + (var3.nextDouble() - 0.5) * 0.5,
                     var5.method_23318() + var3.nextDouble() * var5.method_17682(),
                     var5.method_23321() + (var3.nextDouble() - 0.5) * 0.5
                  );
                  class_243 var9 = new class_243(
                     (var3.nextDouble() - 0.5) * 2.0 * var1 * var2,
                     (var3.nextDouble() - 0.5) * 2.0 * var1 * var2,
                     (var3.nextDouble() - 0.5) * 2.0 * var1 * var2
                  );
                  this.particles.add(this.spawnParticle(var8, var9, this.cachedSize * 0.5F, this.cachedLifeTime * 0.3F));
               }
            }
         }
      }
   }

   @EventHandler
   public void onAttack(AttackEvent var1) {
      if (this.triggers.isSelected("Удар") && var1.getTarget() != null) {
         if (this.particles.size() < 150) {
            class_1297 var2 = var1.getTarget();
            float var3 = this.cachedSpread * 0.15F;
            float var4 = this.cachedSpeed;
            int var5 = Math.min(this.amount.getInt(), 150 - this.particles.size());
            ThreadLocalRandom var6 = ThreadLocalRandom.current();

            for (int var7 = 0; var7 < var5; var7++) {
               class_243 var8 = new class_243(var2.method_23317(), var2.method_23318() + var6.nextDouble() * var2.method_17682(), var2.method_23321());
               class_243 var9 = new class_243(
                  (var6.nextDouble() - 0.5) * 2.0 * var3 * var4, (var6.nextDouble() - 0.5) * 2.0 * var3 * var4, (var6.nextDouble() - 0.5) * 2.0 * var3 * var4
               );
               this.particles.add(this.spawnParticle(var8, var9, this.cachedSize, this.cachedLifeTime));
            }
         }
      }
   }

   public void onTotemPop(class_1297 var1) {
      if (this.triggers.isSelected("Тотем")) {
         this.totemEmitters.add(new TotemEmitter(var1, 20));
      }
   }

   private void spawnTotemParticlesBurst(class_1297 var1, float var2) {
      if (var1 != null && !var1.method_31481()) {
         float var3 = 1.0F - var2 * 0.5F;
         float var4 = this.cachedSpeed;
         ThreadLocalRandom var5 = ThreadLocalRandom.current();

         for (int var6 = 0; var6 < 3 && this.particles.size() < 150; var6++) {
            double var7 = var5.nextDouble() * 2.0 - 1.0;
            double var9 = var5.nextDouble() * 2.0 - 1.0;
            double var11 = var5.nextDouble() * 2.0 - 1.0;
            if (!(var7 * var7 + var9 * var9 + var11 * var11 > 1.0)) {
               class_243 var13 = new class_243(
                  var1.method_23317() + var7 * var1.method_17681() * 0.5,
                  var1.method_23323(0.5) + var9 * var1.method_17682() * 0.5,
                  var1.method_23321() + var11 * var1.method_17681() * 0.5
               );
               double var14 = this.cachedSpread * 0.18 * var3 * var4;
               double var16 = (var5.nextDouble() < 0.4 ? 0.15 + var5.nextDouble() * 0.2 : 0.03 + var5.nextDouble() * 0.07) * var4;
               class_243 var18 = new class_243(var7 * var14, var16, var11 * var14);
               int var19 = TOTEM_COLORS[var5.nextInt(TOTEM_COLORS.length)];
               this.particles
                  .add(
                     new Particle3D(var13, var18, var19, this.cachedSize * 0.8F, this.cachedLifeTime * 0.8F)
                        .setGravity(this.getGravity())
                        .setVelocityMultiplier(0.98F)
                        .setMode(this.cachedMode)
                        .setGlowMode(this.cachedGlowMode)
                  );
            }
         }
      }
   }

   @EventHandler
   public void onRender3D(WorldRenderEvent var1) {
      if (!this.particles.isEmpty()) {
         class_4587 var2 = var1.getStack();
         class_4598 var3 = mc.method_22940().method_23000();
         float var4 = var1.getPartialTicks();
         int var5 = 0;

         for (int var6 = this.particles.size(); var5 < var6; var5++) {
            this.particles.get(var5).render(var2, var3, 7.5F, var4);
         }
      }
   }
}
