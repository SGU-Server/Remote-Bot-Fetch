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

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Accessors for the protected geometry fields of the container screens. org.spongepowered.asm.mixin.Mixin does not
 * resolve {@code @org.spongepowered.asm.mixin.Shadow} fields which are only declared by a super class, so the values
 * are read and written through this interface instead.
 */
@org.spongepowered.asm.mixin.Mixin(AbstractContainerScreen.class)
public interface ContainerScreenAccessor {
    @org.spongepowered.asm.mixin.gen.Accessor("leftPos")
    int rbf$getLeftPos();

    @org.spongepowered.asm.mixin.gen.Accessor("leftPos")
    void rbf$setLeftPos(int leftPos);

    @org.spongepowered.asm.mixin.gen.Accessor("topPos")
    int rbf$getTopPos();

    @org.spongepowered.asm.mixin.gen.Accessor("imageWidth")
    int rbf$getImageWidth();

    @org.spongepowered.asm.mixin.gen.Accessor("imageHeight")
    int rbf$getImageHeight();
}