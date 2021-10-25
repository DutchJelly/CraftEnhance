package com.dutchjelly.craftenhance.gui.guis.viewers;

import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class FurnaceRecipeViewer extends RecipeViewer<FurnaceRecipe> {

    public FurnaceRecipeViewer(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, FurnaceRecipe recipe){
        super(manager, template, previous, p, recipe);
    }

    public FurnaceRecipeViewer(GuiManager manager, GUIElement previous, Player p, FurnaceRecipe recipe){
        super(manager, previous, p, recipe);
    }

    @Override
    protected Map<String, String> getPlaceHolders() {
        return new HashMap<String, String>(){{
            put(InfoItemPlaceHolders.Duration.getPlaceHolder(), String.valueOf(getRecipe().getDuration()));
            put(InfoItemPlaceHolders.Exp.getPlaceHolder(), String.valueOf(getRecipe().getExp()));
        }};
    }
}
