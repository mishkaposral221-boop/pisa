package rich.util.math;

import java.util.ArrayList;
import java.util.List;

public class FrameRateCounter {
   public static final FrameRateCounter INSTANCE = new FrameRateCounter();
   final List<Long> records = new ArrayList<>();
   int fps = 5;

   public void recordFrame() {
      long var1 = System.currentTimeMillis();
      this.records.add(var1);
      this.records.removeIf(var0 -> var0 + 1000L < System.currentTimeMillis());
      this.fps = Math.max(this.records.size(), 4);
   }

   public int getFps() {
      return this.fps;
   }
}
