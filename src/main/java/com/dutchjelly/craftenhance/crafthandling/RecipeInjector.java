package com.dutchjelly.craftenhance.crafthandling;

import java.util.List;

import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.Util.RecipeUtil;
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
		List<CraftRecipe> recipes = fm.getRecipes();
		if(recipes == null || recipes.isEmpty()) {
			Debug.Send("Stopping injecting because empty recipes. Config Error..");
			return;
		}
		ItemStack[] invContent = RecipeUtil.EnsureDefaultSize(inv.getMatrix()).clone();
		RecipeUtil.Format(invContent);
		ItemStack result = getResult(invContent, recipes, inv.getViewers());
		Debug.Send("Found result with type " + (result == null ? "null" : result.getType().toString()));
		//If the result is null, getResult() didn't find any default results. If it's AIR, it found a default
		//result of air which means the default version of the recipe doesn't exist on the server. If 
		//multiple of the same recipes exist they'll both have the same default result, so this'll always
		//be the case.
		if(result != null) inv.setResult(result);
	}

	
	private ItemStack getResult(ItemStack[] invContent, List<CraftRecipe> recipes, List<HumanEntity> viewers){
		ItemStack defaultResult = null;
		for(CraftRecipe r : recipes){
			ItemStack recipeContent[] = r.getContents().clone();
			RecipeUtil.Format(recipeContent);
			if(RecipeUtil.AreEqualTypes(recipeContent, invContent)){
				if(RecipeUtil.AreEqualItems(recipeContent, invContent) && viewersHavePermission(r,viewers)) {
                    Debug.Send("A recipe matches with " + r.toString());
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
