package com.example.apexclient.config;

import com.example.apexclient.ApexConfig;
import com.example.apexclient.module.Module;
import com.example.apexclient.module.ModuleManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("apex-client.json");

    private static boolean loading = false;

    private ConfigManager() {
        // Static manager; do not instantiate.
    }

    public static void save() {
        if (loading) {
            return;
        }

        JsonObject root = new JsonObject();
        JsonObject general = new JsonObject();

        general.addProperty("hudOverlay", ApexConfig.hudOverlay);
        general.addProperty("infoDisplay", ApexConfig.infoDisplay);

        // Aim Assist
        general.addProperty("aimAssistSpeed", ApexConfig.clampAimAssistSpeed(ApexConfig.aimAssistSpeed));
        general.addProperty("aimAssistRange", ApexConfig.clampAimAssistRange(ApexConfig.aimAssistRange));
        general.addProperty("aimAssistThroughWalls", ApexConfig.aimAssistThroughWalls);
        general.addProperty("aimAssistVertical", ApexConfig.aimAssistVertical);

        // Shield Stun
        general.addProperty("shieldStunDelayMs", ApexConfig.clampShieldStunDelayMs(ApexConfig.shieldStunDelayMs));

        // Auto Drain
        general.addProperty("autoDrainWeb", ApexConfig.autoDrainWeb);
        general.addProperty("autoDrainBlock", ApexConfig.autoDrainBlock);
        general.addProperty("autoDrainBucket", ApexConfig.autoDrainBucket);
        general.addProperty("autoDrainRange", ApexConfig.clampAutoDrainRange(ApexConfig.autoDrainRange));

        // Triggerbot
        general.addProperty("triggerBotRandomDelayMs", ApexConfig.clampTriggerBotRandomDelayMs(ApexConfig.triggerBotRandomDelayMs));
        general.addProperty("triggerBotPlayersOnly", ApexConfig.triggerBotPlayersOnly);
        general.addProperty("triggerBotWeaponOnly", ApexConfig.triggerBotWeaponOnly);
        general.addProperty("triggerBotRequireVisible", ApexConfig.triggerBotRequireVisible);
        general.addProperty("triggerBotRequireCrosshair", ApexConfig.triggerBotRequireCrosshair);

        root.add("general", general);

        JsonArray modules = new JsonArray();
        for (Module module : ModuleManager.getModules()) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("name", module.getName());
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("bind", module.getBindKey());
            modules.add(moduleJson);
        }
        root.add("modules", modules);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            clampConfigValues();
            save();
            return;
        }

        loading = true;

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);

            if (root == null) {
                return;
            }

            loadGeneral(root);
            loadModules(root);
            clampConfigValues();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loading = false;
        }

        save();
    }

    private static void loadGeneral(JsonObject root) {
        if (root.has("general") && root.get("general").isJsonObject()) {
            JsonObject general = root.getAsJsonObject("general");

            if (general.has("hudOverlay")) {
                ApexConfig.hudOverlay = general.get("hudOverlay").getAsBoolean();
            }

            if (general.has("infoDisplay")) {
                ApexConfig.infoDisplay = general.get("infoDisplay").getAsBoolean();
            }

            if (general.has("aimAssistSpeed")) {
                ApexConfig.aimAssistSpeed = general.get("aimAssistSpeed").getAsDouble();
            }

            if (general.has("aimAssistRange")) {
                ApexConfig.aimAssistRange = general.get("aimAssistRange").getAsDouble();
            }

            if (general.has("aimAssistThroughWalls")) {
                ApexConfig.aimAssistThroughWalls = general.get("aimAssistThroughWalls").getAsBoolean();
            }

            if (general.has("aimAssistVertical")) {
                ApexConfig.aimAssistVertical = general.get("aimAssistVertical").getAsBoolean();
            }

            if (general.has("shieldStunDelayMs")) {
                ApexConfig.shieldStunDelayMs = general.get("shieldStunDelayMs").getAsDouble();
            }

            if (general.has("autoDrainWeb")) {
                ApexConfig.autoDrainWeb = general.get("autoDrainWeb").getAsBoolean();
            }

            if (general.has("autoDrainBlock")) {
                ApexConfig.autoDrainBlock = general.get("autoDrainBlock").getAsBoolean();
            }

            if (general.has("autoDrainBucket")) {
                ApexConfig.autoDrainBucket = general.get("autoDrainBucket").getAsBoolean();
            }

            if (general.has("autoDrainRange")) {
                ApexConfig.autoDrainRange = general.get("autoDrainRange").getAsDouble();
            }

            // Triggerbot: old triggerBotDelayMs / triggerBotRandomDelay are intentionally ignored now.
            if (general.has("triggerBotRandomDelayMs")) {
                ApexConfig.triggerBotRandomDelayMs = general.get("triggerBotRandomDelayMs").getAsDouble();
            }

            if (general.has("triggerBotPlayersOnly")) {
                ApexConfig.triggerBotPlayersOnly = general.get("triggerBotPlayersOnly").getAsBoolean();
            }

            if (general.has("triggerBotWeaponOnly")) {
                ApexConfig.triggerBotWeaponOnly = general.get("triggerBotWeaponOnly").getAsBoolean();
            }

            if (general.has("triggerBotRequireVisible")) {
                ApexConfig.triggerBotRequireVisible = general.get("triggerBotRequireVisible").getAsBoolean();
            }

            if (general.has("triggerBotRequireCrosshair")) {
                ApexConfig.triggerBotRequireCrosshair = general.get("triggerBotRequireCrosshair").getAsBoolean();
            }
        }
    }

    private static void loadModules(JsonObject root) {
        if (!root.has("modules") || !root.get("modules").isJsonArray()) {
            return;
        }

        JsonArray modules = root.getAsJsonArray("modules");

        for (JsonElement element : modules) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject moduleJson = element.getAsJsonObject();

            if (!moduleJson.has("name")) {
                continue;
            }

            String name = moduleJson.get("name").getAsString();
            Module module = ModuleManager.getByName(name);

            if (module == null) {
                continue;
            }

            if (moduleJson.has("enabled")) {
                module.setEnabled(moduleJson.get("enabled").getAsBoolean());
            }

            if (moduleJson.has("bind")) {
                module.setBindKey(moduleJson.get("bind").getAsInt());
            }

            // Migration support for old module-settings format.
            if (moduleJson.has("settings") && moduleJson.get("settings").isJsonObject()) {
                JsonObject settings = moduleJson.getAsJsonObject("settings");

                if (settings.has("speed")) {
                    ApexConfig.aimAssistSpeed = settings.get("speed").getAsDouble();
                }

                if (settings.has("range")) {
                    ApexConfig.aimAssistRange = settings.get("range").getAsDouble();
                }

                if (settings.has("throughWalls")) {
                    ApexConfig.aimAssistThroughWalls = settings.get("throughWalls").getAsBoolean();
                }

                if (settings.has("vertical")) {
                    ApexConfig.aimAssistVertical = settings.get("vertical").getAsBoolean();
                }

                if (settings.has("shieldStunDelayMs")) {
                    ApexConfig.shieldStunDelayMs = settings.get("shieldStunDelayMs").getAsDouble();
                }

                if (settings.has("autoDrainWeb")) {
                    ApexConfig.autoDrainWeb = settings.get("autoDrainWeb").getAsBoolean();
                }

                if (settings.has("autoDrainBlock")) {
                    ApexConfig.autoDrainBlock = settings.get("autoDrainBlock").getAsBoolean();
                }

                if (settings.has("autoDrainBucket")) {
                    ApexConfig.autoDrainBucket = settings.get("autoDrainBucket").getAsBoolean();
                }

                if (settings.has("autoDrainRange")) {
                    ApexConfig.autoDrainRange = settings.get("autoDrainRange").getAsDouble();
                }

                if (settings.has("triggerBotRandomDelayMs")) {
                    ApexConfig.triggerBotRandomDelayMs = settings.get("triggerBotRandomDelayMs").getAsDouble();
                }

                if (settings.has("triggerBotPlayersOnly")) {
                    ApexConfig.triggerBotPlayersOnly = settings.get("triggerBotPlayersOnly").getAsBoolean();
                }

                if (settings.has("triggerBotWeaponOnly")) {
                    ApexConfig.triggerBotWeaponOnly = settings.get("triggerBotWeaponOnly").getAsBoolean();
                }

                if (settings.has("triggerBotRequireVisible")) {
                    ApexConfig.triggerBotRequireVisible = settings.get("triggerBotRequireVisible").getAsBoolean();
                }

                if (settings.has("triggerBotRequireCrosshair")) {
                    ApexConfig.triggerBotRequireCrosshair = settings.get("triggerBotRequireCrosshair").getAsBoolean();
                }
            }
        }
    }

    private static void clampConfigValues() {
        ApexConfig.aimAssistSpeed = ApexConfig.clampAimAssistSpeed(ApexConfig.aimAssistSpeed);
        ApexConfig.aimAssistRange = ApexConfig.clampAimAssistRange(ApexConfig.aimAssistRange);
        ApexConfig.shieldStunDelayMs = ApexConfig.clampShieldStunDelayMs(ApexConfig.shieldStunDelayMs);
        ApexConfig.autoDrainRange = ApexConfig.clampAutoDrainRange(ApexConfig.autoDrainRange);
        ApexConfig.triggerBotRandomDelayMs = ApexConfig.clampTriggerBotRandomDelayMs(ApexConfig.triggerBotRandomDelayMs);
    }
}