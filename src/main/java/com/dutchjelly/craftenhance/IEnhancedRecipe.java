package com.dutchjelly.craftenhance;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;
import java.util.Map;

public interface IEnhancedRecipe {
    ItemStack getResult();
    Recipe getServerRecipe();
    boolean isSimilar(Recipe r);
    boolean isAlwaysSimilar(Recipe r);
    boolean matches(Recipe r);
}
