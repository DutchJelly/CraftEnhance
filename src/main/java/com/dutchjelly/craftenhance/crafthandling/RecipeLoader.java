package com.dutchjelly.craftenhance.crafthandling;

import com.dutchjelly.craftenhance.IEnhancedRecipe;
import lombok.NonNull;
import org.bukkit.*;

import org.bukkit.inventory.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RecipeLoader {

    //Ensure one instance
    private static RecipeLoader instance = null;
    public static RecipeLoader getInstance(){
        return instance == null ? new RecipeLoader(Bukkit.getServer()) : instance;
    }


    private List<Recipe> serverRecipes = new ArrayList<>();
    private Server server;


    //Recipes are grouped in groups of 'similar' recipes. A server can contain multiple recipes with the same
    //recipe with different results. Think of a diamond chestplate recipe vs a custom diamond chestplate recipe
    //with custom diamonds. Or think of a shapeless recipe of a block of diamond vs a shaped recipe of the block of
    //diamond.
    @NonNull
    private List<RecipeGroup> groupedRecipes = new ArrayList<>();

    private RecipeLoader(Server server){
        this.server = server;
    }

    //Adds or merges group with existing group.
    private RecipeGroup addGroup(RecipeGroup newGroup){
        if(!newGroup.getServerRecipes().isEmpty()){
            //look for merge
            for(RecipeGroup group : groupedRecipes){

                //If all the server recipes of the group are the same, we can safely merge because it implies that the
                //recipes are equivalent.
                if(group.getServerRecipes().containsAll(newGroup.getServerRecipes())){
                    group.getEnhancedRecipes().addAll(newGroup.getEnhancedRecipes());

                    //Ensure that there's no duplicates. Very optional... but just in case.
                    List<IEnhancedRecipe> filtered = group.getEnhancedRecipes().stream().distinct().collect(Collectors.toList());
                    if(filtered.size() != group.getEnhancedRecipes().size()){
                        Bukkit.getLogger().log(Level.SEVERE, "Merging two recipe groups with overlapping recipe.");
                        group.setEnhancedRecipes(filtered);
                    }

                    //If merged, return the destination group.
                    return group;
                }

                //TODO ook mergen als meerdere enhanced dingen alwaysSimilar zijn.
            }
        }
        groupedRecipes.add(newGroup);
        return newGroup;
    }

    public RecipeGroup findGroup(IEnhancedRecipe recipe){
        return groupedRecipes.stream().filter(x -> x.getEnhancedRecipes() == recipe).findFirst().orElse(null);
    }

    //Find groups that contain at least one recipe that maps to result.
    public List<RecipeGroup> findGroupsByResult(ItemStack result){
        List<RecipeGroup> originGroups = new ArrayList<>();
        for(RecipeGroup group : groupedRecipes){
            if(group.getEnhancedRecipes().stream().anyMatch(x -> result.equals(x.getResult())))
                originGroups.add(group);
            else if(group.getServerRecipes().stream().anyMatch(x -> result.equals(x.getResult())))
                originGroups.add(group);
        }
        return originGroups;
    }

    public boolean isLoaded(IEnhancedRecipe recipe){
        return groupedRecipes.stream().anyMatch(x -> x.getEnhancedRecipes().contains(recipe));
    }

    public void unloadRecipe(IEnhancedRecipe recipe){
        RecipeGroup group = findGroup(recipe);
        if(group == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not unload recipe from groups because it doesn't exist.");
            return;
        }
        Recipe serverRecipe = recipe.getServerRecipe();

        //Only unload from server if there are no similar server recipes.
        if(!group.getServerRecipes().contains(serverRecipe)){
            Iterator<Recipe> it = server.recipeIterator();
            boolean found = false;
            while(it.hasNext() && !found){
                if(it.next().equals(serverRecipe)){
                    it.remove();
                    found = true;
                }
            }
            if(!found){
                Bukkit.getLogger().log(Level.SEVERE, "Could not remove recipe from server.");
            }
        }

        //Remove entire recipe group if it's the last enhanced recipe, or remove a single recipe from the group.
        if(group.getEnhancedRecipes().size() == 1)
            groupedRecipes.removeIf(x -> x.getEnhancedRecipes().contains(recipe));
        else group.getEnhancedRecipes().remove(recipe);
    }

    public void loadRecipe(@NonNull IEnhancedRecipe recipe){
        List<Recipe> similarServerRecipes = new ArrayList<>();
        for(Recipe r : serverRecipes){
            if(recipe.isSimilar(r))
                similarServerRecipes.add(r);
        }
        boolean foundOverlappingServerRecipe = false;
        for(Recipe r : similarServerRecipes){
            if(recipe.isAlwaysSimilar(r)){
                foundOverlappingServerRecipe = true;
                break;
            }
        }
        if(!foundOverlappingServerRecipe){
            server.addRecipe(recipe.getServerRecipe());
        }

        RecipeGroup group = new RecipeGroup();
        group.setEnhancedRecipes(Arrays.asList(recipe));
        group.setServerRecipes(similarServerRecipes);
        addGroup(group);
    }
}
