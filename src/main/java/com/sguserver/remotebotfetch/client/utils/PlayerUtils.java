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
package com.sguserver.remotebotfetch.client.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.EntityHitResult;

import fi.dy.masa.malilib.util.InventoryUtils;

public class PlayerUtils {
    public static List<ItemStack> getInventory() {
        LocalPlayer player = MCUtils.getLocalPlayer();
        //#if MC>=12105
        return player == null ? List.of() : player.getInventory().getNonEquipmentItems();
        //#else
        //$$ return player == null ? List.of() : player.getInventory().items;
        //#endif
    }

    public static List<Integer> getAllBoxIndex(int minIndex, int maxIndex) {
        return getAllItemIndex(minIndex, maxIndex, PlayerUtils::isShulkerBox);
    }

    private static List<Integer> getAllItemIndex(int minIndex, int maxIndex, Predicate<ItemStack> filter) {
        List<Integer> results = new ArrayList<>();
        Inventory inventory = MCUtils.getLocalPlayer().getInventory();
        for (int i = minIndex; i < maxIndex; i++) {
            if (filter.test(inventory.getItem(i)))
                results.add(i);
        }
        return results;
    }

    private static List<Integer> getNotEmptyBoxIndexes(List<Integer> indexes) {
        Player player = MCUtils.getLocalPlayer();
        List<Integer> results = new ArrayList<>();
        for (int i : indexes) {
            if (getBoxItemStacks(player.getInventory().getItem(i)).stream().anyMatch(stack -> !stack.isEmpty())) {
                results.add(i);
            }
        }
        return results;
    }

    public static List<ItemStack> getBoxItemStacks(ItemStack box) {
        return InventoryUtils.getStoredItems(box, 27);
    }

    public static int checkRemainCount(Item item) {
        LocalPlayer player = MCUtils.getLocalPlayer();
        if (player == null)
            return 0;
        int storedCount = getNotEmptyBoxIndexes(getAllBoxIndex(0, 36)).stream()
            .flatMap(index -> getBoxItemStacks(player.getInventory().getItem(index)).stream())
            .filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
        int inventoryCount =
            getInventory().stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
        return storedCount + inventoryCount;
    }

    public static boolean isSurvivalMode() {
        return isSurvivalMode(MCUtils.getLocalPlayer());
    }

    public static boolean isSurvivalMode(Player player) {
        return player != null && !player.isCreative() && !player.isSpectator();
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    public static void interactWith(Entity entity, InteractionHand hand) {
        LocalPlayer player = MCUtils.getLocalPlayer();
        Minecraft mc = MCUtils.getMinecraft();
        if (player != null) {
            //#if MC>=260100
            mc.gameMode.interact(player, entity, new EntityHitResult(entity), hand);
            //#else
            //$$ mc.gameMode.interact(player, entity, hand);
            //#endif
        }
    }

    /**
     * Shift clicks (quick moves) the given slot of the currently open container menu.
     */
    public static void quickMoveSlot(int containerId, int slotId) {
        LocalPlayer player = MCUtils.getLocalPlayer();
        Minecraft mc = MCUtils.getMinecraft();
        if (player == null || mc.gameMode == null) {
            return;
        }

        //#if MC>=260100
        mc.gameMode.handleContainerInput(containerId, slotId, 0,
            net.minecraft.world.inventory.ContainerInput.QUICK_MOVE, player);
        //#else
        //$$ mc.gameMode.handleInventoryMouseClick(containerId, slotId, 0,
        //$$     net.minecraft.world.inventory.ClickType.QUICK_MOVE, player);
        //#endif
    }

    public static String getName(Player player) {
        //#if MC>=12110
        return player.getGameProfile().name();
        //#else
        //$$ return player.getGameProfile().getName();
        //#endif
    }
}
