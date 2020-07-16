package com.dutchjelly.craftenhance.crafthandling.util;

import com.dutchjelly.craftenhance.gui.guis.CustomCraftingTable;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomPrepareCraftEvent extends Event {



    private static final HandlerList handlers = new HandlerList();

    @Getter
    private CustomCraftingTable table;

    public CustomPrepareCraftEvent(CustomCraftingTable table){
        this.table = table;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }
}
