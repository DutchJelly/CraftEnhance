package com.dutchjelly.craftenhance.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.util.GUIButtons;
import com.dutchjelly.craftenhance.util.CraftRecipe;

public class RecipesViewer implements GUIElement{
	
	List<CraftRecipe> showedRecipes;
	private boolean showAll;
	private GUIContainer container;
	private Inventory[] inventories;
	private int currentPage;
	
	public RecipesViewer(Player p, GUIContainer container){
		Debug.Send("An instance is being made for an recipesviewer");
		this.container = container;
		currentPage = 0;
		showAll = !container.getMain().getConfig().getBoolean("only-show-available");
		showedRecipes = getAvailableRecipes(p);
		generateInventory();
	}
	
	private void generateInventory(){
		CraftRecipe add;
		int getIndex;
		inventories = new Inventory[getReqPageAmount()];
		Debug.Send("The amount of pages in the new recipes viewer is " + inventories.length + ".");
		for(int i = 0; i < inventories.length; i++){
			inventories[i] = Bukkit.createInventory(null, 6*9, "Recipes Viewer");
			for(int j = 0; j < 45; j++){
				getIndex = i*45 + j;
				if(getIndex >= showedRecipes.size()) continue;
				add = showedRecipes.get(getIndex);
				inventories[i].setItem(j, add.getResult());
			}
			addButtons(inventories[i]);
		}
	}
	
	private int getReqPageAmount(){
		int defaultCase = (int) Math.ceil((double)showedRecipes.size() / 45);
		return defaultCase != 0 ? defaultCase : 1;
	}
	
	private void addButtons(Inventory inv){
		inv.setItem(47, GUIButtons.previous);
		inv.setItem(51, GUIButtons.next);
	}
	
	private List<CraftRecipe> getAvailableRecipes(Player player){
		List<CraftRecipe> recipes = new ArrayList<CraftRecipe>();
		for(CraftRecipe recipe : container.getMain().getFileManager().getRecipes()){
			if(showAll || player.hasPermission(recipe.getPerms())) recipes.add(recipe);
		}
		return recipes;
	}
	
	@Override
	public Inventory getInventory() {
		return inventories.length == 0 ? null : inventories[currentPage];
	}

	@Override
	public boolean isEventTriggerer(Inventory inv) {
		return inv.equals(getInventory());
	}

	@Override
	public void handleEvent(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player)) return;
		
		e.setCancelled(true);
		
		Player player = (Player) e.getWhoClicked();
		
		
		
		//TODO this can be done cleaner.. figure out how..
		CraftRecipe recipe;
		if(!isEventTriggerer(e.getClickedInventory())){
			recipe = findFirstMatchingRecipe((e.getCurrentItem()));
			if(recipe == null) return;
			container.openRecipeViewer(recipe, player);
			return;
		}
		recipe = findResultingRecipe(e.getCurrentItem(), e.getRawSlot());
		if(recipe != null){
			if(e.getClick() == ClickType.MIDDLE && hasEditorPerms(player))
				container.openRecipeEditor(recipe, player, this);
			else
				container.openRecipeViewer(recipe, player);
			return;
		}
		
		if(e.getCurrentItem().equals(GUIButtons.next)){
			scroll(1);
		} else if(e.getCurrentItem().equals(GUIButtons.previous)){
			scroll(-1);
		} 
		container.openGUIElement(this, player);
	}
	
	private boolean hasEditorPerms(HumanEntity entity){
		return entity.hasPermission(container.getMain().getConfig().getString("perms.recipe-editor"));
	}
	
	private CraftRecipe findResultingRecipe(ItemStack result, int clickPos){
		if(clickPos > getInventory().getSize()-9) return null;
		int translatedClickPos = currentPage * (getInventory().getSize()-9) + clickPos;
		if(translatedClickPos >= showedRecipes.size()) return null;
		return showedRecipes.get(translatedClickPos);
	}
	
	private CraftRecipe findFirstMatchingRecipe(ItemStack result){
		for(CraftRecipe recipe : showedRecipes){
			if(recipe.getResult().equals(result)) 
				return recipe;
		}
		return null;
	}
	
	
	private void scroll(int amount){
		currentPage = Math.abs((currentPage + amount) % inventories.length);
	}

	@Override
	public Class<?> getInstance() {
		return this.getClass();
	}
}
