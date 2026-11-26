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

import net.minecraft.client.multiplayer.ClientPacketListener;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sguserver.remotebotfetch.client.feature.stash.StashMarker;

@Mixin(ClientPacketListener.class)
public abstract class StashPlayerInventorySyncMixin {
    //#if MC>=12102
    @Inject(method = "handleSetPlayerInventory", at = @At("TAIL"))
    private void rbf$onPlayerInventorySync(
        net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket packet, CallbackInfo ci) {
        StashMarker.onPlayerInventorySync(packet.slot());
    }
    //#else
    //$$ @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    //$$ private void rbf$onPlayerInventorySync(net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket packet,
    //$$     CallbackInfo ci) {
    //$$     if (packet.getContainerId() == net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket.PLAYER_INVENTORY) {
    //$$         StashMarker.onPlayerInventorySync(packet.getSlot());
    //$$     }
    //$$ }
    //#endif
}
