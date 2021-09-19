package com.dutchjelly.craftenhance.crafthandling.customcraftevents;

import com.dutchjelly.craftenhance.api.CustomCraftListener;
import com.dutchjelly.craftenhance.crafthandling.RecipeGroup;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ExecuteCommand implements CustomCraftListener {
    @Override
    public boolean listener(EnhancedRecipe recipe, Player p, Inventory craftingInventory, RecipeGroup alternatives) {
        if(recipe.getOnCraftCommand() == null || recipe.getOnCraftCommand().trim().equals("")) return false;
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), recipe.getOnCraftCommand());
        return false;
    }
}
