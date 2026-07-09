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
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 360;

    private static final int COLOR_OVERLAY = 0xB8000000;
    private static final int COLOR_OUTER = 0xFF070708;
    private static final int COLOR_PANEL = 0xFF101013;
    private static final int COLOR_HEADER = 0xFF1A0A0D;
    private static final int COLOR_ROW = 0xFF17171C;
    private static final int COLOR_ROW_HOVER = 0xFF21191D;

    private static final int COLOR_RED = 0xFFFF334D;
    private static final int COLOR_RED_SOFT = 0xFFFF6A7A;
    private static final int COLOR_RED_DARK = 0xFF6D111D;
    private static final int COLOR_GREEN = 0xFF4DFF88;
    private static final int COLOR_GREEN_DARK = 0xFF123F24;
    private static final int COLOR_GLOW = 0x33FF334D;

    private static final int COLOR_TEXT = 0xFFEDEDED;
    private static final int COLOR_MUTED = 0xFF9A9AA2;

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

        context.fill(0, 0, this.width, this.height, COLOR_OVERLAY);

        // Glow effect with rounded corners
        context.fill(panelX + 8, panelY + 8, panelX + PANEL_WIDTH + 8, panelY + PANEL_HEIGHT + 8, COLOR_GLOW);
        context.fill(panelX + 6, panelY + 6, panelX + PANEL_WIDTH + 6, panelY + PANEL_HEIGHT + 6, 0x77000000);
        drawRoundedRect(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 12, COLOR_OUTER);
        drawRoundedRect(context, panelX + 1, panelY + 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2, 11, COLOR_PANEL);

        // Header with rounded top corners
        drawRoundedRect(context, panelX + 1, panelY + 1, PANEL_WIDTH - 2, 39, 10, COLOR_HEADER);
        context.fill(panelX + 1, panelY + 40, panelX + PANEL_WIDTH - 1, panelY + 41, COLOR_RED);

        context.drawText(this.textRenderer, this.title, panelX + 26, panelY + 14, COLOR_TEXT, false);
        context.drawText(this.textRenderer, "settings", panelX + PANEL_WIDTH - 108, panelY + 14, COLOR_MUTED, false);

        drawBackButton(context, mouseX, mouseY);

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

    private void drawBackButton(DrawContext context, int mouseX, int mouseY) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int backX = panelX + PANEL_WIDTH - 72;
        int backY = panelY + 11;
        boolean backHover = isInside(mouseX, mouseY, backX, backY, 54, 18);

        drawRoundedRect(context, backX, backY, 54, 18, 6, backHover ? COLOR_RED_DARK : 0xFF251116);
        context.drawText(this.textRenderer, "Back", backX + 14, backY + 5, backHover ? 0xFFFFFFFF : 0xFFFFA3AF, false);
    }

    private void drawSlider(DrawContext context, int mouseX, int mouseY, int index, String label, double value, double min, double max) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        drawRoundedRect(context, x, y, w, h, 6, hovered ? COLOR_ROW_HOVER : COLOR_ROW);
        drawRoundedRect(context, x, y, 3, h, 3, COLOR_RED);

        String valueText = String.format(java.util.Locale.US, "%.1f", value);
        context.drawText(this.textRenderer, label, x + 12, y + 4, COLOR_TEXT, false);
        context.drawText(this.textRenderer, valueText, x + w - 54, y + 4, 0xFFFFA3AF, false);

        int trackX = x + 12;
        int trackY = y + h - 8;
        int trackW = w - 24;
        int trackH = 4;

        double normalized = (value - min) / (max - min);
        if (normalized < 0.0) normalized = 0.0;
        if (normalized > 1.0) normalized = 1.0;

        context.fill(trackX, trackY, trackX + trackW, trackY + trackH, 0xFF2B2B30);

        int filledW = (int) Math.round(trackW * normalized);
        context.fill(trackX, trackY, trackX + filledW, trackY + trackH, COLOR_RED);

        int knobW = 8;
        int knobH = 12;
        int knobX = trackX + filledW - knobW / 2;
        int knobY = trackY - 4;
        context.fill(knobX, knobY, knobX + knobW, knobY + knobH, 0xFFFFCCD2);
    }

    private void drawToggle(DrawContext context, int mouseX, int mouseY, int index, String label, boolean enabled, String description) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        int stateColor = enabled ? COLOR_GREEN : COLOR_RED;
        int stateDark = enabled ? COLOR_GREEN_DARK : COLOR_RED_DARK;

        drawRoundedRect(context, x, y, w, h, 6, hovered ? COLOR_ROW_HOVER : COLOR_ROW);
        drawRoundedRect(context, x, y, 3, h, 3, stateColor);

        context.drawText(this.textRenderer, label, x + 12, y + 4, COLOR_TEXT, false);
        context.drawText(this.textRenderer, description, x + 12, y + 17, COLOR_MUTED, false);

        int pillW = 54;
        int pillH = 16;
        int pillX = x + w - pillW - 10;
        int pillY = y + 6;

        drawRoundedRect(context, pillX, pillY, pillW, pillH, 8, stateDark);
        drawRoundedRect(context, pillX + 1, pillY + 1, pillW - 2, pillH - 2, 7, enabled ? 0xFF102A19 : 0xFF351017);

        int dot = 8;
        int dotX = enabled ? pillX + pillW - dot - 5 : pillX + 5;
        int dotY = pillY + 4;
        context.fill(dotX, dotY, dotX + dot, dotY + dot, stateColor);

        String stateText = enabled ? "ON" : "OFF";
        int textX = enabled ? pillX + 10 : pillX + 22;
        context.drawText(this.textRenderer, stateText, textX, pillY + 4, enabled ? 0xFFB9FFD0 : 0xFFFFA3AF, false);
    }

    private void drawBindRow(DrawContext context, int mouseX, int mouseY, int index) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);

        drawRoundedRect(context, x, y, w, h, 6, hovered ? COLOR_ROW_HOVER : COLOR_ROW);
        drawRoundedRect(context, x, y, 3, h, 3, waitingForBind ? COLOR_GREEN : COLOR_RED);

        context.drawText(this.textRenderer, "Bind", x + 12, y + 4, COLOR_TEXT, false);

        String bindText;
        if (waitingForBind) {
            bindText = "Press a key...  (ESC cancels, BACKSPACE clears)";
        } else {
            bindText = "Current: " + KeyBindManager.getBindName(module) + "  |  Click to change";
        }

        context.drawText(this.textRenderer, bindText, x + 12, y + 17, waitingForBind ? 0xFFB9FFD0 : COLOR_MUTED, false);
    }

    private void drawInfoRow(DrawContext context, int mouseX, int mouseY, int index, String label, String description) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        drawRoundedRect(context, x, y, w, h, 6, hovered ? COLOR_ROW_HOVER : COLOR_ROW);
        drawRoundedRect(context, x, y, 3, h, 3, COLOR_RED_DARK);
        context.drawText(this.textRenderer, label, x + 12, y + 4, COLOR_TEXT, false);
        context.drawText(this.textRenderer, description, x + 12, y + 17, COLOR_MUTED, false);
    }

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

        if (isInside(mouseX, mouseY, panelX + PANEL_WIDTH - 72, panelY + 11, 54, 18)) {
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

    private boolean startSliderDrag(double mouseX, double mouseY, int rowIndex, int sliderType) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + rowIndex * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

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

        int x = panelX + 18;
        int w = PANEL_WIDTH - 36;
        int trackX = x + 12;
        int trackW = w - 24;

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
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        if (!isInside(mouseX, mouseY, x, y, w, h)) {
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
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        if (!isInside(mouseX, mouseY, x, y, w, h)) {
            return false;
        }

        draggingSlider = SLIDER_NONE;

        if (mode == 0) {
            ApexConfig.autoDrainWeb = !ApexConfig.autoDrainWeb;
        } else if (mode == 1) {
            ApexConfig.autoDrainBlock = !ApexConfig.autoDrainBlock;
        } else if (mode == 2) {
            ApexConfig.autoDrainBucket = !ApexConfig.autoDrainBucket;
        }

        ConfigManager.save();
        return true;
    }

    private boolean handleTriggerToggleClick(double mouseX, double mouseY, int index, int mode) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        if (!isInside(mouseX, mouseY, x, y, w, h)) {
            return false;
        }

        draggingSlider = SLIDER_NONE;

        if (mode == 0) {
            ApexConfig.triggerBotPlayersOnly = !ApexConfig.triggerBotPlayersOnly;
        } else if (mode == 1) {
            ApexConfig.triggerBotWeaponOnly = !ApexConfig.triggerBotWeaponOnly;
        } else if (mode == 2) {
            ApexConfig.triggerBotRequireVisible = !ApexConfig.triggerBotRequireVisible;
        } else if (mode == 3) {
            ApexConfig.triggerBotRequireCrosshair = !ApexConfig.triggerBotRequireCrosshair;
        }

        ConfigManager.save();
        return true;
    }

    private boolean handlePressureWebToggleClick(double mouseX, double mouseY, int index) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        if (!isInside(mouseX, mouseY, x, y, w, h)) {
            return false;
        }

        draggingSlider = SLIDER_NONE;
        ApexConfig.pressureWebEnabled = !ApexConfig.pressureWebEnabled;
        ConfigManager.save();
        return true;
    }

    private boolean handleBindRowClick(double mouseX, double mouseY, int index) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        int x = panelX + 18;
        int y = panelY + 56 + index * 34;
        int w = PANEL_WIDTH - 36;
        int h = 28;

        if (!isInside(mouseX, mouseY, x, y, w, h)) {
            return false;
        }

        draggingSlider = SLIDER_NONE;
        waitingForBind = true;
        return true;
    }

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

    private int getPanelX() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int getPanelY() {
        return (this.height - PANEL_HEIGHT) / 2;
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        // Draw center
        context.fill(x + radius, y, x + width - radius, y + height, color);

        // Draw top and bottom strips
        context.fill(x, y + radius, x + width, y + height - radius, color);

        // Draw corners
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                // Top-left
                if (i * i + j * j <= radius * radius) {
                    context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                }
                // Top-right
                if (i * i + j * j <= radius * radius) {
                    context.fill(x + width - radius + i, y + j, x + width - radius + i + 1, y + j + 1, color);
                }
                // Bottom-left
                if (i * i + j * j <= radius * radius) {
                    context.fill(x + i, y + height - radius + j, x + i + 1, y + height - radius + j + 1, color);
                }
                // Bottom-right
                if (i * i + j * j <= radius * radius) {
                    context.fill(x + width - radius + i, y + height - radius + j, x + width - radius + i + 1, y + height - radius + j + 1, color);
                }
            }
        }
    }
}