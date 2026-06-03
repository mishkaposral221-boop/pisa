package rich.util.modules;

import java.util.List;
import rich.modules.module.ModuleStructure;

public class ModuleProvider {
   private final List<ModuleStructure> moduleStructures;

   public <T extends ModuleStructure> T get(String var1) {
      return this.moduleStructures.stream().filter(var1x -> var1x.getName().equalsIgnoreCase(var1)).map(var0 -> (T)var0).findFirst().orElse(null);
   }

   public <T extends ModuleStructure> T get(Class<T> var1) {
      return this.moduleStructures.stream().filter(var1x -> var1.isAssignableFrom(var1x.getClass())).map(var1::cast).findFirst().orElse(null);
   }

   public List<ModuleStructure> getModuleStructures() {
      return this.moduleStructures;
   }

   public ModuleProvider(List<ModuleStructure> var1) {
      this.moduleStructures = var1;
   }
}
