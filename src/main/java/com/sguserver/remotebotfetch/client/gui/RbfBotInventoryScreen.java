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
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Inventory;

import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.stash.StashMarker;
import com.sguserver.remotebotfetch.client.mixin.rbf.ContainerScreenAccessor;
import com.sguserver.remotebotfetch.client.utils.ChatHelper;
import com.sguserver.remotebotfetch.menu.RbfBotInventoryMenu;

public final class RbfBotInventoryScreen extends InventoryScreen {
    public static final int MIN_FETCH_WIDTH = RbfFetchPanel.IMAGE_WIDTH + 6 + RbfBotInventoryMenu.PANEL_WIDTH + 4;
    private static final Object CHEST_TEXTURE = RbfGraphics.id("textures/gui/container/generic_54.png");
    private static final int BUTTONS_PER_PAGE = RbfBotInventoryMenu.BOTS_PER_PAGE;
    private static final int THREE_PANEL_WIDTH = RbfBotInventoryMenu.WIDTH + RbfFetchPanel.IMAGE_WIDTH + 10;
    /** Space the fake player panel occupies to the right of the vanilla 176px inventory image. */
    public static final int BOT_AREA_WIDTH = RbfBotInventoryMenu.WIDTH - RbfBotInventoryMenu.PANEL_WIDTH;
    private static final int MIN_HEIGHT = 204;
    private static final int CHEST_BODY_HEIGHT = 125;
    private static final int BOT_PANEL_HEIGHT = 184;
    private static final int AUTO_SWITCH_DELAY_TICKS = 4;
    private static final int AUTO_SWITCH_LIMIT = 16;
    private double contentScale = 1.0;

    private final RbfBotInventoryMenu botMenu;
    private final String botName;
    private final List<String> botNames;
    private final List<Button> botButtons = new ArrayList<>();
    /** Row inside the current page each button sends to the server, or -1 when the button is hidden. */
    private final int[] buttonRows = new int[BUTTONS_PER_PAGE];
    private Button previous;
    private Button next;
    private int page;
    private int autoSwitchWait;
    private static int autoSwitchBudget = AUTO_SWITCH_LIMIT;
    private boolean filteredHintShown;
    /** Fake player the panel still has to switch to, or null. */
    private String pendingFocus;

    public RbfBotInventoryScreen(RbfBotInventoryMenu menu, Inventory inventory, Component title) {
        super(inventory.player);
        this.botMenu = menu;
        ((ContainerScreenAccessor)(Object)this).rbf$setMenu(menu);
        this.host().rbf$useInventoryMenu(menu);
        this.host().rbf$setPanel(BotInventoryOpening.takePanel());

        Object[] args = title.getContents() instanceof TranslatableContents translated
            && RbfBotInventoryMenu.TITLE_KEY.equals(translated.getKey()) ? translated.getArgs() : new Object[0];
        this.botName = args.length == 0 ? title.getString() : text(args[0]);
        List<String> names = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            names.add(text(args[i]));
        }
        this.botNames = List.copyOf(names);
        this.page = Math.max(0, names.indexOf(this.botName)) / BUTTONS_PER_PAGE;
        ClientMod.LOGGER.info("[RBF] bot inventory screen created for {} ({} choices)", this.botName,
            this.botNames.size());
    }

    private static String text(Object value) {
        return value instanceof Component component ? component.getString() : String.valueOf(value);
    }

    @Override
    protected void init() {
        var window = Minecraft.getInstance().getWindow();
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        int requiredWidth =
            Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue() ? THREE_PANEL_WIDTH : RbfBotInventoryMenu.WIDTH + 4;
        this.contentScale = fittingScale(width, height, requiredWidth);
        this.width = (int)Math.round(width / this.contentScale);
        this.height = (int)Math.round(height / this.contentScale);
        this.botButtons.clear();
        this.previous = null;
        this.next = null;
        super.init();

        for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
            int buttonIndex = i;
            this.buttonRows[i] = -1;
            this.botButtons.add(this.addRenderableWidget(
                Button.builder(Component.empty(), button -> this.selectBot(this.buttonRows[buttonIndex]))
                    .bounds(0, 0, 80, 17).build()));
        }
        this.previous = this.addRenderableWidget(
            Button.builder(Component.literal("‹"), button -> this.changePage(-1)).bounds(0, 0, 18, 17).build());
        this.next = this.addRenderableWidget(
            Button.builder(Component.literal("›"), button -> this.changePage(1)).bounds(0, 0, 18, 17).build());
        this.updatePanelLayout(this.host().rbf$getPanel().isOpen());
    }

    static double fittingScale(int width, int height, int requiredWidth) {
        return Math.min(1.0,
            Math.min((double)Math.max(1, width) / requiredWidth, (double)Math.max(1, height) / MIN_HEIGHT));
    }

    public double contentScale() {
        return this.contentScale;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.updatePanelLayout(this.host().rbf$getPanel().isOpen());
        this.tickAutoSwitch();
    }

    @Override
    public void onClose() {
        BotInventoryOpening.cancelSwitch();
        super.onClose();
    }

    //#if MC>=12110
    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(this.logicalEvent(event), doubleClick);
        this.clearBotButtonMouseFocus();
        return handled;
    }

    private net.minecraft.client.input.MouseButtonEvent
        logicalEvent(net.minecraft.client.input.MouseButtonEvent event) {
        return new net.minecraft.client.input.MouseButtonEvent(event.x() / this.contentScale,
            event.y() / this.contentScale, event.buttonInfo());
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy) {
        return super.mouseDragged(this.logicalEvent(event), dx / this.contentScale, dy / this.contentScale);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        return super.mouseReleased(this.logicalEvent(event));
    }
    //#else
    //$$ @Override
    //$$ public boolean mouseClicked(double x, double y, int button) {
    //$$     boolean handled = super.mouseClicked(x / this.contentScale, y / this.contentScale, button);
    //$$     this.clearBotButtonMouseFocus();
    //$$     return handled;
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
    //$$     return super.mouseDragged(x / this.contentScale, y / this.contentScale, button, dx / this.contentScale, dy / this.contentScale);
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseReleased(double x, double y, int button) {
    //$$     return super.mouseReleased(x / this.contentScale, y / this.contentScale, button);
    //$$ }
    //#endif

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x / this.contentScale, y / this.contentScale);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        return super.mouseScrolled(x / this.contentScale, y / this.contentScale, dx, dy);
    }

    private void clearBotButtonMouseFocus() {
        var focused = this.getFocused();
        if (focused != null
            && (focused == this.previous || focused == this.next || this.botButtons.contains(focused))) {
            this.setFocused(null);
            this.setDragging(false);
        }
    }

    // The fake player panel is drawn beside the vanilla 176px image, so vanilla would treat clicks on its
    // slots as clicks outside the menu and throw the hovered stack on the ground. Keep the whole panel,
    // including the buttons below it, inside the menu.
    @Override
    //#if MC>=12110
    protected boolean hasClickedOutside(double x, double y, int left, int top) {
        return !this.overBotPanel(x, y) && super.hasClickedOutside(x, y, left, top);
    }
    //#else
    //$$ protected boolean hasClickedOutside(double x, double y, int left, int top, int button) {
    //$$     return !this.overBotPanel(x, y) && super.hasClickedOutside(x, y, left, top, button);
    //$$ }
    //#endif

    private boolean overBotPanel(double x, double y) {
        if (!this.botMenu.isBotVisible()) {
            return false;
        }
        int start = this.leftPos + RbfBotInventoryMenu.BOT_X;
        return x >= start && x < start + RbfBotInventoryMenu.PANEL_WIDTH && y >= this.topPos
            && y < this.topPos + BOT_PANEL_HEIGHT;
    }

    public void updatePanelLayout(boolean fetchOpen) {
        int leftPanelWidth = fetchOpen ? RbfFetchPanel.IMAGE_WIDTH + 6 : 0;
        boolean vanillaRecipeOpen = this.host().rbf$isVanillaRecipeOpen();
        boolean showBot = !vanillaRecipeOpen;
        // Keep the vanilla image width so other mods that anchor their widgets to imageWidth stay on the
        // player inventory. The fake player panel is drawn beside that image and only reserves space here.
        this.setPanelWidth(RbfBotInventoryMenu.PANEL_WIDTH);
        if (this.botMenu.isBotVisible() != showBot) {
            this.botMenu.setBotVisible(showBot);
            BotInventoryOpening.setVisible(this.botMenu, showBot);
        }
        if (vanillaRecipeOpen) {
            this.leftPos = this.host().rbf$vanillaLeftPos(this.imageWidth);
        } else {
            int botWidth = showBot ? BOT_AREA_WIDTH : 0;
            this.leftPos = (this.width - this.imageWidth - botWidth - leftPanelWidth) / 2 + leftPanelWidth;
        }
        this.host().rbf$moveRecipeButton();
        this.updateButtons();
    }

    private void changePage(int amount) {
        BotInventoryOpening.requestPage(this.botMenu, amount);
        this.page += amount;
        this.updateButtons();
    }

    private void selectBot(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= BUTTONS_PER_PAGE) {
            return;
        }
        int index = this.page * BUTTONS_PER_PAGE + rowIndex;
        if (index < this.botNames.size() && this.botMenu.getCarried().isEmpty() && StashMarker.isEmpty()) {
            BotInventoryOpening.selectBot(this.botMenu, rowIndex);
        }
    }

    /** True when this screen was opened with the given fake player among its choices. */
    public boolean containsBot(String name) {
        return this.botNames.contains(name);
    }

    /** Switches the panel to the given fake player as soon as paging allows it. */
    public void focusBot(String name) {
        this.pendingFocus = name;
    }

    /**
     * Advances a pending switch. Paging is sent one request per tick because the server keeps its own page index, so
     * the bot is only selected once the reached page matches the one that holds it.
     */
    public void tickFocus() {
        if (this.pendingFocus == null) {
            return;
        }
        String name = this.pendingFocus;
        int index = this.botNames.indexOf(name);
        if (index < 0) {
            this.pendingFocus = null;
            return;
        }
        int targetPage = index / BUTTONS_PER_PAGE;
        if (this.page != targetPage) {
            this.changePage(Integer.signum(targetPage - this.page));
            return;
        }
        this.pendingFocus = null;
        if (this.botMenu.getCarried().isEmpty() && StashMarker.isEmpty()) {
            BotInventoryOpening.selectBot(this.botMenu, index % BUTTONS_PER_PAGE);
        }
    }

    /**
     * The server always opens the first free fake player, so a player that only allows a few names would otherwise have
     * to switch away from a hidden one by hand. Walk to the first allowed name and select it once per screen.
     */
    private void tickAutoSwitch() {
        if (!BotInventoryFilter.isActive() || this.botNames.isEmpty() || !this.botMenu.isBotVisible()) {
            autoSwitchBudget = AUTO_SWITCH_LIMIT;
            return;
        }
        if (BotInventoryFilter.isAllowed(this.botName)) {
            autoSwitchBudget = AUTO_SWITCH_LIMIT;
            this.autoSwitchWait = 0;
            return;
        }
        int target = BotInventoryFilter.firstAllowedIndex(this.botNames);
        if (target < 0 || autoSwitchBudget <= 0) {
            return;
        }
        if (this.autoSwitchWait > 0) {
            this.autoSwitchWait--;
            return;
        }
        int targetPage = target / BUTTONS_PER_PAGE;
        if (this.page != targetPage) {
            this.changePage(Integer.signum(targetPage - this.page));
        } else {
            this.selectBot(target % BUTTONS_PER_PAGE);
        }
        autoSwitchBudget--;
        this.autoSwitchWait = AUTO_SWITCH_DELAY_TICKS;
    }

    private void showFilteredHint() {
        if (this.filteredHintShown || !BotInventoryFilter.isActive() || this.botNames.isEmpty()
            || !this.botMenu.isBotVisible() || BotInventoryFilter.firstAllowedIndex(this.botNames) >= 0) {
            return;
        }
        this.filteredHintShown = true;
        ClientMod.LOGGER.info("[RBF] every fake player is hidden by the bot inventory lists: {}", this.botNames);
        ChatHelper.actionBar(Component.translatable("remote-bot-fetch.gui.botInventoryFiltered"));
    }

    private void updateButtons() {
        int x = this.leftPos + RbfBotInventoryMenu.BOT_X;
        boolean visible = this.botMenu.isBotVisible();
        for (int i = 0; i < this.botButtons.size(); i++) {
            this.buttonRows[i] = -1;
            this.botButtons.get(i).visible = false;
        }
        int shown = 0;
        for (int row = 0; row < BUTTONS_PER_PAGE && shown < this.botButtons.size(); row++) {
            int index = this.page * BUTTONS_PER_PAGE + row;
            if (index >= this.botNames.size()) {
                break;
            }
            String name = this.botNames.get(index);
            if (!BotInventoryFilter.isAllowed(name)) {
                continue;
            }
            Button button = this.botButtons.get(shown);
            this.buttonRows[shown] = row;
            button.setPosition(x + 6 + (shown % 2) * 84, this.topPos + 148 + (shown / 2) * 18);
            button.visible = visible;
            Component label = Component.literal(name);
            if (!button.getMessage().getString().equals(name)) {
                button.setMessage(label);
                button.setTooltip(Tooltip.create(label));
            }
            button.active = !name.equals(this.botName) && this.botMenu.getCarried().isEmpty() && StashMarker.isEmpty();
            shown++;
        }
        if (this.previous != null && this.next != null) {
            boolean paging = visible && this.botNames.size() > BUTTONS_PER_PAGE;
            this.previous.setPosition(x + 6, this.topPos + 128);
            this.next.setPosition(x + 152, this.topPos + 128);
            this.previous.visible = paging;
            this.next.visible = paging;
            this.previous.active = this.page > 0;
            this.next.active = (this.page + 1) * BUTTONS_PER_PAGE < this.botNames.size();
        }
        this.showFilteredHint();
    }

    //#if MC>=260101
    @Override
    public void extractBackground(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, float partial) {
        super.extractBackground(graphics, x, y, partial);
        this.drawBotInventory(new RbfGraphics(graphics));
    }
    //#else
    //$$ @Override
    //$$ protected void renderBg(net.minecraft.client.gui.GuiGraphics graphics, float partial, int x, int y) {
    //$$     super.renderBg(graphics, partial, x, y);
    //$$     this.drawBotInventory(new RbfGraphics(graphics));
    //$$ }
    //#endif

    private void drawBotInventory(RbfGraphics graphics) {
        if (!this.botMenu.isBotVisible()) {
            return;
        }
        int x = this.leftPos + RbfBotInventoryMenu.BOT_X;
        int width = RbfBotInventoryMenu.PANEL_WIDTH;
        graphics.blit(CHEST_TEXTURE, x, this.topPos, 0, 0, width, CHEST_BODY_HEIGHT, 256, 256);
        graphics.blit(CHEST_TEXTURE, x, this.topPos + CHEST_BODY_HEIGHT, 0, 216, width, 6, 256, 256);
        graphics.label(this.font, Component.translatable("remote-bot-fetch.gui.botInventory"), x + 8, this.topPos + 6);
        graphics.centeredText(this.font, Component.literal(this.botName), x + width / 2, this.topPos + 134, 0xFFFFFFFF);
    }

    private RbfPanelHost host() {
        return (RbfPanelHost)(Object)this;
    }

    private void setPanelWidth(int width) {
        ((ContainerScreenAccessor)(Object)this).rbf$setImageWidth(width);
    }
}
