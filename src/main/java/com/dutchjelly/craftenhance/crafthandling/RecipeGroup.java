package com.dutchjelly.craftenhance.crafthandling;


import com.dutchjelly.craftenhance.IEnhancedRecipe;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeGroup {

    @Getter @Setter
    private List<Recipe> serverRecipes = new ArrayList<>();

    @Getter @Setter
    private List<IEnhancedRecipe> enhancedRecipes = new ArrayList<>();

    //Returns this for chaining purposes.
    public RecipeGroup mergeWith(@NonNull RecipeGroup othergroup){
        List<Recipe> mergedServerRecipes = new ArrayList<>();
        mergedServerRecipes.addAll(serverRecipes);
        mergedServerRecipes.addAll(othergroup.serverRecipes);
        serverRecipes =  mergedServerRecipes.stream().distinct().collect(Collectors.toList());
        List<IEnhancedRecipe> mergedEnhancedRecipes = new ArrayList<>();
        mergedEnhancedRecipes.addAll(enhancedRecipes);
        mergedEnhancedRecipes.addAll(othergroup.enhancedRecipes);
        enhancedRecipes = mergedEnhancedRecipes.stream().distinct().collect(Collectors.toList());
        return this;
    }

}
