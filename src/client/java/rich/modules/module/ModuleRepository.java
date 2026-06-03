package rich.modules.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rich.modules.impl.combat.AutoSwap;
import rich.modules.impl.misc.AutoDuel;
import rich.modules.impl.misc.AutoTpAccept;
import rich.modules.impl.misc.ClickFriend;
import rich.modules.impl.misc.ClientSounds;
import rich.modules.impl.misc.FTHelper;
import rich.modules.impl.misc.IrcModule;
import rich.modules.impl.misc.ItemCooldowns;
import rich.modules.impl.misc.ItemHelper;
import rich.modules.impl.movement.AutoSprint;
import rich.modules.impl.player.AutoPotion;
import rich.modules.impl.player.AutoRespawn;
import rich.modules.impl.player.ItemScroller;
import rich.modules.impl.player.NameProtect;
import rich.modules.impl.player.NoDelay;
import rich.modules.impl.render.Ambience;
import rich.modules.impl.render.Animations;
import rich.modules.impl.render.Arrows;
import rich.modules.impl.render.AspectRatio;
import rich.modules.impl.render.AuctionHelper;
import rich.modules.impl.render.BlockOverlay;
import rich.modules.impl.render.CameraSettings;
import rich.modules.impl.render.ChinaHat;
import rich.modules.impl.render.ChunkAnimator;
import rich.modules.impl.render.ClickGuiSettings;
import rich.modules.impl.render.CustomCrosshair;
import rich.modules.impl.render.CustomFog;
import rich.modules.impl.render.CustomSky;
import rich.modules.impl.render.DynamicFov;
import rich.modules.impl.render.Esp;
import rich.modules.impl.render.FreeLook;
import rich.modules.impl.render.FullBright;
import rich.modules.impl.render.GhostTrail;
import rich.modules.impl.render.GlassHands;
import rich.modules.impl.render.GpsArrow;
import rich.modules.impl.render.GroundPulse;
import rich.modules.impl.render.HitEffect;
import rich.modules.impl.render.HitSounds;
import rich.modules.impl.render.Hud;
import rich.modules.impl.render.ItemPhysic;
import rich.modules.impl.render.JumpCircle;
import rich.modules.impl.render.KillEffect;
import rich.modules.impl.render.NoNausea;
import rich.modules.impl.render.NoRender;
import rich.modules.impl.render.Optimizer;
import rich.modules.impl.render.Particles;
import rich.modules.impl.render.SwingAnimation;
import rich.modules.impl.render.TargetESP;
import rich.modules.impl.render.ViewModel;
import rich.modules.impl.render.WorldParticles;
import rich.modules.impl.render.hud.ArmorHudModule;
import rich.modules.impl.render.hud.HotKeysModule;
import rich.modules.impl.render.hud.InfoModule;
import rich.modules.impl.render.hud.InventoryHUDModule;
import rich.modules.impl.render.hud.NotificationsModule;
import rich.modules.impl.render.hud.PotionsModule;
import rich.modules.impl.render.hud.TargetHudModule;
import rich.modules.impl.render.hud.WatermarkModule;
import rich.modules.impl.util.PvpHelper;
import rich.modules.impl.util.RadiusHelper;

public class ModuleRepository {
   private final List<ModuleStructure> moduleStructures = new ArrayList<>();
   private final List<ModuleStructure> hiddenModules = new ArrayList<>();
   private final Set<Class<? extends ModuleStructure>> registeredClasses = new HashSet<>();

   public void setup() {
      this.builder()
         .add(new Hud())
         .add(new Animations())
         .hidden(new ClickGuiSettings())
         .add(new HitEffect())
         .add(new Esp())
         .add(new WorldParticles())
         .add(new Arrows())
         .add(new Particles())
         .add(new AuctionHelper())
         .add(new GlassHands())
         .hidden(new ChunkAnimator())
         .add(new Ambience())
         .add(new ChinaHat())
         .add(new AutoPotion())
         .add(new ClientSounds())
         .add(new TargetESP())
         .add(new BlockOverlay())
         .add(new CustomCrosshair())
         .add(new JumpCircle())
         .add(new ItemScroller())
         .add(new AutoDuel())
         .add(new AutoTpAccept())
         .add(new ClickFriend())
         .add(new ItemHelper())
         .add(new FullBright())
         .add(new CameraSettings())
         .add(new ItemPhysic())
         .add(new NoDelay())
         .add(new NoRender())
         .add(new NameProtect())
         .add(new ViewModel())
         .hidden(new AutoRespawn())
         .add(new SwingAnimation())
         .add(new AutoSprint())
         .add(new DynamicFov())
         .add(new NoNausea())
         .add(new Optimizer())
         .add(new GhostTrail())
         .add(new GroundPulse())
         .hidden(new KillEffect())
         .add(new FTHelper())
         .add(new RadiusHelper())
         .add(new CustomSky())
         .add(new CustomFog())
         .hidden(new GpsArrow())
         .add(new HitSounds())
         .add(new AspectRatio())
         .add(new FreeLook())
         .add(new AutoSwap())
         .add(new PvpHelper())
         .add(new ItemCooldowns())
         .add(new IrcModule())
         .add(new WatermarkModule())
         .add(new HotKeysModule())
         .add(new PotionsModule())
         .add(new TargetHudModule())
         .add(new ArmorHudModule())
         .add(new InventoryHUDModule())
         .add(new InfoModule())
         .add(new NotificationsModule());
   }

   public ModuleBuilder builder() {
      return new ModuleBuilder(this);
   }

   void registerModule(ModuleStructure var1, boolean var2) {
      Class var3 = var1.getClass();
      if (this.registeredClasses.contains(var3)) {
         throw new DuplicateModuleException(var3.getSimpleName());
      }

      this.registeredClasses.add(var3);
      if (var2) {
         this.hiddenModules.add(var1);
         var1.setState(true);
      } else {
         this.moduleStructures.add(var1);
      }
   }

   public List<ModuleStructure> modules() {
      return this.moduleStructures;
   }

   public List<ModuleStructure> hiddenModules() {
      return this.hiddenModules;
   }

   public List<ModuleStructure> allModules() {
      ArrayList var1 = new ArrayList<>(this.moduleStructures);
      var1.addAll(this.hiddenModules);
      return var1;
   }
}
