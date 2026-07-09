package com.example.apexclient;

import com.example.apexclient.config.ConfigManager;
import com.example.apexclient.input.KeyBindManager;
import com.example.apexclient.module.Module;
import com.example.apexclient.module.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ApexScreen extends Screen {
    private static final int PANEL_WIDTH = 480;
    private static final int PANEL_HEIGHT = 320;
    private static final int SIDEBAR_WIDTH = 120;

    private static final int COLOR_OVERLAY = 0xB8000000;
    private static final int COLOR_OUTER = 0xFF070708;
    private static final int COLOR_PANEL = 0xFF101013;
    private static final int COLOR_HEADER = 0xFF1A0A0D;
    private static final int COLOR_SIDEBAR = 0xFF0D0D10;
    private static final int COLOR_SIDEBAR_HOVER = 0xFF211116;
    private static final int COLOR_SIDEBAR_SELECTED = 0xFF2A1116;
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

    private static final String[] CATEGORIES = {"Combat", "Utility", "Visual", "All"};
    private int selectedCategory = 0;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int scrollOffset = 0;
    private int searchCursorTicks = 0;

    public ApexScreen() {
        super(Text.of("Apex Client"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        searchCursorTicks++;

        int panelX = getPanelX();
        int panelY = getPanelY();

        context.fill(0, 0, this.width, this.height, COLOR_OVERLAY);

        // Shadow and frame with glow effect.
        context.fill(panelX + 8, panelY + 8, panelX + PANEL_WIDTH + 8, panelY + PANEL_HEIGHT + 8, COLOR_GLOW);
        context.fill(panelX + 6, panelY + 6, panelX + PANEL_WIDTH + 6, panelY + PANEL_HEIGHT + 6, 0x77000000);
        drawRoundedRect(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 12, COLOR_OUTER);
        drawRoundedRect(context, panelX + 1, panelY + 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2, 11, COLOR_PANEL);

        // Header with rounded top corners matching panel.
        drawRoundedRect(context, panelX + 1, panelY + 1, PANEL_WIDTH - 2, 39, 11, COLOR_HEADER);
        context.fill(panelX + 1, panelY + 40, panelX + PANEL_WIDTH - 1, panelY + 41, COLOR_RED);

        // Header text.
        context.drawText(this.textRenderer, "Apex", panelX + 16, panelY + 14, COLOR_TEXT, false);
        context.drawText(this.textRenderer, "Client", panelX + 48, panelY + 14, COLOR_RED_SOFT, false);
        context.drawText(this.textRenderer, "v1.0.0", panelX + PANEL_WIDTH - 70, panelY + 14, COLOR_MUTED, false);

        // Done button with rounded corners.
        int doneX = panelX + PANEL_WIDTH - 72;
        int doneY = panelY + 11;
        boolean doneHover = isInside(mouseX, mouseY, doneX, doneY, 54, 18);
        drawRoundedRect(context, doneX, doneY, 54, 18, 6, doneHover ? COLOR_RED_DARK : 0xFF251116);
        context.drawText(this.textRenderer, "Done", doneX + 14, doneY + 5, doneHover ? 0xFFFFFFFF : 0xFFFFA3AF, false);

        // Sidebar with rounded corners.
        drawRoundedRect(context, panelX + 1, panelY + 41, SIDEBAR_WIDTH - 1, PANEL_HEIGHT - 42, 8, COLOR_SIDEBAR);
        context.fill(panelX + SIDEBAR_WIDTH, panelY + 41, panelX + SIDEBAR_WIDTH + 1, panelY + PANEL_HEIGHT - 1, 0xFF2A1116);

        // Draw categories.
        drawCategories(context, mouseX, mouseY, panelX, panelY);

        // Draw search bar.
        drawSearchBar(context, mouseX, mouseY, panelX, panelY);

        // Draw module rows.
        drawModuleRows(context, mouseX, mouseY, panelX, panelY);

        // Draw module count at bottom.
        int enabledCount = 0;
        for (Module m : ModuleManager.getModules()) {
            if (m.isEnabled()) enabledCount++;
        }
        String countText = enabledCount + "/" + ModuleManager.getModules().size() + " active";
        context.drawText(this.textRenderer, countText, panelX + SIDEBAR_WIDTH + 16, panelY + PANEL_HEIGHT - 22, COLOR_MUTED, false);
    }

    private void drawCategories(DrawContext context, int mouseX, int mouseY, int panelX, int panelY) {
        int categoryY = panelY + 50;
        int categoryHeight = 26;

        for (int i = 0; i < CATEGORIES.length; i++) {
            int catX = panelX + 6;
            int catY = categoryY + i * (categoryHeight + 4);
            int catW = SIDEBAR_WIDTH - 12;
            boolean isSelected = i == selectedCategory;
            boolean isHovered = isInside(mouseX, mouseY, catX, catY, catW, categoryHeight);

            if (isSelected) {
                drawRoundedRect(context, catX, catY, catW, categoryHeight, 6, COLOR_SIDEBAR_SELECTED);
                drawRoundedRect(context, catX, catY, 3, categoryHeight, 3, COLOR_RED);
            } else if (isHovered) {
                drawRoundedRect(context, catX, catY, catW, categoryHeight, 6, COLOR_SIDEBAR_HOVER);
            }

            context.drawText(this.textRenderer, CATEGORIES[i], catX + (isSelected ? 10 : 6), catY + 8, isSelected ? COLOR_RED_SOFT : COLOR_TEXT, false);
        }
    }

    private void drawSearchBar(DrawContext context, int mouseX, int mouseY, int panelX, int panelY) {
        int searchX = panelX + SIDEBAR_WIDTH + 16;
        int searchY = panelY + 50;
        int searchW = PANEL_WIDTH - SIDEBAR_WIDTH - 32;
        int searchH = 24;

        boolean isHovered = isInside(mouseX, mouseY, searchX, searchY, searchW, searchH);
        int bgColor = searchFocused ? COLOR_ROW_HOVER : (isHovered ? COLOR_ROW_HOVER : COLOR_ROW);
        int borderColor = searchFocused ? COLOR_RED : 0xFF2A2A30;

        // Border
        drawRoundedRect(context, searchX - 1, searchY - 1, searchW + 2, searchH + 2, 7, borderColor);
        drawRoundedRect(context, searchX, searchY, searchW, searchH, 6, bgColor);

        String displayText = searchQuery.isEmpty() && !searchFocused ? "Search modules..." : searchQuery;
        int textColor = searchQuery.isEmpty() && !searchFocused ? COLOR_MUTED : COLOR_TEXT;
        context.drawText(this.textRenderer, displayText, searchX + 8, searchY + 6, textColor, false);

        // Draw cursor if focused and query not empty
        if (searchFocused && searchCursorTicks % 40 < 20) {
            int textWidth = this.textRenderer.getWidth(searchQuery);
            context.fill(searchX + 8 + textWidth, searchY + 5, searchX + 8 + textWidth + 1, searchY + 17, COLOR_RED_SOFT);
        }

        // Clear button when search has text
        if (!searchQuery.isEmpty()) {
            int clearX = searchX + searchW - 18;
            int clearY = searchY + 5;
            boolean clearHover = isInside(mouseX, mouseY, clearX, clearY, 14, 14);
            context.drawText(this.textRenderer, "x", clearX + 4, clearY + 1, clearHover ? COLOR_RED : COLOR_MUTED, false);
        }
    }

    private void drawModuleRows(DrawContext context, int mouseX, int mouseY, int panelX, int panelY) {
        List<Module> modules = getFilteredModules();

        int contentX = panelX + SIDEBAR_WIDTH + 16;
        int startY = panelY + 85;
        int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - 32;
        int rowW = contentWidth - 34;
        int gearW = 27;
        int rowH = 44;
        int rowSpacing = 6;

        // Calculate max scroll offset
        int totalHeight = modules.size() * (rowH + rowSpacing);
        int availableHeight = PANEL_HEIGHT - 100;
        int maxScrollOffset = Math.max(0, totalHeight - availableHeight);

        // Clamp scroll offset
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        // Show result count when searching
        if (!searchQuery.isEmpty()) {
            String resultText = modules.size() + " result" + (modules.size() != 1 ? "s" : "");
            context.drawText(this.textRenderer, resultText, contentX + rowW - 60, panelY + 72, COLOR_MUTED, false);
        }

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int y = startY + i * (rowH + rowSpacing) - scrollOffset;

            // Skip if outside visible area (ensure fully visible)
            if (y + rowH < panelY + 85 || y > panelY + PANEL_HEIGHT - 45) {
                continue;
            }

            boolean enabled = module.isEnabled();
            boolean rowHover = isInside(mouseX, mouseY, contentX, y, rowW, rowH);
            boolean gearHover = isInside(mouseX, mouseY, contentX + rowW + 7, y, gearW, rowH);

            int stateColor = enabled ? COLOR_GREEN : COLOR_RED;
            int stateDark = enabled ? COLOR_GREEN_DARK : COLOR_RED_DARK;

            // Module row with rounded corners.
            drawRoundedRect(context, contentX, y, rowW, rowH, 8, rowHover ? COLOR_ROW_HOVER : COLOR_ROW);
            drawRoundedRect(context, contentX, y, 3, rowH, 3, stateColor);

            context.drawText(this.textRenderer, module.getName(), contentX + 12, y + 6, COLOR_TEXT, false);
            context.drawText(this.textRenderer, getModuleDescription(module), contentX + 12, y + 18, COLOR_MUTED, false);
            context.drawText(this.textRenderer, "Bind: " + KeyBindManager.getBindName(module), contentX + 12, y + 28, COLOR_MUTED, false);

            // ON/OFF pill with rounded corners.
            int pillW = 54;
            int pillH = 16;
            int pillX = contentX + rowW - pillW - 10;
            int pillY = y + 9;

            drawRoundedRect(context, pillX, pillY, pillW, pillH, 8, stateDark);
            drawRoundedRect(context, pillX + 1, pillY + 1, pillW - 2, pillH - 2, 7, enabled ? 0xFF102A19 : 0xFF351017);

            int dot = 8;
            int dotX = enabled ? pillX + pillW - dot - 5 : pillX + 5;
            int dotY = pillY + 4;
            context.fill(dotX, dotY, dotX + dot, dotY + dot, stateColor);

            String stateText = enabled ? "ON" : "OFF";
            int textX = enabled ? pillX + 10 : pillX + 22;
            context.drawText(this.textRenderer, stateText, textX, pillY + 4, enabled ? 0xFFB9FFD0 : 0xFFFFA3AF, false);

            // Gear box with rounded corners.
            int gearX = contentX + rowW + 7;
            drawRoundedRect(context, gearX, y, gearW, rowH, 8, gearHover ? COLOR_RED_DARK : 0xFF211116);
            context.drawText(this.textRenderer, "\u2699", gearX + 9, y + 16, gearHover ? 0xFFFFFFFF : 0xFFFFA3AF, false);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        int panelX = getPanelX();
        int panelY = getPanelY();

        // Search bar focus handling
        int searchX = panelX + SIDEBAR_WIDTH + 16;
        int searchY = panelY + 50;
        int searchW = PANEL_WIDTH - SIDEBAR_WIDTH - 32;
        int searchH = 24;

        // Clear button
        if (!searchQuery.isEmpty() && isInside(mouseX, mouseY, searchX + searchW - 18, searchY + 5, 14, 14)) {
            searchQuery = "";
            searchFocused = false;
            scrollOffset = 0;
            return true;
        }

        // Search bar click
        if (isInside(mouseX, mouseY, searchX, searchY, searchW, searchH)) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // Done button.
        if (isInside(mouseX, mouseY, panelX + PANEL_WIDTH - 72, panelY + 11, 54, 18)) {
            if (this.client != null) {
                this.client.setScreen(null);
            }
            return true;
        }

        // Handle category clicks.
        int categoryY = panelY + 50;
        int categoryHeight = 26;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int catX = panelX + 6;
            int catY = categoryY + i * (categoryHeight + 4);
            int catW = SIDEBAR_WIDTH - 12;
            if (isInside(mouseX, mouseY, catX, catY, catW, categoryHeight)) {
                selectedCategory = i;
                scrollOffset = 0;
                return true;
            }
        }

        // Handle module row clicks.
        List<Module> modules = getFilteredModules();
        int contentX = panelX + SIDEBAR_WIDTH + 16;
        int startY = panelY + 85;
        int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - 32;
        int rowW = contentWidth - 34;
        int gearW = 27;
        int rowH = 44;
        int rowSpacing = 6;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int y = startY + i * (rowH + rowSpacing) - scrollOffset;

            // Skip if outside visible area
            if (y + rowH < panelY + 85 || y > panelY + PANEL_HEIGHT - 20) {
                continue;
            }

            boolean overRow = isInside(mouseX, mouseY, contentX, y, rowW, rowH);
            boolean overGear = isInside(mouseX, mouseY, contentX + rowW + 7, y, gearW, rowH);

            // Left click row = toggle.
            if (button == 0 && overRow) {
                module.setEnabled(!module.isEnabled());
                ConfigManager.save();
                return true;
            }

            // Left click gear OR right click row = open config.
            if ((button == 0 && overGear) || (button == 1 && overRow)) {
                if (this.client != null) {
                    this.client.setScreen(new com.example.apexclient.gui.ModuleConfigScreen(module));
                }
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        // Handle search input
        if (searchFocused) {
            int key = input.key();

            if (key == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }

            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    scrollOffset = 0;
                }
                return true;
            }

            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                searchFocused = false;
                return true;
            }

            // Allow copy/paste
            if (key == GLFW.GLFW_KEY_V && (input.mods() & GLFW.GLFW_MOD_CONTROL) != 0) {
                String clipboard = this.client != null ? this.client.keyboard.getClipboard() : "";
                if (clipboard != null) {
                    searchQuery += clipboard.replaceAll("[^a-zA-Z0-9 _-]", "");
                    scrollOffset = 0;
                }
                return true;
            }

            // Navigation keys
            if (key == GLFW.GLFW_KEY_DELETE) {
                searchQuery = "";
                scrollOffset = 0;
                return true;
            }

            return true; // Consume all other keys while focused
        }

        // Quick search: any letter/number starts search
        if (!searchFocused && !input.mods().isPresent()) {
            String keyName = GLFW.glfwGetKeyName(input.key(), input.scancode());
            if (keyName != null && keyName.length() == 1 && Character.isLetterOrDigit(keyName.charAt(0))) {
                searchFocused = true;
                searchQuery = keyName.toLowerCase();
                scrollOffset = 0;
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused) {
            if (chr >= 32 && chr < 127) {
                searchQuery += Character.toLowerCase(chr);
                scrollOffset = 0;
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        // Only scroll if not focused on search
        if (searchFocused) {
            return true;
        }

        scrollOffset -= (int) (vertical * 20);

        List<Module> modules = getFilteredModules();
        int rowH = 44;
        int rowSpacing = 6;
        int totalHeight = modules.size() * (rowH + rowSpacing);
        int availableHeight = PANEL_HEIGHT - 100;
        int maxScrollOffset = Math.max(0, totalHeight - availableHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        return true;
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

    private List<Module> getFilteredModules() {
        List<Module> allModules = ModuleManager.getModules();
        List<Module> filtered = new java.util.ArrayList<>();

        for (Module module : allModules) {
            // Filter by category
            if (selectedCategory < CATEGORIES.length - 1) {
                String category = CATEGORIES[selectedCategory];
                if (!moduleMatchesCategory(module, category)) {
                    continue;
                }
            }

            // Filter by search query
            if (!searchQuery.isEmpty() && !module.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                continue;
            }

            filtered.add(module);
        }

        return filtered;
    }

    private boolean moduleMatchesCategory(Module module, String category) {
        String name = module.getName().toLowerCase();
        return switch (category) {
            case "Combat" -> name.contains("aim") || name.contains("trigger") || name.contains("shield") || name.contains("kill") || name.contains("stun") || name.contains("pressure");
            case "Utility" -> name.contains("auto") || name.contains("drain") || name.contains("sprint") || name.contains("sneak");
            case "Visual" -> name.contains("esp") || name.contains("xray") || name.contains("fullbright") || name.contains("nametag");
            default -> true;
        };
    }

    private String getModuleDescription(Module module) {
        String name = module.getName().toLowerCase();
        if (name.contains("aim")) return "Auto-aims at nearest player";
        if (name.contains("trigger")) return "Auto-attacks entities under crosshair";
        if (name.contains("shield")) return "Auto-queues follow-up hit after shield break";
        if (name.contains("drain")) return "Auto-selects drain items near enemy water";
        if (name.contains("pressure")) return "Auto-places cobweb on pressure plate";
        if (name.contains("stun")) return "Auto-swaps to mace after shield break";
        if (name.contains("sprint")) return "Automatically sprints when moving";
        if (name.contains("sneak")) return "Automatically sneaks when needed";
        if (name.contains("esp")) return "Draws boxes around entities";
        if (name.contains("fullbright")) return "Removes darkness/gamma limit";
        return "Combat/utility module";
    }
}
