package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.util.math.MatrixStack;
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
import rich.util.profiler.FrameProfiler;

public class Particles extends ModuleStructure {
   private final List<Particle3D> particles = new ArrayList<>(48);
   private final List<TotemEmitter> totemEmitters = new ArrayList<>(4);
   public SelectSetting mode = new SelectSetting("Режим", "Тип партиклов")
      .value("Кубы", "Корона", "Куб", "Доллар", "Сердце", "Молния", "Линия", "Ромб", "Снежинка", "Звезда", "Звезда 2", "Треугольник", "Рандом")
      .selected("Звезда");
   public SelectSetting glowMode = new SelectSetting("Свечение", "Тип эффекта свечения").value("Bloom", "Bloom Sample", "Оба").selected("Bloom Sample");
   public MultiSelectSetting triggers = new MultiSelectSetting("Триггеры", "Когда спавнить партиклы")
      .value("Удар", "Тотем", "Ходьба", "Бросаемый предмет")
      .selected("Удар");
   public SliderSettings amount = new SliderSettings("Количество", "Кол-во партиклов при ударе").range(1, 12).setValue(6.0F);
   public SliderSettings walkAmount = new SliderSettings("Кол-во при ходьбе", "Кол-во партиклов в секунду при ходьбе")
      .range(1, 6)
      .setValue(2.0F)
      .visible(() -> this.triggers.isSelected("Ходьба"));
   public SliderSettings spread = new SliderSettings("Разброс", "Сила разброса частиц в стороны").range(0.5F, 3.0F).setValue(1.0F);
   public SliderSettings speed = new SliderSettings("Скорость", "Скорость движения частиц").range(0.1F, 3.0F).setValue(1.2F);
   public SliderSettings lifeTime = new SliderSettings("Время жизни", "Время жизни частиц в секундах").range(0.5F, 5.0F).setValue(1.0F);
   public SliderSettings size = new SliderSettings("Размер", "Размер частиц").range(0.1F, 1.0F).setValue(0.7F);
   public BooleanSetting randomColor = new BooleanSetting("Рандомный цвет", "Каждый партикл получает случайный цвет").setValue(false);
   public ColorSetting color = new ColorSetting("Цвет", "Цвет партиклов").value(-7773880).visible(() -> !this.randomColor.isValue());

   private static final float GLOW_SIZE = 0.0F;
   private static final int TOTEM_DURATION = 12;
   private static final int MAX_PARTICLES = 40;
   private static final int MAX_RENDER_PER_FRAME = 40;
   private static final long PROJECTILE_SCAN_MS = 250L;
   private static final int[] RANDOM_COLORS = new int[]{-65536, -33024, -256, -16711936, -16711681, -16776961, -7667457, -65281, -60269, -1, -16711809, -40121};
   private static final int[] TOTEM_COLORS = new int[]{-8586240, -10496, -13447886, -23296, -16711936, -5374161};

   private float walkParticleAccumulator = 0.0F;
   private long lastProjectileScan = 0L;
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
      this.cachedSpeed = Math.min(this.speed.getValue(), 1.5F);
      this.cachedLifeTime = Math.min(this.lifeTime.getValue(), 1.25F);
      this.cachedSize = Math.min(this.size.getValue(), 0.8F);
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
      return Particle3D.GlowMode.BLOOM_SAMPLE;
   }

   private Particle3D spawnParticle(Vec3d var1, Vec3d var2, float var3, float var4) {
      return new Particle3D(var1, var2, this.getParticleColor(), var3, var4)
         .setGravity(this.getGravity())
         .setVelocityMultiplier(0.99F)
         .setMode(this.cachedMode)
         .setGlowMode(this.cachedGlowMode);
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.player == null || mc.world == null) {
         return;
      }

      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      if (prof) profiler.begin("Particles/tick");
      try {
         this.refreshCache();
         this.trimOverflow();

         if (this.triggers.isSelected("Ходьба")) {
            this.handleWalkParticles();
         } else {
            this.walkParticleAccumulator = 0.0F;
         }

         long now = System.currentTimeMillis();
         if (this.triggers.isSelected("Бросаемый предмет") && now - this.lastProjectileScan >= PROJECTILE_SCAN_MS) {
            this.lastProjectileScan = now;
            this.handleProjectileParticles();
         }

         Iterator<TotemEmitter> var2 = this.totemEmitters.iterator();
         while (var2.hasNext()) {
            TotemEmitter var3 = var2.next();
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
      } finally {
         if (prof) profiler.end();
      }
   }

   private void trimOverflow() {
      while (this.particles.size() > MAX_PARTICLES) {
         this.particles.remove(0);
      }
   }

   private void handleWalkParticles() {
      double var1 = mc.player.getVelocity().lengthSquared();
      if (!(var1 <= 1.0E-4) && !mc.player.isSneaking()) {
         float var3 = Math.min(this.walkAmount.getValue(), 3.0F) / 20.0F;
         this.walkParticleAccumulator += var3;
         int var4 = Math.min((int)this.walkParticleAccumulator, 1);
         this.walkParticleAccumulator -= var4;
         if (var4 > 0) {
            float var5 = mc.player.getYaw();
            double var6 = Math.toRadians(var5 + 90.0F);
            double var8 = Math.cos(var6) * 0.5;
            double var10 = Math.sin(var6) * 0.5;
            float var12 = this.cachedSpread * 0.05F;
            float var13 = this.cachedSpeed;
            ThreadLocalRandom var14 = ThreadLocalRandom.current();

            for (int var15 = 0; var15 < var4 && this.particles.size() < MAX_PARTICLES; var15++) {
               double var16 = mc.player.getX() - var8 + (var14.nextDouble() - 0.5) * 0.3;
               double var18 = mc.player.getY() + 0.3 + var14.nextDouble() * (mc.player.getHeight() - 0.3);
               double var20 = mc.player.getZ() - var10 + (var14.nextDouble() - 0.5) * 0.3;
               Vec3d var22 = new Vec3d(
                  (var14.nextDouble() - 0.5) * var12 * var13, (var14.nextDouble() - 0.5) * var12 * 0.5 * var13, (var14.nextDouble() - 0.5) * var12 * var13
               );
               this.particles.add(this.spawnParticle(new Vec3d(var16, var18, var20), var22, this.cachedSize * 0.5F, this.cachedLifeTime * 0.5F));
            }
         }
      } else {
         this.walkParticleAccumulator = 0.0F;
      }
   }

   private void handleProjectileParticles() {
      if (this.particles.size() >= MAX_PARTICLES) {
         return;
      }

      float var1 = this.cachedSpread * 0.03F;
      float var2 = this.cachedSpeed;
      ThreadLocalRandom var3 = ThreadLocalRandom.current();
      int checked = 0;

      for (Entity var5 : mc.world.getEntities()) {
         if (++checked > 64 || this.particles.size() >= MAX_PARTICLES) {
            break;
         }

         if (var5 instanceof ThrownEntity || var5 instanceof ArrowEntity || var5 instanceof TridentEntity) {
            Vec3d var6 = var5.getVelocity();
            boolean var7 = var6.lengthSquared() > 0.01
               || Math.abs(var5.getX() - var5.lastX) > 0.01
               || Math.abs(var5.getZ() - var5.lastZ) > 0.01;
            if (var7) {
               Vec3d var8 = new Vec3d(
                  var5.getX() + (var3.nextDouble() - 0.5) * 0.5,
                  var5.getY() + var3.nextDouble() * var5.getHeight(),
                  var5.getZ() + (var3.nextDouble() - 0.5) * 0.5
               );
               Vec3d var9 = new Vec3d(
                  (var3.nextDouble() - 0.5) * 2.0 * var1 * var2,
                  (var3.nextDouble() - 0.5) * 2.0 * var1 * var2,
                  (var3.nextDouble() - 0.5) * 2.0 * var1 * var2
               );
               this.particles.add(this.spawnParticle(var8, var9, this.cachedSize * 0.45F, this.cachedLifeTime * 0.25F));
            }
         }
      }
   }

   @EventHandler
   public void onAttack(AttackEvent var1) {
      if (this.triggers.isSelected("Удар") && var1.getTarget() != null && this.particles.size() < MAX_PARTICLES) {
         Entity var2 = var1.getTarget();
         float var3 = this.cachedSpread * 0.15F;
         float var4 = this.cachedSpeed;
         int var5 = Math.min(Math.min(this.amount.getInt(), 6), MAX_PARTICLES - this.particles.size());
         ThreadLocalRandom var6 = ThreadLocalRandom.current();

         for (int var7 = 0; var7 < var5; var7++) {
            Vec3d var8 = new Vec3d(var2.getX(), var2.getY() + var6.nextDouble() * var2.getHeight(), var2.getZ());
            Vec3d var9 = new Vec3d(
               (var6.nextDouble() - 0.5) * 2.0 * var3 * var4, (var6.nextDouble() - 0.5) * 2.0 * var3 * var4, (var6.nextDouble() - 0.5) * 2.0 * var3 * var4
            );
            this.particles.add(this.spawnParticle(var8, var9, this.cachedSize, this.cachedLifeTime));
         }
      }
   }

   public void onTotemPop(Entity var1) {
      if (this.triggers.isSelected("Тотем")) {
         this.totemEmitters.add(new TotemEmitter(var1, TOTEM_DURATION));
      }
   }

   private void spawnTotemParticlesBurst(Entity var1, float var2) {
      if (var1 != null && !var1.isRemoved()) {
         float var3 = 1.0F - var2 * 0.5F;
         float var4 = this.cachedSpeed;
         ThreadLocalRandom var5 = ThreadLocalRandom.current();

         for (int var6 = 0; var6 < 1 && this.particles.size() < MAX_PARTICLES; var6++) {
            double var7 = var5.nextDouble() * 2.0 - 1.0;
            double var9 = var5.nextDouble() * 2.0 - 1.0;
            double var11 = var5.nextDouble() * 2.0 - 1.0;
            if (!(var7 * var7 + var9 * var9 + var11 * var11 > 1.0)) {
               Vec3d var13 = new Vec3d(
                  var1.getX() + var7 * var1.getWidth() * 0.5,
                  var1.getBodyY(0.5) + var9 * var1.getHeight() * 0.5,
                  var1.getZ() + var11 * var1.getWidth() * 0.5
               );
               double var14 = this.cachedSpread * 0.18 * var3 * var4;
               double var16 = (0.03 + var5.nextDouble() * 0.07) * var4;
               Vec3d var18 = new Vec3d(var7 * var14, var16, var11 * var14);
               int var19 = TOTEM_COLORS[var5.nextInt(TOTEM_COLORS.length)];
               this.particles
                  .add(
                     new Particle3D(var13, var18, var19, this.cachedSize * 0.7F, this.cachedLifeTime * 0.6F)
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
      if (this.particles.isEmpty()) {
         return;
      }

      FrameProfiler profiler = FrameProfiler.getInstance();
      boolean prof = profiler.isEnabled();
      if (prof) profiler.begin("Particles/render3D");
      try {
         MatrixStack var2 = var1.getStack();
         net.minecraft.client.render.VertexConsumerProvider.Immediate var3 = mc.getBufferBuilders().getEntityVertexConsumers();
         float var4 = var1.getPartialTicks();
         int limit = Math.min(this.particles.size(), MAX_RENDER_PER_FRAME);

         for (int i = 0; i < limit; i++) {
            this.particles.get(i).render(var2, var3, GLOW_SIZE, var4);
         }
      } finally {
         if (prof) profiler.end();
      }
   }
}
