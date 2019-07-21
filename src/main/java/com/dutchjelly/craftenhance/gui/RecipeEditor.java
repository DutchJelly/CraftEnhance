package com.dutchjelly.craftenhance.gui;

import com.dutchjelly.craftenhance.model.CraftRecipe;
import com.dutchjelly.craftenhance.Util.GUIButtons;
import com.dutchjelly.craftenhance.Util.RecipeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class RecipeEditor implements GUIElement{
	
	private GUIElement previous;
	private Inventory inventory;
	private CraftRecipe recipe;
	private GUIContainer container;
	
	public RecipeEditor(GUIContainer container, CraftRecipe recipe, GUIElement previous){
		this.previous = previous;
		this.recipe = recipe;
		this.container = container;
		initInventory();
	}
	
	public RecipeEditor(GUIContainer container, String key, String perm){
		this.container = container;
		ItemStack[] content = new ItemStack[9];
		recipe = new CraftRecipe(perm, null, content);
		recipe.setKey(key);
		
		initInventory();
	}
	
	private void initInventory(){
		inventory = Bukkit.createInventory(null, 9*3, recipe.getKey());
		addInventoryFillings();
		addInventoryButtons();
		showMetaData();
		resetInventory();
	}

	private void showMetaData(){
	    if(recipe == null) return;
	    ItemStack dataTag = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = dataTag.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                "&5Recipe Info"));
        meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "&fKey: &e" + recipe.getKey()),
                ChatColor.translateAlternateColorCodes('&', "&fPermission: &e" + recipe.getPerms())
        ));
        meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        dataTag.setItemMeta(meta);
        inventory.setItem(21, dataTag);
    }
	
	private void addInventoryFillings(){
		for(int i = 0; i < 27; i++){
			if((i%9) / 3 > 0 && i != 13) 
				inventory.setItem(i, GUIButtons.filling);
		}

		ItemStack craftShower = new ItemStack(Material.WORKBENCH);
		ItemMeta meta = craftShower.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&5Workbench Recipe"));
        meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "&f<-- &eRecipe &f| &2Result &f-->")
        ));
        meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        craftShower.setItemMeta(meta);
        inventory.setItem(12, craftShower);
	}
	
	private void addInventoryButtons(){
		inventory.setItem(8, GUIButtons.save);
		inventory.setItem(17, GUIButtons.reset);
		inventory.setItem(26, GUIButtons.back);
		inventory.setItem(15, GUIButtons.delete);
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public boolean isEventTriggerer(Inventory inv) {
		if(inv == null)
			return false;
		return inv.equals(inventory);
	}

	@Override
	public void handleEvent(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		boolean cancelEvent = true;
		ItemStack currentItem = e.getCurrentItem();
		int clickedSlot = e.getSlot();
		if(RecipeUtil.IsNullElement(currentItem))
		    return;
		if(currentItem.equals(GUIButtons.save)){
			if(saveRecipe())
				container.getMain().getMessenger().message("Successfully saved the recipe.", e.getWhoClicked());
			else
				container.getMain().getMessenger().message("Failed to save the recipe because it doesn't contain a result item or a recipe.", e.getWhoClicked());
		} else if(currentItem.equals(GUIButtons.reset)){
			resetInventory();
		} else if(currentItem.equals(GUIButtons.back)){
			container.openGUIElement(previous, (Player)e.getWhoClicked());
		} else if(currentItem.equals(GUIButtons.delete)){ // add extra perms to this
			deleteRecipe();
			container.openRecipesViewer((Player)e.getWhoClicked());

		} else if(clickedSlot%9 < 3 || clickedSlot == 13){
			cancelEvent = false;
		}
		e.setCancelled(cancelEvent);
	}

	private void resetInventory() {
		for(int i = 0; i < 9; i++){
			inventory.setItem((i/3 * 9) + i%3, recipe.getContents()[i]);
		}
		inventory.setItem(13, recipe.getResult());
	}
	
	private void deleteRecipe() {
		container.getMain().getFileManager().removeRecipe(recipe);
		container.getMain().getRecipeLoader().loadRecipes();
	}
	
	private boolean saveRecipe() {
		ItemStack[] newContents = new ItemStack[9];
		if(inventory.getItem(13) == null || inventory.getItem(13).getType() == Material.AIR){
			return false;
		}
		for(int i = 0; i < 9; i++){
			newContents[i] = inventory.getItem((i/3 * 9) + i%3);
		}
		if(RecipeUtil.IsNullArray(newContents))
		    return false;
		recipe.setContent(newContents);
		recipe.setResult(inventory.getItem(13));
		container.getMain().getFileManager().saveRecipe(recipe);
		container.getMain().getRecipeLoader().loadRecipes();
		return true;
	}

	@Override
	public Class<?> getInstance() {
		return this.getClass();
	}
	
	

}
