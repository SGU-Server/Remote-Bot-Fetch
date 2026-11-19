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
import java.util.Set;

import net.fabricmc.loader.api.FabricLoader;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import org.objectweb.asm.tree.ClassNode;

/**
 * Applies the litematica integration mixins only when litematica is actually installed, litematica stays an optional
 * dependency of the client mod.
 */
public class ClientMixinPlugin implements IMixinConfigPlugin {
    private static final String LITEMATICA_MOD_ID = "litematica";
    private static final List<String> LITEMATICA_MIXINS =
        List.of("com.sguserver.remotebotfetch.client.mixin.feature.lms.MaterialListEntry_Mixin",
            "com.sguserver.remotebotfetch.client.mixin.feature.lms.WorldUtils_Mixin");

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (LITEMATICA_MIXINS.contains(mixinClassName)) {
            return FabricLoader.getInstance().isModLoaded(LITEMATICA_MOD_ID);
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
