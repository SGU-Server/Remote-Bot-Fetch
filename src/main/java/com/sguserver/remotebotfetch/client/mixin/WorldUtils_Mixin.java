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
package com.sguserver.remotebotfetch.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.utils.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.utils.PlayerUtils;

@Pseudo
@Mixin(targets = "fi.dy.masa.litematica.util.WorldUtils")
public class WorldUtils_Mixin {
    /** Litematica calls this method twice per key press, so the second call in the same tick has to be ignored. */
    private static long rbf$lastSubmitTick = Long.MIN_VALUE;

    @Inject(method = "doSchematicWorldPickBlock", at = @At(value = "INVOKE",
        target = "Lfi/dy/masa/litematica/schematic/pickblock/SchematicPickBlockEventHandler;onSchematicPickBlockPrePick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/ItemStack;)Z"))
    private static void doSchematicWorldPickBlock_Inject(boolean closest, Minecraft mc,
        CallbackInfoReturnable<Boolean> cir, @Local ItemStack stack, @Local BlockPos pos) {
        if (mc.level == null) {
            return;
        }

        long tick = mc.level.getGameTime();
        if (rbf$lastSubmitTick == tick) {
            return;
        }

        if (PlayerUtils.checkRemainCount(stack.getItem()) == 0 && PlayerUtils.isSurvivalMode(mc.player)
            && mc.level.getBlockState(pos).getBlock() == Blocks.AIR) {
            if (Configs.LMS.MID_CLICK_TAKE_ITEM.getBooleanValue()) {
                rbf$lastSubmitTick = tick;
                int count = mc.player.isShiftKeyDown() ? stack.getMaxStackSize() * 27 : stack.getMaxStackSize();
                ClientMod
                    .debugMessage(() -> "Submitted %s x%d to ItemStorageDataManager".formatted(stack.getItem(), count));
                ItemStorageDataManager.submit(stack.getItem(), count);
            }
        }
    }
}
