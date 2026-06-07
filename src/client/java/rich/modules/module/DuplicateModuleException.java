package rich.modules.module;

public class DuplicateModuleException extends RuntimeException {
   public DuplicateModuleException(String var1) {
      super("Duplicate module registration: " + var1);
   }
}
