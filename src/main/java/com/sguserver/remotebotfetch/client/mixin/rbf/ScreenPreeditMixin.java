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
package com.sguserver.remotebotfetch.client.mixin.rbf;

import net.minecraft.client.gui.screens.Screen;

import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;

/**
 * Screen does not override {@code GuiEventListener#preeditUpdated}, so IME composition events (Chinese / Japanese /
 * Korean input) never reach the fetch panel's search box. Adding the method here routes them into the panel while it is
 * open.
 * <p>
 * {@code net.minecraft.client.input.PreeditEvent} does not exist before 26.1, so the method is only declared there; on
 * older versions this mixin is empty and the search box falls back to {@code charTyped}.
 */
@org.spongepowered.asm.mixin.Mixin(Screen.class)
public abstract class ScreenPreeditMixin {
    //#if MC>=260101
    @SuppressWarnings("override")
    public boolean preeditUpdated(net.minecraft.client.input.PreeditEvent event) {
        if ((Object)this instanceof RbfPanelHost host) {
            RbfFetchPanel panel = host.rbf$getPanel();
            if (panel != null && panel.isOpen() && panel.preeditUpdated(event)) {
                return true;
            }
        }

        return false;
    }
    //#endif
}
