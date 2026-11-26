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

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sguserver.remotebotfetch.client.gui.BotInventoryOpening;
import com.sguserver.remotebotfetch.client.gui.RbfBotInventoryScreen;
import com.sguserver.remotebotfetch.client.utils.MCUtils;

/**
 * Switching bots closes the old container on the server before the new one is sent. Ignoring that close keeps the three
 * panel layout on screen instead of dropping back to the world for a frame.
 */
@Mixin(ClientPacketListener.class)
public abstract class ContainerCloseMixin {
    @Inject(method = "handleContainerClose", at = @At("HEAD"), cancellable = true)
    private void rbf$keepBotScreen(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        if (BotInventoryOpening.switchInFlight() && MCUtils.getScreen() instanceof RbfBotInventoryScreen) {
            ci.cancel();
        }
    }
}
