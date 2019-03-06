package com.dutchjelly.craftenhance.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface GUIElement{
	
	public Inventory getInventory();
	
	public boolean isEventTriggerer(Inventory inv);
	
	public void handleEvent(InventoryClickEvent e);
	
	public Class<?> getInstance();
	
}
