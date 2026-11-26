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

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import com.sguserver.remotebotfetch.client.gui.RbfBotInventoryScreen;
import com.sguserver.remotebotfetch.client.gui.RbfGraphics;

@Mixin(Screen.class)
public abstract class BotInventoryFrameMixin {
    // The frame includes the vanilla background, widgets, carried stack and deferred tooltips.
    //#if MC>=260101
    @WrapMethod(method = "extractRenderStateWithTooltipAndSubtitles")
    private void rbf$drawInventory(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, float delta,
        Operation<Void> original) {
        //#else
        //#if MC>=12110
        //$$ @WrapMethod(method = "renderWithTooltipAndSubtitles")
        //#else
        //$$ @WrapMethod(method = "renderWithTooltip")
        //#endif
        //$$ private void rbf$drawInventory(net.minecraft.client.gui.GuiGraphics graphics, int x, int y, float delta, Operation<Void> original) {
        //#endif
        if (!((Object)this instanceof RbfBotInventoryScreen screen) || screen.contentScale() == 1.0) {
            original.call(graphics, x, y, delta);
            return;
        }
        float scale = (float)screen.contentScale();
        RbfGraphics.Viewport viewport = (RbfGraphics.Viewport)(Object)graphics;
        var pose = graphics.pose();
        //#if MC>=12108
        pose.pushMatrix();
        pose.scale(scale, scale);
        //#else
        //$$ pose.pushPose();
        //$$ pose.scale(scale, scale, 1.0F);
        //#endif
        viewport.rbf$setViewport(screen);
        try {
            original.call(graphics, (int)Math.floor(x / screen.contentScale()),
                (int)Math.floor(y / screen.contentScale()), delta);
        } finally {
            viewport.rbf$setViewport(null);
            //#if MC>=12108
            pose.popMatrix();
            //#else
            //$$ pose.popPose();
            //#endif
        }
    }
}
