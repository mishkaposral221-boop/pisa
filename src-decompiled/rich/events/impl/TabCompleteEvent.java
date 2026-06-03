package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class TabCompleteEvent extends EventCancellable {
   public final String prefix;
   public String[] completions;

   public TabCompleteEvent(String var1) {
      this.prefix = var1;
      this.completions = null;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setCompletions(String[] var1) {
      this.completions = var1;
   }
}
