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

/**
 * Duck interface implemented by the mixins on the vanilla screens, so that every place which needs the fetch panel of a
 * screen can reach the very same panel instance without keeping a global map of screens around.
 */
public interface RbfPanelHost {
    RbfFetchPanel rbf$getPanel();

    /**
     * Re-positions the screen for the given panel state: the inventory is shifted to the right while the panel is open,
     * exactly like the vanilla recipe book does it.
     */
    void rbf$updatePanelPosition(boolean open);

    /**
     * Moves the vanilla recipe book button to the position it has to have for the current {@code leftPos}. The vanilla
     * code only does this from its own click handler, so shifting the screen without this would leave the button
     * behind.
     */
    void rbf$moveRecipeButton();
}