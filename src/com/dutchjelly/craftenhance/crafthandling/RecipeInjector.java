package com.dutchjelly.craftenhance.crafthandling;

import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import com.dutchjelly.craftenhance.data.FileManager;
import com.dutchjelly.craftenhance.util.Recipe;

public class RecipeInjector {
	
	private FileManager fm;
	
	public RecipeInjector(FileManager fm){
		this.fm = fm;
	}
	
	public void injectResult(CraftingInventory inv){
		List<Recipe> recipes = fm.getRecipes();
		if(recipes == null || recipes.isEmpty()) return;
		ItemStack[] invContent = formattedContent(inv.getMatrix());
		ItemStack result = getResult(invContent, recipes, inv.getViewers());
		//If the result is null, getResult() didn't find any default results. If it's AIR, it found a default
		//result of air which means the default version of the recipe doesn't exist on the server. If 
		//multiple of the same recipes exist they'll both have the same default result, so this'll always 
		//be the case.
		if(result != null) inv.setResult(result);
	}
	 
	private ItemStack getResult(ItemStack[] invContent, List<Recipe> recipes, List<HumanEntity> viewers){
		ItemStack defaultResult = null;
		for(Recipe r : recipes){
			if(r.materialsMatch(invContent)){
				if(r.itemsMatch(invContent) && viewersHavePermission(r,viewers))
					return r.getResult();
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
	
	private ItemStack[] formattedContent(ItemStack[] content){
		if(!isNullInventory(content)){
			while(nullColumn(content)) content = shiftLeft(content);
			while(nullRow(content)) content = shiftUp(content);
		} return content;
	}
	private boolean nullRow(ItemStack[] content){
		int i = 0;
		while(i < 3){
			if(content[i] != null) return false;
			i++;
		} return true;
	}
	
	private boolean nullColumn(ItemStack[] content){
		int j = 0;
		while(j <= 6){
			if(content[j] != null) return false;
			j+=3;
		} return true;
	}
	
	private ItemStack[] shiftLeft(ItemStack[] content){
		int left;
		for(int i = 0; i <= 6; i+=3){
			left = i;
			for(int j = i+1; j < i+3; j++){
				content[left] = content[j];
				content[j] = null;
				left = j;
			}
		}return content;
	}
	private ItemStack[] shiftUp(ItemStack[] content){
		if(content == null) return null;
		int up;
		for(int j = 0; j < 3; j++){
			up = j;
			for(int i = j+3; i <= j+6; i+=3){
				content[up] = content[i];
				content[i] = null;
				up = i;
			}
		} return content;
	}
	private boolean isNullInventory(ItemStack[] content){
		for(int i = 0; i < content.length; i++){
			if(content[i] != null) return false;
		} return true;
	} 
	
	private boolean viewersHavePermission(Recipe recipe, List<HumanEntity> humans){
		if(recipe.getPerms().equals("")) return true;
		for(HumanEntity human : humans){
			if(!human.hasPermission(recipe.getPerms()))
				return false;
		}
		return true;
	}
}
