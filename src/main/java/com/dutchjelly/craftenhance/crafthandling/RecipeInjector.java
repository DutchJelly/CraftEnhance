package com.dutchjelly.craftenhance.crafthandling;

import java.util.List;

import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.dutchjelly.craftenhance.data.FileManager;
import com.dutchjelly.craftenhance.util.CraftRecipe;

public class RecipeInjector {
	
	private FileManager fm;
	
	public RecipeInjector(FileManager fm){
		this.fm = fm;
	}
	
	public void injectResult(CraftingInventory inv){
		List<CraftRecipe> recipes = fm.getRecipes();
		if(recipes == null || recipes.isEmpty()) return;
		ItemStack[] invContent = ensureDefaultSize(inv.getMatrix()).clone();
		formatContent(invContent);
		ItemStack result = getResult(invContent, recipes, inv.getViewers());
		//If the result is null, getResult() didn't find any default results. If it's AIR, it found a default
		//result of air which means the default version of the recipe doesn't exist on the server. If 
		//multiple of the same recipes exist they'll both have the same default result, so this'll always 
		//be the case.
		if(result != null) inv.setResult(result);
	}
	
	private ItemStack[] ensureDefaultSize(ItemStack[] matrix){
		if(matrix.length == 9) return matrix;
		ItemStack[] defaultMatrix = new ItemStack[9];
		for(int i = 0; i < 9; i++){
			defaultMatrix[i] = null;
		}
		defaultMatrix[0] = matrix[0];
		defaultMatrix[1] = matrix[1];
		defaultMatrix[3] = matrix[2];
		defaultMatrix[4] = matrix[3];
		
		return defaultMatrix;
	}
	
	private ItemStack getResult(ItemStack[] invContent, List<CraftRecipe> recipes, List<HumanEntity> viewers){
		ItemStack defaultResult = null;
		for(CraftRecipe r : recipes){
			ItemStack recipeContent[] = r.getContents().clone();
			formatContent(recipeContent);
			if(materialsMatch(recipeContent, invContent)){
				if(itemsMatch(recipeContent, invContent) && viewersHavePermission(r,viewers)) {
                    Debug.Send("A recipe matches with " + r.toString());
                    return r.getResult();
                }
				else
					defaultResult = r.getDefaultResult();
			}
		}
		return defaultResult;
	}
	
	@SuppressWarnings("unused")
	private void printContent(ItemStack[] content){
		
		for(int i = 0; i < content.length; i++){
			if(content[i] == null) System.out.println(i + ": null");
			else System.out.println(i + ": " + content[i].getType());
		}
	}
	
	private void formatContent(ItemStack[] content){
		if(isNullArray(content)) return;

		while(content[0] == null)
			shiftLeft(content);
	}

	private void shiftLeft(ItemStack[] content){
		for(int i = 0; i < content.length-1; i++){
			content[i] = content[i+1];
		}
		content[content.length-1] = null;
	}


	private boolean isNullArray(ItemStack[] content){
		for(int i = 0; i < content.length; i++){
			if(content[i] != null) return false;
		} return true;
	} 
	
	private boolean viewersHavePermission(CraftRecipe recipe, List<HumanEntity> humans){
		if(recipe.getPerms().equals("")) return true;
		for(HumanEntity human : humans){
			if(!human.hasPermission(recipe.getPerms()))
				return false;
		}
		return true;
	}
	
	public boolean itemsMatch(ItemStack[] recipe, ItemStack[] content){
		for(int i = 0; i < recipe.length; i++){
			if(!areEqualItems(content[i], recipe[i])) return false;
		}
		return true;
	}
	
	//Needs testing... I'm tired.
	public boolean itemsMatch2(ItemStack[] recipe, ItemStack[] content){
		int recipeWidth = (int) Math.sqrt(recipe.length);
		int contentWidth = (int) Math.sqrt(content.length);
		for(int i = 0; i < recipeWidth; i++){
			for(int j = 0; j < recipeWidth; j++){
				if(i < contentWidth && j < contentWidth){
					if(!areEqualItems(recipe[i + (recipeWidth * j)], content[i + (contentWidth * j)]))
						return false;
				} else if(recipe[i + (recipeWidth * j)] != null)
					return false;
				
			}
		}
		return true;
	}
	
	public boolean materialsMatch(ItemStack[] recipe, ItemStack[] content){
		for(int i = 0; i < recipe.length; i++){
			if(!areEqualTypes(content[i], recipe[i])) return false;
		}
		return true;
	}
	private boolean areEqualTypes(ItemStack content, ItemStack recipe){
		return content == recipe || (content != null && recipe != null && 
				recipe.getType().equals(content.getType()));
	}
	private boolean areEqualItems(ItemStack content, ItemStack recipe){
		return content == recipe || (content != null && recipe != null &&
				recipe.isSimilar(content));
	}
	
	
}
