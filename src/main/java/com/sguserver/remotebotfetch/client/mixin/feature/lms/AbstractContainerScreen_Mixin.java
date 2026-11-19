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
package com.sguserver.remotebotfetch.client.mixin.feature.lms;

import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableList;
import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.utils.ChatHelper;
import com.sguserver.remotebotfetch.client.utils.MCUtils;
import com.sguserver.remotebotfetch.client.utils.PlayerUtils;
import com.sguserver.remotebotfetch.client.utils.ThreadUtils;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreen_Mixin<T extends AbstractContainerMenu> extends Screen {
    @Shadow
    @Final
    protected T menu;

    protected AbstractContainerScreen_Mixin(Component title) {
        super(title);
    }

    @Shadow
    protected abstract void init();

    @Inject(method = "onClose", at = @At("TAIL"))
    private void onClose(CallbackInfo ci) {
        this.asa$cacheData();
        this.asa$killFakePlayers();
    }

    @Unique
    private void asa$killFakePlayers() {
        if (!Configs.LMS.AUTO_KILL_FAKE_PLAYERS.getBooleanValue())
            return;

        Set<String> fakePlayerNames = ItemStorageDataManager.WAIT_FOR_KILLING();
        String title = this.title.getString();
        for (String name : List.copyOf(fakePlayerNames)) {
            if (!title.contains(name))
                continue;

            ThreadUtils.runAsync(() -> {
                try {
                    Thread.sleep(Configs.LMS.AUTO_COOLDOWN.getIntegerValue());
                    MCUtils.executeTask(() -> MCUtils.executeCommand("player %s kill".formatted(name)));
                } catch (Exception e) {
                    ChatHelper.clientChat(ChatHelper.c(e.getMessage()));
                    ClientMod.LOGGER.error(e.getMessage(), e);
                } finally {
                    fakePlayerNames.remove(name);
                }
            });
        }
    }

    @Unique
    private void asa$cacheData() {
        if (!Configs.LMS.FAKE_PLAYER_INVENTORY_ITEM_CACHE.getBooleanValue() || (Object)this instanceof InventoryScreen
            || !PlayerUtils.isSurvivalMode())
            return;

        String title = this.title.getString();
        List<String> names = Configs.LMS.FAKE_PLAYER_INVENTORY_ITEM_CACHE_WHITE_LIST.getStrings();
        for (String name : names) {
            if (title.startsWith(name)) {
                List<Slot> slots =
                    this.menu.slots.stream().filter(slot -> !(slot.container instanceof Inventory)).toList();
                ItemStorageDataManager.addPlayerInventory(name,
                    new ItemStorageDataManager.PlayerInventory(ImmutableList.copyOf(slots)));
                ChatHelper.actionBar(ChatHelper.c("已缓存 %s 的物品栏".formatted(name)));
                break;
            }
        }
    }
}
