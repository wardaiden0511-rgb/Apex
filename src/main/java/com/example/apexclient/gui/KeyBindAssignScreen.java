package com.example.apexclient.gui;

import com.example.apexclient.module.Module;
import com.example.apexclient.config.ConfigManager;
import com.example.apexclient.input.KeyBindManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.DrawContext;

public class KeyBindAssignScreen extends Screen {
    private final Module module;
    private final Screen parent;

    protected KeyBindAssignScreen(Module module, Screen parent) {
        super(Text.literal("Press any key to bind: " + module.getName()));
        this.module = module;
        this.parent = parent;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        // Save GLFW key code
        module.setBindKey(input.key());
        ConfigManager.save();
        KeyBindManager.updateBind(module);
        this.client.setScreen(parent);
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(context, mouseX, mouseY, delta);
    }
}