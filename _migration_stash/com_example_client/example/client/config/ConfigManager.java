package com.example.client.config;

import com.example.ExampleMod;
import com.example.client.menu.data.Category;
import com.example.client.menu.data.MenuData;
import com.example.client.menu.data.ModModule;
import com.example.client.menu.data.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "modid.json";

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Category cat : MenuData.CATEGORIES) {
                JsonObject catObj = new JsonObject();
                for (ModModule mod : cat.modules) {
                    JsonObject modObj = new JsonObject();
                    modObj.addProperty("enabled", mod.enabled);
                    if (mod.keybind != -1) {
                        modObj.addProperty("keybind", mod.keybind);
                    }
                    if (mod.settings != null && mod.settings.length > 0) {
                        JsonObject settingsObj = new JsonObject();
                        for (Setting s : mod.settings) {
                            if (s.isToggle()) {
                                settingsObj.addProperty(s.name, s.getBool());
                            } else {
                                settingsObj.addProperty(s.name, s.getValue());
                            }
                        }
                        modObj.add("settings", settingsObj);
                    }
                    catObj.add(mod.name, modObj);
                }
                root.add(cat.name, catObj);
            }

            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            ExampleMod.LOGGER.error("Failed to save config to {}", getConfigPath(), e);
        }
    }

    public static void load() {
        Path configPath = getConfigPath();
        if (!Files.isRegularFile(configPath)) {
            ExampleMod.LOGGER.debug("Config file not found, using defaults: {}", configPath);
            return;
        }
        try {
            String json = Files.readString(configPath, StandardCharsets.UTF_8);
            if (json.isBlank()) {
                return;
            }
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            if (root == null) {
                ExampleMod.LOGGER.warn("Config file is empty or invalid: {}", configPath);
                return;
            }
            for (Category cat : MenuData.CATEGORIES) {
                if (!root.has(cat.name)) {
                    continue;
                }
                JsonObject catObj = root.getAsJsonObject(cat.name);
                if (catObj == null) {
                    continue;
                }
                for (ModModule mod : cat.modules) {
                    if (!catObj.has(mod.name)) {
                        continue;
                    }
                    JsonObject modObj = catObj.getAsJsonObject(mod.name);
                    if (modObj == null) {
                        continue;
                    }
                    if (modObj.has("enabled")) {
                        mod.enabled = modObj.get("enabled").getAsBoolean();
                    }
                    if (modObj.has("keybind")) {
                        mod.keybind = modObj.get("keybind").getAsInt();
                    }
                    if (!modObj.has("settings") || mod.settings == null) {
                        continue;
                    }
                    JsonObject settingsObj = modObj.getAsJsonObject("settings");
                    if (settingsObj == null) {
                        continue;
                    }
                    for (Setting s : mod.settings) {
                        JsonElement val = settingsObj.get(s.name);
                        if (val == null) {
                            continue;
                        }
                        if (s.isToggle()) {
                            s.setBool(val.getAsBoolean());
                        } else {
                            s.setValue(val.getAsDouble());
                        }
                    }
                }
            }
        } catch (IOException | JsonParseException | IllegalStateException e) {
            ExampleMod.LOGGER.error("Failed to load config from {}", configPath, e);
        }
    }
}
