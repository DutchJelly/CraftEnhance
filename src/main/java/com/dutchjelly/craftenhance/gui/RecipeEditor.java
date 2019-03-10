package com.dutchjelly.craftenhance.gui;

import com.dutchjelly.craftenhance.util.CraftRecipe;
import com.dutchjelly.craftenhance.util.GUIButtons;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
		resetInventory();
	}
	
	private void addInventoryFillings(){
		for(int i = 0; i < 27; i++){
			if((i%9) / 3 > 0 && i != 13) 
				inventory.setItem(i, GUIButtons.filling);
		}
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
		if(currentItem.equals(GUIButtons.save)){
			saveRecipe();
			container.getMain().getMessenger().message("Successfully saved the recipe.", e.getWhoClicked());
		} else if(currentItem.equals(GUIButtons.reset)){
			resetInventory();
		} else if(currentItem.equals(GUIButtons.back)){
			container.openGUIElement(previous, (Player)e.getWhoClicked());
		} else if(currentItem.equals(GUIButtons.delete)){ // add extra perms to this
			deleteRecipe();
			container.openRecipesViewer((Player)e.getWhoClicked());
		} else if(!currentItem.equals(GUIButtons.filling)){
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
	
	private void saveRecipe() {
		ItemStack[] newContents = new ItemStack[9];
		for(int i = 0; i < 9; i++){
			newContents[i] = inventory.getItem((i/3 * 9) + i%3);
		}
		recipe.setContent(newContents);
		recipe.setResult(inventory.getItem(13));
		container.getMain().getFileManager().saveRecipe(recipe);
		container.getMain().getRecipeLoader().loadRecipes();
	}

	@Override
	public Class<?> getInstance() {
		return this.getClass();
	}
	
	

}
