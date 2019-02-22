package com.dutchjelly.craftenhance.crafthandling;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import com.dutchjelly.craftenhance.CraftEnhance;

public class EventClass implements Listener{

	private CraftEnhance main;
	
	public EventClass(CraftEnhance main){
		this.main = main;
	}
	
	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e){
		if(e.getInventory().getType().equals(InventoryType.CRAFTING))
			return;
		main.getRecipeInjector().injectResult(e.getInventory());
	}
	
	@EventHandler
	public void onInventoryEvent(InventoryClickEvent e){
		main.getGUIContainer().handleEvent(e);
	}
	@EventHandler
	public void onInventoryEvent(InventoryCloseEvent e){
		main.getGUIContainer().handleEvent(e);
	}
}
