package com.dutchjelly.craftenhance.crafthandling;


import com.dutchjelly.craftenhance.IEnhancedRecipe;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeGroup {

    @Getter @Setter @NonNull
    private List<Recipe> serverRecipes = new ArrayList<>();

    @Getter @Setter @NonNull
    private List<IEnhancedRecipe> enhancedRecipes = new ArrayList<>();

}
