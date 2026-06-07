package rich.util.repository.macro;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.KeyEvent;
import rich.util.config.impl.macro.MacroConfig;

public class MacroRepository {
   private static MacroRepository instance;
   private final List<Macro> macroList = new ArrayList<>();
   private final MinecraftClient mc = MinecraftClient.getInstance();

   public MacroRepository() {
      instance = this;
   }

   public static MacroRepository getInstance() {
      if (instance == null) {
         instance = new MacroRepository();
      }

      return instance;
   }

   public void init() {
      EventManager.register(this);
      MacroConfig.getInstance().load();
   }

   public void addMacro(String var1, String var2, int var3) {
      this.macroList.add(new Macro(var1, var2, var3));
   }

   public void addMacroAndSave(String var1, String var2, int var3) {
      this.addMacro(var1, var2, var3);
      MacroConfig.getInstance().save();
   }

   public boolean hasMacro(String var1) {
      return this.macroList.stream().anyMatch(var1x -> var1x.name().equalsIgnoreCase(var1));
   }

   public Optional<Macro> getMacro(String var1) {
      return this.macroList.stream().filter(var1x -> var1x.name().equalsIgnoreCase(var1)).findFirst();
   }

   public void deleteMacro(String var1) {
      this.macroList.removeIf(var1x -> var1x.name().equalsIgnoreCase(var1));
   }

   public void deleteMacroAndSave(String var1) {
      this.deleteMacro(var1);
      MacroConfig.getInstance().save();
   }

   public void clearList() {
      this.macroList.clear();
   }

   public void clearListAndSave() {
      this.clearList();
      MacroConfig.getInstance().save();
   }

   public int size() {
      return this.macroList.size();
   }

   public List<String> getMacroNames() {
      return this.macroList.stream().map(Macro::name).collect(Collectors.toList());
   }

   public void setMacros(List<Macro> var1) {
      this.macroList.clear();
      this.macroList.addAll(var1);
   }

   @EventHandler
   public void onKey(KeyEvent var1) {
      if (this.mc.player != null && this.mc.currentScreen == null) {
         if (var1.getAction() == 1) {
            this.macroList.stream().filter(var1x -> var1x.key() == var1.getKey()).findFirst().ifPresent(var1x -> {
               String var2 = var1x.message();
               if (var2.startsWith("/")) {
                  this.mc.player.networkHandler.sendChatCommand(var2.substring(1));
               } else {
                  this.mc.player.networkHandler.sendChatMessage(var2);
               }
            });
         }
      }
   }

   public List<Macro> getMacroList() {
      return this.macroList;
   }

   public MinecraftClient getMc() {
      return this.mc;
   }
}
