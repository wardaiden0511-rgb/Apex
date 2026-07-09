package com.example.apexclient;

import com.example.apexclient.config.ConfigManager;
import com.example.apexclient.input.KeyBindManager;
import com.example.apexclient.module.AimAssistModule;
import com.example.apexclient.module.AutoDrainModule;
import com.example.apexclient.module.ModuleManager;
import com.example.apexclient.module.PressureWebModule;
import com.example.apexclient.module.ShieldStunModule;
import com.example.apexclient.module.StunSlamModule;
import com.example.apexclient.module.TriggerBotModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ApexClient implements ClientModInitializer {
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.apex.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                KeyBindManager.APEX_CATEGORY
        ));

        ModuleManager.register(new AimAssistModule());
        ModuleManager.register(new ShieldStunModule());
        ModuleManager.register(new AutoDrainModule());
        ModuleManager.register(new TriggerBotModule());
        ModuleManager.register(new PressureWebModule());
        ModuleManager.register(new StunSlamModule());

        ConfigManager.load();
        KeyBindManager.registerAll();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new ApexScreen());
                }
            }

            KeyBindManager.tick();

            // Only tick modules that are actually enabled.
            // This makes ON/OFF actually control module behavior.
            ModuleManager.tickEnabled(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player != null && ApexConfig.hudOverlay) {
                int panelW = 140;

                int enabledCount = 0;
                for (com.example.apexclient.module.Module module : ModuleManager.getModules()) {
                    if (module.isEnabled()) {
                        enabledCount++;
                    }
                }

                int panelH = 14 + enabledCount * 10;
                int panelX = 8;
                int panelY = 8;

                drawContext.fill(panelX + 2, panelY + 2, panelX + panelW + 2, panelY + panelH + 2, 0x66000000);
                drawContext.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC151515);
                drawContext.fill(panelX, panelY, panelX + panelW, panelY + 1, 0xFFFF334D);

                drawContext.drawText(
                        client.textRenderer,
                        "Apex Client",
                        panelX + 6,
                        panelY + 4,
                        0xFFFF6A7A,
                        true
                );

                int y = panelY + 14;
                for (com.example.apexclient.module.Module module : ModuleManager.getModules()) {
                    if (module.isEnabled()) {
                        drawContext.drawText(
                                client.textRenderer,
                                module.getName(),
                                panelX + 6,
                                y,
                                0xFFEDEDED,
                                true
                        );
                        y += 10;
                    }
                }
            }
        });
    }
}