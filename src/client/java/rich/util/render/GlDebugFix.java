package rich.util.render;

import org.lwjgl.opengl.KHRDebug;

/**
 * Silences the harmless flood of OpenGL debug messages with id=1281
 * (GL_INVALID_VALUE "Operation is not valid from a preview context").
 *
 * In the dev forward-compatible (core) GL context this error is emitted on
 * every line drawn with a width != 1.0 (ESP boxes, shapes, vanilla line
 * layers, etc.). The error itself is benign - the driver simply draws the
 * line at 1px - but Minecraft enables SYNCHRONOUS GL debug output in dev, so
 * each message stalls the render thread and destroys FPS. We disable just
 * that one message id; nothing else is affected and visuals stay identical.
 */
public final class GlDebugFix {
   private static final int GL_DONT_CARE = 4352;          // 0x1100
   private static final int GL_DEBUG_SOURCE_API = 33350;  // 0x8246
   private static final int GL_DEBUG_TYPE_ERROR = 33356;  // 0x824C
   private static boolean applied = false;

   private GlDebugFix() {
   }

   public static void apply() {
      if (applied) {
         return;
      }

      applied = true;

      try {
         KHRDebug.glDebugMessageControl(
            GL_DEBUG_SOURCE_API, GL_DEBUG_TYPE_ERROR, GL_DONT_CARE, new int[]{1281}, false
         );
      } catch (Throwable var1) {
         // KHR_debug may be unavailable in some contexts; ignore.
      }
   }
}
