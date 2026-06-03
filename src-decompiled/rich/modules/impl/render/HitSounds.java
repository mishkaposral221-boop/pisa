package rich.modules.impl.render;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.AttackEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;

public class HitSounds extends ModuleStructure implements IMinecraft {
   private final List<HitSounds.Sound> availableSounds = new ArrayList<>();
   private final SelectSetting soundSetting = new SelectSetting("Sound", "Звук при ударе");
   private final SliderSettings volume = new SliderSettings("Volume", "Громкость звука").range(0.1F, 1.0F).setValue(1.0F);
   private final SliderSettings pitch = new SliderSettings("Pitch", "Высота звука").range(0.1F, 2.0F).setValue(1.0F);
   private final BooleanSetting critOnly = new BooleanSetting("Only on crit", "Только при крите");

   public HitSounds() {
      super("HitSounds", "Воспроизводит звук при ударе.", ModuleCategory.VISUALS);
      this.loadSounds();
      this.soundSetting.value(this.availableSounds.stream().map(HitSounds.Sound::name).toArray(String[]::new));
      this.soundSetting.selected("AWP");
      this.settings(this.soundSetting, this.volume, this.pitch, this.critOnly);
   }

   private void loadSounds() {
      this.availableSounds.add(new HitSounds.Sound("AWP", "hitsound/awp.wav"));
      this.availableSounds.add(new HitSounds.Sound("Litvin", "hitsound/litvin.wav"));
      this.availableSounds.add(new HitSounds.Sound("Pay", "hitsound/pay.wav"));
      this.availableSounds.add(new HitSounds.Sound("Uwu", "hitsound/uwu.wav"));
      this.availableSounds.add(new HitSounds.Sound("Arrow Hit", class_3417.field_15224));
      this.availableSounds.add(new HitSounds.Sound("Anvil Land", class_3417.field_14833));
      this.availableSounds.add(new HitSounds.Sound("Crit", class_3417.field_15016));
   }

   @EventHandler
   public void onAttack(AttackEvent var1) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (var1.getTarget() instanceof class_1309) {
            if (!this.critOnly.isValue() || var1.isCrit()) {
               String var2 = this.soundSetting.getSelected();
               this.availableSounds.stream().filter(var1x -> Objects.equals(var1x.name(), var2)).findFirst().ifPresent(var1x -> {
                  if (var1x.soundSource() instanceof String var2x) {
                     this.playWav(var2x, this.volume.getValue(), this.pitch.getValue());
                  } else if (var1x.soundSource() instanceof class_3414 var3) {
                     mc.field_1724.method_5783(var3, this.volume.getValue(), this.pitch.getValue());
                  }
               });
            }
         }
      }
   }

   private void playWav(String var1, float var2, float var3) {
      new Thread(() -> {
         try {
            InputStream var2x = mc.method_1478().open(class_2960.method_60655("rich", "sounds/" + var1));
            AudioInputStream var3x = AudioSystem.getAudioInputStream(new BufferedInputStream(var2x));
            Clip var4 = AudioSystem.getClip();
            var4.open(var3x);
            if (var4.isControlSupported(Type.MASTER_GAIN)) {
               FloatControl var5 = (FloatControl)var4.getControl(Type.MASTER_GAIN);
               float var6 = (float)(Math.log10(Math.max(var2, 1.0E-4F)) * 20.0);
               var5.setValue(Math.max(var5.getMinimum(), Math.min(var5.getMaximum(), var6)));
            }

            var4.start();
            var4.addLineListener(var1xx -> {
               if (var1xx.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                  var4.close();
               }
            });
         } catch (Exception var7) {
         }
      }, "HitSound").start();
   }

   private record Sound() {
      private final String name;
      private final Object soundSource;

      private Sound(String var1, Object var2) {
         this.name = var1;
         this.soundSource = var2;
      }
   }
}
