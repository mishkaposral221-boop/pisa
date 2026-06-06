package rich.util.profiler;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Lightweight helpers for profiling Minecraft's FrameGraphBuilder passes.
 *
 * WorldRenderer.renderMain builds a frame graph and then executes many named
 * render passes through FramePass#setRenderer(Runnable). The previous profiler
 * could see WorldRenderer.render/renderMain, but not which frame graph pass was
 * actually expensive. This helper keeps the pass names captured at createPass()
 * time and wraps the pass Runnable with FrameProfiler begin/end calls.
 */
public final class ProfilerFrameGraphHooks {
   private static final Map<Object, String> PASS_NAMES = new WeakHashMap<>();

   private ProfilerFrameGraphHooks() {
   }

   public static synchronized void registerPass(Object pass, String name) {
      if (pass == null) {
         return;
      }
      if (name == null || name.isEmpty()) {
         name = "unknown";
      }
      PASS_NAMES.put(pass, name);
   }

   public static Runnable wrapPassRenderer(Object pass, Runnable renderer) {
      if (renderer == null) {
         return null;
      }

      final String passName = passName(pass);
      return () -> {
         FrameProfiler profiler = FrameProfiler.getInstance();
         boolean prof = profiler.isEnabled();
         if (prof) {
            profiler.begin("Minecraft/FrameGraph/pass/" + passName);
         }
         try {
            renderer.run();
         } finally {
            if (prof) {
               profiler.end();
            }
         }
      };
   }

   private static synchronized String passName(Object pass) {
      String name = PASS_NAMES.get(pass);
      if (name != null && !name.isEmpty()) {
         return sanitize(name);
      }
      if (pass == null) {
         return "unknown";
      }
      return sanitize(pass.toString());
   }

   private static String sanitize(String value) {
      if (value == null || value.isEmpty()) {
         return "unknown";
      }
      StringBuilder out = new StringBuilder(value.length());
      for (int i = 0; i < value.length(); i++) {
         char c = value.charAt(i);
         if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '/' || c == ':' || c == '.') {
            out.append(c);
         } else if (Character.isWhitespace(c)) {
            out.append('_');
         }
      }
      if (out.length() == 0) {
         return "unknown";
      }
      if (out.length() > 80) {
         return out.substring(0, 80);
      }
      return out.toString();
   }
}
