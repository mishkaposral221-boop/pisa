package rich.events.impl;

import rich.events.api.events.Event;

public class DeathScreenEvent implements Event {
   private int ticksSinceDeath;

   public int getTicksSinceDeath() {
      return this.ticksSinceDeath;
   }

   public DeathScreenEvent(int var1) {
      this.ticksSinceDeath = var1;
   }
}
