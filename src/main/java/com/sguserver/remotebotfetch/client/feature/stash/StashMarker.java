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

import java.util.LinkedHashSet;
import java.util.Set;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.utils.MCUtils;

/**
 * Collects the container slots the player marked while the stash key was held and sends them to the server as a single
 * {@code /stashToBot} command when the container screen is closed.
 */
public final class StashMarker {
    private static final String DEFAULT_BOT_NAME = "bot_qwp";
    private static final Set<Integer> MARKED_SLOTS = new LinkedHashSet<>();

    private StashMarker() {}

    public static void clear() {
        MARKED_SLOTS.clear();
    }

    public static boolean isMarked(int slotIndex) {
        return MARKED_SLOTS.contains(slotIndex);
    }

    public static boolean isEmpty() {
        return MARKED_SLOTS.isEmpty();
    }

    public static void toggle(int slotIndex) {
        if (!MARKED_SLOTS.remove(slotIndex)) {
            MARKED_SLOTS.add(slotIndex);
        }
    }

    public static String botName() {
        String name = Configs.LMS.STASH_BOT_NAME.getStringValue().trim();
        return name.isEmpty() ? DEFAULT_BOT_NAME : name;
    }

    public static void stashAndClear() {
        if (MARKED_SLOTS.isEmpty()) {
            return;
        }

        StringBuilder slots = new StringBuilder();
        for (int slotIndex : MARKED_SLOTS) {
            if (slots.length() > 0) {
                slots.append(' ');
            }
            slots.append(slotIndex);
        }
        MARKED_SLOTS.clear();
        MCUtils.executeCommand("stashToBot %s %s".formatted(botName(), slots));
    }
}
