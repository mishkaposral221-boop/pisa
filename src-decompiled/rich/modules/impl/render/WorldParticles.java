package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597.class_4598;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.render.worldparticles.Particle;
import rich.modules.impl.render.worldparticles.ParticleSpawner;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.c;
import rich.util.timer.StopWatch;

public class WorldParticles extends ModuleStructure {
   private final List<Particle> particles = new ArrayList<>(256);
   private final StopWatch timer = new StopWatch();
   private class_243 lastPlayerPos = class_243.field_1353;
   private class_243 playerVelocity = class_243.field_1353;
   private double playerSpeed = 0.0;
   public SelectSetting mode = new SelectSetting("Режим", "Тип частиц")
      .value("3D Кубы", "Корона", "Куб", "Доллар", "Сердце", "Молния", "Линия", "Ромб", "Снежинка", "Звезда", "Звезда 2", "Треугольник", "Свечение", "Рандом")
      .selected("Звезда");
   public SliderSettings cubeCount = new SliderSettings("Количество", "Количество частиц").range(10.0F, 300.0F).setValue(80.0F);
   public SliderSettings lifeTime = new SliderSettings("Время жизни", "Время жизни (сек)").range(2.0F, 30.0F).setValue(10.0F);
   public SliderSettings size = new SliderSettings("Размер", "Размер частиц").range(0.1F, 1.5F).setValue(1.0F);
   public SliderSettings glowSize = new SliderSettings("Свечение", "Размер свечения").range(0.1F, 5.0F).setValue(3.0F);
   public BooleanSetting physics = new BooleanSetting("Физика", "Частицы падают вниз").setValue(false);
   public BooleanSetting randomColor = new BooleanSetting("Рандомный цвет", "Каждая частица имеет случайный цвет").setValue(false);
   public BooleanSetting whiteOnSpawn = new BooleanSetting("Белые при спавне", "Частицы белые при появлении и плавно меняют цвет").setValue(true);
   public BooleanSetting whiteCenter = new BooleanSetting("Белый центр", "Белый центр у текстурных частиц")
      .setValue(false)
      .visible(() -> !this.mode.getSelected().equals("3D Кубы"));
   public BooleanSetting showGlow = new BooleanSetting("Свечение", "Рендерить свечение (влияет на FPS)").setValue(false);
   public ColorSetting cubeColor = new ColorSetting("Цвет", "Цвет частиц").value(-7773880).visible(() -> !this.randomColor.isValue());
   private Particle.ParticleType cachedType = Particle.ParticleType.CUBE_3D;
   private boolean cachedPhysics = false;
   private float cachedSize = 1.0F;

   public static WorldParticles getInstance() {
      return c.a(WorldParticles.class);
   }

   public WorldParticles() {
      super("WorldParticles", "Летающие частицы в мире", ModuleCategory.VISUALS);
      this.settings(
         this.mode,
         this.cubeCount,
         this.lifeTime,
         this.size,
         this.glowSize,
         this.physics,
         this.randomColor,
         this.whiteOnSpawn,
         this.whiteCenter,
         this.showGlow,
         this.cubeColor
      );
   }

   @Override
   public void deactivate() {
      this.particles.clear();
      this.lastPlayerPos = class_243.field_1353;
      this.playerVelocity = class_243.field_1353;
      this.playerSpeed = 0.0;
   }

   private Particle.ParticleType getParticleType() {
      return switch (this.mode.getSelected()) {
         case "3D Кубы" -> Particle.ParticleType.CUBE_3D;
         case "Корона" -> Particle.ParticleType.CROWN;
         case "Куб" -> Particle.ParticleType.CUBE_BLAST;
         case "Доллар" -> Particle.ParticleType.DOLLAR;
         case "Сердце" -> Particle.ParticleType.HEART;
         case "Молния" -> Particle.ParticleType.LIGHTNING;
         case "Линия" -> Particle.ParticleType.LINE;
         case "Ромб" -> Particle.ParticleType.RHOMBUS;
         case "Снежинка" -> Particle.ParticleType.SNOWFLAKE;
         case "Звезда" -> Particle.ParticleType.STAR;
         case "Звезда 2" -> Particle.ParticleType.STAR_ALT;
         case "Треугольник" -> Particle.ParticleType.TRIANGLE;
         case "Свечение" -> Particle.ParticleType.GLOW;
         case "Рандом" -> Particle.ParticleType.RANDOM;
         default -> Particle.ParticleType.CUBE_3D;
      };
   }

   @EventHandler
   public void onTick(TickEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_243 var2 = mc.field_1724.method_73189();
         if (this.lastPlayerPos != class_243.field_1353) {
            this.playerVelocity = var2.method_1020(this.lastPlayerPos);
            this.playerSpeed = this.playerVelocity.method_37267();
         }

         this.lastPlayerPos = var2;
         this.cachedType = this.getParticleType();
         this.cachedPhysics = this.physics.isValue();
         this.cachedSize = this.size.getValue();
         long var3 = System.currentTimeMillis();
         double var5 = ParticleSpawner.getDespawnDistanceSquared();
         this.particles.removeIf(var5x -> {
            if (!var5x.isFadingOut() && var5x.getHorizontalDistanceSquaredTo(var2) > var5) {
               var5x.startFadeOut();
            }

            var5x.update(var3);
            return var5x.shouldRemove();
         });
         int var7 = (int)this.cubeCount.getValue();
         int var8 = ParticleSpawner.calculateSpawnDelay(this.playerSpeed);
         if (this.particles.size() < var7 && this.timer.finished(var8)) {
            int var9 = ParticleSpawner.calculateSpawnCount(this.playerSpeed, this.particles.size(), var7);
            long var10 = (long)(this.lifeTime.getValue() * 1000.0F);

            for (int var12 = 0; var12 < var9 && this.particles.size() < var7; var12++) {
               Particle var13 = ParticleSpawner.createParticle(var2, this.playerVelocity, this.playerSpeed, var10, this.cachedType);
               var13.setPhysics(this.cachedPhysics);
               var13.setSize(this.cachedSize);
               this.particles.add(var13);
            }

            this.timer.reset();
         }
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent var1) {
      if (!this.particles.isEmpty()) {
         class_4598 var2 = mc.method_22940().method_23000();
         class_4587 var3 = var1.getStack();
         class_243 var4 = mc.field_1773.method_19418().method_71156();
         long var5 = System.currentTimeMillis();
         float var7 = (float)(var5 % 9000L) / 9000.0F * 360.0F;
         float var8 = mc.field_1773.method_19418().method_19330();
         float var9 = mc.field_1773.method_19418().method_19329();
         int var10 = this.cubeColor.getColor();
         float var11 = this.showGlow.isValue() ? this.glowSize.getValue() : 0.0F;
         boolean var12 = this.randomColor.isValue();
         boolean var13 = this.whiteOnSpawn.isValue();
         boolean var14 = this.whiteCenter.isValue();
         double var15 = 4096.0;
         int var17 = 0;

         for (int var18 = this.particles.size(); var17 < var18; var17++) {
            Particle var19 = this.particles.get(var17);
            if (var19.getDistanceSquaredTo(var4) < var15) {
               var19.render(var3, var2, var4, var10, var7, var8, var9, var11, var12, var13, var14);
            }
         }
      }
   }
}
