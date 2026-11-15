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

import java.util.List;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

import com.sguserver.remotebotfetch.client.config.Configs;
import com.sguserver.remotebotfetch.client.gui.RbfFetchPanel;
import com.sguserver.remotebotfetch.client.gui.RbfGraphics;
import com.sguserver.remotebotfetch.client.gui.RbfPanelHost;

/**
 * Hosts the RBF fetch panel on the player inventory, in the place where the vanilla recipe book normally is. All the
 * panel drawing and the input handling that is declared by the recipe book screen goes through here.
 * <p>
 * 1.21.2 moved the recipe book screen logic out of {@code InventoryScreen} into {@code AbstractRecipeBookScreen}; on
 * 1.21.1 the inventory screen itself is the host.
 */
//#if MC>=12102
@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen.class)
//#else
//$$ @org.spongepowered.asm.mixin.Mixin(InventoryScreen.class)
//#endif
public abstract class AbstractRecipeBookScreenMixin implements RbfPanelHost {
    //#if MC>=12102
    @org.spongepowered.asm.mixin.Shadow
    @org.spongepowered.asm.mixin.Final
    private RecipeBookComponent recipeBookComponent;
    //#endif

    @org.spongepowered.asm.mixin.Unique
    private RbfFetchPanel rbf$panel;

    @org.spongepowered.asm.mixin.Unique
    private boolean rbf$wasOpen;

    @Override
    public RbfFetchPanel rbf$getPanel() {
        if (this.rbf$panel == null) {
            this.rbf$panel = new RbfFetchPanel();
        }
        return this.rbf$panel;
    }

    @Override
    public void rbf$updatePanelPosition(boolean open) {
        this.rbf$access()
            .rbf$setLeftPos(open ? this.rbf$shiftedLeftPos() : (this.rbf$screenWidth() - this.rbf$imageWidth()) / 2);
        this.rbf$moveRecipeButton();
    }

    @Override
    public void rbf$moveRecipeButton() {
        int x = this.rbf$access().rbf$getLeftPos() + 104;
        int y = this.rbf$screenHeight() / 2 - 22;

        for (GuiEventListener listener : this.rbf$children()) {
            if (listener instanceof ImageButton button && button.getWidth() == 20 && button.getHeight() == 18) {
                button.setPosition(x, y);
            }
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "init",
        at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void rbf$onInit(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        RbfFetchPanel panel = this.rbf$getPanel();
        panel.reset();

        if (!this.rbf$isPlayerInventory()) {
            this.rbf$wasOpen = false;
            return;
        }

        if (Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue()) {
            // The recipe book is restored as visible from the player's book data, which
            // would offset the inventory and draw the book next to the panel. Keep it shut.
            ((RecipeBookComponentAccessor)this.rbf$recipeBook()).rbf$setVisible(false);
        }

        // The panel keeps its open state across screens, so the inventory has to be moved
        // to the shifted position again when it is reopened with the panel still open.
        boolean open = panel.isOpen();
        this.rbf$wasOpen = open;
        this.rbf$updatePanelPosition(open);
    }

    //#if MC>=260101
    @org.spongepowered.asm.mixin.injection.Inject(method = "extractRenderState",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void rbf$hideRecipeBook(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY,
        float partial, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.rbf$isPlayerInventory() && Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue()) {
            // The recipe book re-opens itself when a new recipe is unlocked, so keep it
            // hidden on every frame instead of only when the screen is created.
            ((RecipeBookComponentAccessor)this.rbf$recipeBook()).rbf$setVisible(false);
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "extractRenderState",
        at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void rbf$renderPanel(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY,
        float partial, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        this.rbf$drawPanel(new RbfGraphics(graphics), mouseX, mouseY);
    }
    //#else
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "render", at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    //$$ private void rbf$hideRecipeBook(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partial,
    //$$                                 org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    //$$     if (this.rbf$isPlayerInventory() && Configs.LMS.INVENTORY_FETCH_MENU.getBooleanValue()) {
    //$$         ((RecipeBookComponentAccessor) this.rbf$recipeBook()).rbf$setVisible(false);
    //$$     }
    //$$ }
    //$$
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "render", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    //$$ private void rbf$renderPanel(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partial, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
    //$$     this.rbf$drawPanel(new RbfGraphics(graphics), mouseX, mouseY);
    //$$ }
    //#endif

    @org.spongepowered.asm.mixin.Unique
    private void rbf$drawPanel(RbfGraphics graphics, int mouseX, int mouseY) {
        if (!this.rbf$isPlayerInventory()) {
            return;
        }

        RbfFetchPanel panel = this.rbf$getPanel();
        boolean open = panel.isOpen();

        if (this.rbf$wasOpen && !open) {
            this.rbf$updatePanelPosition(false);
        }
        this.rbf$wasOpen = open;

        if (!open) {
            return;
        }

        graphics.nextStratum();
        panel.render(graphics, mouseX, mouseY, this.rbf$panelX(), this.rbf$panelY());
        graphics.nextStratum();
    }

    //#if MC>=12110
    @org.spongepowered.asm.mixin.injection.Inject(method = "mouseClicked",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!this.rbf$isPlayerInventory()) {
            return;
        }

        RbfFetchPanel panel = this.rbf$getPanel();
        if (!panel.isOpen()) {
            return;
        }

        if (panel.mouseClicked(event.x(), event.y(), event.button(), event.hasShiftDown())) {
            cir.setReturnValue(true);
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "mouseDragged",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!this.rbf$isPlayerInventory()) {
            return;
        }

        RbfFetchPanel panel = this.rbf$getPanel();
        if (panel.isOpen() && panel.mouseDragged(event.x(), event.y())) {
            cir.setReturnValue(true);
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "keyPressed",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$keyPressed(net.minecraft.client.input.KeyEvent event,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!this.rbf$isPlayerInventory()) {
            return;
        }

        RbfFetchPanel panel = this.rbf$getPanel();
        if (panel.isOpen() && panel.keyPressed(event)) {
            cir.setReturnValue(true);
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "charTyped",
        at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void rbf$charTyped(net.minecraft.client.input.CharacterEvent event,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!this.rbf$isPlayerInventory()) {
            return;
        }

        RbfFetchPanel panel = this.rbf$getPanel();
        if (panel.isOpen() && panel.charTyped(event)) {
            cir.setReturnValue(true);
        }
    }
    //#else
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "mouseClicked", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$mouseClicked(double mouseX, double mouseY, int button, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
    //$$     if (!this.rbf$isPlayerInventory()) {
    //$$         return;
    //$$     }
    //$$
    //$$     RbfFetchPanel panel = this.rbf$getPanel();
    //$$     if (!panel.isOpen()) {
    //$$         return;
    //$$     }
    //$$
    //$$     if (panel.mouseClicked(mouseX, mouseY, button, Screen.hasShiftDown())) {
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //$$
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "keyPressed", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$keyPressed(int keyCode, int scanCode, int modifiers, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
    //$$     if (!this.rbf$isPlayerInventory()) {
    //$$         return;
    //$$     }
    //$$
    //$$     RbfFetchPanel panel = this.rbf$getPanel();
    //$$     if (panel.isOpen() && panel.keyPressed(keyCode, scanCode, modifiers)) {
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //$$
    //$$ @org.spongepowered.asm.mixin.injection.Inject(method = "charTyped", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    //$$ private void rbf$charTyped(char chr, int modifiers, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
    //$$     if (!this.rbf$isPlayerInventory()) {
    //$$         return;
    //$$     }
    //$$
    //$$     RbfFetchPanel panel = this.rbf$getPanel();
    //$$     if (panel.isOpen() && panel.charTyped(chr, modifiers)) {
    //$$         cir.setReturnValue(true);
    //$$     }
    //$$ }
    //#endif

    @org.spongepowered.asm.mixin.Unique
    private RecipeBookComponent rbf$recipeBook() {
        //#if MC>=12102
        return this.recipeBookComponent;
        //#else
        //$$ return ((InventoryScreen) (Object) this).getRecipeBookComponent();
        //#endif
    }

    @org.spongepowered.asm.mixin.Unique
    private ContainerScreenAccessor rbf$access() {
        return (ContainerScreenAccessor)(Object)this;
    }

    @org.spongepowered.asm.mixin.Unique
    private int rbf$screenWidth() {
        return ((Screen)(Object)this).width;
    }

    @org.spongepowered.asm.mixin.Unique
    private int rbf$screenHeight() {
        return ((Screen)(Object)this).height;
    }

    @org.spongepowered.asm.mixin.Unique
    private List<? extends GuiEventListener> rbf$children() {
        return ((Screen)(Object)this).children();
    }

    @org.spongepowered.asm.mixin.Unique
    private int rbf$imageWidth() {
        return this.rbf$access().rbf$getImageWidth();
    }

    @org.spongepowered.asm.mixin.Unique
    private boolean rbf$isPlayerInventory() {
        return (Object)this instanceof InventoryScreen;
    }

    @org.spongepowered.asm.mixin.Unique
    private int rbf$shiftedLeftPos() {
        int shift = RbfFetchPanel.IMAGE_WIDTH + 6;
        int combined = this.rbf$imageWidth() + shift;
        if (this.rbf$screenWidth() < combined) {
            return (this.rbf$screenWidth() - this.rbf$imageWidth()) / 2;
        }
        return (this.rbf$screenWidth() - combined) / 2 + shift;
    }

    @org.spongepowered.asm.mixin.Unique
    private int rbf$panelX() {
        return Math.max(this.rbf$access().rbf$getLeftPos() - RbfFetchPanel.IMAGE_WIDTH - 6, 2);
    }

    @org.spongepowered.asm.mixin.Unique
    private int rbf$panelY() {
        int topPos = this.rbf$access().rbf$getTopPos();
        int imageHeight = this.rbf$access().rbf$getImageHeight();
        return topPos + Math.max((imageHeight - RbfFetchPanel.IMAGE_HEIGHT) / 2, 0);
    }
}