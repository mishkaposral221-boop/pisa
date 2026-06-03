package rich.events.impl;

import net.minecraft.class_2338;
import net.minecraft.class_2350;
import rich.events.api.events.Event;

public record BlockBreakingEvent() implements Event {
   private final class_2338 blockPos;
   private final class_2350 direction;

   public BlockBreakingEvent(class_2338 var1, class_2350 var2) {
      this.blockPos = var1;
      this.direction = var2;
   }
}
