package com.dutchjelly.craftenhance.api;


import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.RecipeGroup;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class CraftEnhanceAPI {

    private static List<CustomCraftListener> customCraftListeners = new ArrayList<>();

    public static void registerListener(CustomCraftListener listener){
        if(!customCraftListeners.contains(listener))
            customCraftListeners.add(listener);
    }

    public static boolean fireEvent(IEnhancedRecipe recipe, Player p, Inventory craftingInventory, RecipeGroup alternatives){
        try{
            return customCraftListeners.stream().map(x -> x.listener(recipe, p, craftingInventory, alternatives)).anyMatch(x -> x);
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
