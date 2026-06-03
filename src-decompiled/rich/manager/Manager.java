package rich.manager;

import rich.client.draggables.HudManager;
import rich.command.CommandManager;
import rich.events.api.EventManager;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.render.hud.HudModuleBase;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;
import rich.screens.clickgui.ClickGui;
import rich.util.config.ConfigSystem;
import rich.util.config.impl.bind.BindConfig;
import rich.util.config.impl.blockesp.BlockESPConfig;
import rich.util.config.impl.drag.DragConfig;
import rich.util.config.impl.friend.FriendConfig;
import rich.util.config.impl.prefix.PrefixConfig;
import rich.util.config.impl.staff.StaffConfig;
import rich.util.modules.ModuleProvider;
import rich.util.modules.ModuleSwitcher;
import rich.util.render.font.FontInitializer;
import rich.util.render.shader.RenderCore;
import rich.util.render.shader.Scissor;
import rich.util.repository.macro.MacroRepository;
import rich.util.repository.way.WayRepository;
import rich.util.tps.TPSCalculate;

public class Manager {
   private StrikeManager attackPerpetrator = new StrikeManager();
   private EventManager eventManager;
   private RenderCore renderCore;
   private Scissor scissor;
   private ModuleProvider moduleProvider;
   private ModuleRepository moduleRepository;
   private ModuleSwitcher moduleSwitcher;
   private ClickGui clickgui;
   private ConfigSystem configSystem;
   private CommandManager commandManager;
   private TPSCalculate tpsCalculate;
   private HudManager hudManager = new HudManager();

   public void init() {
      MacroRepository.getInstance().init();
      WayRepository.getInstance().init();
      BlockESPConfig.getInstance().load();
      FriendConfig.getInstance().load();
      StaffConfig.getInstance().load();
      DragConfig.getInstance().load();
      BindConfig.getInstance();
      FontInitializer.register();
      this.tpsCalculate = new TPSCalculate();
      this.clickgui = new ClickGui();
      this.eventManager = new EventManager();
      this.renderCore = new RenderCore();
      this.scissor = new Scissor();
      this.hudManager = new HudManager();
      this.hudManager.initElements();
      this.moduleRepository = new ModuleRepository();
      this.moduleRepository.setup();
      this.moduleProvider = new ModuleProvider(this.moduleRepository.modules());
      this.moduleSwitcher = new ModuleSwitcher(this.moduleRepository.modules(), this.eventManager);
      this.configSystem = new ConfigSystem();
      this.configSystem.init();

      for (ModuleStructure var2 : this.moduleRepository.modules()) {
         if (var2 instanceof HudModuleBase var3) {
            var3.syncFromHud();
         }
      }

      this.commandManager = new CommandManager();
      this.commandManager.init();
      PrefixConfig.getInstance().load();
   }

   public StrikeManager getAttackPerpetrator() {
      return this.attackPerpetrator;
   }

   public EventManager getEventManager() {
      return this.eventManager;
   }

   public RenderCore getRenderCore() {
      return this.renderCore;
   }

   public Scissor getScissor() {
      return this.scissor;
   }

   public ModuleProvider getModuleProvider() {
      return this.moduleProvider;
   }

   public ModuleRepository getModuleRepository() {
      return this.moduleRepository;
   }

   public ModuleSwitcher getModuleSwitcher() {
      return this.moduleSwitcher;
   }

   public ClickGui getClickgui() {
      return this.clickgui;
   }

   public ConfigSystem getConfigSystem() {
      return this.configSystem;
   }

   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   public TPSCalculate getTpsCalculate() {
      return this.tpsCalculate;
   }

   public HudManager getHudManager() {
      return this.hudManager;
   }
}
