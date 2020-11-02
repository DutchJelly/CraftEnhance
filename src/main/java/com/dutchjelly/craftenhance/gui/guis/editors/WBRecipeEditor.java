package com.dutchjelly.craftenhance.gui.guis.editors;

import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class WBRecipeEditor extends RecipeEditor<WBRecipe> {
	

	private boolean shapeless;

	public WBRecipeEditor(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, WBRecipe recipe){
	    super(manager,template,previous,p,recipe);
    }

    @Override
    protected void onRecipeDisplayUpdate() {
        shapeless = getRecipe().isShapeless();
    }

    @Override
    public Map<String, String> getPlaceHolders() {
        return new HashMap<String,String>(){{
            put(InfoItemPlaceHolders.Shaped.getPlaceHolder(), shapeless ? "shapeless" : "shaped");
        }};
    }

    @Override
    protected void initBtnListeners() {
        addBtnListener(ButtonType.SwitchShaped, this::switchShaped);
    }

    @Override
    protected void beforeSave() {
        getRecipe().setShapeless(shapeless);
    }

    private void switchShaped(ItemStack button, ButtonType btnType){
        shapeless = !shapeless;
        updatePlaceHolders();
    }




}
