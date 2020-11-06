package com.dutchjelly.craftenhance.gui.guis.viewers;

import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class FurnaceRecipeViewer extends RecipeViewer<FurnaceRecipe> {

    public FurnaceRecipeViewer(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, FurnaceRecipe recipe){
        super(manager, template, previous, p, recipe);
    }

    @Override
    protected Map<String, String> getPlaceHolders() {
        return new HashMap<String, String>(){{
            put("[duration]", String.valueOf(getRecipe().getDuration()));
            put("[exp]", String.valueOf(getRecipe().getExp()));
        }};
    }
}
