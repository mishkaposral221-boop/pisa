package rich.util.inventory;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public class MovementController {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private boolean forward;
   private boolean back;
   private boolean left;
   private boolean right;
   private boolean jump;
   private boolean sprint;
   private boolean saved = false;
   private boolean blocked = false;

   public void saveState() {
      if (mc.player != null) {
         this.forward = this.isKeyPressed(mc.options.forwardKey);
         this.back = this.isKeyPressed(mc.options.backKey);
         this.left = this.isKeyPressed(mc.options.leftKey);
         this.right = this.isKeyPressed(mc.options.rightKey);
         this.jump = this.isKeyPressed(mc.options.jumpKey);
         this.sprint = mc.player.isSprinting();
         this.saved = true;
      }
   }

   public void block() {
      if (mc.player != null) {
         mc.options.forwardKey.setPressed(false);
         mc.options.backKey.setPressed(false);
         mc.options.leftKey.setPressed(false);
         mc.options.rightKey.setPressed(false);
         mc.options.jumpKey.setPressed(false);
         mc.options.sprintKey.setPressed(false);
         this.blocked = true;
      }
   }

   public void stopSprint() {
      if (mc.player != null) {
         mc.player.setSprinting(false);
         mc.options.sprintKey.setPressed(false);
      }
   }

   public void restore() {
      if (this.saved) {
         mc.options.forwardKey.setPressed(this.forward && this.isCurrentlyPressed(mc.options.forwardKey));
         mc.options.backKey.setPressed(this.back && this.isCurrentlyPressed(mc.options.backKey));
         mc.options.leftKey.setPressed(this.left && this.isCurrentlyPressed(mc.options.leftKey));
         mc.options.rightKey.setPressed(this.right && this.isCurrentlyPressed(mc.options.rightKey));
         mc.options.jumpKey.setPressed(this.jump && this.isCurrentlyPressed(mc.options.jumpKey));
         this.blocked = false;
         this.saved = false;
      }
   }

   public void restoreFromCurrent() {
      mc.options.forwardKey.setPressed(this.isCurrentlyPressed(mc.options.forwardKey));
      mc.options.backKey.setPressed(this.isCurrentlyPressed(mc.options.backKey));
      mc.options.leftKey.setPressed(this.isCurrentlyPressed(mc.options.leftKey));
      mc.options.rightKey.setPressed(this.isCurrentlyPressed(mc.options.rightKey));
      mc.options.jumpKey.setPressed(this.isCurrentlyPressed(mc.options.jumpKey));
      mc.options.sprintKey.setPressed(this.isCurrentlyPressed(mc.options.sprintKey));
      this.blocked = false;
   }

   public boolean isPlayerStopped(double var1) {
      if (mc.player == null) {
         return true;
      }

      double var3 = Math.abs(mc.player.getVelocity().x);
      double var5 = Math.abs(mc.player.getVelocity().z);
      return var3 < var1 && var5 < var1;
   }

   public boolean isBlocked() {
      return this.blocked;
   }

   public void reset() {
      this.saved = false;
      this.blocked = false;
   }

   private boolean isKeyPressed(KeyBinding var1) {
      return var1.isPressed();
   }

   private boolean isCurrentlyPressed(KeyBinding var1) {
      return InputUtil.isKeyPressed(mc.getWindow(), InputUtil.fromTranslationKey(var1.getBoundKeyTranslationKey()).getCode());
   }
}
