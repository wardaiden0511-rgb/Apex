package com.example.apexclient.module;

import com.example.apexclient.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public abstract class Module {
    protected final String name;
    protected boolean enabled = false;

    /**
     * GLFW key code for this module bind.
     * GLFW_KEY_UNKNOWN means unbound.
     */
    protected int bindKey = GLFW.GLFW_KEY_UNKNOWN;

    public Module(String name) {
        this.name = name;
    }

    public Module(String name, int defaultBindKey) {
        this.name = name;
        this.bindKey = defaultBindKey;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }

        ConfigManager.save();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    /**
     * Called every client tick while the game is running.
     * Individual modules can override this.
     */
    public void tick(MinecraftClient client) {
        // Default: do nothing.
    }

    public int getBindKey() {
        return bindKey;
    }

    public void setBindKey(int key) {
        this.bindKey = key;
        ConfigManager.save();
    }

    public boolean hasBind() {
        return bindKey != GLFW.GLFW_KEY_UNKNOWN;
    }
}