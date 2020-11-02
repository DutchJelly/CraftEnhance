package com.dutchjelly.craftenhance.gui.guis.editors;

import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import org.bukkit.entity.Player;

import java.util.Map;


public class FurnaceRecipeEditor extends RecipeEditor<FurnaceRecipe> {

    public FurnaceRecipeEditor(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, FurnaceRecipe recipe){
        super(manager, template, previous, p, recipe);
    }

    @Override
    public void initBtnListeners() {

    }

    @Override
    public void onRecipeDisplayUpdate() {

    }

    @Override
    public Map<String, String> getPlaceHolders() {
        return null;
    }

    @Override
    public void beforeSave() {

    }
}
