package rich.util.profiler;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Lightweight helpers for profiling Minecraft's FrameGraphBuilder passes.
 *
 * WorldRenderer.renderMain builds a frame graph and then executes many named
 * render passes through FramePass#setRenderer(Runnable). This helper keeps the
 * pass names captured at createPass() time and wraps each pass Runnable with
 * FrameProfiler begin/end calls. It also tracks known top-level WorldRenderer
 * sub-phases while the vanilla "main" pass is executing, so the logs can show
 * how much of pass/main is still unaccounted for.
 */
public final class ProfilerFrameGraphHooks {
   private static final Map<Object, String> PASS_NAMES = new WeakHashMap<>();

   private static final class MainPassAccounting {
      long knownNanos;
   }

   private static final ThreadLocal<MainPassAccounting> MAIN_PASS = new ThreadLocal<>();
   private static final ThreadLocal<ArrayDeque<Long>> KNOWN_SECTION_STACK = ThreadLocal.withInitial(ArrayDeque::new);

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
         boolean mainPass = "main".equals(passName);
         MainPassAccounting accounting = null;
         long passStart = 0L;
         if (prof && mainPass) {
            accounting = new MainPassAccounting();
            MAIN_PASS.set(accounting);
            KNOWN_SECTION_STACK.get().clear();
            passStart = System.nanoTime();
         }
         if (prof) {
            profiler.begin("Minecraft/FrameGraph/pass/" + passName);
         }
         try {
            renderer.run();
         } finally {
            long passEnd = prof && mainPass ? System.nanoTime() : 0L;
            if (prof) {
               profiler.end();
            }
            if (prof && mainPass) {
               long total = passEnd - passStart;
               long known = accounting == null ? 0L : accounting.knownNanos;
               if (total > 0L) {
                  profiler.record("Minecraft/FrameGraph/pass/main/knownWorldRenderer", Math.min(total, Math.max(0L, known)));
                  profiler.record("Minecraft/FrameGraph/pass/main/unaccounted", Math.max(0L, total - Math.max(0L, known)));
               }
               MAIN_PASS.remove();
               KNOWN_SECTION_STACK.get().clear();
            }
         }
      };
   }

   public static void beginKnownMainSection() {
      if (!FrameProfiler.getInstance().isEnabled()) {
         return;
      }
      MainPassAccounting accounting = MAIN_PASS.get();
      if (accounting == null) {
         return;
      }
      ArrayDeque<Long> stack = KNOWN_SECTION_STACK.get();
      stack.push(stack.isEmpty() ? System.nanoTime() : 0L);
   }

   public static void endKnownMainSection() {
      if (!FrameProfiler.getInstance().isEnabled()) {
         return;
      }
      MainPassAccounting accounting = MAIN_PASS.get();
      if (accounting == null) {
         return;
      }
      ArrayDeque<Long> stack = KNOWN_SECTION_STACK.get();
      Long start = stack.poll();
      if (start != null && start > 0L) {
         long elapsed = System.nanoTime() - start;
         if (elapsed > 0L) {
            accounting.knownNanos += elapsed;
         }
      }
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
