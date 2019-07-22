package com.dutchjelly.craftenhance;

import com.dutchjelly.craftenhance.messaging.Debug;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

public class EventClass implements Listener{

	private CraftEnhance main;
	
	public EventClass(CraftEnhance main){
		this.main = main;
	}
	
	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e){

        if(main.getConfig().getBoolean("enable-recipes") && e.getRecipe() != null)
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
