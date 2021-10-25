package com.dutchjelly.craftenhance.gui.guis.viewers;

import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class WBRecipeViewer extends RecipeViewer<WBRecipe> {

    public WBRecipeViewer(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, WBRecipe recipe) {
        super(manager, template, previous, p, recipe);
    }

    public WBRecipeViewer(GuiManager manager, GUIElement previous, Player p, WBRecipe recipe) {
        super(manager, previous, p, recipe);
    }

    @Override
    protected Map<String, String> getPlaceHolders() {
        return new HashMap<String,String>(){{
            put(InfoItemPlaceHolders.Shaped.getPlaceHolder(), getRecipe().isShapeless() ? "shapeless" : "shaped");
        }};
    }
}