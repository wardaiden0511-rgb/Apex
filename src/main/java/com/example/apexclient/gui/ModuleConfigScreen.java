package com.example.apexclient.gui;

import com.example.apexclient.ApexConfig;
import com.example.apexclient.config.ConfigManager;
import com.example.apexclient.input.KeyBindManager;
import com.example.apexclient.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ModuleConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 380;

    // ===== GLASSMORPHISM COLOR PALETTE =====
    private static final int COLOR_OVERLAY = 0xC4000000;
    private static final int COLOR_GLASS_GLOW_OUTER = 0x20FF334D;
    private static final int COLOR_GLASS_GLOW_INNER = 0x12FF334D;
    // Panel
    private static final int COLOR_PANEL_BG = 0xF20D0D12;
    private static final int COLOR_PANEL_BORDER = 0x28FF334D;
    // Header
    private static final int COLOR_HEADER_BG = 0xE81A080D;
    private static final int COLOR_HEADER_LINE = 0xFFFF334D;
    // Rows
    private static final int COLOR_ROW_BG = 0xD814141A;
    private static final int COLOR_ROW_HOVER = 0xE01E1A22;
    private static final int COLOR_ROW_ACCENT = 0x30FF334D;
    // Accents
    private static final int COLOR_RED = 0xFFFF334D;
    private static final int COLOR_RED_SOFT = 0xFFFF6A7A;
    private static final int COLOR_RED_DARK = 0xFF8B1A26;
    private static final int COLOR_RED_GLOW = 0x20FF334D;
    private static final int COLOR_GREEN = 0xFF4DFF88;
    private static final int COLOR_GREEN_DARK = 0xFF1A4D2E;
    private static final int COLOR_GREEN_GLOW = 0x204DFF88;
    // Text
    private static final int COLOR_TEXT = 0xFFF0F0F5;
    private static final int COLOR_TEXT_DIM = 0xFFB0B0C0;
    private static final int COLOR_TEXT_MUTED = 0xFF707080;
    // Toggle
    private static final int COLOR_TOGGLE_ON_BG = 0xFF1A4D2E;
    private static final int COLOR_TOGGLE_OFF_BG = 0xFF3D1018;
    // Slider
    private static final int COLOR_SLIDER_TRACK = 0xFF2A2A32;
    private static final int COLOR_SLIDER_FILL = 0xFFFF334D;
    private static final int COLOR_SLIDER_KNOB = 0xFFFFE0E5;

    private static final int SLIDER_NONE = -1;
    private static final int SLIDER_AIM_SPEED = 0;
    private static final int SLIDER_AIM_RANGE = 1;
    private static final int SLIDER_SHIELD_DELAY = 2;
    private static final int SLIDER_AUTO_DRAIN_RANGE = 3;
    private static final int SLIDER_TRIGGER_RANDOM_DELAY = 4;
    private static final int SLIDER_PRESSURE_WEB_DELAY = 5;
    private static final int SLIDER_PRESSURE_WEB_SWAP_SPEED = 6;
    private static final int SLIDER_STUN_SLAM_SWAP_DELAY = 7;

    private final Module module;

    private boolean waitingForBind = false;
    private int draggingSlider = SLIDER_NONE;

    public ModuleConfigScreen(Module module) {
        super(Text.of(module == null ? "Module Config" : module.getName() + " Config"));
        this.module = module;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        // Dark overlay
        context.fill(0, 0, this.width, this.height, COLOR_OVERLAY);

        // === GLASSMORPHISM PANEL ===
        drawGlassGlow(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        drawRoundedRect(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 14, COLOR_PANEL_BG);
        drawBorderGlow(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 14, COLOR_PANEL_BORDER);

        // === HEADER ===
        drawRoundedRectTop(context, panelX + 1, panelY + 1, PANEL_WIDTH - 2, 42, 12, COLOR_HEADER_BG);
        context.fill(panelX + 1, panelY + 43, panelX + PANEL_WIDTH - 1, panelY + 44, COLOR_HEADER_LINE);

        // Title
        context.drawText(this.textRenderer, this.title, panelX + 22, panelY + 15, COLOR_TEXT, false);

        // Settings label
        String settingsLabel = "settings";
        int settingsWidth = this.textRenderer.getWidth(settingsLabel);
        context.drawText(this.textRenderer, settingsLabel, panelX + PANEL_WIDTH - 60 - settingsWidth, panelY + 15, COLOR_TEXT_MUTED, false);

        // Back button (glass pill)
        drawBackButton(context, panelX, panelY, mouseX, mouseY);

        // === MODULE SETTINGS ===
        if (isAimAssist()) {
            drawSlider(context, mouseX, mouseY, 0, "Aim Speed", ApexConfig.aimAssistSpeed,
                    ApexConfig.AIM_ASSIST_MIN_SPEED, ApexConfig.AIM_ASSIST_MAX_SPEED);
            drawSlider(context, mouseX, mouseY, 1, "Aim Range", ApexConfig.aimAssistRange,
                    ApexConfig.AIM_ASSIST_MIN_RANGE, ApexConfig.AIM_ASSIST_MAX_RANGE);
            drawToggle(context, mouseX, mouseY, 2, "Through Walls", ApexConfig.aimAssistThroughWalls,
                    "Track even if target is behind blocks");
            drawToggle(context, mouseX, mouseY, 3, "Vertical Assist", ApexConfig.aimAssistVertical,
                    "ON = yaw + pitch, OFF = horizontal only");
            drawBindRow(context, mouseX, mouseY, 4);
        } else if (isShieldStun()) {
            drawSlider(context, mouseX, mouseY, 0, "Hit Delay", ApexConfig.shieldStunDelayMs,
                    ApexConfig.SHIELD_STUN_MIN_DELAY_MS, ApexConfig.SHIELD_STUN_MAX_DELAY_MS);
            drawInfoRow(context, mouseX, mouseY, 1,
                    "After axe shield break, queues hit",
                    "Delay range: 5 ms to 200 ms");
            drawBindRow(context, mouseX, mouseY, 2);
        } else if (isAutoDrain()) {
            drawToggle(context, mouseX, mouseY, 0, "Web Drain", ApexConfig.autoDrainWeb,
                    "Use cobwebs to block enemy water");
            drawToggle(context, mouseX, mouseY, 1, "Block Drain", ApexConfig.autoDrainBlock,
                    "Use normal blocks to block enemy water");
            drawToggle(context, mouseX, mouseY, 2, "Bucket Drain", ApexConfig.autoDrainBucket,
                    "Use empty bucket to pick up water");
            drawSlider(context, mouseX, mouseY, 3, "Drain Range", ApexConfig.autoDrainRange,
                    ApexConfig.AUTO_DRAIN_MIN_RANGE, ApexConfig.AUTO_DRAIN_MAX_RANGE);
            drawBindRow(context, mouseX, mouseY, 4);
        } else if (isTriggerbot()) {
            drawSlider(context, mouseX, mouseY, 0, "Random Delay", ApexConfig.triggerBotRandomDelayMs,
                    ApexConfig.TRIGGER_BOT_MIN_RANDOM_DELAY_MS, ApexConfig.TRIGGER_BOT_MAX_RANDOM_DELAY_MS);
            drawToggle(context, mouseX, mouseY, 1, "Players Only", ApexConfig.triggerBotPlayersOnly,
                    "Only attack player targets");
            drawToggle(context, mouseX, mouseY, 2, "Weapon Only", ApexConfig.triggerBotWeaponOnly,
                    "Only attack with sword or axe");
            drawToggle(context, mouseX, mouseY, 3, "Require Visible", ApexConfig.triggerBotRequireVisible,
                    "Only attack visible targets");
            drawToggle(context, mouseX, mouseY, 4, "Require Crosshair", ApexConfig.triggerBotRequireCrosshair,
                    "Only attack target under crosshair");
            drawBindRow(context, mouseX, mouseY, 5);
        } else if (isPressureWeb()) {
            drawToggle(context, mouseX, mouseY, 0, "Enabled", ApexConfig.pressureWebEnabled,
                    "Auto-place cobweb on pressure plate");
            drawSlider(context, mouseX, mouseY, 1, "Web Delay", ApexConfig.pressureWebDelayTicks,
                    ApexConfig.PRESSURE_WEB_MIN_DELAY_TICKS, ApexConfig.PRESSURE_WEB_MAX_DELAY_TICKS);
            drawSlider(context, mouseX, mouseY, 2, "Swap Speed", ApexConfig.pressureWebSwapSpeedTicks,
                    ApexConfig.PRESSURE_WEB_MIN_SWAP_SPEED_TICKS, ApexConfig.PRESSURE_WEB_MAX_SWAP_SPEED_TICKS);
            drawInfoRow(context, mouseX, mouseY, 3,
                    "Auto-places cobweb after pressure plate",
                    "Requires cobweb in hotbar");
            drawBindRow(context, mouseX, mouseY, 4);
        } else if (isStunSlam()) {
            drawSlider(context, mouseX, mouseY, 0, "Swap Delay", ApexConfig.stunSlamSwapDelayTicks,
                    ApexConfig.STUN_SLAM_MIN_SWAP_DELAY_TICKS, ApexConfig.STUN_SLAM_MAX_SWAP_DELAY_TICKS);
            drawInfoRow(context, mouseX, mouseY, 1,
                    "Auto-swap to mace after shield break",
                    "Requires axe and mace in hotbar");
            drawBindRow(context, mouseX, mouseY, 2);
        } else {
            drawInfoRow(context, mouseX, mouseY, 0, "No settings for this module", "Only bind/toggle available from main GUI");
        }
    }

    // ===== GLASSMORPHISM EFFECTS =====

    private void drawGlassGlow(DrawContext context, int x, int y, int w, int h) {
        for (int i = 5; i >= 1; i--) {
            int alpha = 6 - i;
            int glowColor = (alpha << 24) | 0xFF334D;
            drawRoundedRect(context, x - i, y - i, w + i * 2, h + i * 2, 14 + i, glowColor);
        }
        drawRoundedRect(context, x + 1, y + 1, w - 2, h - 2, 13, COLOR_GLASS_GLOW_INNER);
    }

    private void drawBorderGlow(DrawContext context, int x, int y, int w, int h, int radius, int color) {
        int lineY = y + radius;
        context.fill(x + radius, lineY, x + w - radius, lineY + 1, color);
        context.fill(x + radius, lineY, x + radius + 1, y + h - radius, (0x15 << 24) | (color & 0xFFFFFF));
    }

    // ===== BACK BUTTON =====

    private void drawBackButton(DrawContext context, int panelX, int panelY, int mouseX, int mouseY) {
        int backX = panelX + PANEL_WIDTH - 76;
        int backY = panelY + 12;
        boolean backHover = isInside(mouseX, mouseY, backX, backY, 56, 20);

        int backBg = backHover ? COLOR_RED_DARK : 0xFF1A0A0E;
        drawRoundedRect(context, backX, backY, 56, 20, 8, backBg);

        if (backHover) {
            drawRoundedRect(context, backX - 1, backY - 1, 58, 22, 9, COLOR_RED_GLOW);
            drawRoundedRect(context, backX, backY, 56, 20, 8, backBg);
        }

        int backTextColor = backHover ? 0xFFFFFFFF : COLOR_RED_SOFT;
        context.drawText(this.textRenderer, "Back", backX + 16, backY + 6, backTextColor, false);
    }

    // ===== SLIDER =====

    private void drawSlider(DrawContext context, int mouseX, int mouseY, int index, String label, double value, double min, double max) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 20;
        int y = panelY + 60 + index * 36;
        int w = PANEL_WIDTH - 40;
        int h = 30;

        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);

        // Row background
        drawRoundedRect(context, x, y, w, h, 8, hovered ? COLOR_ROW_HOVER : COLOR_ROW_BG);

        // Left accent
        drawRoundedRect(context, x, y, 3, h, 2, COLOR_RED);
        context.fill(x + 3, y + 4, x + 4, y + h - 4, COLOR_RED_GLOW);

        // Label and value
        String valueText = String.format(java.util.Locale.US, "%.1f", value);
        context.drawText(this.textRenderer, label, x + 14, y + 5, COLOR_TEXT, false);

        int valueWidth = this.textRenderer.getWidth(valueText);
        context.drawText(this.textRenderer, valueText, x + w - 12 - valueWidth, y + 5, COLOR_RED_SOFT, false);

        // Slider track
        int trackX = x + 14;
        int trackY = y + h - 9;
        int trackW = w - 28;
        int trackH = 4;

        double normalized = (value - min) / (max - min);
        if (normalized < 0.0) normalized = 0.0;
        if (normalized > 1.0) normalized = 1.0;

        // Track background
        drawRoundedRect(context, trackX, trackY, trackW, trackH, 2, COLOR_SLIDER_TRACK);

        // Filled portion
        int filledW = (int) Math.round(trackW * normalized);
        if (filledW > 0) {
            drawRoundedRect(context, trackX, trackY, filledW, trackH, 2, COLOR_SLIDER_FILL);
        }

        // Knob
        int knobSize = 10;
        int knobX = trackX + filledW - knobSize / 2;
        int knobY = trackY - 3;
        drawRoundedRect(context, knobX - 1, knobY - 1, knobSize + 2, knobSize + 2, 5, COLOR_RED_GLOW);
        drawRoundedRect(context, knobX, knobY, knobSize, knobSize, 4, COLOR_SLIDER_KNOB);
    }

    // ===== TOGGLE =====

    private void drawToggle(DrawContext context, int mouseX, int mouseY, int index, String label, boolean enabled, String description) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 20;
        int y = panelY + 60 + index * 36;
        int w = PANEL_WIDTH - 40;
        int h = 30;

        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        int stateColor = enabled ? COLOR_GREEN : COLOR_RED;

        // Row background
        drawRoundedRect(context, x, y, w, h, 8, hovered ? COLOR_ROW_HOVER : COLOR_ROW_BG);
        drawRoundedRect(context, x, y, 3, h, 2, stateColor);

        // Label and description
        context.drawText(this.textRenderer, label, x + 14, y + 5, COLOR_TEXT, false);
        context.drawText(this.textRenderer, description, x + 14, y + 18, COLOR_TEXT_MUTED, false);

        // Modern toggle switch
        drawModernToggle(context, x + w - 58, y + 4, enabled, hovered);
    }

    private void drawModernToggle(DrawContext context, int x, int y, boolean enabled, boolean hovered) {
        int pillW = 52;
        int pillH = 22;

        if (hovered) {
            int glowColor = enabled ? COLOR_GREEN_GLOW : COLOR_RED_GLOW;
            drawRoundedRect(context, x - 2, y - 2, pillW + 4, pillH + 4, 12, glowColor);
        }

        int pillBg = enabled ? COLOR_TOGGLE_ON_BG : COLOR_TOGGLE_OFF_BG;
        drawRoundedRect(context, x, y, pillW, pillH, 11, pillBg);

        int highlightColor = enabled ? 0xFF2D6B3E : 0xFF5D1A24;
        drawRoundedRect(context, x + 1, y + 1, pillW - 2, pillH - 2, 10, highlightColor);

        int dotSize = 14;
        int dotX = enabled ? x + pillW - dotSize - 4 : x + 4;
        int dotY = y + (pillH - dotSize) / 2;

        int dotColor = enabled ? COLOR_GREEN : COLOR_RED;
        int dotGlow = enabled ? COLOR_GREEN_GLOW : COLOR_RED_GLOW;
        drawRoundedRect(context, dotX - 1, dotY - 1, dotSize + 2, dotSize + 2, 8, dotGlow);
        drawRoundedRect(context, dotX, dotY, dotSize, dotSize, 7, dotColor);

        String stateText = enabled ? "ON" : "OFF";
        int textX = enabled ? x + 8 : x + pillW - 22;
        int textColor = enabled ? 0xFFB9FFD0 : 0xFFFFB3BD;
        context.drawText(this.textRenderer, stateText, textX, y + 7, textColor, false);
    }

    // ===== BIND ROW =====

    private void drawBindRow(DrawContext context, int mouseX, int mouseY, int index) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 20;
        int y = panelY + 60 + index * 36;
        int w = PANEL_WIDTH - 40;
        int h = 30;

        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        int accentColor = waitingForBind ? COLOR_GREEN : COLOR_RED;

        drawRoundedRect(context, x, y, w, h, 8, hovered ? COLOR_ROW_HOVER : COLOR_ROW_BG);
        drawRoundedRect(context, x, y, 3, h, 2, accentColor);

        context.drawText(this.textRenderer, "Bind", x + 14, y + 5, COLOR_TEXT, false);

        String bindText;
        if (waitingForBind) {
            bindText = "Press a key...  (ESC cancels, BACKSPACE clears)";
        } else {
            bindText = "Current: " + KeyBindManager.getBindName(module) + "  |  Click to change";
        }

        context.drawText(this.textRenderer, bindText, x + 14, y + 18,
                waitingForBind ? 0xFFB9FFD0 : COLOR_TEXT_MUTED, false);
    }

    // ===== INFO ROW =====

    private void drawInfoRow(DrawContext context, int mouseX, int mouseY, int index, String label, String description) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 20;
        int y = panelY + 60 + index * 36;
        int w = PANEL_WIDTH - 40;
        int h = 30;

        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        drawRoundedRect(context, x, y, w, h, 8, hovered ? COLOR_ROW_HOVER : COLOR_ROW_BG);
        drawRoundedRect(context, x, y, 3, h, 2, COLOR_RED_DARK);
        context.drawText(this.textRenderer, label, x + 14, y + 5, COLOR_TEXT, false);
        context.drawText(this.textRenderer, description, x + 14, y + 18, COLOR_TEXT_MUTED, false);
    }

    // ===== INPUT HANDLING =====

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button != 0) {
            return super.mouseClicked(click, doubled);
        }

        int panelX = getPanelX();
        int panelY = getPanelY();

        // Back button
        if (isInside(mouseX, mouseY, panelX + PANEL_WIDTH - 76, panelY + 12, 56, 20)) {
            if (this.client != null) {
                this.client.setScreen(new com.example.apexclient.ApexScreen());
            }
            return true;
        }

        if (isAimAssist()) {
            if (startSliderDrag(mouseX, mouseY, 0, SLIDER_AIM_SPEED)) return true;
            if (startSliderDrag(mouseX, mouseY, 1, SLIDER_AIM_RANGE)) return true;
            if (handleAimToggleClick(mouseX, mouseY, 2, true)) return true;
            if (handleAimToggleClick(mouseX, mouseY, 3, false)) return true;
            if (handleBindRowClick(mouseX, mouseY, 4)) return true;
        } else if (isShieldStun()) {
            if (startSliderDrag(mouseX, mouseY, 0, SLIDER_SHIELD_DELAY)) return true;
            if (handleBindRowClick(mouseX, mouseY, 2)) return true;
        } else if (isAutoDrain()) {
            if (handleAutoDrainToggleClick(mouseX, mouseY, 0, 0)) return true;
            if (handleAutoDrainToggleClick(mouseX, mouseY, 1, 1)) return true;
            if (handleAutoDrainToggleClick(mouseX, mouseY, 2, 2)) return true;
            if (startSliderDrag(mouseX, mouseY, 3, SLIDER_AUTO_DRAIN_RANGE)) return true;
            if (handleBindRowClick(mouseX, mouseY, 4)) return true;
        } else if (isTriggerbot()) {
            if (startSliderDrag(mouseX, mouseY, 0, SLIDER_TRIGGER_RANDOM_DELAY)) return true;
            if (handleTriggerToggleClick(mouseX, mouseY, 1, 0)) return true;
            if (handleTriggerToggleClick(mouseX, mouseY, 2, 1)) return true;
            if (handleTriggerToggleClick(mouseX, mouseY, 3, 2)) return true;
            if (handleTriggerToggleClick(mouseX, mouseY, 4, 3)) return true;
            if (handleBindRowClick(mouseX, mouseY, 5)) return true;
        } else if (isPressureWeb()) {
            if (handlePressureWebToggleClick(mouseX, mouseY, 0)) return true;
            if (startSliderDrag(mouseX, mouseY, 1, SLIDER_PRESSURE_WEB_DELAY)) return true;
            if (startSliderDrag(mouseX, mouseY, 2, SLIDER_PRESSURE_WEB_SWAP_SPEED)) return true;
            if (handleBindRowClick(mouseX, mouseY, 4)) return true;
        } else if (isStunSlam()) {
            if (startSliderDrag(mouseX, mouseY, 0, SLIDER_STUN_SLAM_SWAP_DELAY)) return true;
            if (handleBindRowClick(mouseX, mouseY, 2)) return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (draggingSlider == SLIDER_AIM_SPEED) {
            updateSliderFromMouse(click.x(), ApexConfig.AIM_ASSIST_MIN_SPEED, ApexConfig.AIM_ASSIST_MAX_SPEED, SLIDER_AIM_SPEED);
            return true;
        }
        if (draggingSlider == SLIDER_AIM_RANGE) {
            updateSliderFromMouse(click.x(), ApexConfig.AIM_ASSIST_MIN_RANGE, ApexConfig.AIM_ASSIST_MAX_RANGE, SLIDER_AIM_RANGE);
            return true;
        }
        if (draggingSlider == SLIDER_SHIELD_DELAY) {
            updateSliderFromMouse(click.x(), ApexConfig.SHIELD_STUN_MIN_DELAY_MS, ApexConfig.SHIELD_STUN_MAX_DELAY_MS, SLIDER_SHIELD_DELAY);
            return true;
        }
        if (draggingSlider == SLIDER_AUTO_DRAIN_RANGE) {
            updateSliderFromMouse(click.x(), ApexConfig.AUTO_DRAIN_MIN_RANGE, ApexConfig.AUTO_DRAIN_MAX_RANGE, SLIDER_AUTO_DRAIN_RANGE);
            return true;
        }
        if (draggingSlider == SLIDER_TRIGGER_RANDOM_DELAY) {
            updateSliderFromMouse(click.x(), ApexConfig.TRIGGER_BOT_MIN_RANDOM_DELAY_MS, ApexConfig.TRIGGER_BOT_MAX_RANDOM_DELAY_MS, SLIDER_TRIGGER_RANDOM_DELAY);
            return true;
        }
        if (draggingSlider == SLIDER_PRESSURE_WEB_DELAY) {
            updateSliderFromMouse(click.x(), ApexConfig.PRESSURE_WEB_MIN_DELAY_TICKS, ApexConfig.PRESSURE_WEB_MAX_DELAY_TICKS, SLIDER_PRESSURE_WEB_DELAY);
            return true;
        }
        if (draggingSlider == SLIDER_PRESSURE_WEB_SWAP_SPEED) {
            updateSliderFromMouse(click.x(), ApexConfig.PRESSURE_WEB_MIN_SWAP_SPEED_TICKS, ApexConfig.PRESSURE_WEB_MAX_SWAP_SPEED_TICKS, SLIDER_PRESSURE_WEB_SWAP_SPEED);
            return true;
        }
        if (draggingSlider == SLIDER_STUN_SLAM_SWAP_DELAY) {
            updateSliderFromMouse(click.x(), ApexConfig.STUN_SLAM_MIN_SWAP_DELAY_TICKS, ApexConfig.STUN_SLAM_MAX_SWAP_DELAY_TICKS, SLIDER_STUN_SLAM_SWAP_DELAY);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (waitingForBind && module != null) {
            int key = input.key();

            if (key == GLFW.GLFW_KEY_ESCAPE) {
                waitingForBind = false;
                return true;
            }

            if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) {
                module.setBindKey(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                module.setBindKey(key);
            }

            KeyBindManager.updateBind(module);
            ConfigManager.save();
            waitingForBind = false;
            return true;
        }

        return super.keyPressed(input);
    }

    // ===== CLICK HANDLERS =====

    private boolean startSliderDrag(double mouseX, double mouseY, int rowIndex, int sliderType) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 20;
        int y = panelY + 60 + rowIndex * 36;
        int w = PANEL_WIDTH - 40;
        int h = 30;

        if (!isInside(mouseX, mouseY, x, y, w, h)) {
            return false;
        }

        draggingSlider = sliderType;

        if (sliderType == SLIDER_AIM_SPEED) {
            updateSliderFromMouse(mouseX, ApexConfig.AIM_ASSIST_MIN_SPEED, ApexConfig.AIM_ASSIST_MAX_SPEED, sliderType);
        } else if (sliderType == SLIDER_AIM_RANGE) {
            updateSliderFromMouse(mouseX, ApexConfig.AIM_ASSIST_MIN_RANGE, ApexConfig.AIM_ASSIST_MAX_RANGE, sliderType);
        } else if (sliderType == SLIDER_SHIELD_DELAY) {
            updateSliderFromMouse(mouseX, ApexConfig.SHIELD_STUN_MIN_DELAY_MS, ApexConfig.SHIELD_STUN_MAX_DELAY_MS, sliderType);
        } else if (sliderType == SLIDER_AUTO_DRAIN_RANGE) {
            updateSliderFromMouse(mouseX, ApexConfig.AUTO_DRAIN_MIN_RANGE, ApexConfig.AUTO_DRAIN_MAX_RANGE, sliderType);
        } else if (sliderType == SLIDER_TRIGGER_RANDOM_DELAY) {
            updateSliderFromMouse(mouseX, ApexConfig.TRIGGER_BOT_MIN_RANDOM_DELAY_MS, ApexConfig.TRIGGER_BOT_MAX_RANDOM_DELAY_MS, sliderType);
        } else if (sliderType == SLIDER_PRESSURE_WEB_DELAY) {
            updateSliderFromMouse(mouseX, ApexConfig.PRESSURE_WEB_MIN_DELAY_TICKS, ApexConfig.PRESSURE_WEB_MAX_DELAY_TICKS, sliderType);
        } else if (sliderType == SLIDER_PRESSURE_WEB_SWAP_SPEED) {
            updateSliderFromMouse(mouseX, ApexConfig.PRESSURE_WEB_MIN_SWAP_SPEED_TICKS, ApexConfig.PRESSURE_WEB_MAX_SWAP_SPEED_TICKS, sliderType);
        } else if (sliderType == SLIDER_STUN_SLAM_SWAP_DELAY) {
            updateSliderFromMouse(mouseX, ApexConfig.STUN_SLAM_MIN_SWAP_DELAY_TICKS, ApexConfig.STUN_SLAM_MAX_SWAP_DELAY_TICKS, sliderType);
        }

        return true;
    }

    private void updateSliderFromMouse(double mouseX, double min, double max, int sliderType) {
        int panelX = getPanelX();

        int x = panelX + 20;
        int w = PANEL_WIDTH - 40;
        int trackX = x + 14;
        int trackW = w - 28;

        double normalized = (mouseX - trackX) / (double) trackW;
        if (normalized < 0.0) normalized = 0.0;
        if (normalized > 1.0) normalized = 1.0;

        double newValue = min + (max - min) * normalized;
        newValue = Math.round(newValue * 10.0) / 10.0;

        if (sliderType == SLIDER_AIM_SPEED) {
            ApexConfig.aimAssistSpeed = ApexConfig.clampAimAssistSpeed(newValue);
        } else if (sliderType == SLIDER_AIM_RANGE) {
            ApexConfig.aimAssistRange = ApexConfig.clampAimAssistRange(newValue);
        } else if (sliderType == SLIDER_SHIELD_DELAY) {
            ApexConfig.shieldStunDelayMs = ApexConfig.clampShieldStunDelayMs(newValue);
        } else if (sliderType == SLIDER_AUTO_DRAIN_RANGE) {
            ApexConfig.autoDrainRange = ApexConfig.clampAutoDrainRange(newValue);
        } else if (sliderType == SLIDER_TRIGGER_RANDOM_DELAY) {
            ApexConfig.triggerBotRandomDelayMs = ApexConfig.clampTriggerBotRandomDelayMs(newValue);
        } else if (sliderType == SLIDER_PRESSURE_WEB_DELAY) {
            ApexConfig.pressureWebDelayTicks = ApexConfig.clampPressureWebDelayTicks(newValue);
        } else if (sliderType == SLIDER_PRESSURE_WEB_SWAP_SPEED) {
            ApexConfig.pressureWebSwapSpeedTicks = ApexConfig.clampPressureWebSwapSpeedTicks(newValue);
        } else if (sliderType == SLIDER_STUN_SLAM_SWAP_DELAY) {
            ApexConfig.stunSlamSwapDelayTicks = ApexConfig.clampStunSlamSwapDelayTicks(newValue);
        }

        ConfigManager.save();
    }

    private boolean handleAimToggleClick(double mouseX, double mouseY, int index, boolean throughWallsToggle) {
        if (!isInside(mouseX, mouseY, getPanelX() + 20, getPanelY() + 60 + index * 36, PANEL_WIDTH - 40, 30)) {
            return false;
        }
        draggingSlider = SLIDER_NONE;
        if (throughWallsToggle) {
            ApexConfig.aimAssistThroughWalls = !ApexConfig.aimAssistThroughWalls;
        } else {
            ApexConfig.aimAssistVertical = !ApexConfig.aimAssistVertical;
        }
        ConfigManager.save();
        return true;
    }

    private boolean handleAutoDrainToggleClick(double mouseX, double mouseY, int index, int mode) {
        if (!isInside(mouseX, mouseY, getPanelX() + 20, getPanelY() + 60 + index * 36, PANEL_WIDTH - 40, 30)) {
            return false;
        }
        draggingSlider = SLIDER_NONE;
        if (mode == 0) ApexConfig.autoDrainWeb = !ApexConfig.autoDrainWeb;
        else if (mode == 1) ApexConfig.autoDrainBlock = !ApexConfig.autoDrainBlock;
        else if (mode == 2) ApexConfig.autoDrainBucket = !ApexConfig.autoDrainBucket;
        ConfigManager.save();
        return true;
    }

    private boolean handleTriggerToggleClick(double mouseX, double mouseY, int index, int mode) {
        if (!isInside(mouseX, mouseY, getPanelX() + 20, getPanelY() + 60 + index * 36, PANEL_WIDTH - 40, 30)) {
            return false;
        }
        draggingSlider = SLIDER_NONE;
        if (mode == 0) ApexConfig.triggerBotPlayersOnly = !ApexConfig.triggerBotPlayersOnly;
        else if (mode == 1) ApexConfig.triggerBotWeaponOnly = !ApexConfig.triggerBotWeaponOnly;
        else if (mode == 2) ApexConfig.triggerBotRequireVisible = !ApexConfig.triggerBotRequireVisible;
        else if (mode == 3) ApexConfig.triggerBotRequireCrosshair = !ApexConfig.triggerBotRequireCrosshair;
        ConfigManager.save();
        return true;
    }

    private boolean handlePressureWebToggleClick(double mouseX, double mouseY, int index) {
        if (!isInside(mouseX, mouseY, getPanelX() + 20, getPanelY() + 60 + index * 36, PANEL_WIDTH - 40, 30)) {
            return false;
        }
        draggingSlider = SLIDER_NONE;
        ApexConfig.pressureWebEnabled = !ApexConfig.pressureWebEnabled;
        ConfigManager.save();
        return true;
    }

    private boolean handleBindRowClick(double mouseX, double mouseY, int index) {
        if (!isInside(mouseX, mouseY, getPanelX() + 20, getPanelY() + 60 + index * 36, PANEL_WIDTH - 40, 30)) {
            return false;
        }
        draggingSlider = SLIDER_NONE;
        waitingForBind = true;
        return true;
    }

    // ===== MODULE TYPE CHECKS =====

    private boolean isAimAssist() {
        return module != null && module.getName().equalsIgnoreCase("Aim Assist");
    }

    private boolean isShieldStun() {
        return module != null && module.getName().equalsIgnoreCase("Shield Stun");
    }

    private boolean isAutoDrain() {
        return module != null && module.getName().equalsIgnoreCase("Auto Drain");
    }

    private boolean isTriggerbot() {
        return module != null && module.getName().equalsIgnoreCase("Triggerbot");
    }

    private boolean isPressureWeb() {
        return module != null && module.getName().equalsIgnoreCase("Pressure Web");
    }

    private boolean isStunSlam() {
        return module != null && module.getName().equalsIgnoreCase("Stun Slam");
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ===== HELPERS =====

    private int getPanelX() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int getPanelY() {
        return (this.height - PANEL_HEIGHT) / 2;
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    // ===== ROUNDED RECTANGLE DRAWING =====

    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height - radius, color);
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                    context.fill(x + width - radius + i, y + j, x + width - radius + i + 1, y + j + 1, color);
                    context.fill(x + i, y + height - radius + j, x + i + 1, y + height - radius + j + 1, color);
                    context.fill(x + width - radius + i, y + height - radius + j, x + width - radius + i + 1, y + height - radius + j + 1, color);
                }
            }
        }
    }

    private void drawRoundedRectTop(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height, color);
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                    context.fill(x + width - radius + i, y + j, x + width - radius + i + 1, y + j + 1, color);
                }
            }
        }
    }
}
