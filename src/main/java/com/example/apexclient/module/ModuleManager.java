package com.example.apexclient.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {
        // Static manager; do not instantiate.
    }

    public static void register(Module module) {
        if (module == null) {
            return;
        }

        // Prevent duplicate module registration if init somehow runs twice.
        if (getByName(module.getName()) != null) {
            return;
        }

        MODULES.add(module);
    }

    public static List<Module> getModules() {
        return Collections.unmodifiableList(MODULES);
    }

    public static Module getByName(String name) {
        if (name == null) {
            return null;
        }

        for (Module module : MODULES) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }

        return null;
    }

    /**
     * Ticks every registered module.
     * Keep this only for debugging; normal gameplay should use tickEnabled(...).
     */
    public static void tickAll(MinecraftClient client) {
        for (Module module : MODULES) {
            module.tick(client);
        }
    }

    /**
     * Ticks only enabled modules.
     * This is what makes the GUI ON/OFF toggle actually control module behavior.
     */
    public static void tickEnabled(MinecraftClient client) {
        for (Module module : MODULES) {
            if (module.isEnabled()) {
                module.tick(client);
            }
        }
    }
}