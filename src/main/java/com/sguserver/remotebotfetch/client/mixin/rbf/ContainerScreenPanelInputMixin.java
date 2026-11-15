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

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;

/**
 * Mouse wheel, mouse release and (on the versions that do not carry those callbacks in the recipe book screen) mouse
 * drag handling for the fetch panel. Those methods are only declared by {@link AbstractContainerScreen}, not by the
 * recipe book screen, so they are hooked here and guarded to the player inventory.
 */
@org.spongepowered.asm.mixin.Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenPanelInputMixin {
    //#if MC>=12103
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

    //#if MC>=12110
    @org.spongepowered.asm.mixin.injection.Inject(method = "mouseReleased",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$mouseReleased(net.minecraft.client.input.MouseButtonEvent event,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!((Object)this instanceof RbfPanelHost host)) {
            return;
        }

        if (host.rbf$getPanel().mouseReleased()) {
            cir.setReturnValue(true);
        }
    }
    //#else
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "mouseReleased", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$mouseReleased(double mouseX, double mouseY, int button, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
    //$$     if (!((Object) this instanceof RbfPanelHost host)) {
    //$$         return;
    //$$     }
    //$$
    //$$     if (host.rbf$getPanel().mouseReleased()) {
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //$$
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "mouseDragged", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
    //$$     if (!((Object) this instanceof InventoryScreen) || !((Object) this instanceof RbfPanelHost host)) {
    //$$         return;
    //$$     }
    //$$
    //$$     RbfFetchPanel panel = host.rbf$getPanel();
    //$$     if (panel.isOpen() && panel.mouseDragged(mouseX, mouseY)) {
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //#endif
}