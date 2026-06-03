package com.example.client.config;

import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.minecraft.client.MinecraftClient;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "config/modid";
    private static final String CONFIG_FILE = "config.json";

    private static Path getConfigPath() {
        Path gameDir = MinecraftClient.getInstance().runDirectory.toPath();
        return gameDir.resolve(CONFIG_DIR).resolve(CONFIG_FILE);
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Category cat : MenuData.CATEGORIES) {
                JsonObject catObj = new JsonObject();
                for (ModModule mod : cat.modules) {
                    JsonObject modObj = new JsonObject();
                    modObj.addProperty("enabled", Boolean.valueOf(mod.enabled));
                    if (mod.keybind != -1) {
                        modObj.addProperty("keybind", (Number)mod.keybind);
                    }
                    if (mod.settings != null && mod.settings.length > 0) {
                        JsonObject settingsObj = new JsonObject();
                        for (Setting s : mod.settings) {
                            if (s.isToggle()) {
                                settingsObj.addProperty(s.name, Boolean.valueOf(s.getBool()));
                                continue;
                            }
                            settingsObj.addProperty(s.name, (Number)s.getValue());
                        }
                        modObj.add("settings", (JsonElement)settingsObj);
                    }
                    catObj.add(mod.name, (JsonElement)modObj);
                }
                root.add(cat.name, (JsonElement)catObj);
            }
            Path configPath = ConfigManager.getConfigPath();
            Files.createDirectories(configPath.getParent(), new FileAttribute[0]);
            Files.writeString(configPath, (CharSequence)GSON.toJson((JsonElement)root), new OpenOption[0]);
        }
        catch (IOException e) {
            System.err.println("[ModMenu] Failed to save config: " + e.getMessage());
        }
    }

    public static void load() {
        try {
            Path configPath = ConfigManager.getConfigPath();
            if (!Files.exists(configPath, new LinkOption[0])) {
                return;
            }
            String json = Files.readString(configPath);
            JsonObject root = (JsonObject)GSON.fromJson(json, JsonObject.class);
            if (root == null) {
                return;
            }
            for (Category cat : MenuData.CATEGORIES) {
                JsonObject catObj = root.getAsJsonObject(cat.name);
                if (catObj == null) continue;
                for (ModModule mod : cat.modules) {
                    JsonObject settingsObj;
                    JsonObject modObj = catObj.getAsJsonObject(mod.name);
                    if (modObj == null) continue;
                    if (modObj.has("enabled")) {
                        mod.enabled = modObj.get("enabled").getAsBoolean();
                    }
                    if (modObj.has("keybind")) {
                        mod.keybind = modObj.get("keybind").getAsInt();
                    }
                    if (!modObj.has("settings") || mod.settings == null || (settingsObj = modObj.getAsJsonObject("settings")) == null) continue;
                    for (Setting s : mod.settings) {
                        JsonElement val = settingsObj.get(s.name);
                        if (val == null) continue;
                        if (s.isToggle()) {
                            s.setBool(val.getAsBoolean());
                            continue;
                        }
                        s.setValue(val.getAsDouble());
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println("[ModMenu] Failed to load config: " + e.getMessage());
        }
    }
}

