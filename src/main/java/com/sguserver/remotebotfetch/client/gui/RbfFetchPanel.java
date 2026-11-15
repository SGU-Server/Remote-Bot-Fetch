/*
 * Copyright (C) 2025  Remote-Bot-Fetch contributors
 * https://github.com/SGU-Server/Remote-Bot-Fetch

 * Remote Bot Fetch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Remote Bot Fetch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Remote Bot Fetch.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.sguserver.remotebotfetch.client.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;

/**
 * The in-inventory item fetch menu.
 * <p>
 * It looks and behaves like the item picker of the creative inventory (category tabs, search field, scrollbar, 9x5
 * grid), but instead of putting items into the cursor it asks the server side storage for them. The panel is docked to
 * the left of the player inventory, in the place where the vanilla recipe book would be, and it is toggled with the
 * vanilla recipe book button.
 */
public class RbfFetchPanel {
    public static final int IMAGE_WIDTH = 195;
    public static final int IMAGE_HEIGHT = 136;

    public static final int GRID_X = 9;
    public static final int GRID_Y = 18;
    public static final int COLS = 9;
    public static final int ROWS = 5;
    public static final int SLOT_SIZE = 18;

    public static final int SCROLLER_X = 175;
    public static final int SCROLLER_Y = 18;
    public static final int SCROLLER_WIDTH = 12;
    public static final int SCROLLER_HEIGHT = 15;
    public static final int SCROLLER_TRACK = 112;

    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int TABS_PER_ROW = 7;

    private static final int SEARCH_X = 82;
    private static final int SEARCH_Y = 4;
    private static final int SEARCH_WIDTH = 80;
    private static final int SEARCH_HEIGHT = 10;

    private static final int REFRESH_X = 165;
    private static final int REFRESH_Y = 3;
    private static final int REFRESH_WIDTH = 26;
    private static final int REFRESH_HEIGHT = 10;

    private static final int COLOR_TEXT = 0xFFE0E0E0;
    private static final int COLOR_HINT = 0xFF808080;
    private static final int COLOR_FIELD = 0xFF000000;
    private static final int COLOR_BUTTON = 0xFF8B8B8B;
    private static final int COLOR_BUTTON_HOVER = 0xFFA0A0A0;
    private static final int COLOR_BUTTON_BORDER = 0xFF373737;
    private static final int COLOR_BUTTON_TEXT = 0xFFFFFFFF;

    private final Minecraft mc = Minecraft.getInstance();

    /** Vanilla edit box, used so that IME input (e.g. Chinese) works in the search field. */
    private final EditBox searchBox;

    /** Kept across screens so closing and reopening the inventory restores the panel. */
    private static boolean lastOpen;

    private boolean open;
    private boolean dragging;
    private ItemStack heldStack = ItemStack.EMPTY;
    private String query = "";
    private float scrollOffs;
    private int panelX;
    private int panelY;

    private CreativeModeTab selectedTab;
    private List<CreativeModeTab> tabs = List.of();
    private List<ItemStack> items = List.of();

    public RbfFetchPanel() {
        this.searchBox =
            new EditBox(this.mc.font, 0, 0, SEARCH_WIDTH, SEARCH_HEIGHT, Component.translatable("itemGroup.search"));
        this.searchBox.setBordered(false);
        this.searchBox.setMaxLength(50);
        this.searchBox.setTextColor(0xFFFFFFFF);
        this.searchBox.setResponder(value -> {
            this.query = value;
            this.scrollOffs = 0.0F;
            this.rebuildItems();
        });
    }

    public boolean isOpen() {
        return this.open && Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue();
    }

    public void toggle() {
        if (this.isOpen()) {
            this.close();
        } else {
            this.openPanel();
        }
    }

    public void openPanel() {
        this.open = true;
        lastOpen = true;
        this.searchBox.setFocused(false);
        this.dragging = false;
        this.scrollOffs = 0.0F;
        this.refreshTabs();
        this.rebuildItems();
    }

    public void close() {
        this.open = false;
        lastOpen = false;
        this.searchBox.setFocused(false);
        this.dragging = false;
        this.heldStack = ItemStack.EMPTY;
    }

    /**
     * Called when the screen is (re)initialised. The stale per-screen state is cleared, but whether the panel is open
     * is remembered so reopening the inventory shows it again.
     */
    public void reset() {
        this.open = lastOpen && Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue();
        this.searchBox.setFocused(false);
        this.dragging = false;
        this.heldStack = ItemStack.EMPTY;
        this.scrollOffs = 0.0F;
        this.query = "";

        if (this.open) {
            this.items = List.of();
            this.tabs = List.of();
            this.refreshTabs();
            this.rebuildItems();
        }
    }

    /**
     * The creative tabs only have their contents once the vanilla code rebuilt them for the current feature flags, so
     * this has to happen before reading the items.
     */
    public void refreshTabs() {
        LocalPlayer player = this.mc.player;
        if (player == null || player.connection == null) {
            this.tabs = List.of();
            this.selectedTab = null;
            return;
        }

        CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), true,
            player.level().registryAccess());
        this.tabs = CreativeModeTabs.tabs().stream()
            .filter(
                tab -> tab.getType() != CreativeModeTab.Type.INVENTORY && tab.getType() != CreativeModeTab.Type.HOTBAR)
            .toList();

        if (this.selectedTab == null || !this.tabs.contains(this.selectedTab)) {
            this.selectedTab = this.tabs.isEmpty() ? CreativeModeTabs.getDefaultTab() : this.tabs.get(0);
        }
    }

    public void rebuildItems() {
        if (this.selectedTab == null) {
            this.items = List.of();
            return;
        }

        if (this.query.isBlank()) {
            this.items = List.copyOf(this.selectedTab.getDisplayItems());
            return;
        }

        String needle = this.query.toLowerCase(Locale.ROOT);
        Map<Item, ItemStack> matches = new LinkedHashMap<>();
        for (CreativeModeTab tab : this.tabs) {
            for (ItemStack stack : tab.getDisplayItems()) {
                if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle)) {
                    matches.putIfAbsent(stack.getItem(), stack);
                }
            }
        }
        this.items = new ArrayList<>(matches.values());
    }

    private int rowCount() {
        return Math.max(0, (this.items.size() + COLS - 1) / COLS - ROWS);
    }

    private int startRow() {
        return Math.round(Mth.clamp(this.scrollOffs, 0.0F, 1.0F) * this.rowCount());
    }

    private float scrollForRow(int row) {
        int rows = this.rowCount();
        return rows <= 0 ? 0.0F : (float)row / (float)rows;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    public void render(RbfGraphics graphics, int mouseX, int mouseY, int x, int y) {
        this.panelX = x;
        this.panelY = y;

        CreativeModeTab tab = this.selectedTab;
        if (tab == null) {
            return;
        }

        // The creative inventory draws the unselected tabs *before* the window
        // background and the selected tab *after* it, so that the selected tab
        // merges with the window border. Do the same here, otherwise the tabs
        // are painted on top of the border and look misaligned.
        this.renderTabs(graphics, mouseX, mouseY, false);

        graphics.blit(tab.getBackgroundTexture(), x, y, 0.0F, 0.0F, IMAGE_WIDTH, IMAGE_HEIGHT, 256, 256);

        this.renderItems(graphics, mouseX, mouseY);
        this.renderSearch(graphics, mouseX, mouseY);
        this.renderScroller(graphics);
        this.renderRefreshButton(graphics, mouseX, mouseY);

        this.renderTabs(graphics, mouseX, mouseY, true);

        // Like the creative inventory, an entry that was clicked sticks to the mouse
        // cursor until it is fetched by clicking outside of the panel.
        if (!this.heldStack.isEmpty()) {
            int heldX = mouseX - 8;
            int heldY = mouseY - 8;
            graphics.item(this.heldStack, heldX, heldY);
            graphics.itemDecorations(this.mc.font, this.heldStack, heldX, heldY);
        }
    }

    private void renderTabs(RbfGraphics graphics, int mouseX, int mouseY, boolean selectedPass) {
        for (CreativeModeTab tab : this.tabs) {
            // The vanilla tabs carry their own row/column (CreativeModeTab#row/#column), which is
            // what the creative inventory uses. Deriving them from the list index breaks as soon as
            // a tab is hidden (the hotbar tab sits at TOP 5 and is filtered out here), which made
            // the search tab and the tools tab overlap.
            boolean top = tab.row() == CreativeModeTab.Row.TOP;
            int column = tab.column();
            if (column < 0 || column >= TABS_PER_ROW) {
                continue;
            }

            boolean selected = tab == this.selectedTab;
            if (selected != selectedPass) {
                continue;
            }

            int tabX = this.tabX(tab, column);
            int tabY = this.tabY(top);

            String spriteName = "container/creative_inventory/tab_" + (top ? "top_" : "bottom_")
                + (selected ? "selected_" : "unselected_") + (Mth.clamp(column + 1, 1, TABS_PER_ROW));
            graphics.blitSprite(RbfGraphics.id(spriteName), tabX, tabY, TAB_WIDTH, TAB_HEIGHT);

            int iconX = tabX + 13 - 8;
            int iconY = tabY + 16 - 8 + (top ? 1 : -1);
            graphics.item(tab.getIconItem(), iconX, iconY);

            if (this.hovering(tabX, tabY, TAB_WIDTH, TAB_HEIGHT, mouseX, mouseY)) {
                graphics.tooltip(this.mc.font, tab.getDisplayName(), mouseX, mouseY);
            }
        }
    }

    // Same geometry as the creative inventory (CreativeModeInventoryScreen#getTabX/#getTabY
    // plus the offsets used by #extractTabButton).
    private int tabX(CreativeModeTab tab, int column) {
        int offset = tab.isAlignedRight() ? IMAGE_WIDTH - 27 * (7 - column) + 1 : 27 * column;
        return this.panelX + offset;
    }

    private int tabY(boolean top) {
        return top ? this.panelY - 28 : this.panelY + IMAGE_HEIGHT - 4;
    }

    private void renderItems(RbfGraphics graphics, int mouseX, int mouseY) {
        int startRow = this.startRow();
        List<ItemStack> list = this.items;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int index = (startRow + row) * COLS + col;
                if (index < 0 || index >= list.size()) {
                    continue;
                }

                ItemStack stack = list.get(index);
                if (stack.isEmpty()) {
                    continue;
                }

                int slotX = this.slotX(col);
                int slotY = this.slotY(row);
                graphics.item(stack, slotX, slotY);

                if (this.heldStack.isEmpty()
                    && this.hovering(slotX - 1, slotY - 1, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
                    List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(this.mc, stack));
                    lines.addAll(ItemStorageDataManager.get(stack));
                    graphics.tooltip(this.mc.font, lines, stack.getTooltipImage(), mouseX, mouseY);
                }
            }
        }
    }

    private void renderSearch(RbfGraphics graphics, int mouseX, int mouseY) {
        int x = this.panelX + SEARCH_X;
        int y = this.panelY + SEARCH_Y;

        graphics.fill(x, y, x + SEARCH_WIDTH, y + SEARCH_HEIGHT, COLOR_FIELD);
        graphics.outline(x - 1, y - 1, SEARCH_WIDTH + 2, SEARCH_HEIGHT + 2, 0xFFA0A0A0);

        // The edit box draws the text, the blinking cursor, the hint and, most importantly,
        // handles IME composition, so non-latin input works like in the vanilla search field.
        this.searchBox.setX(x);
        this.searchBox.setY(y);
        this.searchBox.setWidth(SEARCH_WIDTH);
        this.searchBox.setHeight(SEARCH_HEIGHT);
        graphics.editBox(this.searchBox, mouseX, mouseY);
    }

    private void renderScroller(RbfGraphics graphics) {
        int x = this.panelX + SCROLLER_X;
        int y = this.panelY + SCROLLER_Y;

        boolean enabled = this.rowCount() > 0;
        Object sprite = RbfGraphics
            .id(enabled ? "container/creative_inventory/scroller" : "container/creative_inventory/scroller_disabled");
        int thumbY = y + (int)((SCROLLER_TRACK - (SCROLLER_HEIGHT + 2)) * Mth.clamp(this.scrollOffs, 0.0F, 1.0F));
        graphics.blitSprite(sprite, x, thumbY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
    }

    private void renderRefreshButton(RbfGraphics graphics, int mouseX, int mouseY) {
        int x = this.panelX + REFRESH_X;
        int y = this.panelY + REFRESH_Y;
        boolean hovered = this.hovering(x, y, REFRESH_WIDTH, REFRESH_HEIGHT, mouseX, mouseY);

        graphics.fill(x, y, x + REFRESH_WIDTH, y + REFRESH_HEIGHT, hovered ? COLOR_BUTTON_HOVER : COLOR_BUTTON);
        graphics.outline(x, y, REFRESH_WIDTH, REFRESH_HEIGHT, COLOR_BUTTON_BORDER);
        graphics.centeredText(this.mc.font, Component.translatable("remote-bot-fetch.gui.refresh"),
            x + REFRESH_WIDTH / 2, y + 2, COLOR_BUTTON_TEXT);
    }

    private int slotX(int col) {
        return this.panelX + GRID_X + col * SLOT_SIZE;
    }

    private int slotY(int row) {
        return this.panelY + GRID_Y + row * SLOT_SIZE;
    }

    private boolean hovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private boolean hovering(double x, double y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.hovering(this.panelX, this.panelY - TAB_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT + TAB_HEIGHT * 2,
            (int)mouseX, (int)mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean shift) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            // A fetch is done by clicking outside of the panel while an entry is held
            // on the cursor, the same way the creative inventory drops items.
            if (!this.heldStack.isEmpty()) {
                this.fetchHeld(button);
                this.searchBox.setFocused(false);
                return true;
            }
            this.searchBox.setFocused(false);
            return false;
        }

        if (!this.hovering(this.panelX, this.panelY, IMAGE_WIDTH, IMAGE_HEIGHT, (int)mouseX, (int)mouseY)) {
            if (this.handleTabClick(mouseX, mouseY)) {
                return true;
            }
            this.searchBox.setFocused(false);
            return false;
        }

        if (this.handleTabClick(mouseX, mouseY)) {
            return true;
        }

        int searchX = this.panelX + SEARCH_X;
        int searchY = this.panelY + SEARCH_Y;
        if (this.hovering(searchX, searchY, SEARCH_WIDTH, SEARCH_HEIGHT, (int)mouseX, (int)mouseY)) {
            this.searchBox.setFocused(true);
            return true;
        }
        this.searchBox.setFocused(false);

        int refreshX = this.panelX + REFRESH_X;
        int refreshY = this.panelY + REFRESH_Y;
        if (this.hovering(refreshX, refreshY, REFRESH_WIDTH, REFRESH_HEIGHT, (int)mouseX, (int)mouseY)) {
            ItemStorageDataManager.reflushCache();
            return true;
        }

        int scrollerX = this.panelX + SCROLLER_X;
        int scrollerY = this.panelY + SCROLLER_Y;
        if (this.hovering(scrollerX, scrollerY, SCROLLER_WIDTH + 2, SCROLLER_TRACK, (int)mouseX, (int)mouseY)) {
            this.dragging = true;
            this.scrollToMouse(mouseY);
            return true;
        }

        ItemStack stack = this.getHoveredStack(mouseX, mouseY);
        if (!stack.isEmpty()) {
            this.pickFromEntry(stack, button, shift);
            return true;
        }

        return true;
    }

    /**
     * Picks entries exactly the way the creative inventory does: the middle button clones a full stack, holding the
     * left button with shift takes a full stack, every other left click adds one and every right click removes one.
     * Clicking another entry while an item sits on the cursor clears the cursor instead of silently switching to the
     * new entry.
     */
    private void pickFromEntry(ItemStack clicked, int button, boolean shift) {
        if (button == 2) {
            if (this.heldStack.isEmpty() && !clicked.isEmpty()) {
                this.heldStack = clicked.copyWithCount(clicked.getMaxStackSize());
            }

            return;
        }

        if (!this.heldStack.isEmpty() && !clicked.isEmpty()
            && ItemStack.isSameItemSameComponents(this.heldStack, clicked)) {
            if (button == 0) {
                if (shift) {
                    this.heldStack.setCount(this.heldStack.getMaxStackSize());
                } else if (this.heldStack.getCount() < this.heldStack.getMaxStackSize()) {
                    this.heldStack.grow(1);
                }
            } else {
                this.heldStack.shrink(1);
                if (this.heldStack.isEmpty()) {
                    this.heldStack = ItemStack.EMPTY;
                }
            }
        } else if (!clicked.isEmpty() && this.heldStack.isEmpty()) {
            int count = shift ? clicked.getMaxStackSize() : clicked.getCount();
            this.heldStack = clicked.copyWithCount(count);
        } else if (button == 0) {
            this.heldStack = ItemStack.EMPTY;
        } else if (!this.heldStack.isEmpty()) {
            this.heldStack.shrink(1);
            if (this.heldStack.isEmpty()) {
                this.heldStack = ItemStack.EMPTY;
            }
        }
    }

    /**
     * Requests the item that is currently held on the cursor, using the very same amounts as the original client
     * implementation (the creative inventory picker): the left button requests one stack per held entry and the right
     * button requests one shulker box (27 stacks) per held entry, with "stack" being the item's own maximum stack size.
     */
    private void fetchHeld(int button) {
        ItemStack held = this.heldStack;
        this.heldStack = ItemStack.EMPTY;
        if (held.isEmpty()) {
            return;
        }

        int maxCount = held.getMaxStackSize();
        int count = button == 1 ? maxCount * 27 : maxCount;
        ItemStorageDataManager.submit(held.getItem(), held.getCount() * count);
    }

    private boolean handleTabClick(double mouseX, double mouseY) {
        for (CreativeModeTab tab : this.tabs) {
            boolean top = tab.row() == CreativeModeTab.Row.TOP;
            int column = tab.column();
            if (column < 0 || column >= TABS_PER_ROW) {
                continue;
            }

            int tabX = this.tabX(tab, column);
            int tabY = this.tabY(top);

            if (this.hovering(tabX, tabY, TAB_WIDTH, TAB_HEIGHT, (int)mouseX, (int)mouseY)) {
                if (tab != this.selectedTab) {
                    this.selectedTab = tab;
                    this.scrollOffs = 0.0F;
                    this.rebuildItems();
                }
                return true;
            }
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (!this.dragging) {
            return false;
        }
        this.scrollToMouse(mouseY);
        return true;
    }

    public boolean mouseReleased() {
        boolean wasDragging = this.dragging;
        this.dragging = false;
        return wasDragging;
    }

    private void scrollToMouse(double mouseY) {
        int rows = this.rowCount();
        if (rows <= 0) {
            this.scrollOffs = 0.0F;
            return;
        }
        double relative =
            (mouseY - (this.panelY + SCROLLER_Y) - SCROLLER_HEIGHT / 2.0) / (SCROLLER_TRACK - SCROLLER_HEIGHT);
        this.scrollOffs = Mth.clamp((float)relative, 0.0F, 1.0F);
    }

    public boolean mouseScrolled(double scrollY) {
        if (this.rowCount() <= 0) {
            return false;
        }

        int step = (int)Math.signum(scrollY);
        int row = Mth.clamp(this.startRow() - step, 0, this.rowCount());
        this.scrollOffs = this.scrollForRow(row);
        return true;
    }

    //#if MC>=12110
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (!this.isOpen()) {
            return false;
        }

        if (event.key() == 256) {
            if (!this.heldStack.isEmpty()) {
                this.heldStack = ItemStack.EMPTY;
                return true;
            }
            this.close();
            return true;
        }

        return this.searchBox.isFocused() && this.searchBox.keyPressed(event);
    }

    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        if (!this.isOpen() || !this.searchBox.isFocused()) {
            return false;
        }

        return this.searchBox.charTyped(event);
    }
    //#else
    //$$ public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    //$$     if (!this.isOpen()) {
    //$$         return false;
    //$$     }
    //$$
    //$$     if (keyCode == 256) {
    //$$         if (!this.heldStack.isEmpty()) {
    //$$             this.heldStack = ItemStack.EMPTY;
    //$$             return true;
    //$$         }
    //$$         this.close();
    //$$         return true;
    //$$     }
    //$$
    //$$     return this.searchBox.isFocused() && this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    //$$ }
    //$$
    //$$ public boolean charTyped(char chr, int modifiers) {
    //$$     if (!this.isOpen() || !this.searchBox.isFocused()) {
    //$$         return false;
    //$$     }
    //$$
    //$$     return this.searchBox.charTyped(chr, modifiers);
    //$$ }
    //#endif

    //#if MC>=260101
    /**
     * Forwards IME composition updates (needed for Chinese/Japanese/Korean input) to the vanilla edit box, which is
     * what makes typing those languages into the search field work.
     */
    public boolean preeditUpdated(net.minecraft.client.input.PreeditEvent event) {
        return this.isOpen() && this.searchBox.isFocused() && this.searchBox.preeditUpdated(event);
    }
    //#endif

    private ItemStack getHoveredStack(double mouseX, double mouseY) {
        int startRow = this.startRow();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int slotX = this.slotX(col);
                int slotY = this.slotY(row);
                if (!this.hovering(slotX - 1, slotY - 1, SLOT_SIZE, SLOT_SIZE, (int)mouseX, (int)mouseY)) {
                    continue;
                }
                int index = (startRow + row) * COLS + col;
                return index >= 0 && index < this.items.size() ? this.items.get(index) : ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }
}