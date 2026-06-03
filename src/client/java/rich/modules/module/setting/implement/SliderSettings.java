package rich.modules.module.setting.implement;

import java.util.function.Supplier;
import rich.modules.module.setting.Setting;

public class SliderSettings extends Setting {
   private float value;
   private float min;
   private float max;
   private boolean integer;

   public SliderSettings(String var1, String var2) {
      super(var1, var2);
   }

   public SliderSettings range(float var1, float var2) {
      this.min = var1;
      this.max = var2;
      return this;
   }

   public SliderSettings range(int var1, int var2) {
      this.min = var1;
      this.max = var2;
      this.integer = true;
      return this;
   }

   public int getInt() {
      return (int)this.value;
   }

   public SliderSettings visible(Supplier<Boolean> var1) {
      this.setVisible(var1);
      return this;
   }

   public float getValue() {
      return this.value;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   public boolean isInteger() {
      return this.integer;
   }

   public SliderSettings setValue(float var1) {
      this.value = var1;
      return this;
   }

   public SliderSettings setMin(float var1) {
      this.min = var1;
      return this;
   }

   public SliderSettings setMax(float var1) {
      this.max = var1;
      return this;
   }

   public SliderSettings setInteger(boolean var1) {
      this.integer = var1;
      return this;
   }
}
