package com.example.apexclient;

import com.example.apexclient.config.ConfigManager;
import com.example.apexclient.input.KeyBindManager;
import com.example.apexclient.module.Module;
import com.example.apexclient.module.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class ApexScreen extends Screen {
    private static final int PANEL_WIDTH = 500;
    private static final int PANEL_HEIGHT = 340;
    private static final int SIDEBAR_WIDTH = 130;

    // ===== GLASSMORPHISM COLOR PALETTE =====
    // Overlay behind the panel - dark semi-transparent
    private static final int COLOR_OVERLAY = 0xC4000000;
    // Outer glass glow - subtle red tint
    private static final int COLOR_GLASS_GLOW = 0x26FF334D;
    private static final int COLOR_GLASS_GLOW_INNER = 0x15FF334D;
    // Panel background - semi-transparent dark with slight red tint
    private static final int COLOR_PANEL_BG = 0xF20D0D12;
    private static final int COLOR_PANEL_BORDER = 0x30FF334D;
    // Header - dark red-tinted glass
    private static final int COLOR_HEADER_BG = 0xE81A080D;
    private static final int COLOR_HEADER_LINE = 0xFFFF334D;
    // Sidebar
    private static final int COLOR_SIDEBAR_BG = 0xE0101015;
    private static final int COLOR_SIDEBAR_HOVER = 0xD0181218;
    private static final int COLOR_SIDEBAR_SELECTED = 0xD8251018;
    private static final int COLOR_SIDEBAR_ACCENT = 0x40FF334D;
    // Module rows
    private static final int COLOR_ROW_BG = 0xD814141A;
    private static final int COLOR_ROW_HOVER = 0xE01E1A22;
    private static final int COLOR_ROW_ACCENT = 0x35FF334D;
    // Accent colors
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

    private static final String[] CATEGORIES = {"Combat", "Utility", "Visual", "All"};
    private int selectedCategory = 0;
    private String searchQuery = "";
    private int scrollOffset = 0;

    // Smooth hover animation state
    private int hoverCategory = -1;
    private long[] hoverStartTime = new long[CATEGORIES.length];

    public ApexScreen() {
        super(Text.of("Apex Client"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelX = getPanelX();
        int panelY = getPanelY();

        // Dark overlay background
        context.fill(0, 0, this.width, this.height, COLOR_OVERLAY);

        // === GLASSMORPHISM PANEL ===
        // Outer soft glow (multiple layers for bloom effect)
        drawGlassGlow(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        // Main glass panel with rounded corners
        drawRoundedRect(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 14, COLOR_PANEL_BG);

        // Subtle border glow
        drawBorderGlow(context, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 14, COLOR_PANEL_BORDER);

        // === HEADER ===
        drawHeader(context, panelX, panelY, mouseX, mouseY);

        // === SIDEBAR ===
        drawSidebar(context, panelX, panelY, mouseX, mouseY);

        // === SEARCH BAR ===
        drawSearchBar(context, panelX, panelY, mouseX, mouseY);

        // === MODULE LIST ===
        drawModuleRows(context, panelX, panelY, mouseX, mouseY);

        // === BOTTOM NAVIGATION ===
        drawNavigation(context, panelX, panelY, mouseX, mouseY);
    }

    // ===== GLASSMORPHISM EFFECTS =====

    private void drawGlassGlow(DrawContext context, int x, int y, int w, int h) {
        // Multi-layer soft glow for glassmorphism bloom effect
        for (int i = 6; i >= 1; i--) {
            int alpha = 8 - i;
            int glowColor = (alpha << 24) | 0xFF334D;
            drawRoundedRect(context, x - i, y - i, w + i * 2, h + i * 2, 14 + i, glowColor);
        }
        // Inner subtle glow
        drawRoundedRect(context, x + 1, y + 1, w - 2, h - 2, 13, COLOR_GLASS_GLOW_INNER);
    }

    private void drawBorderGlow(DrawContext context, int x, int y, int w, int h, int radius, int color) {
        // Top edge glow line (accent)
        int lineY = y + radius;
        context.fill(x + radius, lineY, x + w - radius, lineY + 1, color);
        // Left edge subtle glow
        context.fill(x + radius, y + radius, x + radius + 1, y + h - radius, (0x18 << 24) | (color & 0xFFFFFF));
    }

    // ===== HEADER =====

    private void drawHeader(DrawContext context, int panelX, int panelY, int mouseX, int mouseY) {
        // Header background with rounded top corners
        drawRoundedRectTop(context, panelX + 1, panelY + 1, PANEL_WIDTH - 2, 42, 12, COLOR_HEADER_BG);

        // Red accent line under header
        context.fill(panelX + 1, panelY + 43, panelX + PANEL_WIDTH - 1, panelY + 44, COLOR_HEADER_LINE);

        // Logo text with subtle glow
        String logoText = "Apex";
        String subText = "Client";
        String versionText = "v1.0";

        // Draw "Apex" in white
        context.drawText(this.textRenderer, logoText, panelX + 20, panelY + 15, COLOR_TEXT, false);
        // Draw "Client" in soft red
        int logoWidth = this.textRenderer.getWidth(logoText);
        context.drawText(this.textRenderer, subText, panelX + 22 + logoWidth, panelY + 15, COLOR_RED_SOFT, false);

        // Version text (right side)
        int verWidth = this.textRenderer.getWidth(versionText);
        context.drawText(this.textRenderer, versionText, panelX + PANEL_WIDTH - 30 - verWidth, panelY + 16, COLOR_TEXT_MUTED, false);

        // Done button (glass pill)
        int doneX = panelX + PANEL_WIDTH - 82;
        int doneY = panelY + 12;
        boolean doneHover = isInside(mouseX, mouseY, doneX, doneY, 56, 20);

        int doneBg = doneHover ? COLOR_RED_DARK : 0xFF1A0A0E;
        int doneText = doneHover ? 0xFFFFFFFF : COLOR_RED_SOFT;
        drawRoundedRect(context, doneX, doneY, 56, 20, 8, doneBg);
        if (doneHover) {
            // Subtle glow on hover
            drawRoundedRect(context, doneX - 1, doneY - 1, 58, 22, 9, (0x30 << 24) | 0xFF334D);
            drawRoundedRect(context, doneX, doneY, 56, 20, 8, doneBg);
        }
        context.drawText(this.textRenderer, "Done", doneX + 15, doneY + 6, doneText, false);
    }

    // ===== SIDEBAR =====

    private void drawSidebar(DrawContext context, int panelX, int panelY, int mouseX, int mouseY) {
        // Sidebar background
        drawRoundedRectLeft(context, panelX + 1, panelY + 44, SIDEBAR_WIDTH - 1, PANEL_HEIGHT - 45, 10, COLOR_SIDEBAR_BG);

        // Vertical divider line
        context.fill(panelX + SIDEBAR_WIDTH, panelY + 48, panelX + SIDEBAR_WIDTH + 1, panelY + PANEL_HEIGHT - 8, COLOR_SIDEBAR_ACCENT);

        // Category items
        int categoryY = panelY + 56;
        int categoryHeight = 28;
        int catX = panelX + 8;
        int catW = SIDEBAR_WIDTH - 16;

        for (int i = 0; i < CATEGORIES.length; i++) {
            int catY = categoryY + i * (categoryHeight + 4);
            boolean isSelected = i == selectedCategory;
            boolean isHovered = isInside(mouseX, mouseY, catX, catY, catW, categoryHeight);

            // Selection/hover background with smooth transition
            if (isSelected) {
                drawRoundedRect(context, catX, catY, catW, categoryHeight, 7, COLOR_SIDEBAR_SELECTED);
                // Left accent bar
                drawRoundedRect(context, catX, catY, 3, categoryHeight, 2, COLOR_RED);
                // Subtle glow
                drawRoundedRect(context, catX + 1, catY + 1, catW - 2, categoryHeight - 2, 6, (0x15 << 24) | 0xFF334D);
            } else if (isHovered) {
                drawRoundedRect(context, catX, catY, catW, categoryHeight, 7, COLOR_SIDEBAR_HOVER);
            }

            // Category text
            int textColor = isSelected ? COLOR_RED_SOFT : (isHovered ? COLOR_TEXT : COLOR_TEXT_DIM);
            int textX = catX + (isSelected ? 12 : 8);
            context.drawText(this.textRenderer, CATEGORIES[i], textX, catY + 9, textColor, false);

            // Selected indicator dot
            if (isSelected) {
                context.fill(catX + catW - 10, catY + 12, catX + catW - 6, catY + 16, COLOR_RED);
            }
        }
    }

    // ===== SEARCH BAR =====

    private void drawSearchBar(DrawContext context, int panelX, int panelY, int mouseX, int mouseY) {
        int searchX = panelX + SIDEBAR_WIDTH + 18;
        int searchY = panelY + 52;
        int searchW = PANEL_WIDTH - SIDEBAR_WIDTH - 36;
        int searchH = 26;

        boolean isHovered = isInside(mouseX, mouseY, searchX, searchY, searchW, searchH);
        int searchBg = isHovered ? 0xD81E1E28 : COLOR_ROW_BG;

        // Search bar background
        drawRoundedRect(context, searchX, searchY, searchW, searchH, 8, searchBg);

        // Search icon (magnifying glass approximation)
        context.drawText(this.textRenderer, "\u2315", searchX + 8, searchY + 7, COLOR_TEXT_MUTED, false);

        // Search text
        String searchText = searchQuery.isEmpty() ? "Search modules..." : searchQuery;
        int textColor = searchQuery.isEmpty() ? COLOR_TEXT_MUTED : COLOR_TEXT;
        context.drawText(this.textRenderer, searchText, searchX + 22, searchY + 7, textColor, false);

        // Focus glow if hovered
        if (isHovered) {
            drawRoundedRect(context, searchX - 1, searchY - 1, searchW + 2, searchH + 2, 9, (0x20 << 24) | 0xFF334D);
            drawRoundedRect(context, searchX, searchY, searchW, searchH, 8, searchBg);
            context.drawText(this.textRenderer, searchText, searchX + 22, searchY + 7, textColor, false);
        }
    }

    // ===== MODULE ROWS =====

    private void drawModuleRows(DrawContext context, int panelX, int panelY, int mouseX, int mouseY) {
        List<Module> modules = getFilteredModules();

        int contentX = panelX + SIDEBAR_WIDTH + 18;
        int startY = panelY + 88;
        int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - 36;
        int rowW = contentWidth - 38;
        int gearW = 30;
        int rowH = 48;
        int rowSpacing = 6;

        // Calculate max scroll offset
        int totalHeight = modules.size() * (rowH + rowSpacing);
        int availableHeight = PANEL_HEIGHT - 120;
        int maxScrollOffset = Math.max(0, totalHeight - availableHeight);

        // Clamp scroll offset
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int y = startY + i * (rowH + rowSpacing) - scrollOffset;

            // Skip if outside visible area
            if (y + rowH < panelY + 88 || y > panelY + PANEL_HEIGHT - 50) {
                continue;
            }

            boolean enabled = module.isEnabled();
            boolean rowHover = isInside(mouseX, mouseY, contentX, y, rowW, rowH);
            boolean gearHover = isInside(mouseX, mouseY, contentX + rowW + 8, y, gearW, rowH);

            int stateColor = enabled ? COLOR_GREEN : COLOR_RED;
            int stateGlow = enabled ? COLOR_GREEN_GLOW : COLOR_RED_GLOW;

            // Module row background - glass effect
            int rowBg = rowHover ? COLOR_ROW_HOVER : COLOR_ROW_BG;
            drawRoundedRect(context, contentX, y, rowW, rowH, 10, rowBg);

            // Left accent stripe with glow
            drawRoundedRect(context, contentX, y, 3, rowH, 2, stateColor);
            // Glow extension
            context.fill(contentX + 3, y + 4, contentX + 4, y + rowH - 4, stateGlow);

            // Module name
            context.drawText(this.textRenderer, module.getName(), contentX + 14, y + 8, COLOR_TEXT, false);
            // Module description
            context.drawText(this.textRenderer, getModuleDescription(module), contentX + 14, y + 21, COLOR_TEXT_DIM, false);
            // Keybind display
            String bindText = KeyBindManager.getBindName(module);
            context.drawText(this.textRenderer, "\u2318 " + bindText, contentX + 14, y + 32, COLOR_TEXT_MUTED, false);

            // ===== MODERN TOGGLE SWITCH =====
            drawModernToggle(context, contentX + rowW - 64, y + 14, enabled, rowHover);

            // Gear/settings button
            int gearX = contentX + rowW + 8;
            int gearBg = gearHover ? COLOR_RED_DARK : 0xFF1A1216;
            drawRoundedRect(context, gearX, y, gearW, rowH, 10, gearBg);
            if (gearHover) {
                drawRoundedRect(context, gearX - 1, y - 1, gearW + 2, rowH + 2, 11, (0x30 << 24) | 0xFF334D);
                drawRoundedRect(context, gearX, y, gearW, rowH, 10, gearBg);
            }
            // Gear icon
            String gearIcon = "\u2699";
            int gearIconX = gearX + (gearW - this.textRenderer.getWidth(gearIcon)) / 2;
            context.drawText(this.textRenderer, gearIcon, gearIconX, y + 17, gearHover ? COLOR_TEXT : COLOR_RED_SOFT, false);
        }
    }

    /**
     * Draw a modern sleek toggle switch (pill with sliding dot).
     */
    private void drawModernToggle(DrawContext context, int x, int y, boolean enabled, boolean hovered) {
        int pillW = 52;
        int pillH = 22;

        // Outer pill shadow/glow
        if (hovered) {
            int glowColor = enabled ? COLOR_GREEN_GLOW : COLOR_RED_GLOW;
            drawRoundedRect(context, x - 2, y - 2, pillW + 4, pillH + 4, 12, glowColor);
        }

        // Pill background
        int pillBg = enabled ? COLOR_TOGGLE_ON_BG : COLOR_TOGGLE_OFF_BG;
        drawRoundedRect(context, x, y, pillW, pillH, 11, pillBg);

        // Inner highlight line
        int highlightColor = enabled ? 0xFF2D6B3E : 0xFF5D1A24;
        drawRoundedRect(context, x + 1, y + 1, pillW - 2, pillH - 2, 10, highlightColor);

        // Sliding dot
        int dotSize = 14;
        int dotX = enabled ? x + pillW - dotSize - 4 : x + 4;
        int dotY = y + (pillH - dotSize) / 2;

        // Dot glow
        int dotColor = enabled ? COLOR_GREEN : COLOR_RED;
        int dotGlow = enabled ? COLOR_GREEN_GLOW : COLOR_RED_GLOW;
        drawRoundedRect(context, dotX - 1, dotY - 1, dotSize + 2, dotSize + 2, 8, dotGlow);
        drawRoundedRect(context, dotX, dotY, dotSize, dotSize, 7, dotColor);

        // ON/OFF text
        String stateText = enabled ? "ON" : "OFF";
        int textX = enabled ? x + 8 : x + pillW - 22;
        int textColor = enabled ? 0xFFB9FFD0 : 0xFFFFB3BD;
        context.drawText(this.textRenderer, stateText, textX, y + 7, textColor, false);
    }

    // ===== NAVIGATION =====

    private void drawNavigation(DrawContext context, int panelX, int panelY, int mouseX, int mouseY) {
        String[] navItems = {"Home", "Modules", "Settings"};
        int navY = panelY + PANEL_HEIGHT - 32;
        int navItemW = (SIDEBAR_WIDTH - 16) / navItems.length;

        for (int i = 0; i < navItems.length; i++) {
            int navX = panelX + 8 + i * navItemW;
            boolean isHovered = isInside(mouseX, mouseY, navX, navY, navItemW, 26);

            if (isHovered) {
                drawRoundedRect(context, navX, navY, navItemW, 26, 6, COLOR_SIDEBAR_HOVER);
            }

            String text = navItems[i];
            int textW = this.textRenderer.getWidth(text);
            int textX = navX + (navItemW - textW) / 2;
            int textColor = isHovered ? COLOR_RED_SOFT : COLOR_TEXT_MUTED;
            context.drawText(this.textRenderer, text, textX, navY + 9, textColor, false);
        }

        // Bottom subtle line
        context.fill(panelX + SIDEBAR_WIDTH + 18, panelY + PANEL_HEIGHT - 6,
                panelX + PANEL_WIDTH - 18, panelY + PANEL_HEIGHT - 5, COLOR_SIDEBAR_ACCENT);
    }

    // ===== INPUT HANDLING =====

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        int panelX = getPanelX();
        int panelY = getPanelY();

        // Done button
        if (isInside(mouseX, mouseY, panelX + PANEL_WIDTH - 82, panelY + 12, 56, 20)) {
            if (this.client != null) {
                this.client.setScreen(null);
            }
            return true;
        }

        // Category clicks
        int categoryY = panelY + 56;
        int categoryHeight = 28;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int catX = panelX + 8;
            int catY = categoryY + i * (categoryHeight + 4);
            int catW = SIDEBAR_WIDTH - 16;
            if (isInside(mouseX, mouseY, catX, catY, catW, categoryHeight)) {
                selectedCategory = i;
                return true;
            }
        }

        // Search bar click
        int searchX = panelX + SIDEBAR_WIDTH + 18;
        int searchY = panelY + 52;
        int searchW = PANEL_WIDTH - SIDEBAR_WIDTH - 36;
        int searchH = 26;
        if (isInside(mouseX, mouseY, searchX, searchY, searchW, searchH)) {
            return true;
        }

        // Navigation clicks
        String[] navItems = {"Home", "Modules", "Settings"};
        int navY = panelY + PANEL_HEIGHT - 32;
        int navItemW = (SIDEBAR_WIDTH - 16) / navItems.length;
        for (int i = 0; i < navItems.length; i++) {
            int navX = panelX + 8 + i * navItemW;
            if (isInside(mouseX, mouseY, navX, navY, navItemW, 26)) {
                return true;
            }
        }

        // Module row clicks
        List<Module> modules = getFilteredModules();
        int contentX = panelX + SIDEBAR_WIDTH + 18;
        int startY = panelY + 88;
        int contentWidth = PANEL_WIDTH - SIDEBAR_WIDTH - 36;
        int rowW = contentWidth - 38;
        int gearW = 30;
        int rowH = 48;
        int rowSpacing = 6;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            int y = startY + i * (rowH + rowSpacing) - scrollOffset;

            if (y + rowH < panelY + 88 || y > panelY + PANEL_HEIGHT - 20) {
                continue;
            }

            boolean overRow = isInside(mouseX, mouseY, contentX, y, rowW, rowH);
            boolean overGear = isInside(mouseX, mouseY, contentX + rowW + 8, y, gearW, rowH);

            // Left click row = toggle
            if (button == 0 && overRow) {
                module.setEnabled(!module.isEnabled());
                ConfigManager.save();
                return true;
            }

            // Left click gear OR right click row = open config
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scrollOffset -= (int) (vertical * 24);

        List<Module> modules = getFilteredModules();
        int rowH = 48;
        int rowSpacing = 6;
        int totalHeight = modules.size() * (rowH + rowSpacing);
        int availableHeight = PANEL_HEIGHT - 120;
        int maxScrollOffset = Math.max(0, totalHeight - availableHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));

        return true;
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
        // Draw center
        context.fill(x + radius, y, x + width - radius, y + height, color);
        // Draw top and bottom strips
        context.fill(x, y + radius, x + width, y + height - radius, color);
        // Draw corners
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
        // Draw center
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height, color);
        // Draw top corners only
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                    context.fill(x + width - radius + i, y + j, x + width - radius + i + 1, y + j + 1, color);
                }
            }
        }
    }

    private void drawRoundedRectLeft(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        // Draw center
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height - radius, color);
        // Draw left-side corners only
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                    context.fill(x + i, y + height - radius + j, x + i + 1, y + height - radius + j + 1, color);
                }
            }
        }
    }

    private List<Module> getFilteredModules() {
        List<Module> allModules = ModuleManager.getModules();
        List<Module> filtered = new java.util.ArrayList<>();

        for (Module module : allModules) {
            if (selectedCategory < CATEGORIES.length - 1) {
                String category = CATEGORIES[selectedCategory];
                if (!moduleMatchesCategory(module, category)) {
                    continue;
                }
            }

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
        if (name.contains("trigger")) return "Auto-attacks when crosshair on target";
        if (name.contains("shield")) return "Auto-queues follow-up after shield break";
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
