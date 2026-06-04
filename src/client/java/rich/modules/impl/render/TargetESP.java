package rich.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.animations.Animation;
import rich.util.animations.Direction;
import rich.util.animations.OutBack;
import rich.util.render.Render3D;
import rich.util.render.clientpipeline.ClientPipelines;
import rich.util.timer.StopWatch;

public class TargetESP extends ModuleStructure implements IMinecraft {
   private static final int SIN_COS_TABLE_SIZE = 720;
   private static final float[] SIN_TABLE = new float[720];
   private static final float[] COS_TABLE = new float[720];
   private static final double RAD_TO_DEG = 180.0 / Math.PI;
   private static final float GHOST_RADIUS = 0.7F;
   private static final int GHOST_PARTICLE_COUNT = 40;
   private static final int GHOST_ALPHA_FACTOR = 15;
   private static final float GHOST_PARTICLE_SPIN_STEP = 10.0F;
   private static TargetESP instance;
   private Animation espAnim = new OutBack().setMs(300).setValue(1.0);
   private StopWatch stopWatch = new StopWatch();
   private LivingEntity lastTarget = null;
   private long targetLostTime = 0L;
   private static final long TARGET_DELAY_MS = 5000L;
   private SelectSetting mode = new SelectSetting("\u0420\u0435\u0436\u0438\u043c", "\u0422\u0438\u043f TargetESP").value("Rhomb", "Ghost", "Chain", "Crystals", "Circle").selected("Rhomb");
   private SliderSettings crystalRotationSpeed = new SliderSettings("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u0440\u0430\u0449\u0435\u043d\u0438\u044f \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u0440\u0430\u0449\u0435\u043d\u0438\u044f \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432")
      .range(0.1F, 2.0F)
      .visible(() -> this.mode.isSelected("Crystals"));
   private SliderSettings crystalDistance = new SliderSettings("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432 \u043e\u0442 \u0438\u0433\u0440\u043e\u043a\u0430")
      .range(0.1F, 2.0F)
      .visible(() -> this.mode.isSelected("Crystals"));
   private ColorSetting color1 = new ColorSetting("\u0426\u0432\u0435\u0442 1", "\u041f\u0435\u0440\u0432\u044b\u0439 \u0446\u0432\u0435\u0442 \u0433\u0440\u0430\u0434\u0438\u0435\u043d\u0442\u0430").setColor(new Color(255, 101, 57, 255).getRGB());
   private ColorSetting color2 = new ColorSetting("\u0426\u0432\u0435\u0442 2", "\u0412\u0442\u043e\u0440\u043e\u0439 \u0446\u0432\u0435\u0442 \u0433\u0440\u0430\u0434\u0438\u0435\u043d\u0442\u0430")
      .setColor(new Color(255, 50, 150, 255).getRGB())
      .visible(() -> !this.mode.isSelected("Crystals"));
   private SliderSettings ghostSpeed = new SliderSettings("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u043e\u0432", "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u0440\u0430\u0449\u0435\u043d\u0438\u044f \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u043e\u0432")
      .range(0.1F, 5.0F)
      .visible(() -> this.mode.isSelected("Ghost"));
   private SliderSettings ghostHeadSize = new SliderSettings("\u0420\u0430\u0437\u043c\u0435\u0440 \u0433\u043e\u043b\u043e\u0432\u044b", "\u0420\u0430\u0437\u043c\u0435\u0440 \u0433\u043e\u043b\u043e\u0432\u044b \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u0430")
      .range(0.1F, 2.0F)
      .visible(() -> this.mode.isSelected("Ghost"));
   private SliderSettings ghostMiddleSize = new SliderSettings("\u0420\u0430\u0437\u043c\u0435\u0440 \u0441\u0435\u0440\u0435\u0434\u0438\u043d\u044b", "\u0420\u0430\u0437\u043c\u0435\u0440 \u0441\u0435\u0440\u0435\u0434\u0438\u043d\u044b \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u0430")
      .range(0.1F, 2.0F)
      .visible(() -> this.mode.isSelected("Ghost"));
   private SliderSettings ghostTailSize = new SliderSettings("\u0420\u0430\u0437\u043c\u0435\u0440 \u0445\u0432\u043e\u0441\u0442\u0430", "\u0420\u0430\u0437\u043c\u0435\u0440 \u0445\u0432\u043e\u0441\u0442\u0430 \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u0430")
      .range(0.1F, 2.0F)
      .visible(() -> this.mode.isSelected("Ghost"));
   private SliderSettings ghostDistance = new SliderSettings("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u043e\u0432 \u043e\u0442 \u0438\u0433\u0440\u043e\u043a\u0430")
      .range(1.0F, 20.0F)
      .visible(() -> this.mode.isSelected("Ghost"));
   private double smoothedPosX;
   private double smoothedPosY;
   private double smoothedPosZ;
   private boolean hasSmoothedPos = false;
   private float movingValue = 0.0F;
   private float hurtProgress = 0.0F;
   private Entity lastRenderedTarget = null;
   private final List<TargetESP.Crystal> crystalList = new ArrayList<>();
   private float rotationAngle = 0.0F;
   private long lastFrameTime = System.nanoTime();
   private static final float TARGET_FPS = 60.0F;
   private static final float NANO_PER_TICK = 1.6666667E7F;
   private final Vector3f reusableVector = new Vector3f();

   public static TargetESP getInstance() {
      return instance;
   }

   public TargetESP() {
      super("TargetEsp", "Target Esp", ModuleCategory.VISUALS);
      instance = this;
      this.crystalRotationSpeed.setValue(0.5F);
      this.crystalDistance.setValue(1.0F);
      this.ghostSpeed.setValue(1.5F);
      this.ghostHeadSize.setValue(0.8F);
      this.ghostMiddleSize.setValue(0.8F);
      this.ghostTailSize.setValue(0.3F);
      this.ghostDistance.setValue(4.5F);
      this.settings(
         this.mode,
         this.crystalRotationSpeed,
         this.crystalDistance,
         this.color1,
         this.color2,
         this.ghostSpeed,
         this.ghostHeadSize,
         this.ghostMiddleSize,
         this.ghostTailSize,
         this.ghostDistance
      );
   }

   private float getDeltaTime() {
      long var1 = System.nanoTime();
      float var3 = (float)(var1 - this.lastFrameTime);
      this.lastFrameTime = var1;
      var3 = Math.max(1000000.0F, Math.min(var3, 1.0E8F));
      return var3 / 1.6666667E7F;
   }

   @EventHandler
   public void onRender3D(WorldRenderEvent var1) {
      float var2 = this.getDeltaTime();
      LivingEntity var3 = null;
      if (mc.targetedEntity instanceof LivingEntity var4 && var4.isAlive() && var4 != mc.player) {
         var3 = var4;
      }

      if (var3 != null) {
         this.lastTarget = var3;
         this.targetLostTime = 0L;
      } else if (this.lastTarget != null && this.targetLostTime == 0L) {
         this.targetLostTime = System.currentTimeMillis();
      }

      boolean var14 = this.lastTarget != null && (var3 != null || System.currentTimeMillis() - this.targetLostTime < 5000L);
      if (!var14) {
         this.hasSmoothedPos = false;
         this.lastTarget = null;
         this.targetLostTime = 0L;
         this.espAnim.setDirection(Direction.BACKWARDS);
         Render3D.resetCircleSmoothing();
      } else {
         LivingEntity var15 = this.lastTarget;
         this.espAnim.setDirection(Direction.FORWARDS);
         float var6 = this.espAnim.getOutput().floatValue();
         if (!(var6 <= 0.01F)) {
            this.movingValue += 2.0F * var2;
            if (this.movingValue > 360000.0F) {
               this.movingValue = 0.0F;
            }

            float var7 = 0.1F * var2;
            this.hurtProgress = var15.hurtTime > 0 ? var15.hurtTime / 10.0F : Math.max(0.0F, this.hurtProgress - var7);
            Render3D.updateTargetEsp(var2);
            if (this.mode.isSelected("Circle")) {
               this.renderCircle(var1.getStack(), var15, var6);
            } else {
               MatrixStack var8 = var1.getStack();
               net.minecraft.client.render.VertexConsumerProvider.Immediate var9 = mc.getBufferBuilders().getEntityVertexConsumers();
               Vec3d var10 = mc.gameRenderer.getCamera().getCameraPos();
               float var11 = var1.getPartialTicks();
               Vec3d var12 = var15.getLerpedPos(var11);
               if (this.lastTarget == var15 && this.hasSmoothedPos) {
                  float var13 = Math.min(1.0F, var11 * 1.5F);
                  this.smoothedPosX = this.smoothedPosX + (var12.x - this.smoothedPosX) * var13;
                  this.smoothedPosY = this.smoothedPosY + (var12.y - this.smoothedPosY) * var13;
                  this.smoothedPosZ = this.smoothedPosZ + (var12.z - this.smoothedPosZ) * var13;
               } else {
                  this.smoothedPosX = var12.x;
                  this.smoothedPosY = var12.y;
                  this.smoothedPosZ = var12.z;
                  this.lastTarget = var15;
                  this.hasSmoothedPos = true;
               }

               var8.push();
               var8.translate(this.smoothedPosX - var10.x, this.smoothedPosY - var10.y, this.smoothedPosZ - var10.z);
               if (this.mode.isSelected("Rhomb")) {
                  this.renderRhomb(var8, var9, var15, var6);
               } else if (this.mode.isSelected("Ghost")) {
                  this.renderGhost(var8, var9, var15, var6);
               } else if (this.mode.isSelected("Chain")) {
                  this.renderChain(var8, var9, var15, var6, var2);
               } else if (this.mode.isSelected("Crystals")) {
                  if (this.crystalList.isEmpty() || this.lastRenderedTarget != var15) {
                     this.createCrystals(var15);
                     this.lastRenderedTarget = var15;
                  }

                  this.renderCrystals(var8, var9, var15, var6, var2);
               }

               var9.draw();
               var8.pop();
            }
         }
      }
   }

   private void renderCircle(MatrixStack var1, LivingEntity var2, float var3) {
      int var4 = this.color1.getColor();
      int var5 = this.color2.getColor();
      if (this.hurtProgress > 0.0F) {
         var4 = this.lerpColor(var4, -65536, this.hurtProgress);
         var5 = this.lerpColor(var5, -65536, this.hurtProgress);
      }

      Render3D.drawCircle(var1, var2, var3, this.hurtProgress, var4, var5);
   }

   private void renderChain(MatrixStack var1, VertexConsumerProvider var2, LivingEntity var3, float var4, float var5) {
      VertexConsumer var6 = var2.getBuffer(ClientPipelines.CHAIN_ESP.apply(Identifier.of("rich", "images/world/chain.png")));
      int var7 = (int)this.movingValue % 720;
      float var8 = SIN_TABLE[var7];
      float var9 = 20.0F * Math.min(1.0F + var8, 1.0F);
      float var10 = 20.0F * (Math.min(1.0F + var8, 2.0F) - 1.0F);
      float var11 = var3.getWidth() * 3.0F;
      byte var12 = 18;
      short var13 = 720;
      float var14 = 8.0F;
      float var15 = 1.5F;
      float var16 = 0.5F;
      int var17 = MathHelper.clamp((int)(var4 * 128.0F), 0, 128);
      int var18 = this.color1.getColor();
      int var19 = this.color2.getColor();
      if (this.hurtProgress > 0.0F) {
         var18 = this.lerpColor(var18, -65536, this.hurtProgress);
         var19 = this.lerpColor(var19, -65536, this.hurtProgress);
      }

      int var20 = this.withAlpha(var18, var17);
      int var21 = this.withAlpha(var19, var17);
      float var22 = this.movingValue / 2.0F;

      for (int var23 = 0; var23 < 2; var23++) {
         float var24 = 1.2F - 0.5F * (var23 == 0 ? 1.0F : 0.9F);
         var1.push();
         var1.translate(0.0F, var3.getHeight() / 2.0F, 0.0F);
         var1.scale(var16, var16, var16);
         var1.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var23 == 0 ? var9 : -var9));
         var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(var23 == 0 ? var10 : -var10));
         float var25 = 0.0F;
         float var26 = -0.5F;
         float var27 = 0.0F;
         Matrix4f var28 = var1.peek().getPositionMatrix();
         int var29 = var12 / 2;

         for (int var30 = 0; var30 < var13; var30 += var29) {
            float var31 = (var23 == 0 ? var9 : -var9) / 100.0F;
            float var32 = (var23 == 0 ? -var10 : var10) / 100.0F;
            int var33 = (int)(var30 - var29 + var22) % 720;
            if (var33 < 0) {
               var33 += 720;
            }

            int var34 = (int)(var30 + var22) % 720;
            if (var34 < 0) {
               var34 += 720;
            }

            float var35 = var25 + var31 + SIN_TABLE[var33] * var11 * var24;
            float var36 = var27 + var32 + COS_TABLE[var33] * var11 * var24;
            float var37 = var25 + var31 + SIN_TABLE[var34] * var11 * var24;
            float var38 = var27 + var32 + COS_TABLE[var34] * var11 * var24;
            float var39 = 0.0027777778F * (var30 - var29) * var14;
            float var40 = 0.0027777778F * var30 * var14;
            var6.vertex(var28, var35, var26, var36).texture(var39, 0.0F).color(var20);
            var6.vertex(var28, var37, var26, var38).texture(var40, 0.0F).color(var20);
            var6.vertex(var28, var37, var26 + var15, var38).texture(var40, 0.99F).color(var21);
            var6.vertex(var28, var35, var26 + var15, var36).texture(var39, 0.99F).color(var21);
         }

         var1.pop();
      }
   }

   private void renderRhomb(MatrixStack var1, VertexConsumerProvider var2, LivingEntity var3, float var4) {
      VertexConsumer var5 = var2.getBuffer(ClientPipelines.ROMB_ESP.apply(Identifier.of("rich", "images/world/cube.png")));
      Quaternionf var6 = mc.gameRenderer.getCamera().getRotation();
      var1.translate(0.0F, var3.getHeight() / 2.0F, 0.0F);
      var1.multiply(var6);
      float var7 = (float)(System.currentTimeMillis() % 6283L) / 1000.0F;
      var1.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)Math.sin(var7) * 360.0F));
      float var8 = 0.5F;
      var1.scale(var8, var8, 1.0F);
      int var9 = this.withAlpha(this.color1.getColor(), (int)(255.0F * var4));
      int var10 = this.withAlpha(this.color2.getColor(), (int)(255.0F * var4));
      Vector3f[] var11 = new Vector3f[]{
         new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
      };
      net.minecraft.client.util.math.MatrixStack.Entry var12 = var1.peek();
      var5.vertex(var12, var11[0].x, var11[0].y, 0.0F).texture(0.0F, 0.0F).color(var10);
      var5.vertex(var12, var11[1].x, var11[1].y, 0.0F).texture(0.0F, 1.0F).color(var9);
      var5.vertex(var12, var11[2].x, var11[2].y, 0.0F).texture(1.0F, 1.0F).color(var10);
      var5.vertex(var12, var11[3].x, var11[3].y, 0.0F).texture(1.0F, 0.0F).color(var9);
   }

   private void renderGhost(MatrixStack var1, VertexConsumerProvider var2, LivingEntity var3, float var4) {
      VertexConsumer var5 = var2.getBuffer(ClientPipelines.GHOSTS_ESP.apply(Identifier.of("rich", "images/particle/ghost-glow.png")));
      var1.translate(0.0F, var3.getHeight() * 0.5F, 0.0F);
      long var6 = System.currentTimeMillis() % 360000L;
      Quaternionf var8 = mc.gameRenderer.getCamera().getRotation();
      this.particle(var1, var5, (var0, var2x, var4x) -> var4x.set((float)var0, (float)var2x, (float)(-var2x)), var4, 0, var6, var8);
      this.particle(var1, var5, (var0, var2x, var4x) -> var4x.set((float)(-var0), (float)var0, (float)(-var2x)), var4, 1, var6, var8);
      this.particle(var1, var5, (var0, var2x, var4x) -> var4x.set((float)(-var0), (float)(-var0), (float)var2x), var4, 2, var6, var8);
   }

   private void particle(MatrixStack var1, VertexConsumer var2, TargetESP.Transformation var3, float var4, int var5, long var6, Quaternionf var8) {
      float var9 = this.ghostSpeed.getValue();
      double var10 = this.ghostDistance.getValue();
      float var12 = this.ghostHeadSize.getValue();
      float var13 = this.ghostMiddleSize.getValue();
      float var14 = this.ghostTailSize.getValue();
      int var15 = (int)(40.0F * var4);
      if (var15 > 0) {
         double var16 = 0.15 * (var6 * 0.5 * var9) / 30.0;
         double var18 = 0.15 * var10 / 60.0;
         float var20 = (float)var6 * 0.1F * var9;
         float var21 = 1.0F / var15;
         int var22 = var5 != 0 && var5 != 2 ? this.color2.getColor() : this.color1.getColor();
         int var23 = this.getNextColor(var5);

         for (int var24 = 0; var24 < var15; var24++) {
            var1.push();
            double var25 = var16 - var24 * var18;
            int var27 = (int)(var25 * (180.0 / Math.PI)) % 720;
            if (var27 < 0) {
               var27 += 720;
            }

            double var28 = SIN_TABLE[var27] * 0.7F;
            double var30 = COS_TABLE[var27] * 0.7F;
            var3.make(var28, var30, this.reusableVector);
            var1.translate(this.reusableVector.x, this.reusableVector.y, this.reusableVector.z);
            var1.multiply(var8);
            float var32 = var20 - var24 * 10.0F;
            var1.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(var32));
            float var33 = var24 * var21;
            float var34;
            if (var33 < 0.5F) {
               var34 = MathHelper.lerp(var33 * 2.0F, var12, var13);
            } else {
               var34 = MathHelper.lerp((var33 - 0.5F) * 2.0F, var13, var14);
            }

            int var35 = this.lerpColor(var22, var23, var33);
            var34 *= 1.0F - var33;
            int var36 = (int)(255.0F * var4);
            var1.translate(var34 / 2.0F, var34 / 2.0F, 0.0F);
            int var37 = this.withAlpha(var35, var36);
            net.minecraft.client.util.math.MatrixStack.Entry var38 = var1.peek();
            var2.vertex(var38, 0.0F, -var34, 0.0F).texture(0.0F, 0.0F).color(var37);
            var2.vertex(var38, -var34, -var34, 0.0F).texture(0.0F, 1.0F).color(var37);
            var2.vertex(var38, -var34, 0.0F, 0.0F).texture(1.0F, 1.0F).color(var37);
            var2.vertex(var38, 0.0F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(var37);
            var1.pop();
         }
      }
   }

   private void createCrystals(Entity var1) {
      this.crystalList.clear();
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.0, 2.2, 0.3), new Vec3d(-49.0, 0.0, 40.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.3, 2.1, -0.4), new Vec3d(35.0, 0.0, -30.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.3, 2.3, 0.2), new Vec3d(-30.0, 0.0, 35.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.5, 2.0, 0.5), new Vec3d(-25.0, 0.0, -30.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.0, 1.4, 0.8), new Vec3d(-49.0, 0.0, 40.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.2, 1.5, -0.675), new Vec3d(35.0, 0.0, -30.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.6, 1.8, 0.6), new Vec3d(-30.0, 0.0, 35.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.74, 1.6, 0.4), new Vec3d(-25.0, 0.0, -30.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.74, 1.55, -0.4), new Vec3d(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.475, 1.45, -0.375), new Vec3d(30.0, 0.0, -25.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.0, 1.75, -0.6), new Vec3d(45.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.85, 1.35, 0.1), new Vec3d(-30.0, 0.0, 30.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.7, 1.7, -0.3), new Vec3d(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.3, 1.65, 0.55), new Vec3d(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.5, 1.3, 0.7), new Vec3d(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.5, 1.3, 0.7), new Vec3d(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.7, 1.4, 0.0), new Vec3d(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.2, 1.25, -0.7), new Vec3d(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.2, 0.3, 0.4), new Vec3d(25.0, 0.0, 20.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.3, 0.2, -0.3), new Vec3d(-20.0, 0.0, -25.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.4, 0.4, 0.2), new Vec3d(30.0, 0.0, 15.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(-0.2, 0.1, 0.5), new Vec3d(-35.0, 0.0, 30.0)));
      this.crystalList.add(new TargetESP.Crystal(new Vec3d(0.3, 0.5, -0.2), new Vec3d(15.0, 0.0, -20.0)));
   }

   private void renderCrystals(MatrixStack var1, VertexConsumerProvider var2, LivingEntity var3, float var4, float var5) {
      if (var3 != null && !this.crystalList.isEmpty()) {
         this.rotationAngle = this.rotationAngle + this.crystalRotationSpeed.getValue() * var5;
         this.rotationAngle %= 360.0F;
         var1.push();
         var1.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotationAngle));
         int var6 = this.color1.getColor();
         if (this.hurtProgress > 0.0F) {
            var6 = this.lerpColor(var6, -65536, this.hurtProgress);
         }

         for (TargetESP.Crystal var8 : this.crystalList) {
            var8.render(var1, var2, var4, var6);
         }

         var1.pop();
      }
   }

   private int darkenColor(int var1, float var2) {
      int var3 = var1 >> 24 & 0xFF;
      int var4 = (int)((var1 >> 16 & 0xFF) * var2);
      int var5 = (int)((var1 >> 8 & 0xFF) * var2);
      int var6 = (int)((var1 & 0xFF) * var2);
      return var3 << 24 | var4 << 16 | var5 << 8 | var6;
   }

   private int lightenColor(int var1, float var2) {
      int var3 = var1 >> 24 & 0xFF;
      int var4 = Math.min(255, (int)((var1 >> 16 & 0xFF) * var2));
      int var5 = Math.min(255, (int)((var1 >> 8 & 0xFF) * var2));
      int var6 = Math.min(255, (int)((var1 & 0xFF) * var2));
      return var3 << 24 | var4 << 16 | var5 << 8 | var6;
   }

   private int getNextColor(int var1) {
      if (var1 == 0) {
         return this.color2.getColor();
      } else if (var1 == 1) {
         return this.color1.getColor();
      } else {
         return var1 == 2 ? this.color2.getColor() : this.color1.getColor();
      }
   }

   private int lerpColor(int var1, int var2, float var3) {
      int var4 = var1 >> 24 & 0xFF;
      int var5 = var1 >> 16 & 0xFF;
      int var6 = var1 >> 8 & 0xFF;
      int var7 = var1 & 0xFF;
      int var8 = var2 >> 24 & 0xFF;
      int var9 = var2 >> 16 & 0xFF;
      int var10 = var2 >> 8 & 0xFF;
      int var11 = var2 & 0xFF;
      int var12 = (int)(var4 + (var8 - var4) * var3);
      int var13 = (int)(var5 + (var9 - var5) * var3);
      int var14 = (int)(var6 + (var10 - var6) * var3);
      int var15 = (int)(var7 + (var11 - var7) * var3);
      return var12 << 24 | var13 << 16 | var14 << 8 | var15;
   }

   private int withAlpha(int var1, int var2) {
      return var1 & 16777215 | var2 << 24;
   }

   public void applyThemeColors(int var1, int var2) {
      this.color1.setColor(var1);
      this.color2.setColor(var2);
   }

   static {
      for (int var0 = 0; var0 < 720; var0++) {
         SIN_TABLE[var0] = (float)Math.sin(Math.toRadians(var0));
         COS_TABLE[var0] = (float)Math.cos(Math.toRadians(var0));
      }
   }

   private class Crystal {
      private final Vec3d position;
      private final Vec3d rotation;
      private final float rotationSpeed;
      private static final int NUM_SIDES = 8;
      private static final float S = 0.07F;
      private static final float H_PRISM = 0.07F;
      private static final float H_PYR = 0.126F;
      private static final float[] COS_TABLE = new float[8];
      private static final float[] SIN_TABLE = new float[8];
      private static final Vector3f[] TOP;
      private static final Vector3f[] BOT;
      private static final Vector3f V_TOP;
      private static final Vector3f V_BOT;

      public Crystal(Vec3d var2, Vec3d var3) {
         this.position = var2;
         this.rotation = var3;
         this.rotationSpeed = 0.5F + (float)(Math.random() * 1.5);
      }

      public void render(MatrixStack var1, VertexConsumerProvider var2, float var3, int var4) {
         var1.push();
         float var5 = TargetESP.getInstance().crystalDistance.getValue();
         var1.translate(this.position.x * var5, this.position.y * var5, this.position.z * var5);
         float var6 = (float)(System.currentTimeMillis() % 31416L) / 1000.0F;
         int var7 = (int)Math.toDegrees(var6 * 2.0F) % 720;
         if (var7 < 0) {
            var7 += 720;
         }

         float var8 = 1.0F + TargetESP.SIN_TABLE[var7] * 0.1F;
         var1.scale(var8, var8, var8);
         float var9 = (float)(System.currentTimeMillis() % 36000L) / 100.0F * this.rotationSpeed;
         var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)this.rotation.x));
         var1.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)this.rotation.y + var9));
         var1.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)this.rotation.z));
         float var10 = 0.4F;
         VertexConsumer var11 = var2.getBuffer(ClientPipelines.CRYSTAL_FILLED);
         this.drawFilledCrystal(var1, var11, var4, var10 * 0.85F, var3);
         VertexConsumer var12 = var2.getBuffer(ClientPipelines.CRYSTAL_GLOW);
         var1.push();
         var1.scale(1.2F, 1.2F, 1.2F);
         this.drawFilledCrystal(var1, var12, var4, var10 * 0.3F, var3);
         var1.pop();
         var1.push();
         var1.scale(1.3F, 1.3F, 1.3F);
         this.drawFilledCrystal(var1, var12, var4, var10 * 0.1F, var3);
         var1.pop();
         var1.push();
         var1.scale(1.5F, 1.5F, 1.5F);
         this.drawFilledCrystal(var1, var12, var4, var10 * 0.05F, var3);
         var1.pop();
         this.drawBloomEffect(var1, var2, var4, var3);
         var1.pop();
      }

      private void drawFilledCrystal(MatrixStack var1, VertexConsumer var2, int var3, float var4, float var5) {
         int var6 = (int)(var4 * 255.0F * var5);
         int var7 = TargetESP.this.withAlpha(var3, var6);
         int var8 = TargetESP.this.withAlpha(TargetESP.this.darkenColor(var3, 0.7F), var6);
         int var9 = TargetESP.this.withAlpha(TargetESP.this.lightenColor(var3, 1.2F), var6);
         Matrix4f var10 = var1.peek().getPositionMatrix();

         for (int var11 = 0; var11 < 8; var11++) {
            int var12 = (var11 + 1) % 8;
            this.drawQuadFilled(var10, var2, BOT[var11], BOT[var12], TOP[var12], TOP[var11], var11 % 2 == 0 ? var7 : var8);
         }

         for (int var13 = 0; var13 < 8; var13++) {
            int var15 = (var13 + 1) % 8;
            this.drawTriangleFilled(var10, var2, V_TOP, TOP[var15], TOP[var13], var13 % 2 == 0 ? var9 : var7);
         }

         for (int var14 = 0; var14 < 8; var14++) {
            int var16 = (var14 + 1) % 8;
            this.drawTriangleFilled(var10, var2, V_BOT, BOT[var14], BOT[var16], var14 % 2 == 0 ? var8 : var7);
         }
      }

      private void drawTriangleFilled(Matrix4f var1, VertexConsumer var2, Vector3f var3, Vector3f var4, Vector3f var5, int var6) {
         var2.vertex(var1, var3.x, var3.y, var3.z).color(var6);
         var2.vertex(var1, var4.x, var4.y, var4.z).color(var6);
         var2.vertex(var1, var5.x, var5.y, var5.z).color(var6);
         var2.vertex(var1, var5.x, var5.y, var5.z).color(var6);
      }

      private void drawQuadFilled(Matrix4f var1, VertexConsumer var2, Vector3f var3, Vector3f var4, Vector3f var5, Vector3f var6, int var7) {
         var2.vertex(var1, var3.x, var3.y, var3.z).color(var7);
         var2.vertex(var1, var4.x, var4.y, var4.z).color(var7);
         var2.vertex(var1, var5.x, var5.y, var5.z).color(var7);
         var2.vertex(var1, var6.x, var6.y, var6.z).color(var7);
      }

      private void drawBloomEffect(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4) {
         VertexConsumer var5 = var2.getBuffer(ClientPipelines.BLOOM_ESP.apply(Identifier.of("rich", "images/particle/glow.png")));
         int var6 = (int)(18.0F * var4);
         int var7 = TargetESP.this.withAlpha(var3, var6);
         float var8 = 0.75F;
         Quaternionf var9 = IMinecraft.mc.gameRenderer.getCamera().getRotation();
         byte var10 = 6;

         for (int var11 = 0; var11 < var10; var11++) {
            var1.push();
            float var12 = 360.0F / var10 * var11;
            var1.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(var12));
            var1.multiply(var9);
            Matrix4f var13 = var1.peek().getPositionMatrix();
            var5.vertex(var13, -var8 / 2.0F, -var8 / 2.0F, 0.0F).texture(0.0F, 1.0F).color(var7);
            var5.vertex(var13, var8 / 2.0F, -var8 / 2.0F, 0.0F).texture(1.0F, 1.0F).color(var7);
            var5.vertex(var13, var8 / 2.0F, var8 / 2.0F, 0.0F).texture(1.0F, 0.0F).color(var7);
            var5.vertex(var13, -var8 / 2.0F, var8 / 2.0F, 0.0F).texture(0.0F, 0.0F).color(var7);
            var1.pop();
         }

         for (int var14 = 0; var14 < var10; var14++) {
            var1.push();
            float var15 = 360.0F / var10 * var14;
            var1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            var1.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(var15));
            var1.multiply(var9);
            Matrix4f var16 = var1.peek().getPositionMatrix();
            var5.vertex(var16, -var8 / 2.0F, -var8 / 2.0F, 0.0F).texture(0.0F, 1.0F).color(var7);
            var5.vertex(var16, var8 / 2.0F, -var8 / 2.0F, 0.0F).texture(1.0F, 1.0F).color(var7);
            var5.vertex(var16, var8 / 2.0F, var8 / 2.0F, 0.0F).texture(1.0F, 0.0F).color(var7);
            var5.vertex(var16, -var8 / 2.0F, var8 / 2.0F, 0.0F).texture(0.0F, 0.0F).color(var7);
            var1.pop();
         }
      }

      static {
         for (int var0 = 0; var0 < 8; var0++) {
            float var1 = (float)((Math.PI * 2) * var0 / 8.0);
            COS_TABLE[var0] = (float)(0.07F * Math.cos(var1));
            SIN_TABLE[var0] = (float)(0.07F * Math.sin(var1));
         }

         TOP = new Vector3f[8];
         BOT = new Vector3f[8];
         V_TOP = new Vector3f(0.0F, 0.161F, 0.0F);
         V_BOT = new Vector3f(0.0F, -0.161F, 0.0F);

         for (int var2 = 0; var2 < 8; var2++) {
            TOP[var2] = new Vector3f(COS_TABLE[var2], 0.035F, SIN_TABLE[var2]);
            BOT[var2] = new Vector3f(COS_TABLE[var2], -0.035F, SIN_TABLE[var2]);
         }
      }
   }

   @FunctionalInterface
   private interface Transformation {
      void make(double var1, double var3, Vector3f var5);
   }
}
