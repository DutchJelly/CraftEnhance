package com.dutchjelly.craftenhance.crafthandling.recipes;

import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public enum RecipeType {
    WORKBENCH, FURNACE;

    public static RecipeType getType(Recipe r) {
        if(r instanceof ShapedRecipe) return WORKBENCH;
        if(r instanceof ShapelessRecipe) return WORKBENCH;
        if(r instanceof FurnaceRecipe) return FURNACE;
        return null;
    }
}
