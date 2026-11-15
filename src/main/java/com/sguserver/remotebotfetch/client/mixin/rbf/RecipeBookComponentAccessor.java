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

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

/**
 * Exposes {@link RecipeBookComponent#setVisible(boolean)} so the screen mixin can force the vanilla recipe book closed
 * when the fetch panel replaces it.
 */
@org.spongepowered.asm.mixin.Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {
    @org.spongepowered.asm.mixin.gen.Invoker("setVisible")
    void rbf$setVisible(boolean visible);
}