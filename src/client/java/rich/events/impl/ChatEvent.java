package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class ChatEvent extends EventCancellable {
   private String message;

   public ChatEvent(String var1) {
      this.message = var1;
   }

   public String getMessage() {
      return this.message;
   }

   public void setMessage(String var1) {
      this.message = var1;
   }
}
