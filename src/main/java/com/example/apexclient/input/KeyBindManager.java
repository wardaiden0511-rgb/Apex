package com.example.apexclient.input;

import com.example.apexclient.config.ConfigManager;
import com.example.apexclient.module.Module;
import com.example.apexclient.module.ModuleManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class KeyBindManager {
    public static final KeyBinding.Category APEX_CATEGORY =
            new KeyBinding.Category(Identifier.of("apex", "modules"));

    private static final Map<Module, KeyBinding> BINDS = new HashMap<>();

    private KeyBindManager() {
        // Static manager; do not instantiate.
    }

    private static String sanitize(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
    }

    public static void registerAll() {
        for (Module module : ModuleManager.getModules()) {
            register(module);
        }
    }

    public static KeyBinding register(Module module) {
        if (module == null) {
            return null;
        }

        KeyBinding existing = BINDS.get(module);
        if (existing != null) {
            return existing;
        }

        int keyCode = module.getBindKey();
        if (keyCode <= 0) {
            keyCode = GLFW.GLFW_KEY_UNKNOWN;
        }

        KeyBinding keyBinding = new KeyBinding(
                "key.apex.module." + sanitize(module.getName()),
                InputUtil.Type.KEYSYM,
                keyCode,
                APEX_CATEGORY
        );

        KeyBindingHelper.registerKeyBinding(keyBinding);
        BINDS.put(module, keyBinding);
        return keyBinding;
    }

    public static KeyBinding getBinding(Module module) {
        KeyBinding keyBinding = BINDS.get(module);
        if (keyBinding == null) {
            keyBinding = register(module);
        }
        return keyBinding;
    }

    public static String getBindName(Module module) {
        KeyBinding keyBinding = getBinding(module);
        if (keyBinding == null) {
            return "Unbound";
        }

        Text text = keyBinding.getBoundKeyLocalizedText();
        String name = text == null ? "Unbound" : text.getString();

        if (name == null || name.isBlank() || name.equalsIgnoreCase("key.keyboard.unknown")) {
            return "Unbound";
        }

        return name;
    }

    public static void setBind(Module module, InputUtil.Key key) {
        if (module == null || key == null) {
            return;
        }

        KeyBinding keyBinding = getBinding(module);
        if (keyBinding == null) {
            return;
        }

        keyBinding.setBoundKey(key);
        KeyBinding.updateKeysByCode();

        // This requires Module.java to have setBindKey(int).
        // If your Module.java does not have it yet, paste Module.java next and I will add it.
        module.setBindKey(getRawKeyCode(key));

        ConfigManager.save();
    }

    public static void clearBind(Module module) {
        setBind(module, InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_UNKNOWN));
    }

    public static void updateBind(Module module) {
        if (module == null) {
            return;
        }

        KeyBinding keyBinding = getBinding(module);
        if (keyBinding == null) {
            return;
        }

        int keyCode = module.getBindKey();
        if (keyCode <= 0) {
            keyCode = GLFW.GLFW_KEY_UNKNOWN;
        }

        keyBinding.setBoundKey(InputUtil.Type.KEYSYM.createFromCode(keyCode));
        KeyBinding.updateKeysByCode();
    }

    public static void tick() {
        for (Map.Entry<Module, KeyBinding> entry : BINDS.entrySet()) {
            Module module = entry.getKey();
            KeyBinding keyBinding = entry.getValue();

            if (module == null || keyBinding == null) {
                continue;
            }

            while (keyBinding.wasPressed()) {
                module.setEnabled(!module.isEnabled());
                ConfigManager.save();
            }
        }
    }

    private static int getRawKeyCode(InputUtil.Key key) {
        String translationKey = key.getTranslationKey();

        if (translationKey == null || translationKey.equals("key.keyboard.unknown")) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }

        // For keyboard keys, InputUtil.Key#getCode() is the GLFW key code.
        // For mouse keys, it is the mouse button code.
        return key.getCode();
    }
}