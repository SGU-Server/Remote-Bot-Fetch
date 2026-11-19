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
package com.sguserver.remotebotfetch.client.feature.lms;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.config.options.OpenFakePlayerInvMode;
import com.sguserver.remotebotfetch.client.utils.ChatHelper;
import com.sguserver.remotebotfetch.client.utils.MCUtils;
import com.sguserver.remotebotfetch.client.utils.PlayerUtils;
import com.sguserver.remotebotfetch.client.utils.ThreadUtils;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

public class ItemStorageDataManager {
    private static final Map<String, PlayerInventory> PLAYER_INV = new Object2ReferenceArrayMap<>();
    private static final Object2IntMap<String> FAKE_ITEM_STORAGES = new Object2IntArrayMap<>();
    private static final Object2IntMap<String> ITEM_STORAGES = new Object2IntArrayMap<>();
    private static final Gson LENIENT_GSON = new GsonBuilder().setLenient().create();
    private static final Type PLAYER_TYPE = new TypeToken<List<PlayerItemStorage>>() {}.getType();
    private static final Type ITEM_TYPE = new TypeToken<List<ItemStorage>>() {}.getType();
    private static final Set<String> WAIT_FOR_INV = new ObjectArraySet<>();
    private static final Set<String> WAIT_FOR_KILLING = new ObjectArraySet<>();

    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!Configs.LMS.LMS_FETCH_SUPPORT.getBooleanValue())
                return true;

            var str = message.getString().trim();

            if (str.contains("maxCount:") && str.startsWith("{") && str.endsWith("}")) {
                ChatHelper.clientChatWithSound(ChatHelper.c("r 请求的数量超出配置的最大上限").copy().withStyle(ChatFormatting.RED),
                    SoundEvents.VILLAGER_DEATH);
                return false;
            } else if (str.startsWith("{") && str.endsWith("}") && str.contains("waitSecond:")) {
                ChatHelper.clientChatWithSound(ChatHelper.c("r 假人取货还在冷却中"), SoundEvents.VILLAGER_DEATH);
                return false;
            } else if (str.equals("[]")) {
                ChatHelper.clientChatWithSound(ChatHelper.c("r 全无品: 这个物品暂时没有存货"), SoundEvents.VILLAGER_NO);
                return false;
            } else if (str.contains("id:") && str.contains("count:") && str.startsWith("[{") && str.endsWith("}]")) {
                if (str.contains("name:")) {
                    try {
                        List<PlayerItemStorage> itemStorageList = LENIENT_GSON.fromJson(str, PLAYER_TYPE);

                        if (itemStorageList == null || itemStorageList.isEmpty())
                            return false;

                        for (PlayerItemStorage storage : itemStorageList) {
                            String name = storage.name();

                            if (name == null)
                                continue;

                            MCUtils.executeCommand("player %s spawn".formatted(name));
                            ChatHelper.clientChatWithSound(
                                ChatHelper.c("g 假人: [%s] 取出数量: [%d]".formatted(name, storage.count())),
                                SoundEvents.VILLAGER_YES);

                            WAIT_FOR_INV.add(name);
                            WAIT_FOR_KILLING.add(name);
                        }
                        return false;
                    } catch (Exception e) {
                        ClientMod.debugMessage(() -> e.getCause() + e.getMessage());
                    }
                } else {
                    try {
                        List<ItemStorage> list = LENIENT_GSON.fromJson(str, ITEM_TYPE);
                        ITEM_STORAGES.clear();
                        list.forEach(storage -> {
                            String id = storage.id();
                            int count = storage.count();
                            ClientMod.debugMessage(() -> "Item: %s Count: %d".formatted(id, count));
                            ITEM_STORAGES.put(id, count);
                        });
                        return false;
                    } catch (Exception e) {
                        ClientMod.debugMessage(() -> e.getCause() + e.getMessage());
                    }
                }
            } else if (str.startsWith("[{") && str.endsWith("]") && str.contains("<...>")) {
                ChatHelper.clientChatWithSound(ChatHelper.c(
                    "r 无法通过getStorageData命令查询容器数据 \n 原因: NBT被折叠 \n 请安装Antideath-carpet-addition v1.4.5以上版本并开启 fixNbtFold 规则 \n 或者将LMS 更新至 1.14.1"),
                    SoundEvents.VILLAGER_NO);
                return false;
            }
            return true;
        });
    }

    public static void submit(Item item, int count) {
        if (item == null)
            return;
        if (Configs.LMS.FAKE_PLAYER_INVENTORY_ITEM_CACHE.getBooleanValue()) {
            for (String name : PLAYER_INV.keySet()) {
                PlayerInventory inventory = PLAYER_INV.get(name);
                for (Slot slot : inventory.slots) {
                    if (!canSend(slot.getItem(), item))
                        continue;
                    MCUtils.executeCommand("player %s spawn".formatted(name));
                    WAIT_FOR_INV.add(name);
                    return;
                }
            }
        }

        MCUtils.executeCommand("getItem %s %d nbt".formatted(MCUtils.getItemID(item), count));
    }

    public static void removeAll() {
        PLAYER_INV.clear();
        FAKE_ITEM_STORAGES.clear();
        ChatHelper.actionBar(ChatHelper.c("l 缓存已清空"));
    }

    public static boolean canSend(ItemStack stack, Item item) {
        if (stack.getItem() == item)
            return true;
        if (PlayerUtils.isShulkerBox(stack)) {
            List<ItemStack> stacks = PlayerUtils.getBoxItemStacks(stack);
            for (ItemStack boxStack : stacks) {
                if (boxStack.getItem() == item)
                    return true;
            }
        }
        return false;
    }

    public static void addPlayerInventory(String name, PlayerInventory inventory) {
        PLAYER_INV.put(name, inventory);
        calculateInventory();
    }

    public static void calculateInventory() {
        FAKE_ITEM_STORAGES.clear();
        for (PlayerInventory pi : PLAYER_INV.values()) {
            for (Slot slot : pi.slots) {
                ItemStack stack = slot.getItem();
                String itemID = MCUtils.getItemID(stack.getItem());
                if (PlayerUtils.isShulkerBox(stack)) {
                    for (ItemStack boxStack : PlayerUtils.getBoxItemStacks(stack)) {
                        FAKE_ITEM_STORAGES.merge(MCUtils.getItemID(boxStack.getItem()), boxStack.getCount(),
                            Integer::sum);
                    }
                }
                FAKE_ITEM_STORAGES.merge(itemID, stack.getCount(), Integer::sum);
            }
        }
    }

    public static List<Component> get(ItemStack stack) {
        List<Component> components = new ArrayList<>();

        if (stack.isEmpty())
            return components;

        Item item = stack.getItem();
        int count = getCount(item, true) + getCount(item, false);

        if (ITEM_STORAGES.isEmpty() && FAKE_ITEM_STORAGES.isEmpty()) {
            components.add(ChatHelper.c("r 物品未查询/缓存"));
        } else if (count > 0) {
            int oneBoxCount = stack.getMaxStackSize() * 27;
            if (count < oneBoxCount) {
                components.add(ChatHelper.c("bl 存货: %s".formatted(count)));
            } else {
                components.add(ChatHelper.c("bl 存货: %d (%.2f 潜影盒) ".formatted(count, (float)count / oneBoxCount)));
            }
        } else {
            components.add(ChatHelper.c("br 暂无存货"));
        }

        return components;
    }

    public static int getCount(Item item, boolean fake) {
        String stackId = MCUtils.getItemID(item);
        int count = 0;
        if (fake) {
            count += Math.max(FAKE_ITEM_STORAGES.getInt(stackId), 0);
        } else {
            count += Math.max(ITEM_STORAGES.getInt(stackId), 0);
        }
        return count;
    }

    public static void reflushCache() {
        MCUtils.executeCommand("getStorageData");
    }

    public static void scanMatchedPlayersAndInteract(Minecraft mc) {
        if (!Configs.LMS.AUTO_OPEN_FAKE_PLAYER_INV.getBooleanValue() || mc.level == null)
            return;

        for (AbstractClientPlayer player : mc.level.players()) {
            var name = PlayerUtils.getName(player);

            if (WAIT_FOR_INV.remove(name)) {
                ThreadUtils.runAsync(() -> {
                    try {
                        Thread.sleep(Configs.LMS.AUTO_COOLDOWN.getIntegerValue());
                        MCUtils.executeTask(() -> {
                            if (Configs.LMS.AUTO_OPEN_FAKE_PLAYER_INV_MODE
                                .getOptionListValue() == OpenFakePlayerInvMode.COMMAND) {
                                MCUtils.executeCommand("player %s inventory".formatted(name));
                            } else {
                                PlayerUtils.interactWith(player, InteractionHand.MAIN_HAND);
                            }
                        }).join();
                    } catch (Exception e) {
                        ChatHelper.clientChat(ChatHelper.c(e.getMessage()));
                        ClientMod.LOGGER.error(e.getMessage(), e);
                    }

                    WAIT_FOR_KILLING.add(name);
                });
            }
        }
    }

    public static Set<String> WAIT_FOR_KILLING() {
        return WAIT_FOR_KILLING;
    }

    public record PlayerInventory(ImmutableList<Slot> slots) {
    }

    record PlayerItemStorage(String name, int count, String id) {
    }

    record ItemStorage(int count, String id) {
    }
}
