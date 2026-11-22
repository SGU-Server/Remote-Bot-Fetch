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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.stash.StashMarker;
import com.sguserver.remotebotfetch.client.mixin.rbf.ContainerScreenAccessor;

/**
 * "Stash to bot" marking: while alt is held, clicking a player inventory slot marks the stack instead of moving it.
 * Marked slots get a red frame and are moved into the configured fake player when the container screen closes.
 */
@org.spongepowered.asm.mixin.Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenStashMixin {
    private static final int RBF_MARK_COLOR = 0xFFFF3B30;

    @org.spongepowered.asm.mixin.injection.Inject(method = "onClose",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void rbf$onClose(CallbackInfo ci) {
        StashMarker.requestTransfer();
    }

    //#if MC>=12110
    @org.spongepowered.asm.mixin.injection.Inject(method = "mouseClicked",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick,
        CallbackInfoReturnable<Boolean> cir) {
        if (this.rbf$tryMark(event.x(), event.y(), event.hasAltDown())) {
            cir.setReturnValue(true);
        }
    }
    //#else
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "mouseClicked", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$mouseClicked(double mouseX, double mouseY, int button, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
    //$$     if (this.rbf$tryMark(mouseX, mouseY, net.minecraft.client.gui.screens.Screen.hasAltDown())) {
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //#endif

    //#if MC>=260100
    @org.spongepowered.asm.mixin.injection.Inject(method = "extractRenderState",
        at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void rbf$extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY,
        float partialTick, CallbackInfo ci) {
        this.rbf$renderMarks(graphics);
    }
    //#else
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "render", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    //$$ private void rbf$render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    //$$     this.rbf$renderMarks(graphics);
    //$$ }
    //#endif

    //#if MC>=260100
    private void rbf$renderMarks(net.minecraft.client.gui.GuiGraphicsExtractor graphics) {
        //#else
        //$$ private void rbf$renderMarks(net.minecraft.client.gui.GuiGraphics graphics) {
        //#endif
        if (!Configs.LMS.STASH_TO_BOT.getBooleanValue() || StashMarker.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        ContainerScreenAccessor accessor = (ContainerScreenAccessor)(Object)this;
        int leftPos = accessor.rbf$getLeftPos();
        int topPos = accessor.rbf$getTopPos();
        for (Slot slot : menu.slots) {
            if (slot.container != mc.player.getInventory() || !StashMarker.isMarked(slot.getContainerSlot())) {
                continue;
            }
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            graphics.fill(x, y, x + 16, y + 1, RBF_MARK_COLOR);
            graphics.fill(x, y + 15, x + 16, y + 16, RBF_MARK_COLOR);
            graphics.fill(x, y, x + 1, y + 16, RBF_MARK_COLOR);
            graphics.fill(x + 15, y, x + 16, y + 16, RBF_MARK_COLOR);
        }
    }

    private boolean rbf$tryMark(double mouseX, double mouseY, boolean altDown) {
        Minecraft mc = Minecraft.getInstance();
        if (!Configs.LMS.STASH_TO_BOT.getBooleanValue() || mc.player == null || !altDown) {
            return false;
        }

        AbstractContainerMenu menu = mc.player.containerMenu;
        ContainerScreenAccessor accessor = (ContainerScreenAccessor)(Object)this;
        int leftPos = accessor.rbf$getLeftPos();
        int topPos = accessor.rbf$getTopPos();
        for (Slot slot : menu.slots) {
            if (slot.container != mc.player.getInventory()) {
                continue;
            }
            int x = leftPos + slot.x;
            int y = topPos + slot.y;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                StashMarker.toggle(slot.getContainerSlot());
                return true;
            }
        }
        return false;
    }
}
