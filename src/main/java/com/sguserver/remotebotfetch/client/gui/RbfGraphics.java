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
package com.sguserver.remotebotfetch.client.gui;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Thin drawing adapter between the fetch panel and the version specific gui graphics API. The panel only talks to this
 * class, so the panel body stays identical on every version while the actual draw calls are resolved here once per
 * supported gui graphics generation.
 */
public final class RbfGraphics {
    //#if MC>=260101
    private final net.minecraft.client.gui.GuiGraphicsExtractor graphics;
    //#else
    //$$ private final net.minecraft.client.gui.GuiGraphics graphics;
    //#endif

    //#if MC>=260101
    public RbfGraphics(net.minecraft.client.gui.GuiGraphicsExtractor graphics) {
        this.graphics = graphics;
    }
    //#else
    //$$ public RbfGraphics(net.minecraft.client.gui.GuiGraphics graphics) {
    //$$     this.graphics = graphics;
    //$$ }
    //#endif

    /**
     * Creates a namespace bound texture id. 26.x and 1.21.11 call it {@code net.minecraft.resources.Identifier}, the
     * older versions still call it {@code net.minecraft.resources.ResourceLocation}.
     */
    public static Object id(String path) {
        //#if MC>=12111
        return net.minecraft.resources.Identifier.withDefaultNamespace(path);
        //#else
        //$$ return net.minecraft.resources.ResourceLocation.withDefaultNamespace(path);
        //#endif
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        this.graphics.fill(x1, y1, x2, y2, color);
    }

    public void outline(int x, int y, int width, int height, int color) {
        //#if MC>=260101
        this.graphics.outline(x, y, width, height, color);
        //#else
        //#if MC>=12111
        //$$ this.graphics.renderOutline(x, y, width, height, color);
        //#else
        //#if MC>=12110
        //$$ this.graphics.submitOutline(x, y, width, height, color);
        //#else
        //$$ this.graphics.renderOutline(x, y, width, height, color);
        //#endif
        //#endif
        //#endif
    }

    public void blit(Object texture, int x, int y, float u, float v, int width, int height, int textureWidth,
        int textureHeight) {
        //#if MC>=12111
        this.graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
            (net.minecraft.resources.Identifier)texture, x, y, u, v, width, height, textureWidth, textureHeight);
        //#else
        //#if MC>=12108
        //$$ this.graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, (net.minecraft.resources.ResourceLocation) texture, x, y, u, v, width, height, textureWidth, textureHeight);
        //#else
        //#if MC>=12103
        //$$ this.graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, (net.minecraft.resources.ResourceLocation) texture, x, y, u, v, width, height, textureWidth, textureHeight);
        //#else
        //$$ this.graphics.blit((net.minecraft.resources.ResourceLocation) texture, x, y, (int) u, (int) v, width, height);
        //#endif
        //#endif
        //#endif
    }

    public void blitSprite(Object sprite, int x, int y, int width, int height) {
        //#if MC>=12111
        this.graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
            (net.minecraft.resources.Identifier)sprite, x, y, width, height);
        //#else
        //#if MC>=12108
        //$$ this.graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, (net.minecraft.resources.ResourceLocation) sprite, x, y, width, height);
        //#else
        //#if MC>=12103
        //$$ this.graphics.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, (net.minecraft.resources.ResourceLocation) sprite, x, y, width, height);
        //#else
        //$$ this.graphics.blitSprite((net.minecraft.resources.ResourceLocation) sprite, x, y, width, height);
        //#endif
        //#endif
        //#endif
    }

    public void item(ItemStack stack, int x, int y) {
        //#if MC>=260101
        this.graphics.item(stack, x, y);
        //#else
        //$$ this.graphics.renderItem(stack, x, y);
        //#endif
    }

    public void itemDecorations(Font font, ItemStack stack, int x, int y) {
        //#if MC>=260101
        this.graphics.itemDecorations(font, stack, x, y);
        //#else
        //$$ this.graphics.renderItemDecorations(font, stack, x, y);
        //#endif
    }

    public void centeredText(Font font, Component text, int x, int y, int color) {
        //#if MC>=260101
        this.graphics.centeredText(font, text, x, y, color);
        //#else
        //$$ this.graphics.drawCenteredString(font, text, x, y, color);
        //#endif
    }

    public void tooltip(Font font, List<Component> lines, Optional<TooltipComponent> image, int mouseX, int mouseY) {
        //#if MC>=12108
        this.graphics.setTooltipForNextFrame(font, lines, image, mouseX, mouseY);
        //#else
        //$$ this.graphics.renderTooltip(font, lines, image, mouseX, mouseY);
        //#endif
    }

    public void tooltip(Font font, Component text, int mouseX, int mouseY) {
        //#if MC>=12108
        this.graphics.setTooltipForNextFrame(font, text, mouseX, mouseY);
        //#else
        //$$ this.graphics.renderTooltip(font, text, mouseX, mouseY);
        //#endif
    }

    public void editBox(EditBox box, int mouseX, int mouseY) {
        //#if MC>=260101
        box.extractWidgetRenderState(this.graphics, mouseX, mouseY, 0.0F);
        //#else
        //$$ box.renderWidget(this.graphics, mouseX, mouseY, 0.0F);
        //#endif
    }

    /**
     * Starts a new render stratum. Only the newer gui graphics implementations know about strata, so this is a no-op
     * everywhere else.
     */
    public void nextStratum() {
        //#if MC>=12108
        this.graphics.nextStratum();
        //#endif
    }
}