package rich.util.inventory.script;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ScriptManager {
   private final Map<String, Script> scripts = new ConcurrentHashMap<>();

   public Optional<Script> getScript(String var1) {
      return this.isNullOrEmpty(var1) ? Optional.empty() : Optional.of(this.scripts.computeIfAbsent(var1, var0 -> new Script()));
   }

   public Script addScript(String var1, Script var2) {
      if (!this.isNullOrEmpty(var1) && var2 != null) {
         return this.scripts.put(var1, var2);
      } else {
         throw new IllegalArgumentException("Script name or instance cannot be null or empty");
      }
   }

   public boolean containsScript(String var1) {
      return !this.isNullOrEmpty(var1) && this.scripts.containsKey(var1);
   }

   public boolean finished(String var1) {
      return !this.isNullOrEmpty(var1) && this.getScript(var1).isPresent() && this.getScript(var1).get().isFinished();
   }

   public void removeScript(String var1) {
      if (!this.isNullOrEmpty(var1)) {
         this.scripts.remove(var1);
      }
   }

   public void cleanupScript(String var1) {
      if (!this.isNullOrEmpty(var1)) {
         this.scripts.computeIfPresent(var1, (var0, var1x) -> {
            var1x.cleanup();
            return (Script)var1x;
         });
      }
   }

   public void cleanupAll() {
      this.scripts.forEach((var0, var1) -> var1.cleanup());
   }

   public void clearAll() {
      this.scripts.clear();
   }

   public void updateScript(String var1) {
      this.updateScript(var1, () -> true);
   }

   public void updateScript(String var1, Supplier<Boolean> var2) {
      if ((Boolean)var2.get() && !this.isNullOrEmpty(var1)) {
         this.scripts.computeIfPresent(var1, (var0, var1x) -> {
            var1x.update();
            return (Script)var1x;
         });
      }
   }

   public void updateAll() {
      this.scripts.values().forEach(Script::update);
   }

   public Set<String> getAllScriptNames() {
      return Collections.unmodifiableSet(this.scripts.keySet());
   }

   public Map<String, Script> getAllScripts() {
      return Collections.unmodifiableMap(this.scripts);
   }

   private boolean isNullOrEmpty(String var1) {
      return var1 == null || var1.trim().isEmpty();
   }
}
