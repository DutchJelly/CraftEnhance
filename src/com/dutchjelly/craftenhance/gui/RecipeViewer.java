package com.dutchjelly.craftenhance.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.dutchjelly.craftenhance.util.GUIButtons;
import com.dutchjelly.craftenhance.util.Recipe;

public class RecipeViewer implements GUIElement {
	
	
	private GUIContainer container;
	private GUIElement previousGUI;
	private Inventory inventory;
	private Recipe recipe;
	
	public RecipeViewer(GUIContainer container, GUIElement previousGUI, Recipe recipe){
		this.container = container;
		this.previousGUI = previousGUI;
		this.recipe = recipe;
		initInventory();
	}
	private void initInventory(){
		inventory = Bukkit.createInventory(null, 9*3, "Recipe Viewer");
		addInventoryFillings();
		addInventoryButtons();
		addRecipe();
	}
	private void addInventoryFillings(){
		for(int i = 0; i < 27; i++){
			if((i%9) / 3 > 0) inventory.setItem(i, GUIButtons.filling);
		}
	}
	private void addRecipe(){
		for(int i = 0; i < 9; i++){
			inventory.setItem((i/3 * 9) + i%3, recipe.getContents()[i]);
		}
		inventory.setItem(13, recipe.getResult());
	}
	private void addInventoryButtons(){
		inventory.setItem(26, GUIButtons.back);
	}
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@Override
	public boolean isEventTriggerer(Inventory inv) {
		return inv.equals(inventory);
	}

	@Override
	public void handleEvent(InventoryClickEvent e) {
		e.setCancelled(true);
		//TODO add instanceof Player check to eventclass.
		Player player = (Player) e.getWhoClicked();
		if(e.getCurrentItem().equals(GUIButtons.back)){
			container.openGUIElement(previousGUI, player);
		} else if(e.getRawSlot() % 9 < 3){ 
			//Clicked item is part of recipe.
			previousGUI.handleEvent(e);
		}
	}

	@Override
	public Class<?> getInstance() {
		return this.getClass();
	}

}
