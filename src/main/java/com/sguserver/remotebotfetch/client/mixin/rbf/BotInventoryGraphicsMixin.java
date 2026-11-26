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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.sguserver.remotebotfetch.client.gui.RbfBotInventoryScreen;
import com.sguserver.remotebotfetch.client.gui.RbfGraphics;

// Tooltip positioning uses the graphics dimensions independently of the container screen.
//#if MC>=260101
@Mixin(net.minecraft.client.gui.GuiGraphicsExtractor.class)
//#else
//$$ @Mixin(net.minecraft.client.gui.GuiGraphics.class)
//#endif
public abstract class BotInventoryGraphicsMixin implements RbfGraphics.Viewport {
    @Unique
    private RbfBotInventoryScreen rbf$viewport;

    @Override
    public void rbf$setViewport(RbfBotInventoryScreen screen) {
        this.rbf$viewport = screen;
    }

    @Inject(method = "guiWidth", at = @At("HEAD"), cancellable = true)
    private void rbf$logicalWidth(CallbackInfoReturnable<Integer> cir) {
        if (this.rbf$viewport != null) {
            cir.setReturnValue(this.rbf$viewport.width);
        }
    }

    @Inject(method = "guiHeight", at = @At("HEAD"), cancellable = true)
    private void rbf$logicalHeight(CallbackInfoReturnable<Integer> cir) {
        if (this.rbf$viewport != null) {
            cir.setReturnValue(this.rbf$viewport.height);
        }
    }
}
