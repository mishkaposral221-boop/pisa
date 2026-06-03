package rich.modules.impl.combat.aura;

import net.minecraft.class_243;
import net.minecraft.class_3532;
import rich.util.math.MathUtils;

public class Angle {
   public static Angle DEFAULT = new Angle(0.0F, 0.0F);
   private float yaw;
   private float pitch;

   public static Angle fromTargetHead(class_243 var0, class_243 var1, double var2) {
      double var4 = var1.field_1351 + var2 * 0.9;
      double var6 = var1.field_1352 - var0.field_1352;
      double var8 = var4 - (var0.field_1351 + 1.5);
      double var10 = var1.field_1350 - var0.field_1350;
      float var12 = (float)Math.toDegrees(Math.atan2(var10, var6)) - 90.0F;
      var12 = class_3532.method_15393(var12);
      double var13 = Math.sqrt(var6 * var6 + var10 * var10);
      float var15 = (float)Math.toDegrees(-Math.atan2(var8, var13));
      var15 = class_3532.method_15363(var15, -90.0F, 90.0F);
      return new Angle(var12, var15);
   }

   public Angle adjustSensitivity() {
      double var1 = MathUtils.computeGcd();
      Angle var3 = AngleConnection.INSTANCE.getServerAngle();
      float var4 = this.adjustAxis(this.yaw, var3.yaw, var1);
      float var5 = this.adjustAxis(this.pitch, var3.pitch, var1);
      return new Angle(var4, class_3532.method_15363(var5, -90.0F, 90.0F));
   }

   public Angle random(float var1) {
      return new Angle(this.yaw + MathUtils.getRandom(-var1, var1), this.pitch + MathUtils.getRandom(-var1, var1));
   }

   private float adjustAxis(float var1, float var2, double var3) {
      float var5 = var1 - var2;
      return var2 + (float)Math.round(var5 / var3) * (float)var3;
   }

   public final class_243 toVector() {
      float var1 = this.pitch * (float) (Math.PI / 180.0);
      float var2 = -this.yaw * (float) (Math.PI / 180.0);
      float var3 = class_3532.method_15362(var2);
      float var4 = class_3532.method_15374(var2);
      float var5 = class_3532.method_15362(var1);
      float var6 = class_3532.method_15374(var1);
      return new class_243(var4 * var5, -var6, var3 * var5);
   }

   public Angle addYaw(float var1) {
      return new Angle(this.yaw + var1, this.pitch);
   }

   public Angle addPitch(float var1) {
      this.pitch = class_3532.method_15363(this.pitch + var1, -90.0F, 90.0F);
      return this;
   }

   public Angle of(Angle var1) {
      return new Angle(var1.getYaw(), var1.getPitch());
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setYaw(float var1) {
      this.yaw = var1;
   }

   public void setPitch(float var1) {
      this.pitch = var1;
   }

   @Override
   public String toString() {
      return "Angle(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
   }

   public Angle(float var1, float var2) {
      this.yaw = var1;
      this.pitch = var2;
   }

   public static class VecRotation {
      private final Angle angle;
      private final class_243 vec;

      @Override
      public String toString() {
         return "Angle.VecRotation(angle=" + this.getAngle() + ", vec=" + this.getVec() + ")";
      }

      public Angle getAngle() {
         return this.angle;
      }

      public class_243 getVec() {
         return this.vec;
      }

      public VecRotation(Angle var1, class_243 var2) {
         this.angle = var1;
         this.vec = var2;
      }
   }
}
