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

import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import com.sguserver.remotebotfetch.client.gui.RbfBotInventoryScreen;

/**
 * {@link InventoryScreen} swaps itself to {@code CreativeModeInventoryScreen} when the player has infinite materials,
 * which would replace the three-panel fake player screen right after it opened. Players with creative mode keep the
 * regular inventory layout here, so the fake player panel stays available.
 */
@org.spongepowered.asm.mixin.Mixin(InventoryScreen.class)
public abstract class InventoryScreenCreativeGuardMixin {
    @ModifyExpressionValue(method = "init", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE", target =
    //#if MC>=12105
    "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"
    //#else
    //$$ "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"
    //#endif
    ))
    private boolean rbf$keepBotInventoryScreen(boolean original) {
        return (Object)this instanceof RbfBotInventoryScreen ? false : original;
    }

    @ModifyExpressionValue(method = "containerTick",
        at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE", target =
        //#if MC>=12105
        "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"
        //#else
        //$$ "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"
        //#endif
        ))
    private boolean rbf$keepBotInventoryScreenWhileTicking(boolean original) {
        return (Object)this instanceof RbfBotInventoryScreen ? false : original;
    }
}
