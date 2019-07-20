package com.dutchjelly.craftenhance.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutchjelly.craftenhance.CraftEnhance;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

import com.dutchjelly.craftenhance.files.FileManager;

@SerializableAs("Recipe")
public class CraftRecipe implements ConfigurationSerializable{
	
	private String configKey;
	private String permission;
	private ItemStack defaultResult;
	private ItemStack[] recipe;
	private ItemStack result;

	private CraftRecipe(){
    }
	public CraftRecipe(String perm, ItemStack result, ItemStack[] recipe){
		permission = perm;
		setContent(recipe);
		formatContentAmount();
	}
	
	
	public String getConfigKey(){
		return configKey;
	}

	//Still implemented because a lot of classes used this version.
    // TODO abandon getConfigKey() to not have a redundant version of the function.
	public String getKey(){
	    return configKey;
    }

	public ItemStack[] getContents(){
		return recipe;
	}
	public String getPerms(){
		return permission;
	}
	public ItemStack getDefaultResult(){
		return defaultResult;
	}
	public void setKey(String key){
		this.configKey = key;
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

	public ItemStack getResult(){
	    return result;
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
		recipe.setContent(new ItemStack[9]);
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

	
	private void formatContentAmount(){
		for(ItemStack item : recipe){
			if(item != null && item.getAmount() != 1)
				item.setAmount(1);
		}
	}
	
	public String toString(){
		return "Recipe of " + (result != null ? result.getType().name() : "null") + " with key " + (this.getConfigKey() != null ? this.getConfigKey() : "null");
	}
}
