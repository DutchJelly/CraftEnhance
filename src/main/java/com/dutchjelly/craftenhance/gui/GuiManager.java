package com.dutchjelly.craftenhance.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.util.Pair;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.interfaces.IChatInputHandler;
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

	private final Map<UUID, Pair<GUIElement, IChatInputHandler>> chatWaiting = new HashMap<>();

	private final CraftEnhance main;
	
	public GuiManager(CraftEnhance main){
		this.main = main;
	}
	
	public CraftEnhance getMain(){
		return main;
	}


	@EventHandler
    public void onDrag(InventoryDragEvent e){
	    if(!(e.getView().getTopInventory().getHolder() instanceof GUIElement)) return;
        GUIElement openGUI = (GUIElement)e.getView().getTopInventory().getHolder();
        if(openGUI == null || e.getInventory() == null) return;

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

        if(!(clickEvent.getView().getTopInventory().getHolder() instanceof GUIElement)) return;
        GUIElement openGUI = (GUIElement)clickEvent.getView().getTopInventory().getHolder();

        if(openGUI == null) return;

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
            IChatInputHandler callback = chatWaiting.get(id).getSecond();
            if(callback.handle(e.getMessage())) return;
            openGUI(e.getPlayer(), chatWaiting.get(id).getFirst());
            chatWaiting.remove(id);
        });

	    e.setCancelled(true);
    }

	public void openGUI(Player p, GUIElement gui){
	    if(countPreviousPages(gui) >= MaxPreviousPageBuffer){
            Messenger.Message("For performance reasons you cannot open more gui's in that chain (the server keeps track of the previous gui's so you can go back).", p);
            return;
	    }
        if(gui == null) {
            Debug.Send("trying to open null gui...");
            return;
        }
        Debug.Send("Opening a gui element: " + gui.getClass().getName());
        p.openInventory(gui.getInventory());
	}

	private int countPreviousPages(GUIElement gui){
	    if(gui == null) return 0;
	    int counter = 0;
	    while(gui != null){
            gui = gui.getPreviousGui();
            counter++;
        }

        return counter;
    }

    public void waitForChatInput(GUIElement gui, Player p, IChatInputHandler callback){
        UUID playerId = p.getUniqueId();
        p.closeInventory();
        chatWaiting.put(playerId, new Pair(gui, callback));
	}
}
