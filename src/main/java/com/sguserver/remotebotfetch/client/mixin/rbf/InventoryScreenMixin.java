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

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;

/**
 * The vanilla recipe book button becomes the on/off switch of the RBF fetch panel while the feature is enabled. This
 * hook is placed here instead of on the {@code AbstractRecipeBookScreen} because {@link InventoryScreen} overrides the
 * click callback without calling super, and it is invoked after the screen has already been re-positioned.
 * <p>
 * 1.21.1 has no {@code onRecipeBookButtonClick} method, the panel switch there rides on
 * {@code RecipeBookComponent#toggleVisibility} instead, so this mixin is empty.
 */
@org.spongepowered.asm.mixin.Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    //#if MC>=12108
    // The entity preview is a picture-in-picture pass with absolute bounds. It bypasses the GUI pose.
    //#if MC>=260101
    @org.spongepowered.asm.mixin.injection.ModifyArgs(method = "extractBackground",
        at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"))
    //#else
    //$$ @org.spongepowered.asm.mixin.injection.ModifyArgs(method = "renderBg", at = @org.spongepowered.asm.mixin.injection.At(value = "INVOKE",
    //$$     target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"))
    //#endif
    private void rbf$fitPlayerPreview(org.spongepowered.asm.mixin.injection.invoke.arg.Args args) {
        if ((Object)this instanceof com.sguserver.remotebotfetch.client.gui.RbfBotInventoryScreen screen
            && screen.contentScale() != 1.0) {
            double scale = screen.contentScale();
            for (int i = 1; i <= 5; i++) {
                args.set(i, (int)Math.round(args.<Integer>get(i) * scale));
            }
            args.set(7, (float)(args.<Float>get(7) * scale));
            args.set(8, (float)(args.<Float>get(8) * scale));
        }
    }
    //#endif

    //#if MC>=12102
    @org.spongepowered.asm.mixin.injection.Inject(method = "onRecipeBookButtonClick",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void rbf$onRecipeBookButtonClick(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (!Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue()) {
            return;
        }

        RbfPanelHost host = (RbfPanelHost)(Object)this;
        RbfFetchPanel panel = host.rbf$getPanel();
        panel.toggle();
        host.rbf$updatePanelPosition(panel.isOpen());
    }
    //#endif
}
