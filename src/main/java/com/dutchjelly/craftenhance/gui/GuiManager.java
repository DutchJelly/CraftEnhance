package com.dutchjelly.craftenhance.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.Pair;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GuiManager implements Listener {
	
    //TODO: make gui's check which user clicks on button click instead of using the stored user.
    private static final int MaxPreviousPageBuffer = 20;

	private final Map<UUID, GUIElement> openGUIs = new HashMap<>();

	private final Map<UUID, Pair<GUIElement, IChatInputHandler>> chatWaiting = new HashMap<>();

	private final CraftEnhance main;
	
	public GuiManager(CraftEnhance main){
		this.main = main;
	}
	
	public CraftEnhance getMain(){
		return main;
	}


	@EventHandler
	public void onClose(InventoryCloseEvent e){
	    if(!openGUIs.containsKey(e.getPlayer().getUniqueId()))
	        return;
		openGUIs.remove(e.getPlayer().getUniqueId());
		Debug.Send("A GUI closed, now there are " + openGUIs.size() + " left in memory.");
	}


	@EventHandler
    public void onDrag(InventoryDragEvent e){
        GUIElement openGUI = openGUIs.values().stream().filter(x -> e.getView().getTopInventory().equals(x.getInventory())).findFirst().orElse(null);
        if(openGUI == null) return;
        if(e.getInventory() == null) return;

        Debug.Send("Player dragged over " + e.getInventorySlots().stream().map(x -> String.valueOf((int)x)).collect(Collectors.joining(",")));

        try{
            openGUI.handleDragging(e);

            if(!openGUI.isCancelResponsible() && !e.isCancelled())
                e.setCancelled(true);

        }catch(Exception exception){
            exception.printStackTrace();
            if(!e.isCancelled())
                e.setCancelled(true);
        }
    }

	@EventHandler
	public void onClick(InventoryClickEvent clickEvent){

	    GUIElement openGUI = openGUIs.values().stream().filter(x -> clickEvent.getView().getTopInventory().equals(x.getInventory())).findFirst().orElse(null);
        if(openGUI == null) return;
        if(clickEvent.getClickedInventory() == null) return;


        try{


            if(clickEvent.getClickedInventory().equals(openGUI.getInventory()))
                openGUI.handleEvent(clickEvent);
            else openGUI.handleOutsideClick(clickEvent);

            if(!openGUI.isCancelResponsible() && !clickEvent.isCancelled())
                clickEvent.setCancelled(true);

        }catch(Exception exception){
            exception.printStackTrace();
            if(!clickEvent.isCancelled())
                clickEvent.setCancelled(true);
        }
	}

	@EventHandler
    public void onChat(AsyncPlayerChatEvent e){
	    if(e.getPlayer() == null) return;

	    UUID id = e.getPlayer().getUniqueId();

	    if(!chatWaiting.containsKey(id)) return;

        Bukkit.getScheduler().runTask(getMain(), () -> {
            openGUI(e.getPlayer(), chatWaiting.get(id).getFirst());
            IChatInputHandler callback = chatWaiting.get(id).getSecond();
            chatWaiting.remove(id);
            callback.handle(e.getMessage());
        });

	    e.setCancelled(true);
    }

	public void openGUI(Player p, GUIElement gui){
	    if(countPreviousPages(p) >= MaxPreviousPageBuffer){
            Messenger.Message("For performance reasons you cannot open more gui's in that chain (the server keeps track of the previous gui's so you can go back).", p);
            return;
	    }
        UUID id = p.getUniqueId();
        if(gui == null) {
            Debug.Send("trying to open null gui...");
            return;
        }
        Debug.Send("Opening a gui element: " + gui.getClass().getName());
        p.openInventory(gui.getInventory());
        if(openGUIs.containsKey(id))
            openGUIs.remove(id);
        openGUIs.put(id, gui);
	}

	private int countPreviousPages(Player p){
	    GUIElement gui = openGUIs.get(p.getUniqueId());
	    int counter = 0;
	    while(gui != null){
            gui = gui.getPreviousGui();
            counter++;
        }

        return counter;
    }

    public void waitForChatInput(GUIElement gui, Player p, IChatInputHandler callback){
        UUID playerId = p.getUniqueId();
        if(!openGUIs.containsKey(playerId)) throw new IllegalStateException("A non registered GUI is trying to wait for chat input.");
        p.closeInventory();
        chatWaiting.put(playerId, new Pair(gui, callback));
	}

	
	public void closeAll(){
		for(UUID key : openGUIs.keySet()){
			getMain().getServer().getPlayer(key).closeInventory();
		}
	}
}
