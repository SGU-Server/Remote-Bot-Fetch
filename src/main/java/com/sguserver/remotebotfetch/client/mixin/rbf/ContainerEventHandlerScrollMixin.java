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

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;

/**
 * Mouse wheel handling for the fetch panel on 1.21.1, where {@code AbstractContainerScreen} does not declare
 * {@code mouseScrolled} and the scrolling runs through the {@link ContainerEventHandler} default implementation
 * instead. 1.21.3 and newer hook {@link ContainerScreenPanelInputMixin} directly, so the injector is only declared
 * there.
 */
@org.spongepowered.asm.mixin.Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerScrollMixin {
    //#if MC<12103
    @org.spongepowered.asm.mixin.injection.Inject(method = "mouseScrolled",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!((Object)this instanceof InventoryScreen) || !((Object)this instanceof RbfPanelHost host)) {
            return;
        }

        RbfFetchPanel panel = host.rbf$getPanel();
        if (panel.isOpen() && panel.isMouseOver(mouseX, mouseY) && panel.mouseScrolled(scrollY)) {
            cir.setReturnValue(true);
        }
    }
    //#endif
}
