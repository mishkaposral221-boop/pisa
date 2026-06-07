package rich.util.render.font;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FontRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger("rich/FontRenderer");
   private final FontPipeline pipeline;
   private final Map<String, FontAtlas> fonts;
   private boolean initialized = false;

   public FontRenderer() {
      this.pipeline = new FontPipeline();
      this.fonts = new HashMap<>();
   }

   public void loadFont(String var1, String var2) {
      Identifier var3 = Identifier.of("rich", "fonts/" + var2 + ".json");
      Identifier var4 = Identifier.of("rich", "fonts/" + var2 + ".png");
      FontAtlas var5 = new FontAtlas(var3, var4);
      this.fonts.put(var1, var5);
      LOGGER.info("Registered font: {} -> {}", var1, var2);
   }

   public void loadAllFonts(Map<String, String> var1) {
      for (Entry var3 : var1.entrySet()) {
         this.loadFont((String)var3.getKey(), (String)var3.getValue());
      }
   }

   public void initialize() {
      if (!this.initialized) {
         LOGGER.info("Initializing {} fonts...", this.fonts.size());
         long var1 = System.currentTimeMillis();

         for (Entry var4 : this.fonts.entrySet()) {
            ((FontAtlas)var4.getValue()).forceLoad();
         }

         this.initialized = true;
         LOGGER.info("All fonts initialized in {}ms", System.currentTimeMillis() - var1);
      }
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public FontAtlas getFont(String var1) {
      return this.fonts.get(var1);
   }

   public void beginBatch() {
      this.pipeline.beginBatch();
   }

   public void endBatch() {
      this.pipeline.endBatch();
   }

   public void drawText(String var1, String var2, float var3, float var4, float var5, int var6) {
      FontAtlas var7 = this.fonts.get(var1);
      if (var7 != null) {
         this.pipeline.drawText(var7, var2, var3, var4, var5, var6, 0.0F, 0, 0.0F);
      }
   }

   public void drawText(String var1, String var2, float var3, float var4, float var5, int var6, float var7) {
      FontAtlas var8 = this.fonts.get(var1);
      if (var8 != null) {
         this.pipeline.drawText(var8, var2, var3, var4, var5, var6, 0.0F, 0, var7);
      }
   }

   public void drawTextWithOutline(String var1, String var2, float var3, float var4, float var5, int var6, float var7, int var8) {
      FontAtlas var9 = this.fonts.get(var1);
      if (var9 != null) {
         this.pipeline.drawText(var9, var2, var3, var4, var5, var6, var7, var8, 0.0F);
      }
   }

   public void drawTextWithOutline(String var1, String var2, float var3, float var4, float var5, int var6, float var7, int var8, float var9) {
      FontAtlas var10 = this.fonts.get(var1);
      if (var10 != null) {
         this.pipeline.drawText(var10, var2, var3, var4, var5, var6, var7, var8, var9);
      }
   }

   public void drawCenteredText(String var1, String var2, float var3, float var4, float var5, int var6) {
      FontAtlas var7 = this.fonts.get(var1);
      if (var7 != null) {
         float var8 = this.pipeline.getTextWidth(var7, var2, var5);
         this.pipeline.drawText(var7, var2, var3 - var8 / 2.0F, var4, var5, var6, 0.0F, 0, 0.0F);
      }
   }

   public void drawCenteredText(String var1, String var2, float var3, float var4, float var5, int var6, float var7) {
      FontAtlas var8 = this.fonts.get(var1);
      if (var8 != null) {
         float var9 = this.pipeline.getTextWidth(var8, var2, var5);
         float var10 = this.pipeline.getTextHeight(var8, var2, var5);
         float var11 = var3;
         float var12 = var4 + var10 / 2.0F;
         this.pipeline.drawTextRotatedAroundPoint(var8, var2, var3 - var9 / 2.0F, var4, var5, var6, 0.0F, 0, var7, var11, var12);
      }
   }

   public float getTextWidth(String var1, String var2, float var3) {
      FontAtlas var4 = this.fonts.get(var1);
      return var4 == null ? 0.0F : this.pipeline.getTextWidth(var4, var2, var3);
   }

   public float getLineHeight(String var1, float var2) {
      FontAtlas var3 = this.fonts.get(var1);
      return var3 == null ? var2 : var3.getLineHeight() / var3.getFontSize() * var2;
   }

   public void close() {
      this.pipeline.close();
      this.fonts.clear();
      this.initialized = false;
   }
}
