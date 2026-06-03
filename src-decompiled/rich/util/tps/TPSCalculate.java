package rich.util.tps;

import net.minecraft.class_2761;
import net.minecraft.class_3532;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.PacketEvent;

public class TPSCalculate {
   private static TPSCalculate instance;
   private float TPS = 20.0F;
   private float adjustTicks = 0.0F;
   private long timestamp;

   public TPSCalculate() {
      instance = this;
      Initialization.getInstance().getManager().getEventManager();
      EventManager.register(this);
   }

   public static TPSCalculate getInstance() {
      return instance;
   }

   @EventHandler
   private void onPacket(PacketEvent var1) {
      if (var1.getPacket() instanceof class_2761) {
         this.updateTPS();
      }
   }

   private void updateTPS() {
      long var1 = System.nanoTime() - this.timestamp;
      float var3 = 20.0F;
      float var4 = var3 * (1.0E9F / (float)var1);
      float var5 = class_3532.method_15363(var4, 0.0F, var3);
      this.TPS = (float)this.round(var5);
      this.adjustTicks = var5 - var3;
      this.timestamp = System.nanoTime();
   }

   public double round(double var1) {
      return Math.round(var1 * 100.0) / 100.0;
   }

   public float getTpsRounded() {
      return (float)(Math.round(this.TPS * 2.0F) / 2.0);
   }

   public float getTPS() {
      return this.TPS;
   }

   public float getAdjustTicks() {
      return this.adjustTicks;
   }

   public long getTimestamp() {
      return this.timestamp;
   }
}
