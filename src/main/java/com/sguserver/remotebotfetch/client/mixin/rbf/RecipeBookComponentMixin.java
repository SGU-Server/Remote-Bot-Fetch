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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.Slot;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;

/**
 * Keeps the vanilla recipe book closed while the fetch panel mode is enabled, so the recipe book button can be used as
 * the panel switch without the recipe book animation fighting the panel for the same screen space. The recipe book is
 * suppressed on every render pass as well, because the book re-opens itself as soon as a new recipe is unlocked.
 */
@org.spongepowered.asm.mixin.Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
    @org.spongepowered.asm.mixin.injection.Inject(method = "toggleVisibility",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$toggleVisibility(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.rbf$isSuppressed()) {
            ci.cancel();
            //#if MC<12102
            //$$ // 1.21.1 declares no recipe book button callback on the inventory screen,
            //$$ // so the panel switch rides on the vanilla visibility toggle instead.
            //$$ net.minecraft.client.gui.screens.Screen current = com.sguserver.remotebotfetch.client.utils.MCUtils.getScreen();
            //$$ if (current instanceof RbfPanelHost host) {
            //$$     com.sguserver.remotebotfetch.client.gui.RbfFetchPanel panel = host.rbf$getPanel();
            //$$     panel.toggle();
            //$$     host.rbf$updatePanelPosition(panel.isOpen());
            //$$ }
            //#endif
        }
    }

    //#if MC>=260101
    @org.spongepowered.asm.mixin.injection.Inject(method = "extractRenderState",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$hideBook(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY,
        float partial, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.rbf$isSuppressed()) {
            ci.cancel();
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "extractGhostRecipe",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$hideGhostRecipe(net.minecraft.client.gui.GuiGraphicsExtractor graphics, boolean resultSlot,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.rbf$isSuppressed()) {
            ci.cancel();
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "extractTooltip",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$hideTooltip(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY,
        Slot slot, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.rbf$isSuppressed()) {
            ci.cancel();
        }
    }
    //#else
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "render", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$hideBook(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partial, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //#if MC>=12103
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "renderGhostRecipe", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$hideGhostRecipe(net.minecraft.client.gui.GuiGraphics graphics, boolean resultSlot, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "renderTooltip", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$hideTooltip(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, Slot slot, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //#else
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "renderGhostRecipe", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$hideGhostRecipe(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, boolean resultSlot,
    //$$                                 float partial, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "renderTooltip", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$hideTooltip(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, int left, int top, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    //$$     if (this.rbf$isSuppressed()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //#endif
    //#endif

    @org.spongepowered.asm.mixin.Unique
    private boolean rbf$isSuppressed() {
        if (!Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue()) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        // Minecraft.screen moved to Minecraft.gui in 26.2; MCUtils.getScreen() already handles both.
        net.minecraft.client.gui.screens.Screen current = com.sguserver.remotebotfetch.client.utils.MCUtils.getScreen();
        return current instanceof InventoryScreen screen && screen instanceof RbfPanelHost;
    }
}