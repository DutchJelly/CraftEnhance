package com.dutchjelly.craftenhance.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Debug;
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
		formatContentAmount();
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
		formatContentAmount();
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
		return recipe;
	}

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
	
	private void formatContentAmount(){
		for(ItemStack item : recipe){
			if(item != null && item.getAmount() != 1)
				item.setAmount(1);
		}
	}
	
	public String toString(){
		return "Recipe of " + result != null ? result.getType().name() : "null" + " with key " + this.getKey() != null ? this.getKey() : "null";
	}
}
