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

import java.util.List;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.utils.ItemStorageDataManager;
import com.sguserver.remotebotfetch.client.utils.MCUtils;
import com.sguserver.remotebotfetch.client.utils.PlayerUtils;

import org.objectweb.asm.Opcodes;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreen_Mixin
    extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    public CreativeModeInventoryScreen_Mixin(CreativeModeInventoryScreen.ItemPickerMenu handler, Inventory inventory,
        Component title) {
        super(handler, inventory, title);
    }

    @Inject(method = "slotClicked", at = @At(value = "FIELD", target =
    //#if MC>=260100
    "Lnet/minecraft/world/inventory/ContainerInput;THROW:Lnet/minecraft/world/inventory/ContainerInput;",
        //#else
        //$$ "Lnet/minecraft/world/inventory/ClickType;THROW:Lnet/minecraft/world/inventory/ClickType;",
        //#endif
        ordinal = 0, opcode = Opcodes.GETSTATIC, shift = At.Shift.AFTER))
    protected void onMouseClick_1(Slot slot, int slotId, int button, @Coerce Object actionType, CallbackInfo ci,
        @Share("type") LocalRef<Object> asa$type) {
        if (Configs.lockCreativeScreen && !isType(actionType, "THROW") && !isType(actionType, "QUICK_CRAFT")) {
            asa$type.set(actionType);
        }
    }

    @Inject(method = "containerTick", at = @At(value = "INVOKE", target =
    //#if MC>=12105
    "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"),
        //#else
        //$$ "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"),
        //#endif
        cancellable = true)
    public void handledScreenTick(CallbackInfo ci) {
        if (Configs.lockCreativeScreen) {
            ci.cancel();
        }
    }

    /*
        slotID -999 (从创造模式物品栏丢出物品)
        button 0 左键
        button 1 右键
        SlotActionType PickUp 单击
    */
    @Inject(method = "slotClicked", at = @At(value = "INVOKE", target =
    //#if MC>=12105
    "Lnet/minecraft/client/player/LocalPlayer;canDropItems()Z",
        //#else
        //$$ "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleCreativeModeItemDrop(Lnet/minecraft/world/item/ItemStack;)V",
        //#endif
        ordinal = 1), cancellable = true)
    protected void onMouseClick_2(Slot slot, int slotId, int button, @Coerce Object actionType, CallbackInfo ci,
        @Share("type") LocalRef<Object> asa$type) {

        if (Configs.lockCreativeScreen && isType(asa$type.get(), "PICKUP") && slotId == -999) {
            if (!Configs.LMS.LMS_FETCH_SUPPORT.getBooleanValue())
                return;
            ItemStack stack = this.menu.getCarried();
            int count = -1;
            int maxCount = stack.getMaxStackSize();
            if (button == 0)
                count = maxCount;
            else if (button == 1)
                count = maxCount * 27;
            ItemStorageDataManager.submit(stack.getItem(), stack.getCount() * count);
            menu.setCarried(ItemStack.EMPTY);
            Configs.lockCreativeScreen = false;
            MCUtils.setScreen(null);
            ci.cancel();
        }
    }

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    public void getTooltipFromItem(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        if (Configs.LMS.LMS_FETCH_SUPPORT.getBooleanValue() && Configs.lockCreativeScreen) {
            if (!PlayerUtils.isSurvivalMode(minecraft.player))
                return;
            var texts = cir.getReturnValue();
            var text = ItemStorageDataManager.get(stack);
            texts.addAll(text);
            cir.setReturnValue(texts);
        }
    }

    @ModifyExpressionValue(method = "init", at = @At(value = "INVOKE", target =
    //#if MC>=12105
    "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"))
    //#else
    //$$ "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;hasInfiniteItems()Z"))
    //#endif
    public boolean isInCreativeMode(boolean original) {
        return Configs.lockCreativeScreen || original;
    }

    @ModifyExpressionValue(method = "hasPermissions",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canUseGameMasterBlocks()Z"))
    public boolean isCreativeLevelTwoOp(boolean original) {
        if (Configs.LMS.LMS_FETCH_SUPPORT.getBooleanValue() && Configs.lockCreativeScreen) {
            return true;
        }
        return original;
    }

    private static boolean isType(Object value, String name) {
        return value instanceof Enum<?> enumValue && enumValue.name().equals(name);
    }
}
