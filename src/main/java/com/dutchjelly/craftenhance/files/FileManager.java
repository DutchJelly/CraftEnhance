package com.dutchjelly.craftenhance.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

public class FileManager {
	
	private File dataFolder;
	private File itemsFile;
	private File recipesFile;
	private FileConfiguration recipesConfig;
	private FileConfiguration itemsConfig;
	private Logger logger;
	private Map<String, ItemStack> items;
	private List<IEnhancedRecipe> recipes;

	public static FileManager init(CraftEnhance main){
		FileManager fm = new FileManager();
		fm.items = new HashMap<>();
		fm.recipes = new ArrayList<>();
		fm.logger = main.getLogger();
		fm.dataFolder = main.getDataFolder();
		fm.dataFolder.mkdir();
		fm.itemsFile = fm.getFile("items.yml");
		fm.recipesFile = fm.getFile("recipes.yml");
		return fm;
	}

	private File ensureCreated(File file){
		if(!file.exists()){
			logger.info(file.getName() + " doesn't exist... creating it.");
			try {
				file.createNewFile();
			} catch (IOException e) {
				logger.warning("The file " + file.getName() 
				+ " couldn't be created!");
			}
		}
		return file;
	}
	
	private File getFile(String name){
		File file = new File(dataFolder, name);
		ensureCreated(file);
		return file;
	}
	
	private FileConfiguration getYamlConfig(File file){
		return YamlConfiguration.loadConfiguration(file);
	}
	
	public void cacheRecipes(){
		Debug.Send("The file manager is caching recipes...");
		IEnhancedRecipe keyValue;
		recipesConfig = getYamlConfig(recipesFile);
		recipes.clear();
		for(String key : recipesConfig.getKeys(false)){
			Debug.Send("Caching recipe with key " + key);
			keyValue = (IEnhancedRecipe)recipesConfig.get(key);
			keyValue.setKey(key);
			recipes.add(keyValue);
		}
	}
	
	public void cacheItems(){
		itemsConfig = getYamlConfig(itemsFile);
		items.clear();
		for(String key : itemsConfig.getKeys(false)){
			items.put(key, itemsConfig.getItemStack(key));
		}
	}
	
	public Map<String, ItemStack> getItems(){
		return items;
	}
	
	public ItemStack getItem(String key){
		return items.get(key);
	}
	
	public String getItemKey(ItemStack item){
		if(item == null) return null;
		for(String key : items.keySet()){
			if(item.equals(items.get(key)))
				return key;
		}
		String uniqueKey = getUniqueItemKey(item);
		saveItem(uniqueKey, item);
		return uniqueKey; 
	}
	
	private String getUniqueItemKey(ItemStack item){
		if(item == null) return null;
		String base = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
				item.getItemMeta().getDisplayName() : item.getType().name();
		base = base.replaceAll("\\.", "");
		String unique = base;
		int incrementer = 1;
		while(items.keySet().contains(unique))
			unique = base + incrementer++;
		return unique;
	}
	
	public List<IEnhancedRecipe> getRecipes(){
		return recipes;
	}
	
	public IEnhancedRecipe getRecipe(String key){
		for(IEnhancedRecipe recipe : recipes){
			if(recipe.getKey().equals(key))
				return recipe;
		}
		return null;
	}
	
	public boolean isUniqueRecipeKey(String key){
		return getRecipe(key) == null;
	}
	
	public boolean saveItem(String key, ItemStack item){
		itemsConfig = getYamlConfig(itemsFile);
		if(!itemsConfig.contains(key)){
			itemsConfig.set(key, item);
			try{
				itemsConfig.save(itemsFile);
				items.put(key, item);
				return true;
			} catch(IOException e){
				logger.severe("Error saving an item to the items.yml file.");
			}
		}
		return false;
	}
	
	public void saveRecipe(IEnhancedRecipe recipe){
		Debug.Send("Saving recipe " + recipe.toString() + " with key " + recipe.getKey());
		recipesConfig = getYamlConfig(recipesFile);
		recipesConfig.set(recipe.getKey(), recipe);
		try{
			recipesConfig.save(recipesFile);
			if(getRecipe(recipe.getKey()) == null)
				recipes.add(recipe);
			Debug.Send("Succesfully saved the recipe, there are now " + recipes.size() + " recipes cached.");
		} catch(IOException e){
			logger.severe("Error saving a recipe to the recipes.yml file.");
		}
	}
	
	public void removeRecipe(IEnhancedRecipe recipe){
		Debug.Send("Removing recipe " + recipe.toString() + " with key " + recipe.getKey());
		recipesConfig = getYamlConfig(recipesFile);
		recipesConfig.set(recipe.getKey(), null);
		recipes.remove(recipe);
		try{
			recipesConfig.save(recipesFile);
		} catch(IOException e){
			logger.severe("Error removing a recipe.");
		}
	}
	
	public void overrideSave(){
		Debug.Send("Overriding saved recipes with new list..");
		List<IEnhancedRecipe> cloned = new ArrayList<>();
		recipes.forEach(x -> cloned.add(x));
		removeAllRecipes();
		cloned.forEach(x -> saveRecipe(x));
		recipes = cloned;
		recipesConfig = getYamlConfig(recipesFile);
	}
	
	private void removeAllRecipes(){
		if(recipes.isEmpty()) return;
		removeRecipe(recipes.get(0));
		removeAllRecipes();
	}
	
	public void cleanItemFile(){
		Debug.Send("Cleaning up unused items.");
		for(String itemKey : items.keySet()){
			if(!isItemInUse(items.get(itemKey))){
				Debug.Send("Item with key " + itemKey + " is not used and will be removed.");
				itemsConfig.set(itemKey, null);
				try {
					itemsConfig.save(itemsFile);
				} catch (IOException e) {
					Debug.Send("Failed saving itemsConfig");
				}
			}
		}
	}
	
	private boolean isItemInUse(ItemStack item){
		for(IEnhancedRecipe r : recipes){
			if(r.getResult().equals(item)) return true;
            for(ItemStack inRecipe : r.getContent()){
                if(inRecipe != null && inRecipe.equals(item)) return true;
            }

		}
		return false;
	}
	
}
