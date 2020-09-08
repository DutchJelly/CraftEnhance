package com.dutchjelly.craftenhance.gui.guis;

import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.IButtonHandler;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GUIElement implements InventoryHolder{

    @Getter
    private final GuiManager manager;

    @Getter @NonNull
    private final GuiTemplate template;

    @Getter
    private final Player player;

    @Getter
    private final GUIElement previousGui;

    private Map<ButtonType, List<IButtonHandler>> buttonClickHandlers;

    public GUIElement(GuiManager manager, GuiTemplate template, GUIElement previousGui, Player player){
        this.manager = manager;
        this.template = template;
        this.player = player;
        this.previousGui = previousGui;
        buttonClickHandlers = new HashMap<>();
        buttonClickHandlers.put(ButtonType.Back, Arrays.asList(this::handleBackBtnClicked));
    }

    public void handleBackBtnClicked(ItemStack btn, ButtonType btnType){
        if(previousGui == null) return;
        manager.openGUI(player, previousGui);
    }

    public void handleEvent(InventoryClickEvent e){

        if(!(e.getWhoClicked() instanceof Player)) return;

        //could be removed if multiple players are allowed to use same gui
        if(!e.getWhoClicked().equals(getPlayer()))
            throw new IllegalStateException("Other player clicked than owner of GUI.");

        int clickedSlot = e.getSlot();

        //Handle button clicks.
        ButtonType clickedButton = getTemplate().getButtonMapping().get(clickedSlot);
        if(clickedButton != null){
            List<IButtonHandler> btnHandlers = buttonClickHandlers.get(clickedButton);
            if(btnHandlers != null)
                btnHandlers.forEach(x -> x.handleClick(e.getCurrentItem(), clickedButton));
        }

        //Allow implementation to handle event.
        handleEventRest(e);
    }

    public void handleOutsideClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player)) return;
    }

    public void handleDragging(InventoryDragEvent e){
    }

    public void addBtnListener(ButtonType type, IButtonHandler listener){
        if(buttonClickHandlers.containsKey(type)){
            buttonClickHandlers.get(type).add(listener);
        }else{
            buttonClickHandlers.put(type, Arrays.asList(listener));
        }
    }

	public abstract void handleEventRest(InventoryClickEvent e);

	public abstract boolean isCancelResponsible();

	
}
