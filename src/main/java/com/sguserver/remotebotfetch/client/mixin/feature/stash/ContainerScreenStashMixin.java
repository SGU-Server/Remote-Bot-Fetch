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
package com.sguserver.remotebotfetch.client.mixin.feature.stash;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import com.mojang.blaze3d.platform.InputConstants;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.stash.StashMarker;
import com.sguserver.remotebotfetch.client.gui.RbfGraphics;
import com.sguserver.remotebotfetch.client.mixin.rbf.ContainerScreenAccessor;

import fi.dy.masa.malilib.hotkeys.KeybindMulti;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenStashMixin {
    @Unique
    private static final int RBF_MARK_COLOR = 0xFFFF3B30;
    @Unique
    private boolean rbf$marking;
    @Unique
    private boolean rbf$addMarks;

    @Inject(method = "onClose", at = @At("HEAD"))
    private void rbf$onClose(CallbackInfo ci) {
        StashMarker.requestTransfer();
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void rbf$onRemoved(CallbackInfo ci) {
        this.rbf$marking = false;
        StashMarker.clear();
    }

    //#if MC>=12110
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void rbf$stashClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick,
        CallbackInfoReturnable<Boolean> cir) {
        if (this.rbf$startMarking(event.x(), event.y(), event.button(), event.hasAltDown())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void rbf$stashDragged(net.minecraft.client.input.MouseButtonEvent event, double dx, double dy,
        CallbackInfoReturnable<Boolean> cir) {
        if (this.rbf$continueMarking(event.x(), event.y(), event.button(), event.hasAltDown())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void rbf$stashReleased(net.minecraft.client.input.MouseButtonEvent event,
        CallbackInfoReturnable<Boolean> cir) {
        this.rbf$finishMarking(event.button(), cir);
    }
    //#else
    //$$ @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    //$$ private void rbf$stashClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
    //$$     if (this.rbf$startMarking(x, y, button, net.minecraft.client.gui.screens.Screen.hasAltDown())) {
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    //$$ private void rbf$stashDragged(double x, double y, int button, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
    //$$     if (this.rbf$continueMarking(x, y, button, net.minecraft.client.gui.screens.Screen.hasAltDown())) {
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    //$$ private void rbf$stashReleased(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
    //$$     this.rbf$finishMarking(button, cir);
    //$$ }
    //#endif

    @Unique
    private boolean rbf$startMarking(double x, double y, int button, boolean altDown) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }

        int slot = this.rbf$stashSlotAt(x, y, altDown);
        this.rbf$marking = slot >= 0;
        if (this.rbf$marking) {
            this.rbf$addMarks = !StashMarker.isMarked(slot);
            StashMarker.setMarked(slot, this.rbf$addMarks);
        }
        return this.rbf$marking;
    }

    @Unique
    private boolean rbf$continueMarking(double x, double y, int button, boolean altDown) {
        if (!this.rbf$marking || button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }

        int slot = this.rbf$stashSlotAt(x, y, altDown);
        if (slot >= 0) {
            StashMarker.setMarked(slot, this.rbf$addMarks);
        }
        return true;
    }

    @Unique
    private void rbf$finishMarking(int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.rbf$marking && button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.rbf$marking = false;
            cir.setReturnValue(true);
        }
    }

    @Unique
    private int rbf$stashSlotAt(double x, double y, boolean altDown) {
        Minecraft mc = Minecraft.getInstance();
        if (!Configs.LMS.STASH_TO_BOT.getBooleanValue() || mc.player == null) {
            return -1;
        }

        // Gameplay key mappings stop receiving presses while a container is open.
        if (!altDown && !KeybindMulti.isKeyDown(KeybindMulti.getKeyCode(mc.options.keyJump))) {
            return -1;
        }

        Slot slot = ((ContainerScreenAccessor)(Object)this).rbf$getSlotAt(x, y);
        return slot != null && slot.hasItem() ? rbf$inventoryIndex(slot, mc.player.getInventory()) : -1;
    }

    // Recipe book screens can draw the container contents directly, bypassing the base screen's render method.
    //#if MC>=260100
    @Inject(method = "extractContents", at = @At("TAIL"))
    private void rbf$renderMarks(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY,
        float partialTick, CallbackInfo ci) {
        //#else
        //#if MC>=12108
        //$$ @Inject(method = "renderContents", at = @At("TAIL"))
        //#else
        //$$ @Inject(method = "render", at = @At("TAIL"))
        //#endif
        //$$ private void rbf$renderMarks(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY,
        //$$     float partialTick, CallbackInfo ci) {
        //#endif
        if (!Configs.LMS.STASH_TO_BOT.getBooleanValue() || StashMarker.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        RbfGraphics overlay = new RbfGraphics(graphics);
        overlay.nextStratum();
        ContainerScreenAccessor accessor = (ContainerScreenAccessor)(Object)this;
        int left = accessor.rbf$getLeftPos();
        int top = accessor.rbf$getTopPos();
        Inventory inventory = mc.player.getInventory();
        for (Slot slot : ((AbstractContainerScreen<?>)(Object)this).getMenu().slots) {
            if (slot.isActive() && StashMarker.isMarked(rbf$inventoryIndex(slot, inventory))) {
                overlay.outline(left + slot.x - 1, top + slot.y - 1, 18, 18, RBF_MARK_COLOR);
            }
        }
    }

    @Unique
    private static int rbf$inventoryIndex(Slot slot, Inventory inventory) {
        if (slot instanceof CreativeSlotAccessor wrapper) {
            slot = wrapper.rbf$getTarget();
        }
        // The bot container exposes the player's main inventory and hotbar for quick moves.
        int index = slot.getContainerSlot();
        return slot.container == inventory && index >= 0 && index < Inventory.INVENTORY_SIZE ? index : -1;
    }
}
