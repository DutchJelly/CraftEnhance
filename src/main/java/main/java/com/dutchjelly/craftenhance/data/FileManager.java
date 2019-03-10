package main.java.com.dutchjelly.craftenhance.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import main.java.com.dutchjelly.craftenhance.CraftEnhance;
import main.java.com.dutchjelly.craftenhance.messaging.Debug;
import main.java.com.dutchjelly.craftenhance.util.CraftRecipe;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class FileManager {
	
	private File dataFolder;
	private File itemsFile;
	private File recipesFile;
	private FileConfiguration recipesConfig;
	private FileConfiguration itemsConfig;
	private String seperator;
	private Logger logger;
	
	private Map<String, ItemStack> items;
	private List<CraftRecipe> recipes;
	private List<CraftRecipe> bin;

	public static FileManager init(CraftEnhance main){
		FileManager fm = new FileManager();
		fm.seperator = File.separator;
		fm.items = new HashMap<String, ItemStack>();
		fm.recipes = new ArrayList<CraftRecipe>();
		fm.bin = new ArrayList<CraftRecipe>();
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
		File file = new File(dataFolder.getPath() + seperator + name);
		ensureCreated(file);
		return file;
	}
	
	private FileConfiguration getYamlConfig(File file){
		return YamlConfiguration.loadConfiguration(file);
	}
	
	public void cacheRecipes(){
		Debug.Send("The filemanageer is caching recipes...");
		CraftRecipe keyValue;
		recipesConfig = getYamlConfig(recipesFile);
		recipes.clear();
		for(String key : recipesConfig.getKeys(false)){
			Debug.Send("Caching recipe with key " + key);
			keyValue = (CraftRecipe)recipesConfig.get(key);
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
			if(item.isSimilar(items.get(key)))
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
		String unique = base;
		int incrementer = 1;
		while(items.keySet().contains(unique))
			unique = base + incrementer++;
		return unique;
	}
	
	public List<CraftRecipe> getRecipes(){
		return recipes;
	}
	
	public CraftRecipe getRecipe(String key){
		for(CraftRecipe recipe : recipes){
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
	
	public void saveRecipe(CraftRecipe recipe){
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
	
	public void removeRecipe(CraftRecipe recipe){
		Debug.Send("Removing recipe " + recipe.toString() + " with key " + recipe.getKey());
		recipesConfig = getYamlConfig(recipesFile);
		recipesConfig.set(recipe.getKey(), null);
		recipes.remove(recipe);
		bin.add(recipe);
		try{
			recipesConfig.save(recipesFile);
		} catch(IOException e){
			logger.severe("Error removing a recipe.");
		}
	}
	
	public void changeKey(CraftRecipe recipe, String newKey){
		recipe.setKey(newKey);
		overrideSave();
	}
	
	public void overrideSave(){
		Debug.Send("Overriding saved recipes with new list..");
		List<CraftRecipe> cloned = new ArrayList<CraftRecipe>();
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
		for(CraftRecipe r : recipes){
			if(r.getResult().equals(item)) return true;
			for(ItemStack inRecipe : r.getContents()){
				if(inRecipe != null && inRecipe.equals(item)) return true;
			}
		}
		return false;
	}
	
}
