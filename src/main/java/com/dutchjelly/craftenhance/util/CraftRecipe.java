package com.dutchjelly.craftenhance.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutchjelly.craftenhance.CraftEnhance;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import com.dutchjelly.craftenhance.data.FileManager;

@SerializableAs("Recipe")
public class CraftRecipe implements ConfigurationSerializable {
	
	private String key;
	private String permission;
	private ItemStack result;
	private ItemStack defaultResult;
	private ItemStack[] recipe;
	private CraftRecipe(){}
	public CraftRecipe(String perm, ItemStack result, ItemStack[] recipe){
		permission = perm;
		this.recipe = recipe;
		this.result = result;
		formatRecipeContent();
	}
	
	
	public String getKey(){
		return key;
	}
	public ItemStack[] getContents(){
		return recipe;
	}
	public ItemStack getResult(){
		return result;
	}
	public String getPerms(){
		return permission;
	}
	public ItemStack getDefaultResult(){
		return defaultResult;
	}
	public void setKey(String key){
		this.key = key;
	}
	public void setPerms(String perms){
		this.permission = perms;
	}
	public void setDefaultResult(ItemStack item){
		defaultResult = item;
	}
	public void setResult(ItemStack result){
		this.result = result;
	}
	public void setContent(ItemStack[] content){
		recipe = content;
		formatRecipeContent();
	}
	
	public boolean resultMatches(ItemStack result){
		return result != null && (result.equals(defaultResult) || result.equals(result));
	}
	
	
	@SuppressWarnings("unchecked")
	public static CraftRecipe deserialize(Map<String,Object> args){
		FileManager fm = CraftEnhance.getPlugin(CraftEnhance.class).getFileManager();
		
		List<String> recipeKeys;
		CraftRecipe recipe = new CraftRecipe();
		fm = CraftEnhance.getPlugin(CraftEnhance.class).getFileManager();
		recipe.result = fm.getItem((String)args.get("result"));
		recipe.permission = (String)args.get("permission");
		recipe.recipe = new ItemStack[9];
		recipeKeys = (List<String>)args.get("recipe");
		for(int i = 0; i < recipe.recipe.length; i++){
			recipe.recipe[i] = fm.getItem(recipeKeys.get(i));
		}
		recipe.formatRecipeContent();
		return recipe;
	}
	
	//TODO make it so the item gets generated in fileconfig when it doesn't exist.
	@Override
	public Map<String, Object> serialize() {
		FileManager fm = CraftEnhance.getPlugin(CraftEnhance.class).getFileManager();
		
		Map<String, Object> serialized = new HashMap<String, Object>();
		serialized.put("permission", permission);
		serialized.put("result", fm.getItemKey(result));
		String recipeKeys[] = new String[recipe.length];
		for(int i = 0; i < recipe.length; i++){
			recipeKeys[i] = fm.getItemKey(recipe[i]);
		}
		serialized.put("recipe", recipeKeys);
		return serialized;
	}
	
	
	//Returns the shaped recipe if the crafting type is workbench.
	public ShapedRecipe getShapedRecipe(){
		@SuppressWarnings("deprecation")
		ShapedRecipe shapedRecipe = new ShapedRecipe(result);
		shapedRecipe.shape(getShape());
		mapShapedIngredients(shapedRecipe);
		return shapedRecipe;
	}
	private String[] getShape(){
		String recipeShape[] = {"","",""};
		for(int i = 0; i < 9; i++){
			if(recipe[i] != null)
				recipeShape[i/3] += (char)('A' + i);
			else
				recipeShape[i/3] += ' ';
		}
		return recipeShape;
	}
	
	private void mapShapedIngredients(ShapedRecipe shapedRecipe){
		for(int i = 0; i < 9; i++){
			if(recipe[i] != null){
				shapedRecipe.setIngredient((char) ('A' + i), recipe[i].getType());
			}
		}
	}
	
	private void formatRecipeContent(){
		if(!isNullRecipe()){
			while(columnIsNull(0)) shiftLeft();
			while(rowIsNull(0)) shiftUp();
		}
		formatContentAmount();
	}
	
	private void formatContentAmount(){
		for(ItemStack item : recipe){
			if(item != null && item.getAmount() != 1)
				item.setAmount(1);
		}
	}
	
	private void shiftLeft2(){
		int left;
		for(int i = 0; i <= 6; i+=3){
			left = i;
			for(int j = i+1; j < i+3; j++){
				recipe[left] = recipe[j];
				recipe[j] = null;
				left = j;
			}
		}
	}

	public void shiftLeft(){
	    for(int i = 0; i < 3; i++){
	        for(int j = i; j < 9; j+= 3){
	            if(i != 2) recipe[j] = recipe[j+1];
	            else recipe[j] = recipe[j+1];
            }
        }
    }

	private void shiftUp(){
	    for(int i = 3; i < 9; i++){
            recipe[i-3] = recipe[i];
        }
        for(int i = 5; i < 9; i++){
	        recipe[i] = null;
        }
    }

	private void shiftUp2(){
		int up;
		for(int j = 0; j < 3; j++){
			up = j;
			for(int i = j+3; i <= j+6; i++){
				recipe[up] = recipe[i];
				recipe[i] = null;
				up = i;
			}
		}
	}
	private boolean isNullRecipe(){
		for(int i = 0; i < recipe.length; i++){
			if(recipe[i] != null) return false;
		} return true;
	} 
	
	private boolean rowIsNull(int row){
		int i = row;
		while(i < row+3){
			if(recipe[i] != null) return false;
			i++;
		} return true;
	}
	
	private boolean columnIsNull(int column){
		int j = column;
		while(j < 9){
			if(recipe[j] != null) return false;
			j+=3;
		} return true;
	}
	
	
	public String toString(){
		return "Recipe of " + result != null ? result.getType().name() : "null" + " with key " + this.getKey() != null ? this.getKey() : "null";
	}
}
