package rich.modules.impl.render;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_1011.class_1012;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CosmoGifLoader {
   private static final List<class_2960> frames = new ArrayList<>();
   private static final List<Integer> delays = new ArrayList<>();
   private static int frameIndex = 0;
   private static long lastFrameTime = 0L;
   private static final AtomicBoolean loaded = new AtomicBoolean(false);
   private static final AtomicBoolean loading = new AtomicBoolean(false);
   private static int counter = 0;

   public static void init() {
      if (!loaded.get() && !loading.get()) {
         loading.set(true);
         new Thread(() -> {
            try {
               InputStream var0 = class_310.method_1551().method_1478().open(class_2960.method_60655("rich", "images/gifs/cosmo.gif"));
               ImageInputStream var1 = ImageIO.createImageInputStream(var0);
               Iterator var2 = ImageIO.getImageReadersByFormatName("gif");
               if (!var2.hasNext()) {
                  loading.set(false);
                  return;
               }

               ImageReader var3 = (ImageReader)var2.next();
               var3.setInput(var1);
               int var4 = var3.getNumImages(true);
               ArrayList var5 = new ArrayList();
               ArrayList var6 = new ArrayList();

               for (int var7 = 0; var7 < var4; var7++) {
                  var5.add(var3.read(var7));

                  try {
                     IIOMetadata var8 = var3.getImageMetadata(var7);
                     Node var9 = var8.getAsTree("javax_imageio_gif_image_1.0");
                     int var10 = extractDelay(var9);
                     var6.add(Math.max(var10, 20));
                  } catch (Exception var11) {
                     var6.add(100);
                  }
               }

               var3.dispose();
               class_310.method_1551().execute(() -> {
                  for (int var2x = 0; var2x < var5.size(); var2x++) {
                     class_2960 var3x = register((BufferedImage)var5.get(var2x), var2x);
                     if (var3x != null) {
                        frames.add(var3x);
                        delays.add((Integer)var6.get(var2x));
                     }
                  }

                  lastFrameTime = System.currentTimeMillis();
                  loaded.set(true);
                  loading.set(false);
               });
            } catch (Exception var12) {
               loading.set(false);
            }
         }, "CosmoGifLoader").start();
      }
   }

   private static int extractDelay(Node var0) {
      NodeList var1 = var0.getChildNodes();

      for (int var2 = 0; var2 < var1.getLength(); var2++) {
         Node var3 = var1.item(var2);
         if ("GraphicControlExtension".equals(var3.getNodeName())) {
            NamedNodeMap var4 = var3.getAttributes();
            if (var4 != null) {
               Node var5 = var4.getNamedItem("delayTime");
               if (var5 != null) {
                  return Integer.parseInt(var5.getNodeValue()) * 10;
               }
            }
         }

         int var6 = extractDelay(var3);
         if (var6 > 0) {
            return var6;
         }
      }

      return 0;
   }

   public static void tick() {
      if (loaded.get() && !frames.isEmpty()) {
         long var0 = System.currentTimeMillis();
         int var2 = delays.isEmpty() ? 100 : delays.get(frameIndex % delays.size());
         if (var0 - lastFrameTime >= var2) {
            frameIndex = (frameIndex + 1) % frames.size();
            lastFrameTime = var0;
         }
      }
   }

   public static class_2960 getCurrentFrame() {
      return loaded.get() && !frames.isEmpty() ? frames.get(frameIndex) : null;
   }

   public static boolean isLoaded() {
      return loaded.get();
   }

   private static class_2960 register(BufferedImage var0, int var1) {
      try {
         int var2 = var0.getWidth();
         int var3 = var0.getHeight();
         class_1011 var4 = new class_1011(class_1012.field_4997, var2, var3, false);

         for (int var5 = 0; var5 < var3; var5++) {
            for (int var6 = 0; var6 < var2; var6++) {
               int var7 = var0.getRGB(var6, var5);
               int var8 = var7 >> 24 & 0xFF;
               int var9 = var7 >> 16 & 0xFF;
               int var10 = var7 >> 8 & 0xFF;
               int var11 = var7 & 0xFF;
               var4.method_61941(var6, var5, var8 << 24 | var11 << 16 | var10 << 8 | var9);
            }
         }

         class_1043 var13 = new class_1043(() -> "cosmo_gif_" + var1, var4);
         class_2960 var14 = class_2960.method_60655("rich", "cosmo_gif_frame_" + counter++);
         class_310.method_1551().method_1531().method_4616(var14, var13);
         return var14;
      } catch (Exception var12) {
         return null;
      }
   }
}
