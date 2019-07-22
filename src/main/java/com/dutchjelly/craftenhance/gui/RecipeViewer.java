package com.dutchjelly.craftenhance.gui;

import com.dutchjelly.bukkitadapter.Adapter;
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

public class RecipeViewer implements GUIElement {
	
	
	private GUIContainer container;
	private GUIElement previousGUI;
	private Inventory inventory;
	private CraftRecipe recipe;
	
	public RecipeViewer(GUIContainer container, GUIElement previousGUI, CraftRecipe recipe){
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
		ItemStack craftShower = new ItemStack(Adapter.GetWorkBench());
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
		if(RecipeUtil.IsNullElement(e.getCurrentItem()))
			return;
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
