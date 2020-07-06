package com.dutchjelly.craftenhance.api;

import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.RecipeGroup;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface CustomCraftListener {
    boolean listener(IEnhancedRecipe recipe, Player p, Inventory craftingInventory, RecipeGroup alternatives);
}
