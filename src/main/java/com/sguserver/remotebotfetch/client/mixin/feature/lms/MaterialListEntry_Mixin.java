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
package com.sguserver.remotebotfetch.client.mixin.feature.lms;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.feature.lms.ItemStorageDataManager;

import fi.dy.masa.litematica.materials.MaterialListEntry;

@Mixin(MaterialListEntry.class)
public class MaterialListEntry_Mixin {
    @Shadow
    @Final
    //#if MC>=260101
    private fi.dy.masa.malilib.util.data.ItemType item;
    //#else
    //$$ private fi.dy.masa.malilib.util.ItemType item;
    //#endif

    @ModifyReturnValue(method = "getCountAvailable", at = @At("RETURN"))
    private int asa$getCountAvailable(int original) {
        int value = original;
        ItemStack stack = this.item.getStack();

        if (Configs.LMS.LITEMATICA_CALCULATE_QWP.getBooleanValue())
            value += ItemStorageDataManager.getCount(stack.getItem(), false);
        if (Configs.LMS.LITEMATICA_CALCULATE_FAKE.getBooleanValue())
            value += ItemStorageDataManager.getCount(stack.getItem(), true);

        return value;
    }
}
