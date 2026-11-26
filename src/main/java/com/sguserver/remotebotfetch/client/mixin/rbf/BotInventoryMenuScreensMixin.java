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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.MenuType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sguserver.remotebotfetch.client.ClientMod;
import com.sguserver.remotebotfetch.client.gui.BotInventoryOpening;
import com.sguserver.remotebotfetch.client.gui.RbfBotInventoryScreen;
import com.sguserver.remotebotfetch.client.utils.MCUtils;
import com.sguserver.remotebotfetch.menu.RbfBotInventoryMenu;

@Mixin(MenuScreens.class)
public abstract class BotInventoryMenuScreensMixin {
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void rbf$openBotInventory(MenuType<?> type, Minecraft mc, int id, Component title, CallbackInfo ci) {
        if (mc.player == null || !(title.getContents() instanceof TranslatableContents translated)
            || !RbfBotInventoryMenu.TITLE_KEY.equals(translated.getKey())) {
            return;
        }

        if (!BotInventoryOpening.acceptsScreen()) {
            ClientMod.LOGGER.info("[RBF] rejected bot inventory screen {} (container {})", title.getString(), id);
            mc.player.connection.send(new ServerboundContainerClosePacket(id));
            ci.cancel();
            return;
        }

        ClientMod.LOGGER.info("[RBF] creating bot inventory screen {} (container {})", title.getString(), id);
        RbfBotInventoryMenu menu = new RbfBotInventoryMenu(id, mc.player.getInventory());
        RbfBotInventoryScreen screen = new RbfBotInventoryScreen(menu, mc.player.getInventory(), title);
        mc.player.containerMenu = menu;
        MCUtils.setScreen(screen);
        ci.cancel();
    }
}