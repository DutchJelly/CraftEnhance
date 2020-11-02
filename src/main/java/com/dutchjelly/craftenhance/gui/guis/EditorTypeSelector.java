package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.editors.WBRecipeEditor;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EditorTypeSelector extends GUIElement {

    private Inventory inventory;
    private String key;
    private String permission;

    public EditorTypeSelector(GuiManager manager, GuiTemplate template, GUIElement previousGui, Player player, String key, String permission){
        super(manager, template, previousGui, player);
        this.addBtnListener(ButtonType.ChooseFurnaceType, this::chooseFurnace);
        this.addBtnListener(ButtonType.ChooseWorkbenchType, this::chooseWorkbench);
        inventory = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitle(), this);
        this.key = key;
        this.permission = permission;
    }

    private void chooseWorkbench(ItemStack itemStack, ButtonType buttonType) {
        WBRecipe newRecipe = new WBRecipe(permission, null, new ItemStack[9]);
        newRecipe.setKey(key);

        WBRecipeEditor gui = new WBRecipeEditor(
                CraftEnhance.self().getGuiManager(),
                CraftEnhance.self().getGuiTemplatesFile().getTemplate(WBRecipeEditor.class),
                null, getPlayer(), newRecipe
        );
        getManager().openGUI(getPlayer(), gui);
    }

    private void chooseFurnace(ItemStack itemStack, ButtonType buttonType) {

    }

    @Override
    public void handleEventRest(InventoryClickEvent e) {

    }

    @Override
    public boolean isCancelResponsible() {
        return false;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
