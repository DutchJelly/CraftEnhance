package com.dutchjelly.craftenhance.crafthandling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.util.Recipe;

public class RecipeLoader {
	
	
	private CraftEnhance main;
	private Iterator<org.bukkit.inventory.Recipe> iterator;

	
	public RecipeLoader(CraftEnhance main){
		this.main = main;
	}
	
	public void loadRecipes(){
		if(!main.getConfig().getBoolean("enable-recipes")){
			Debug.Send("The cusom recipes are disabled on the server.");
			return;
		}
		List<Recipe> queue = new ArrayList<Recipe>();
		org.bukkit.inventory.Recipe similar;
		main.getServer().resetRecipes();
		for(Recipe r : main.getFileManager().getRecipes()){
			resetIterator();
			similar = getNextSimilar(r);
			handleDefaults(r, similar);
			if(needsLoading(r, similar)){
				queue.add(r);
			}
		}
		addAll(queue);
	}
	
	private boolean needsLoading(Recipe r, org.bukkit.inventory.Recipe similar){
		if(similar == null) return true;
		return !areEqualTypes(similar.getResult(), r.getResult());
	}
	
	//Er gaat iets mis als er geen similar recipes zijn.
	private void handleDefaults(Recipe r, org.bukkit.inventory.Recipe similar){
		if(similar == null)
			r.setDefaultResult(new ItemStack(Material.AIR));
		else
			r.setDefaultResult(similar.getResult());
	}
	
	private void resetIterator(){
		iterator = main.getServer().recipeIterator();
	}
	
	private void addAll(List<Recipe> queue){
		if(queue == null) return;
		queue.forEach(x -> main.getServer().addRecipe(x.getShapedRecipe()));
	}
	
	//Uses the iterator to find a recipe with equal content.
	private org.bukkit.inventory.Recipe getNextSimilar(Recipe r){
		org.bukkit.inventory.Recipe currentIteration;
		while(iterator.hasNext()){
			currentIteration = iterator.next();
			if(isSimilarShapedRecipe(currentIteration, r)) return currentIteration;
			if(isSimilarShapeLessRecipe(currentIteration, r)) return currentIteration;
		}
		return null;
	}
	
	private boolean isSimilarShapedRecipe(org.bukkit.inventory.Recipe serverRecipe, Recipe customRecipe){
		if(!(serverRecipe instanceof ShapedRecipe)) return false;
		return contentsEqual(getShapedRecipeContent((ShapedRecipe) serverRecipe), customRecipe.getContents());
	}
	
	private boolean isSimilarShapeLessRecipe(org.bukkit.inventory.Recipe serverRecipe, Recipe customRecipe){
		if(!(serverRecipe instanceof ShapelessRecipe)) return false;
		return allMaterialsMatch((ShapelessRecipe) serverRecipe, customRecipe);
	}
	
	private boolean allMaterialsMatch(ShapelessRecipe recipe, Recipe customRecipe){
		ItemStack[] content = customRecipe.getContents();
		List<ItemStack> choices = new ArrayList<>();
		choices.addAll(recipe.getIngredientList());
		for(ItemStack item : content){
			if(item == null || item.getType().equals(Material.AIR)) continue;
			//This system works differently in 1.13.2
			if(!choices.contains(item)) return false;
			choices.remove(item);
		}
		return true;
	}
	
	
	@SuppressWarnings("unused")
	private void printContent(ItemStack[] content){
		
		for(int i = 0; i < content.length; i++){
			if(content[i] == null) System.out.println(i + ": null");
			else System.out.println(i + ": " + content[i].getType());
		}
	}
	
	private ItemStack[] getShapedRecipeContent(ShapedRecipe r){
		ItemStack[] content = new ItemStack[9];
		String[] shape = r.getShape();
		int columnIndex;
		for(int i = 0; i < shape.length; i++){
			columnIndex = 0;
			for(char c : shape[i].toCharArray()){
				content[(i*3) + columnIndex] = r.getIngredientMap().get(c);
				columnIndex++;
			}
		}
		return content;
	}
	
	private boolean contentsEqual(ItemStack[] one, ItemStack[] two){
		if(one.length != two.length) return false;
		for(int i = 0; i < one.length; i++){
			if(!areEqualItems(one[i], two[i]))
				return false;
		}
		return true;
	}
	
	private boolean areEqualItems(ItemStack one, ItemStack two){
		return one == two || (one != null && two != null && areEqualTypes(one,two));
	}
	
	private boolean areEqualTypes(ItemStack a, ItemStack b){
		return a.getType().equals(b.getType());
	}
}
