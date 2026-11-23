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
package com.sguserver.remotebotfetch.client;

import java.util.function.Supplier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.config.ConfigsManager;
import com.sguserver.remotebotfetch.client.config.HotkeysCallback;
import com.sguserver.remotebotfetch.client.config.InitHandler;
import com.sguserver.remotebotfetch.client.config.InputHandler;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.feature.stash.StashMarker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;

public class ClientMod implements ClientModInitializer {
    public static final String MOD_ID_FANCY = "Remote Bot Fetch";
    public static final String MOD_ID = "remote-bot-fetch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static String version;

    public static void debugMessage(Supplier<String> message) {
        LOGGER.debug("{}", message.get());
    }

    @Override
    public void onInitializeClient() {
        version = FabricLoader.getInstance().getModContainer("remote-bot-fetch").orElseThrow().getMetadata()
            .getVersion().getFriendlyString();
        LOGGER.info("Remote Bot Fetch v{} is being loaded...", version);
        ConfigsManager.init();
        Configs.INSTANCE.load();
        HotkeysCallback.init();
        ConfigManager.getInstance().registerConfigHandler(MOD_ID, Configs.INSTANCE);
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.INSTANCE);
        InputEventHandler.getInputManager().registerKeyboardInputHandler(InputHandler.INSTANCE);
        InitializationHandler.getInstance().registerInitializationHandler(InitHandler.INSTANCE);
        ItemStorageDataManager.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ItemStorageDataManager.scanMatchedPlayersAndInteract(client);
            StashMarker.tick(client);
            ItemStorageDataManager.tickStockRefresh(client);
        });
    }
}
