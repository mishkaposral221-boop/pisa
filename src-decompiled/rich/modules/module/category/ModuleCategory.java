package rich.modules.module.category;

public enum ModuleCategory {
   VISUALS("Visuals"),
   HUD("HUD"),
   UTILITIES("Utilities"),
   THEMES("Themes"),
   CONFIGS("Configs");

   final String readableName;

   ModuleCategory(String var3) {
      this.readableName = var3;
   }

   public String getReadableName() {
      return this.readableName;
   }
}
