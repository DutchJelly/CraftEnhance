package com.dutchjelly.craftenhance.files;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FileManager {

	private final boolean useJson;
	
	private File dataFolder;
	private File itemsFile;
	private File recipesFile;
	private File containerOwnerFile;
	private File serverRecipeFile;
	private FileConfiguration recipesConfig;
	private FileConfiguration itemsConfig;
	private FileConfiguration serverRecipeConfig;
	private FileConfiguration containerOwnerConfig;

	private String itemsJson;
	private Logger logger;
	private Map<String, ItemStack> items;
	private List<EnhancedRecipe> recipes;

	private FileManager(boolean useJson) {
	    this.useJson = useJson;
    }

	public static FileManager init(CraftEnhance main){
//		FileManager fm = new FileManager(main.getConfig().getBoolean("use-json"));
        FileManager fm = new FileManager(main.getConfig().getBoolean("use-json"));
		fm.items = new HashMap<>();
		fm.recipes = new ArrayList<>();
		fm.logger = main.getLogger();
		fm.dataFolder = main.getDataFolder();
		fm.dataFolder.mkdir();
		fm.itemsFile = fm.getFile(fm.useJson ? "items.json" : "items.yml");
		fm.recipesFile = fm.getFile("recipes.yml");
		fm.serverRecipeFile = fm.getFile("server-recipes.yml");
		fm.containerOwnerFile = fm.getFile("container-owners.yml");
		return fm;
	}

	@SneakyThrows
	public static void EnsureResourceUpdate(String resourceName, File file, FileConfiguration fileConfig, JavaPlugin plugin) {
		if(!file.exists()){
			plugin.saveResource(resourceName, false);
			return;
		}

		Reader jarConfigReader = new InputStreamReader(plugin.getResource(resourceName));
		FileConfiguration jarResourceConfig = YamlConfiguration.loadConfiguration(jarConfigReader);
		jarConfigReader.close();

		boolean unsavedChanges = false;

		for(String key : jarResourceConfig.getKeys(false)) {
			if(!fileConfig.contains(key, false)) {
				fileConfig.set(key, jarResourceConfig.get(key));
				unsavedChanges = true;
			}
		}

		if(unsavedChanges)
			fileConfig.save(file);

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
		EnhancedRecipe keyValue;
		recipesConfig = getYamlConfig(recipesFile);
		recipes.clear();
		for(String key : recipesConfig.getKeys(false)){
			Debug.Send("Caching recipe with key " + key);
            keyValue = (EnhancedRecipe)recipesConfig.get(key);
            String validation = keyValue.validate();
            if(validation != null){
                Messenger.Error("Recipe with key " + key + " has issues: " + validation);
                Messenger.Error("This recipe will not be cached and loaded.");
                continue;
            }
			keyValue.setKey(key);
			recipes.add(keyValue);
		}
	}

	@SneakyThrows
	public void cacheItems(){

	    if(useJson){

            StringBuilder json = new StringBuilder("");
            Scanner scanner = new Scanner(itemsFile);
            while(scanner.hasNextLine())
                json.append(scanner.nextLine());
            scanner.close();
	        items.clear();
	        Type typeToken = new TypeToken<HashMap<String,Map<String,Object>>>(){}.getType();
            Gson gson = new Gson();
            final Map<String,Map<String,Object>> serialized = gson.fromJson(json.toString(), typeToken);
            if(serialized != null)
                serialized.keySet().forEach(x -> items.put(x, ItemStack.deserialize(serialized.get(x))));
            return;
        }

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
	
	public List<EnhancedRecipe> getRecipes(){
		return recipes;
	}
	
	public EnhancedRecipe getRecipe(String key){
		for(EnhancedRecipe recipe : recipes){
			if(recipe.getKey().equals(key))
				return recipe;
		}
		return null;
	}
	
	public boolean isUniqueRecipeKey(String key){
		return getRecipe(key) == null;
	}

	@SneakyThrows
	public boolean saveItem(String key, ItemStack item){

	    if(useJson){
	        items.put(key, item);
	        Gson gson = new Gson();
	        Map<String,Map<String,Object>> serialized = new HashMap<>();
	        items.keySet().forEach(x -> serialized.put(x, items.get(x).serialize()));
	        itemsJson = gson.toJson(serialized, new TypeToken<HashMap<String,Map<String,Object>>>(){}.getType());
            FileWriter writer = new FileWriter(itemsFile);
            writer.write(itemsJson);
            writer.close();
	        return true;
        }

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

	public List<String> readDisabledServerRecipes(){
	    if(serverRecipeConfig == null)
	        serverRecipeConfig = getYamlConfig(serverRecipeFile);
	    return serverRecipeConfig.getStringList("disabled");
    }

    public boolean saveDisabledServerRecipes(List<String> keys){
        serverRecipeConfig.set("disabled", keys);
        try {
            serverRecipeConfig.save(serverRecipeFile);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public Map<Location, UUID> getContainerOwners() {
		containerOwnerConfig = getYamlConfig(containerOwnerFile);
		Map<Location, UUID> blockOwners = new HashMap<>();
		for (String key : containerOwnerConfig.getKeys(false)) {
			String[] parsedKey = key.split(",");
			Location loc = new Location(
					Bukkit.getServer().getWorld(UUID.fromString(parsedKey[3])),
					Integer.valueOf(parsedKey[0]),
					Integer.valueOf(parsedKey[1]),
					Integer.valueOf(parsedKey[2]));
			blockOwners.put(loc, UUID.fromString(containerOwnerConfig.getString(key)));
		}
		return blockOwners;
	}

	public boolean saveContainerOwners(Map<Location, UUID> blockOwners) {
		containerOwnerConfig.getKeys(false).forEach(x -> containerOwnerConfig.set(x, null));
		for (Map.Entry<Location, UUID> blockOwnerSet : blockOwners.entrySet()) {
			Location key = blockOwnerSet.getKey();
			String keyString = key.getBlockX() + "," + key.getBlockY() + "," + key.getBlockZ() + "," + key.getWorld().getUID();
			containerOwnerConfig.set(keyString, blockOwnerSet.getValue().toString());
		}
		try {
			containerOwnerConfig.save(containerOwnerFile);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public void saveRecipe(EnhancedRecipe recipe){
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
	
	public void removeRecipe(EnhancedRecipe recipe){
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
		List<EnhancedRecipe> cloned = new ArrayList<>();
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
		for(EnhancedRecipe r : recipes){
			if(r.getResult().equals(item)) return true;
            for(ItemStack inRecipe : r.getContent()){
                if(inRecipe != null && inRecipe.equals(item)) return true;
            }

		}
		return false;
	}
	
}
