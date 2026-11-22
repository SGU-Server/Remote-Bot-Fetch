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
package com.sguserver.remotebotfetch.client.feature.stash;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.utils.MCUtils;
import com.sguserver.remotebotfetch.client.utils.PlayerUtils;

/**
 * "Stash to bot" support. While the stash key is held (alt), clicking a slot marks the stack instead of moving it; the
 * marked slots are drawn with a red frame. When the container screen closes the marked stacks are moved into a fake
 * player: the client opens that fake player's inventory and quick-moves the marked player inventory slots into it, so
 * no server side command or server side code is involved.
 */
public final class StashMarker {
    private static final String DEFAULT_BOT_NAME = "bot_qwp";
    /** Ticks to wait for the fake player to appear, or for its inventory screen to open, before giving up. */
    private static final int TIMEOUT_TICKS = 120;

    private static final Set<Integer> MARKED = new LinkedHashSet<>();
    private static final Deque<Integer> PENDING = new ArrayDeque<>();

    private static Stage stage = Stage.IDLE;
    private static int timer;
    private static boolean askedForInventory;
    private static String bot = DEFAULT_BOT_NAME;

    private StashMarker() {}

    private enum Stage {
        IDLE, WAIT_CLOSE, WAIT_BOT, WAIT_SCREEN, MOVE
    }

    public static boolean isEmpty() {
        return MARKED.isEmpty();
    }

    public static boolean isMarked(int inventorySlot) {
        return MARKED.contains(inventorySlot);
    }

    public static int count() {
        return MARKED.size();
    }

    public static void toggle(int inventorySlot) {
        if (!MARKED.remove(inventorySlot)) {
            MARKED.add(inventorySlot);
        }
    }

    public static void clear() {
        MARKED.clear();
    }

    public static String botName() {
        String name = Configs.LMS.STASH_BOT_NAME.getStringValue().trim();
        return name.isEmpty() ? DEFAULT_BOT_NAME : name;
    }

    /** Called when the container screen is closed: remembers the marks and starts moving them on the next ticks. */
    public static void requestTransfer() {
        if (MARKED.isEmpty() || !Configs.LMS.STASH_TO_BOT.getBooleanValue()) {
            MARKED.clear();
            return;
        }

        PENDING.clear();
        PENDING.addAll(MARKED);
        MARKED.clear();
        bot = botName();
        askedForInventory = false;
        stage = Stage.WAIT_CLOSE;
        timer = 0;
    }

    /** Client tick hook that drives the transfer. */
    public static void tick(Minecraft mc) {
        if (stage == Stage.IDLE) {
            return;
        }

        if (mc.player == null || mc.level == null || !Configs.LMS.STASH_TO_BOT.getBooleanValue()) {
            reset();
            return;
        }

        if (stage == Stage.WAIT_CLOSE) {
            if (MCUtils.getScreen() == null) {
                stage = Stage.WAIT_BOT;
                timer = 0;
            }
            return;
        }

        if (stage == Stage.WAIT_BOT) {
            Player entity = findBot(mc);
            if (entity != null) {
                PlayerUtils.interactWith(entity, InteractionHand.MAIN_HAND);
                stage = Stage.WAIT_SCREEN;
                timer = 0;
                return;
            }

            if (timer == 0) {
                MCUtils.executeCommand("player %s rejoin".formatted(bot));
            }
            if (++timer > TIMEOUT_TICKS) {
                message(mc, "假人 %s 不在线，暂存已取消".formatted(bot));
                reset();
            }
            return;
        }

        if (stage == Stage.WAIT_SCREEN) {
            AbstractContainerMenu menu = mc.player.containerMenu;
            if (MCUtils.getScreen() instanceof AbstractContainerScreen<?> && menu != mc.player.inventoryMenu) {
                stage = Stage.MOVE;
                timer = 0;
                return;
            }

            if (++timer > TIMEOUT_TICKS && !askedForInventory) {
                askedForInventory = true;
                timer = 0;
                MCUtils.executeCommand("player %s inventory".formatted(bot));
                return;
            }
            if (timer > TIMEOUT_TICKS) {
                message(mc, "无法打开假人 %s 的背包，暂存已取消".formatted(bot));
                reset();
            }
            return;
        }

        moveOne(mc);
    }

    private static void moveOne(Minecraft mc) {
        AbstractContainerMenu menu = mc.player.containerMenu;
        while (!PENDING.isEmpty()) {
            int inventorySlot = PENDING.poll();
            Slot target = findInventorySlot(mc, menu, inventorySlot);
            if (target == null) {
                continue;
            }

            PlayerUtils.quickMoveSlot(menu.containerId, target.index);
            return;
        }

        mc.player.closeContainer();
        message(mc, "已把物品暂存到假人 %s".formatted(bot));
        reset();
    }

    private static Slot findInventorySlot(Minecraft mc, AbstractContainerMenu menu, int inventorySlot) {
        for (Slot slot : menu.slots) {
            if (slot.container == mc.player.getInventory() && slot.getContainerSlot() == inventorySlot
                && slot.hasItem()) {
                return slot;
            }
        }
        return null;
    }

    private static Player findBot(Minecraft mc) {
        for (Player player : mc.level.players()) {
            if (player.getName().getString().equals(bot)) {
                return player;
            }
        }
        return null;
    }

    private static void message(Minecraft mc, String text) {
        if (mc.player != null) {
            //#if MC>=260100
            mc.player.sendOverlayMessage(Component.literal(text));
            //#else
            //$$ mc.player.displayClientMessage(Component.literal(text), true);
            //#endif
        }
    }

    private static void reset() {
        stage = Stage.IDLE;
        timer = 0;
        askedForInventory = false;
        PENDING.clear();
    }
}
