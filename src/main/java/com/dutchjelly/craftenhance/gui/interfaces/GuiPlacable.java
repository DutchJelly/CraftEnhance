package com.dutchjelly.craftenhance.gui.interfaces;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class GuiPlacable implements ConfigurationSerializable{

    public GuiPlacable(){}

    public GuiPlacable(Map<String, Object> args){
        page = (int)args.get("page");
        slot = (int)args.get("slot");
    }

    @Getter @Setter
    private int page = -1;

    @Getter @Setter
    private int slot = -1;

    @Override
    public Map<String,Object> serialize(){
        return new HashMap<String, Object>(){{
            put("page", page);
            put("slot", slot);
        }};
    }

    public abstract ItemStack getDisplayItem();
}
