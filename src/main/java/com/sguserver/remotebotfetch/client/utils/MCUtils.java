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

import java.util.concurrent.CompletableFuture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import com.sguserver.remotebotfetch.client.ClientMod;

public class MCUtils {
    private static final Minecraft MC = Minecraft.getInstance();

    public static Minecraft getMinecraft() {
        return MC;
    }

    public static LocalPlayer getLocalPlayer() {
        return MC.player;
    }

    public static void executeCommand(String command) {
        ClientMod.debugMessage(() -> "Executing command: " + command);
        if (MC.player != null && command != null) {
            MC.player.connection.sendCommand(command.replaceFirst("/", ""));
        }
    }

    public static String getItemID(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static void setScreen(Screen screen) {
        //#if MC>=260200
        MC.setScreenAndShow(screen);
        //#else
        //$$ MC.setScreen(screen);
        //#endif
    }

    public static Screen getScreen() {
        //#if MC>=260200
        return MC.gui.screen();
        //#else
        //$$ return MC.screen;
        //#endif
    }

    public static ClientPacketListener getConnection() {
        return MC.getConnection();
    }

    public static CompletableFuture<Void> executeTask(Runnable task) {
        return MC.submit(task);
    }
}
