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

import java.util.List;
import java.util.regex.Pattern;

import com.sguserver.remotebotfetch.client.config.Configs;

/**
 * Per-player filter for the fake player buttons of the bot inventory screen. Both lists are plain comma separated
 * names, and {@code *} matches any run of characters. The blacklist wins when a name appears in both lists, and an
 * empty whitelist allows every name.
 */
public final class BotInventoryFilter {
    private BotInventoryFilter() {}

    public static boolean isAllowed(String botName) {
        String blacklist = Configs.LMS.BOT_INVENTORY_BLACKLIST.getStringValue();
        if (matchesAny(blacklist, botName)) {
            return false;
        }
        String whitelist = Configs.LMS.BOT_INVENTORY_WHITELIST.getStringValue();
        return isBlank(whitelist) || matchesAny(whitelist, botName);
    }

    public static boolean isActive() {
        return !isBlank(Configs.LMS.BOT_INVENTORY_WHITELIST.getStringValue())
            || !isBlank(Configs.LMS.BOT_INVENTORY_BLACKLIST.getStringValue());
    }

    /** Index of the first allowed name, or -1 when the filter hides every name. */
    public static int firstAllowedIndex(List<String> names) {
        for (int i = 0; i < names.size(); i++) {
            if (isAllowed(names.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean matchesAny(String list, String botName) {
        if (isBlank(list)) {
            return false;
        }
        for (String entry : list.split(",")) {
            String pattern = entry.trim();
            if (!pattern.isEmpty() && matches(pattern, botName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String pattern, String botName) {
        if (!pattern.contains("*")) {
            return pattern.equalsIgnoreCase(botName);
        }
        StringBuilder regex = new StringBuilder();
        for (String part : pattern.split("\\*", -1)) {
            if (regex.length() > 0) {
                regex.append(".*");
            }
            regex.append(Pattern.quote(part));
        }
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE).matcher(botName).matches();
    }
}
