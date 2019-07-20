package com.dutchjelly.craftenhance.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.model.CraftRecipe;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;

public class GUIContainer {
	
	//TODO set names to the GUIs, so the events can be cancelled more reliably.
	
	private Map<UUID, GUIElement> openGUIs;
	private CraftEnhance main;
	
	public GUIContainer(CraftEnhance main){
		this.main = main;
		openGUIs = new HashMap<UUID, GUIElement>();
	}
	
	public CraftEnhance getMain(){
		return main;
	}
	
	public void handleEvent(InventoryEvent e){
		//This doesn't catch the lists of GUIs pointing to each other with "previous".
		if(isGUIClosingEvent(e)){
			
			openGUIs.remove(((InventoryCloseEvent)e).getPlayer().getUniqueId());
			Debug.Send("A GUI closed, now there are " + openGUIs.size() + " left in memory.");
			return;
		}
		
		if(e instanceof InventoryClickEvent){
			InventoryClickEvent clickEvent = (InventoryClickEvent)e;
			boolean handlerAssigned = true; //We know that, if an exception occurs, that an assigned handler causes it.
			try{
				handlerAssigned = assignHandler(clickEvent);
			}
			finally { //Make sure that the click event gets cancelled when exceptions occur.
				if(handlerAssigned){
					boolean ret = cancelClick(clickEvent);
					if (!clickEvent.isCancelled() && ret)
						clickEvent.setCancelled(ret);
				}
			}
		}
	}
	
	public void openOrderEditor(Player player){
		OrderEditor editor = new OrderEditor(this);
		openGUIElement(editor, player);
	}
	
	public void openRecipeCreator(String key, String perm, Player player){
		RecipeEditor editor = new RecipeEditor(this, key, perm);
		openGUIElement(editor, player);
	}
	
	public void openRecipeEditor(CraftRecipe recipe, Player player){
		RecipeEditor editor = new RecipeEditor(this, recipe, null);
		openGUIElement(editor, player);
	}
	
	public void openRecipeEditor(CraftRecipe recipe, Player player, GUIElement previous){
		RecipeEditor editor = new RecipeEditor(this, recipe, previous);
		openGUIElement(editor, player);
	}
	
	public void openRecipesViewer(Player player){
		RecipesViewer viewer = new RecipesViewer(player, this);
		openGUIElement(viewer, player);
	}
	
	public void openRecipeViewer(CraftRecipe recipe, Player player){
		
		//TODO add the backpage (the recipes viewer) so nullpointers don't occur.
		
		RecipeViewer viewer = new RecipeViewer(this, openGUIs.get(player.getUniqueId()), recipe);
		openGUIElement(viewer, player);
	}
	
	public void openGUIElement(GUIElement gui, Player player){
		UUID id = player.getUniqueId();
		if(gui == null) return;
		Debug.Send("Opening a gui element: " + gui.getClass().getName());
		player.openInventory(gui.getInventory());
		if(openGUIs.containsKey(id))
			openGUIs.remove(id);
		openGUIs.put(id, gui);
	}
	
	private boolean isGUIClosingEvent(InventoryEvent e){
		return (e instanceof InventoryCloseEvent) && 
			openGUIs.containsKey(((InventoryCloseEvent)e).getPlayer().getUniqueId());
	}
	
	
	private boolean cancelClick(InventoryClickEvent e){
		GUIElement openGUI = getGUI(e.getWhoClicked().getOpenInventory().getTopInventory());
		return openGUI != null && !(openGUI instanceof RecipeEditor);
	}
	
	private boolean assignHandler(InventoryClickEvent e){
		GUIElement clickedGUI = getGUI(e.getClickedInventory());
		if(clickedGUI == null) return false;
		clickedGUI.handleEvent(e);
		return true;
	}
	
	private GUIElement getGUI(Inventory inv){
		for(GUIElement openGUI : openGUIs.values()){
			if(openGUI.isEventTriggerer(inv))
				return openGUI;
		}
		return null;
	} 
	
	public void closeAll(){
		for(UUID key : openGUIs.keySet()){
			getMain().getServer().getPlayer(key).closeInventory();
		}
	}
}
