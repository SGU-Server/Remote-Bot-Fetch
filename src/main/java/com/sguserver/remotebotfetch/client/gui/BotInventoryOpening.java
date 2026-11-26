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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.stash.StashMarker;
import com.sguserver.remotebotfetch.client.utils.ChatHelper;
import com.sguserver.remotebotfetch.client.utils.MCUtils;
import com.sguserver.remotebotfetch.menu.RbfBotInventoryMenu;

public final class BotInventoryOpening {
    private static final int RESPONSE_TIMEOUT_TICKS = 40;
    private static final int MAX_ATTEMPTS = 3;

    private static Screen requested;
    private static Screen skipLogged;
    private static int attempts;
    private static int waitingTicks;
    private static Object timeoutHintConnection;
    private static Object spawnConnection;
    private static Object switchConnection;
    private static long switchUntil;
    private static RbfFetchPanel switchingPanel;
    private static RbfFetchPanel acceptedPanel;

    private BotInventoryOpening() {}

    public static void tick(Minecraft mc) {
        Screen screen = MCUtils.getScreen();
        if (mc.player == null || mc.gameMode == null) {
            cancel();
            return;
        }
        if (screen instanceof RbfBotInventoryScreen) {
            // The bot menu is already open. Keep any pending switch window alive: the server closes the
            // current container before opening the next one, and that close arrives as a null screen.
            requested = null;
            attempts = 0;
            waitingTicks = 0;
            return;
        }
        if (!(screen instanceof InventoryScreen) && !(screen instanceof CreativeModeInventoryScreen)) {
            cancel();
            return;
        }
        if (!canRequest(mc)) {
            logCannotRequest(mc, screen);
            return;
        }
        skipLogged = null;
        if (requested != screen) {
            requested = screen;
            attempts = 0;
            waitingTicks = 0;
            sendRequest(mc, screen);
            return;
        }
        waitingTicks++;
        if (waitingTicks < RESPONSE_TIMEOUT_TICKS) {
            return;
        }
        waitingTicks = 0;
        if (attempts == MAX_ATTEMPTS - 1 && spawnConfiguredBot(mc)) {
            // Give the bot a moment to join before the last attempt.
            return;
        }
        if (attempts < MAX_ATTEMPTS) {
            sendRequest(mc, screen);
            return;
        }
        if (timeoutHintConnection != MCUtils.getConnection()) {
            timeoutHintConnection = MCUtils.getConnection();
            ClientMod.LOGGER.warn("[RBF] no answer after {} bot inventory requests, giving up", attempts);
            ChatHelper.clientChat(Component.translatable("remote-bot-fetch.gui.botInventoryNoResponse"));
        }
    }

    private static void sendRequest(Minecraft mc, Screen screen) {
        attempts++;
        ClientMod.LOGGER.info("[RBF] bot inventory request #{} (screen={}, containerId={})", attempts,
            screen.getClass().getSimpleName(), mc.player.containerMenu.containerId);
        mc.gameMode.handleInventoryButtonClick(mc.player.containerMenu.containerId, RbfBotInventoryMenu.OPEN_BUTTON);
    }

    private static boolean spawnConfiguredBot(Minecraft mc) {
        if (spawnConnection == MCUtils.getConnection()) {
            return false;
        }
        String name = Configs.LMS.STASH_BOT_NAME.getStringValue();
        if (name == null || name.isBlank()) {
            return false;
        }
        spawnConnection = MCUtils.getConnection();
        ClientMod.LOGGER.info("[RBF] no answer yet, spawning fake player {} before the last attempt", name);
        MCUtils.executeCommand("player " + name + " spawn");
        return true;
    }

    private static void logCannotRequest(Minecraft mc, Screen screen) {
        if (skipLogged == screen) {
            return;
        }
        skipLogged = screen;
        ClientMod.LOGGER.info(
            "[RBF] cannot open bot inventory: config={} stashEmpty={} spectator={} cursorEmpty={} menu={} isInventoryMenu={}",
            Configs.LMS.BOT_INVENTORY_PANEL.getBooleanValue(), StashMarker.isEmpty(), mc.player.isSpectator(),
            mc.player.inventoryMenu.getCarried().isEmpty(), mc.player.containerMenu.getClass().getSimpleName(),
            mc.player.containerMenu == mc.player.inventoryMenu);
    }

    private static boolean canRequest(Minecraft mc) {
        if (!Configs.LMS.BOT_INVENTORY_PANEL.getBooleanValue() || !StashMarker.isEmpty() || mc.player.isSpectator()
            || !mc.player.inventoryMenu.getCarried().isEmpty()) {
            return false;
        }
        // Creative mode keeps the regular inventory menu on the server, so the same button works there.
        return mc.player.containerMenu == mc.player.inventoryMenu;
    }

    private static void cancel() {
        requested = null;
        attempts = 0;
        waitingTicks = 0;
    }

    public static boolean switchInFlight() {
        return switchConnection != null && switchConnection == MCUtils.getConnection()
            && System.nanoTime() - switchUntil < 0;
    }

    public static boolean acceptsScreen() {
        Screen screen = MCUtils.getScreen();
        boolean opening = screen != null && screen == requested;
        boolean switching = switchInFlight()
            && (screen == null || screen instanceof RbfBotInventoryScreen || screen instanceof InventoryScreen);
        // A bot switch closes the old container first, so the screen can legitimately be null here. The plain
        // player inventory is replaced by the bot inventory as well: fetching items from its panel makes the server
        // open the bot inventory right away, and the vanilla screen must not swallow that packet.
        boolean replaceable = screen == null || screen instanceof RbfBotInventoryScreen
            || screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
        boolean accept = (opening || switching || replaceable) && StashMarker.isEmpty();
        if (accept) {
            acceptedPanel = switching ? switchingPanel : panelOf(screen);
            if (opening && Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue() && !acceptedPanel.isOpen()) {
                acceptedPanel.openPanel();
            }
        }
        ClientMod.LOGGER.info("[RBF] bot menu offered: screen={} opening={} switching={} accept={}",
            screen == null ? "null" : screen.getClass().getSimpleName(), opening, switching, accept);
        requested = null;
        cancelSwitch();
        return accept;
    }

    private static RbfFetchPanel panelOf(Screen screen) {
        if (screen instanceof RbfPanelHost host) {
            return host.rbf$getPanel();
        }
        // The creative inventory screen has no fetch panel of its own, so carry a fresh one into the bot inventory.
        return new RbfFetchPanel();
    }

    static RbfFetchPanel takePanel() {
        RbfFetchPanel panel = acceptedPanel;
        acceptedPanel = null;
        return panel;
    }

    static void selectBot(RbfBotInventoryMenu menu, int index) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null && mc.player != null && mc.player.containerMenu == menu) {
            switchConnection = MCUtils.getConnection();
            switchingPanel = ((RbfPanelHost)MCUtils.getScreen()).rbf$getPanel();
            switchUntil = System.nanoTime() + 5_000_000_000L;
            mc.gameMode.handleInventoryButtonClick(menu.containerId, index);
        }
    }

    static void setVisible(RbfBotInventoryMenu menu, boolean visible) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null && mc.player != null && mc.player.containerMenu == menu) {
            mc.gameMode.handleInventoryButtonClick(menu.containerId,
                visible ? RbfBotInventoryMenu.SHOW_BOT_BUTTON : RbfBotInventoryMenu.HIDE_BOT_BUTTON);
        }
    }

    static void requestPage(RbfBotInventoryMenu menu, int amount) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null && mc.player != null && mc.player.containerMenu == menu) {
            mc.gameMode.handleInventoryButtonClick(menu.containerId,
                amount > 0 ? RbfBotInventoryMenu.NEXT_BOT_PAGE : RbfBotInventoryMenu.PREVIOUS_BOT_PAGE);
        }
    }

    static void cancelSwitch() {
        switchConnection = null;
        switchingPanel = null;
    }
}
