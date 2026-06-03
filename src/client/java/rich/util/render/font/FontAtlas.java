package rich.util.render.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FontAtlas {
   private static final Logger LOGGER = LoggerFactory.getLogger("rich/Font");
   private final Identifier jsonId;
   private final Identifier textureId;
   private final Map<Integer, Glyph> glyphs;
   private float atlasWidth = 512.0F;
   private float atlasHeight = 512.0F;
   private float fontSize = 32.0F;
   private float lineHeight = 40.0F;
   private float distanceRange = 4.0F;
   private boolean yOriginBottom = false;
   private final AtomicBoolean loaded = new AtomicBoolean(false);

   public FontAtlas(Identifier var1, Identifier var2) {
      this.jsonId = var1;
      this.textureId = var2;
      this.glyphs = new HashMap<>();
   }

   public void forceLoad() {
      if (!this.loaded.get()) {
         synchronized (this) {
            if (!this.loaded.get()) {
               this.doLoad();
            }
         }
      }
   }

   public void ensureLoaded() {
      if (!this.loaded.get()) {
         synchronized (this) {
            if (!this.loaded.get()) {
               this.doLoad();
            }
         }
      }
   }

   private void doLoad() {
      try {
         Optional var1 = MinecraftClient.getInstance().getResourceManager().getResource(this.jsonId);
         if (var1.isEmpty()) {
            LOGGER.warn("Font JSON not found: {}", this.jsonId);
            this.loaded.set(true);
            return;
         }

         try (
            InputStream var2 = ((Resource)var1.get()).getInputStream();
            InputStreamReader var3 = new InputStreamReader(var2, StandardCharsets.UTF_8);
         ) {
            JsonObject var4 = JsonParser.parseReader(var3).getAsJsonObject();
            this.parseJson(var4);
            this.loaded.set(true);
            LOGGER.info("Loaded font: {} with {} glyphs", this.jsonId, this.glyphs.size());
         }
      } catch (Exception var10) {
         LOGGER.error("Failed to load font: {}", this.jsonId, var10);
         this.loaded.set(true);
      }
   }

   private void parseJson(JsonObject var1) {
      float var2 = 1.0F;
      if (var1.has("atlas")) {
         JsonObject var3 = var1.getAsJsonObject("atlas");
         this.atlasWidth = this.getFloat(var3, "width", 512.0F);
         this.atlasHeight = this.getFloat(var3, "height", 512.0F);
         this.fontSize = this.getFloat(var3, "size", 32.0F);
         this.distanceRange = this.getFloat(var3, "distanceRange", 4.0F);
         if (var3.has("yOrigin")) {
            String var4 = var3.get("yOrigin").getAsString();
            this.yOriginBottom = var4.equalsIgnoreCase("bottom");
         }

         LOGGER.info(
            "Atlas: {}x{}, size={}, distanceRange={}, yOrigin={}",
            new Object[]{this.atlasWidth, this.atlasHeight, this.fontSize, this.distanceRange, this.yOriginBottom ? "bottom" : "top"}
         );
      }

      if (var1.has("metrics")) {
         JsonObject var7 = var1.getAsJsonObject("metrics");
         var2 = this.getFloat(var7, "emSize", 1.0F);
         float var9 = this.getFloat(var7, "lineHeight", 1.2F);
         this.lineHeight = var9 * this.fontSize;
      }

      if (var1.has("glyphs")) {
         for (JsonElement var5 : var1.getAsJsonArray("glyphs")) {
            JsonObject var6 = var5.getAsJsonObject();
            this.parseMsdfGlyph(var6, var2);
         }
      }
   }

   private void parseMsdfGlyph(JsonObject var1, float var2) {
      int var3 = -1;
      if (var1.has("unicode")) {
         var3 = var1.get("unicode").getAsInt();
      } else if (var1.has("char")) {
         String var4 = var1.get("char").getAsString();
         if (!var4.isEmpty()) {
            var3 = var4.codePointAt(0);
         }
      } else if (var1.has("id")) {
         var3 = var1.get("id").getAsInt();
      }

      if (var3 >= 0) {
         float var17 = this.getFloat(var1, "advance", 0.0F) * this.fontSize;
         if (var17 == 0.0F) {
            var17 = this.getFloat(var1, "xadvance", 0.0F);
         }

         float var5 = 0.0F;
         float var6 = 0.0F;
         float var7 = 0.0F;
         float var8 = 0.0F;
         float var9 = 0.0F;
         float var10 = 0.0F;
         if (var1.has("atlasBounds")) {
            JsonObject var11 = var1.getAsJsonObject("atlasBounds");
            float var12 = this.getFloat(var11, "left", 0.0F);
            float var13 = this.getFloat(var11, "bottom", 0.0F);
            float var14 = this.getFloat(var11, "right", 0.0F);
            float var15 = this.getFloat(var11, "top", 0.0F);
            var5 = var12;
            var7 = var14 - var12;
            var8 = var15 - var13;
            if (this.yOriginBottom) {
               var6 = this.atlasHeight - var15;
            } else {
               var6 = var13;
            }
         } else if (var1.has("x") && var1.has("y") && var1.has("width") && var1.has("height")) {
            var5 = this.getFloat(var1, "x", 0.0F);
            var6 = this.getFloat(var1, "y", 0.0F);
            var7 = this.getFloat(var1, "width", 0.0F);
            var8 = this.getFloat(var1, "height", 0.0F);
         }

         if (var1.has("planeBounds")) {
            JsonObject var18 = var1.getAsJsonObject("planeBounds");
            float var19 = this.getFloat(var18, "left", 0.0F);
            float var20 = this.getFloat(var18, "bottom", 0.0F);
            float var21 = this.getFloat(var18, "right", 0.0F);
            float var22 = this.getFloat(var18, "top", 0.0F);
            var9 = var19 * this.fontSize;
            float var16 = 0.95F;
            var10 = (var16 - var22) * this.fontSize;
         } else if (var1.has("xoffset") && var1.has("yoffset")) {
            var9 = this.getFloat(var1, "xoffset", 0.0F);
            var10 = this.getFloat(var1, "yoffset", 0.0F);
         }

         this.glyphs.put(var3, new Glyph(var3, var5, var6, var7, var8, var9, var10, var17, this.atlasWidth, this.atlasHeight));
      }
   }

   private float getFloat(JsonObject var1, String var2, float var3) {
      return var1.has(var2) ? var1.get(var2).getAsFloat() : var3;
   }

   public Glyph getGlyph(int var1) {
      return this.glyphs.get(var1);
   }

   public boolean hasGlyph(int var1) {
      return this.glyphs.containsKey(var1);
   }

   public Identifier getTextureId() {
      return this.textureId;
   }

   public float getFontSize() {
      return this.fontSize;
   }

   public float getLineHeight() {
      return this.lineHeight;
   }

   public float getAtlasWidth() {
      return this.atlasWidth;
   }

   public float getAtlasHeight() {
      return this.atlasHeight;
   }

   public float getDistanceRange() {
      return this.distanceRange;
   }

   public boolean isLoaded() {
      return this.loaded.get();
   }

   public int getGlyphCount() {
      return this.glyphs.size();
   }
}
