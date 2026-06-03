package rich.events.api.events.callables;

import rich.events.api.events.Cancellable;
import rich.events.api.events.Event;

public abstract class EventCancellable implements Cancellable, Event {
   private boolean cancelled;

   protected EventCancellable() {
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void cancel() {
      this.cancelled = true;
   }

   public void setCancelled(boolean var1) {
      this.cancelled = var1;
   }
}
