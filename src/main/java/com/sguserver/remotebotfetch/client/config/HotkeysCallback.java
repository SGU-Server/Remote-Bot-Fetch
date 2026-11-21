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
package com.sguserver.remotebotfetch.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;

import com.sguserver.remotebotfetch.client.utils.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.utils.MCUtils;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;

public class HotkeysCallback implements IHotkeyCallback {
    public static void init() {
        HotkeysCallback callback = new HotkeysCallback();
        for (ConfigHotkey hotkey : ConfigsManager.KEY_LIST) {
            hotkey.getKeybind().setCallback(callback);
        }
    }

    @Override
    public boolean onKeyAction(KeyAction action, IKeybind key) {
        if (key == Configs.OPEN_CONFIG.getKeybind()) {
            MCUtils.setScreen(new ConfigUi());
            return true;
        }
        if (key == Configs.LMS.LMS_TAKE_ITEM.getKeybind()) {
            Configs.lockCreativeScreen = true;
            LocalPlayer player = MCUtils.getLocalPlayer();
            Minecraft mc = MCUtils.getMinecraft();
            if (player != null) {
                MCUtils.setScreen(new CreativeModeInventoryScreen(player, player.connection.enabledFeatures(),
                    mc.options.operatorItemsTab().get()));
            }
            return true;
        }
        if (key == Configs.LMS.CLEAN_PLAYER_INV_CHACHE.getKeybind()) {
            ItemStorageDataManager.removeAll();
            return true;
        }
        return false;
    }
}
