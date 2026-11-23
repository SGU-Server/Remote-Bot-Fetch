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
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.utils.CommandUtils;
import com.sguserver.remotebotfetch.client.utils.MCUtils;
import com.sguserver.remotebotfetch.client.utils.PlayerUtils;

/**
 * "Stash to bot" support. While the stash key is held (space by default), left clicking a slot marks the stack instead
 * of moving it; the marked slots are drawn with a red frame. When the container screen closes the marked stacks are
 * moved into a fake player: the client opens that fake player's inventory and quick-moves the marked player inventory
 * slots into it.
 */
public final class StashMarker {
    private static final String DEFAULT_BOT_NAME = "bot_qwp";
    /** Maximum wait for the bot, the initial inventory contents, or a source slot update. */
    private static final int TIMEOUT_TICKS = 120;
    private static final int OPEN_FALLBACK_TICKS = 10;

    private static final Set<Integer> MARKED = new LinkedHashSet<>();
    private static final Deque<Integer> PENDING = new ArrayDeque<>();

    private static Stage stage = Stage.IDLE;
    private static int timer;
    private static boolean canOpenByCommand;
    private static String bot = DEFAULT_BOT_NAME;
    private static Pattern botInTitle;
    private static AbstractContainerMenu transferMenu;
    private static Slot waitingSlot;
    private static ItemStack beforeMove = ItemStack.EMPTY;
    private static int movedItems;

    private StashMarker() {}

    private enum Stage {
        IDLE, WAIT_BOT, WAIT_SCREEN, TRANSFER
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

    public static void setMarked(int inventorySlot, boolean marked) {
        boolean changed = marked ? MARKED.add(inventorySlot) : MARKED.remove(inventorySlot);
        if (changed) {
            notifyMark(inventorySlot);
        }
    }

    public static void clear() {
        MARKED.clear();
    }

    /** Shows the new mark state in the action bar, so the player gets immediate feedback for the click. */
    private static void notifyMark(int inventorySlot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        if (MARKED.contains(inventorySlot)) {
            message(mc, "已标记 %d 个槽位，关闭界面后送入假人 %s".formatted(MARKED.size(), botName()));
        } else {
            message(mc, "已取消标记，剩余 %d 个槽位".formatted(MARKED.size()));
        }
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

        reset();
        PENDING.addAll(MARKED);
        MARKED.clear();
        bot = botName();
        botInTitle =
            Pattern.compile("(?<![a-zA-Z0-9_])" + Pattern.quote(bot) + "(?![a-zA-Z0-9_])", Pattern.CASE_INSENSITIVE);
        canOpenByCommand = CommandUtils.canUseCommand("player %s inventory".formatted(bot));
        stage = Stage.WAIT_BOT;
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

        if (stage == Stage.TRANSFER) {
            tickTransfer(mc);
            return;
        }

        if (stage == Stage.WAIT_BOT) {
            if (MCUtils.getScreen() != null) {
                reset();
                return;
            }
            Player entity = findBot(mc);
            boolean online = entity != null || mc.player.connection.getPlayerInfo(bot) != null;
            if (online && canOpenByCommand) {
                MCUtils.executeCommand("player %s inventory".formatted(bot));
            } else if (entity != null) {
                PlayerUtils.interactWith(entity, InteractionHand.MAIN_HAND);
            } else {
                if (!online && timer == 0) {
                    MCUtils.executeCommand("player %s rejoin".formatted(bot));
                }
                if (++timer >= TIMEOUT_TICKS) {
                    message(mc, "找不到可交互的假人 %s，暂存已取消".formatted(bot));
                    reset();
                }
                return;
            }
            stage = Stage.WAIT_SCREEN;
            timer = 0;
            return;
        }

        if (MCUtils.getScreen() != null && !isBotInventory(mc)) {
            message(mc, "打开的不是假人 %s 的背包，暂存已取消".formatted(bot));
            reset();
            return;
        }
        if (++timer == OPEN_FALLBACK_TICKS && MCUtils.getScreen() == null) {
            if (canOpenByCommand) {
                Player entity = findBot(mc);
                if (entity != null) {
                    PlayerUtils.interactWith(entity, InteractionHand.MAIN_HAND);
                }
            } else {
                // Some servers accept the inventory command without advertising the subcommand.
                MCUtils.executeCommand("player %s inventory".formatted(bot));
            }
        }
        if (timer >= TIMEOUT_TICKS) {
            message(mc, "无法打开假人 %s 的背包，暂存已取消".formatted(bot));
            reset();
        }
    }

    private static boolean isBotInventory(Minecraft mc) {
        return MCUtils.getScreen() instanceof AbstractContainerScreen<?> screen
            && screen.getMenu() == mc.player.containerMenu && screen.getMenu() != mc.player.inventoryMenu
            && botInTitle.matcher(screen.getTitle().getString()).find();
    }

    /** Called after vanilla applies a complete snapshot, both on open and on resync. */
    public static void onContentsReady(AbstractContainerMenu menu) {
        if (menu == transferMenu && waitingSlot != null) {
            onSlotSync(menu, waitingSlot.index);
            return;
        }
        if (stage != Stage.WAIT_SCREEN) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || menu != mc.player.containerMenu || !Configs.LMS.STASH_TO_BOT.getBooleanValue()
            || !isBotInventory(mc)) {
            return;
        }
        transferMenu = menu;
        stage = Stage.TRANSFER;
        timer = 0;
    }

    private static void tickTransfer(Minecraft mc) {
        if (mc.player.containerMenu != transferMenu || !isBotInventory(mc)) {
            reset();
            return;
        }
        if (waitingSlot != null) {
            if (++timer >= TIMEOUT_TICKS) {
                message(mc, "未收到槽位变动，已停止暂存。已确认转入 %d 件，请查看假人背包。".formatted(movedItems));
                reset();
            }
            return;
        }

        while (!PENDING.isEmpty()) {
            Slot target = findInventorySlot(mc, transferMenu, PENDING.removeFirst());
            if (target != null) {
                waitingSlot = target;
                beforeMove = target.getItem().copy();
                timer = 0;
                PlayerUtils.requestQuickMove(mc.player, transferMenu, target.index);
                return;
            }
        }

        mc.player.closeContainer();
        message(mc, "已向假人 %s 暂存 %d 件物品".formatted(bot, movedItems));
        reset();
    }

    /** Only an authoritative update for the outstanding source slot can advance the transfer. */
    public static void onSlotSync(AbstractContainerMenu menu, int slotIndex) {
        if (menu != transferMenu || waitingSlot == null || waitingSlot.index != slotIndex) {
            return;
        }
        ItemStack after = waitingSlot.getItem();
        if (!after.isEmpty()
            && (!ItemStack.isSameItemSameComponents(beforeMove, after) || after.getCount() > beforeMove.getCount())) {
            message(Minecraft.getInstance(), "物品栏内容已改变，已停止暂存。");
            reset();
            return;
        }
        int transferred = beforeMove.getCount() - after.getCount();
        if (transferred > 0) {
            movedItems += transferred;
            waitingSlot = null;
            beforeMove = ItemStack.EMPTY;
        }
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
            if (PlayerUtils.getName(player).equalsIgnoreCase(bot)) {
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
        PENDING.clear();
        transferMenu = null;
        waitingSlot = null;
        beforeMove = ItemStack.EMPTY;
        movedItems = 0;
    }
}
