package rich.events.impl;

import rich.events.api.events.callables.EventCancellable;

public class MouseRotationEvent extends EventCancellable {
   float cursorDeltaX;
   float cursorDeltaY;

   public float getCursorDeltaX() {
      return this.cursorDeltaX;
   }

   public float getCursorDeltaY() {
      return this.cursorDeltaY;
   }

   public void setCursorDeltaX(float var1) {
      this.cursorDeltaX = var1;
   }

   public void setCursorDeltaY(float var1) {
      this.cursorDeltaY = var1;
   }

   public MouseRotationEvent(float var1, float var2) {
      this.cursorDeltaX = var1;
      this.cursorDeltaY = var2;
   }
}
