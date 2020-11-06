package com.dutchjelly.craftenhance.crafthandling.recipes;

import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import org.bukkit.inventory.Recipe;


public interface ServerLoadable {

    String getKey();

    Recipe getServerRecipe();

    boolean isSimilar(Recipe r);

    boolean isSimilar(EnhancedRecipe r);

    boolean isAlwaysSimilar(Recipe r);

}
