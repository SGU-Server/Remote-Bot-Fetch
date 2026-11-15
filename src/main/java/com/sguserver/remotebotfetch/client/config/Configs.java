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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.annotations.Config;
import com.sguserver.remotebotfetch.client.config.options.OpenFakePlayerInvMode;

import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigStringList;

public class Configs implements IConfigHandler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Configs INSTANCE = new Configs();
    public static final String ALL = ClientMod.MOD_ID + ".config.all";
    public static final Class<?>[] classes = new Class[] {LMS.class, Configs.class};

    @Config
    public static final ConfigHotkey OPEN_CONFIG = new ConfigHotkey("openGui", "Z,K", "").apply(ALL);

    public static class LMS {
        @Config(tab = Tab.LMS)
        public static final ConfigHotkey LMS_TAKE_ITEM = new ConfigHotkey("openFakeFetchGui", "", "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigBoolean INVENTORY_FETCH_MENU =
            new ConfigBoolean("inventoryFetchMenu", true, "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigBoolean LMS_FETCH_SUPPORT =
            new ConfigBoolean("fakeFetchSupport", false, "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigBoolean LMS_AUTO_REFRESH = new ConfigBoolean("fakeAutoRefresh", false, "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigBooleanHotkeyed MID_CLICK_TAKE_ITEM =
            new ConfigBooleanHotkeyed("middleClickLitematicBlockFetch", false, "", "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigBooleanHotkeyed AUTO_OPEN_FAKE_PLAYER_INV =
            new ConfigBooleanHotkeyed("autoOpenFakeInv", false, "", "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigOptionList AUTO_OPEN_FAKE_PLAYER_INV_MODE =
            new ConfigOptionList("autoOpenFakeInv-Mode", OpenFakePlayerInvMode.INTERACTION, "", "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigInteger AUTO_COOLDOWN =
            new ConfigInteger("autoActionCooldown", 100, 100, 1000, "", "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigBooleanHotkeyed FAKE_PLAYER_INVENTORY_ITEM_CACHE =
            new ConfigBooleanHotkeyed("fakeInvCache", false, "", "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigHotkey CLEAN_PLAYER_INV_CHACHE =
            new ConfigHotkey("fakeInvCache-clear", "", "", "").apply(ALL);
        @Config(tab = Tab.LMS)
        public static final ConfigStringList FAKE_PLAYER_INVENTORY_ITEM_CACHE_WHITE_LIST =
            new ConfigStringList("fakeInvCache-Whitelist", ImmutableList.of(), "").apply(ALL);
    }

    public static boolean lockCreativeScreen = false;
    private static final String FILE_PATH = "./config/" + ClientMod.MOD_ID + ".json";

    @Override
    public void load() {
        File file = new File(FILE_PATH);
        if (file.isFile()) {
            try {
                JsonElement element = JsonParser.parseString(Files.readString(file.toPath()));
                if (element.isJsonObject()) {
                    ConfigUtils.readConfigBase(element.getAsJsonObject(), ClientMod.MOD_ID,
                        ConfigsManager.getConfigs(Tab.ALL));
                }
            } catch (IOException | RuntimeException e) {
                ClientMod.LOGGER.error("Failed to load ASA config", e);
            }
        }
    }

    @Override
    public void save() {
        File dir = new File("./config");
        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            JsonObject root = new JsonObject();
            ConfigUtils.writeConfigBase(root, ClientMod.MOD_ID, ConfigsManager.getConfigs(Tab.ALL));
            try {
                Files.writeString(dir.toPath().resolve("asa.json"), GSON.toJson(root));
            } catch (IOException e) {
                ClientMod.LOGGER.error("Failed to save ASA config", e);
            }
        }
    }
}
