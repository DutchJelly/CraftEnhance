package com.dutchjelly.craftenhance.crafthandling;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.Util.RecipeUtil;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.dutchjelly.craftenhance.files.FileManager;
import com.dutchjelly.craftenhance.model.CraftRecipe;

public class RecipeInjector {
	
	private FileManager fm;
	
	public RecipeInjector(FileManager fm){
		this.fm = fm;
	}
	
	public void injectResult(CraftingInventory inv){
        LocalDateTime start = LocalDateTime.now();
		List<CraftRecipe> recipes = fm.getRecipes();
		if(recipes == null || recipes.isEmpty()) {
			Debug.Send("Stopping injecting because empty recipes. Config Error..");
			return;
		}


		ItemStack[] invContent = RecipeUtil.EnsureDefaultSize(inv.getMatrix()).clone();
		RecipeUtil.Format(invContent);
		ItemStack result = getResult(invContent, recipes, inv.getViewers());
		//If the result is null, getResult() didn't find any default results. If it's AIR, it found a default
		//result of air which means the default version of the recipe doesn't exist on the server. If 
		//multiple of the same recipes exist they'll both have the same default result, so this'll always
		//be the case.
		if(result != null && !RecipeUtil.AreEqualItems(result, inv.getResult())) {
		    Debug.Send("Injected " + result);
		    inv.setResult(result);
        }
        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start, end);
        Debug.Send("Injecting took " + duration.toNanos() + " nanoseconds.");
	}

	
	private ItemStack getResult(ItemStack[] invContent, List<CraftRecipe> recipes, List<HumanEntity> viewers){
	    ItemStack defaultResult = null;

	    //Shaped recipes can be mirrored verticly, think of the recipe of a bow.
        ItemStack[] mirroredInventoryContent = RecipeUtil.MirrorVerticle(invContent);
        RecipeUtil.Format(mirroredInventoryContent); //Make sure the mirrored version is also formatted for comparison.

        if(mirroredInventoryContent == null) return null;
		for(CraftRecipe r : recipes){
			ItemStack recipeContent[] = r.getContents().clone();
			RecipeUtil.Format(recipeContent);

			//Check if the type of the items in the recipe matches with the inventory or mirrored inventory.
			if((RecipeUtil.AreEqualTypes(recipeContent, invContent))
                || RecipeUtil.AreEqualTypes(recipeContent, mirroredInventoryContent)){

			    //Check if the recipe is equal to the normal version or mirrored version of the inventory
                //and if the player has permissions for the recipe.
				if((RecipeUtil.AreEqualItems(recipeContent, invContent)
                        || RecipeUtil.AreEqualItems(mirroredInventoryContent, recipeContent))
                        && viewersHavePermission(r,viewers)) {
                    return r.getResult();
                }
				else
					defaultResult = r.getDefaultResult();

			}
		}
		return defaultResult;
	}

	private boolean viewersHavePermission(CraftRecipe recipe, List<HumanEntity> humans){
		if(recipe.getPerms().equals("")) return true;
		for(HumanEntity human : humans){
			if(!human.hasPermission(recipe.getPerms()))
				return false;
		}
		return true;
	}
	


	
	
}
