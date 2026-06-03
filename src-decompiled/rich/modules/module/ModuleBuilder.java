package rich.modules.module;

public class ModuleBuilder {
   private final ModuleRepository repository;

   public ModuleBuilder add(ModuleStructure var1) {
      this.repository.registerModule(var1, false);
      return this;
   }

   public ModuleBuilder hidden(ModuleStructure var1) {
      this.repository.registerModule(var1, true);
      return this;
   }

   public ModuleBuilder(ModuleRepository var1) {
      this.repository = var1;
   }
}
