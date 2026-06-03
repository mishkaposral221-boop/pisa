package rich.client.draggables;

import net.minecraft.class_332;
import rich.events.impl.PacketEvent;

public interface HudElement {
   void render(class_332 var1, float var2);

   void tick();

   default void onPacket(PacketEvent var1) {
   }

   boolean isEnabled();

   void setEnabled(boolean var1);

   String getName();

   int getX();

   int getY();

   void setX(int var1);

   void setY(int var1);

   int getWidth();

   int getHeight();

   void setWidth(int var1);

   void setHeight(int var1);

   default float getRoundingRadius() {
      return 4.0F;
   }

   default boolean visible() {
      return true;
   }

   default boolean mouseClicked(double var1, double var3, int var5) {
      return false;
   }

   default boolean mouseReleased(double var1, double var3, int var5) {
      return false;
   }
}
